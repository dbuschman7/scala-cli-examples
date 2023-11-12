package me.lightspeed7.examples.rabbitmq

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._

object RpcClientOrig extends App {

  import com.rabbitmq.client.ConnectionFactory

  val factory = new ConnectionFactory
  factory.setHost("localhost")

  val connection = factory.newConnection
  val channel = connection.createChannel
  val requestQueueName = "rpc_queue"

  import java.io.IOException
  import java.util.UUID
  import java.util.concurrent.{CompletableFuture, ExecutionException}

  @throws[IOException]
  @throws[InterruptedException]
  @throws[ExecutionException]
  def call(message: String): String = {
    val corrId = UUID.randomUUID.toString
    val replyQueueName = channel.queueDeclare.getQueue
    println("DeclaredQueue - " + replyQueueName)
    val props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build
    channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"))

    val response = new CompletableFuture[String]

    val deliverCallback = new DeliverCallback {
      override def handle(consumerTag: String, delivery: Delivery): Unit = {
        if (delivery.getProperties.getCorrelationId.equals(corrId))
          response.complete(new String(delivery.getBody, "UTF-8"))
      }
    }

    val cancelCallback = new CancelCallback {
      override def handle(consumerTag: String): Unit = {}
    }

    val ctag = channel.basicConsume(replyQueueName, true, deliverCallback, cancelCallback)
    val result = response.get
    channel.basicCancel(ctag)
    result
  }

  @throws[IOException]
  def close(): Unit = {
    connection.close
  }


  //
  //
  try {
    for (i <- 0 until 32) {
      val i_str = s"""{"num":$i}"""
      System.out.println(" [x] Requesting fib(" + i_str + ")")
      val response = call(i_str)
      System.out.println(" [.] Got '" + response + "'")
    }
  } catch {
    case e: Throwable => e.printStackTrace()
  } finally {
    channel.close()
    connection.close()
  }
}
