package me.lightspeed7.examples.rabbitmq

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._

object RpcServerOrig extends App {

  import com.rabbitmq.client.ConnectionFactory

  private val RPC_QUEUE_NAME = "rpc_queue"

  private def fib(n: Int): Int = {
    if (n == 0) return 0
    if (n == 1) return 1
    fib(n - 1) + fib(n - 2)
  }

  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel: Channel = connection.createChannel
  channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null)
  channel.queuePurge(RPC_QUEUE_NAME)
  channel.basicQos(1)
  System.out.println(" [x] Awaiting RPC requests")

  val deliverCallback: DeliverCallback = new DeliverCallback {
    override def handle(consumerTag: String, delivery: Delivery): Unit = {
      val replyProps = new BasicProperties.Builder().correlationId(delivery.getProperties.getCorrelationId).build
      var response = ""
      try {
        val message = new String(delivery.getBody, "UTF-8")
        val n = message.toInt
        System.out.println(" [.] fib(" + message + ")")
        Thread.sleep(100)
        response += fib(n)
      } catch {
        case e: RuntimeException =>
          System.out.println(" [.] " + e)
      } finally {
        channel.basicPublish("", delivery.getProperties.getReplyTo, replyProps, response.getBytes("UTF-8"))
        channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
      }
    }
  }
  val cancelCallBack: CancelCallback = new CancelCallback {
    override def handle(consumerTag: String): Unit =  {}
  }


  channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, cancelCallBack)

}
