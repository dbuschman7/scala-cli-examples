package me.lightspeed7.examples.rabbitmq

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._
import play.api.libs.json._

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

final case class RabbitContext(host: String, port: Int, username: String, password: String) extends AutoCloseable {
  val factory = new ConnectionFactory
  factory.setHost(host)
  factory.setPort(port)
  factory.setUsername(username)
  factory.setPassword(password)
  factory.setAutomaticRecoveryEnabled(true)

  lazy val connection: Connection = factory.newConnection

  def rpqQueue(name: String): RpcQueue = RpcQueue(connection, name)(this)

  def publishTo(name: String): FireAndForget = FireAndForget(connection, name)(this)

  def consumerFrom(queue: String, groupName: String): ConsumerProcessor = ConsumerProcessor(connection, queue, groupName)

  private val shutdownVal: AtomicBoolean = new AtomicBoolean(false)

  def runUntilStopped(): Unit =
    while (!shutdownVal.get()) {
      Thread.sleep(2000)
    }

  def shutdown(): Unit = shutdownVal.set(true)

  override def close(): Unit = connection.close()
}

final case class FireAndForget(connection: Connection, queue: String)(implicit val ctx: RabbitContext) extends AutoCloseable {

  val channel: Channel = connection.createChannel

  def send[REQUEST <: Product](payload: => REQUEST)(implicit writes: Writes[REQUEST], ex: ExecutionContext): Future[Unit] = Future {
    val props = new BasicProperties.Builder().build
    val json = Json.stringify(writes.writes(payload))
    val bytes = json.getBytes("UTF-8")
    Try(channel.basicPublish("", queue, props, bytes)) match {
      case Failure(exception) => Future.failed(exception)
      case Success(value) => Future.successful(value)
    }
  }.flatten

  override def close(): Unit = channel.close()
}

final case class ConsumerProcessor(connection: Connection, queue: String, groupName: String) extends AutoCloseable {
  val channel: Channel = connection.createChannel

  def cancel(consumerTag: String): Unit = channel.basicCancel(consumerTag)

  def process[MESSAGE <: Product](block: MESSAGE => Either[Throwable, Unit])(implicit reads: Reads[MESSAGE]): Either[Throwable, String] = {
    val deliverCallback = new DeliverCallback {
      override def handle(consumerTag: String, delivery: Delivery): Unit = {
        println(s"Delivery - $consumerTag - ${delivery.getProperties.getCorrelationId}")

        val body = new String(delivery.getBody, "UTF-8")
        println("Received - " + body)
        Json.fromJson[MESSAGE](Json.parse(body)) match {
          case JsSuccess(value, _) =>
            println("Executing block")
            block(value)
          case JsError(errors) => Left(new IllegalStateException(errors.flatMap(_._2).map(_.message).mkString(",")))
        }

      }
    }

    val cancelCallback = new CancelCallback {
      override def handle(consumerTag: String): Unit = {} // FIXME: What to do here
    }

    Try(channel.basicConsume(queue, true, deliverCallback, cancelCallback)) match {
      case Success(ctag) => Right(ctag)
      case Failure(th) => Left(th)
    }
  }

  override def close(): Unit = channel.close()
}

final case class RpcQueue(connection: Connection, queue: String)(implicit val ctx: RabbitContext) extends AutoCloseable {
  val channel: Channel = connection.createChannel

  def send[REQUEST <: Product, RESPONSE <: Product](payload: => REQUEST)(implicit writes: Writes[REQUEST], reads: Reads[RESPONSE], ex: ExecutionContext): Future[RESPONSE] = {

    try {
      val corrId = UUID.randomUUID.toString
      val replyQueueName = channel.queueDeclare.getQueue
      println("DeclaredQueue - " + replyQueueName)
      val props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build
      val json = Json.stringify(writes.writes(payload))
      val bytes = json.getBytes("UTF-8")
      channel.basicPublish("", queue, props, bytes)

      val promise = Promise[RESPONSE]()

      val deliverCallback = new DeliverCallback {
        override def handle(consumerTag: String, delivery: Delivery): Unit = {
          println(s"Delivery - $consumerTag - ${delivery.getProperties.getCorrelationId}")
          if (delivery.getProperties.getCorrelationId.equals(corrId)) {
            val body = new String(delivery.getBody, "UTF-8")
            println("Received - " + body)
            Json.fromJson[RESPONSE](Json.parse(body)) match {
              case JsSuccess(value, _) =>
                println("Promise fulfilled - success")
                promise.success(value)
              case JsError(errors) => promise.failure(new IllegalStateException(errors.flatMap(_._2).map(_.message).mkString(",")))
            }
          }
        }
      }

      val cancelCallback = new CancelCallback {
        override def handle(consumerTag: String): Unit = {} // FIXME: What to do here
      }

      Try(channel.basicConsume(replyQueueName, true, deliverCallback, cancelCallback)) match {
        case Success(ctag) =>
          promise.future.map { resp =>
            channel.basicCancel(ctag)
            resp
          }
        case Failure(th) => Future.failed(th)
      }
    } catch {
      case th: Exception => Future.failed(th)
    }
  }

  def consume[REQUEST <: Product, RESPONSE <: Product](block: REQUEST => RESPONSE)(implicit reads: Reads[REQUEST], writes: Writes[RESPONSE]) = {
    val channel: Channel = connection.createChannel
    channel.queueDeclare(queue, false, false, false, null)
    channel.queuePurge(queue)
    channel.basicQos(1)
    System.out.println(" [x] Awaiting RPC requests")

    val deliverCallback: DeliverCallback = new DeliverCallback {
      override def handle(consumerTag: String, delivery: Delivery): Unit = {
        try {
          val message = new String(delivery.getBody, "UTF-8")
          println("Message - " + message)

          val response: Either[String, RESPONSE] = Json.fromJson[REQUEST](Json.parse(message)) match {
            case JsSuccess(value, _) => Try(block(value)) match {
              case Failure(exception) => Left(exception.getMessage)
              case Success(value) => Right(value)
            }
            case JsError(errors) =>
              Left(errors.map(_.toString).mkString(","))
          }

          response match {
            case Left(msg) =>
              println("ERROR - " + msg)
            // FIXME : Return an error response here
            case Right(payload) =>
              val json: String = Json.stringify(writes.writes(payload))
              println("Responding with " + json)
              val replyProps = new BasicProperties.Builder().correlationId(delivery.getProperties.getCorrelationId).build
              channel.basicPublish("", delivery.getProperties.getReplyTo, replyProps, json.getBytes("UTF-8"))
              channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
          }

        } catch {
          case e: RuntimeException =>
            System.out.println(" [.] " + e)
        }
      }
    }

    val cancelCallBack: CancelCallback = new CancelCallback {
      override def handle(consumerTag: String): Unit = {}
    }

    channel.basicConsume(queue, false, deliverCallback, cancelCallBack)
  }


  override def close(): Unit = channel.close()
}



