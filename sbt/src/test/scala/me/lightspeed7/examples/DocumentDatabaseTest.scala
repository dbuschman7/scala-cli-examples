// /////////////////////////////////////////
//> using lib "org.mongodb.scala:mongo-scala-driver_2.12:4.7.2"
// /////////////////////////////////////////
package me.lightspeed7.examples

import com.mongodb.client.model.ReturnDocument
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{Aggregates, Field, FindOneAndUpdateOptions, Projections}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class DocumentDatabaseTest extends AnyFunSuite with Matchers {

  val now: Long = System.currentTimeMillis()

  val uri: String = "mongodb://localhost:27017"

  val mongoClientSettings: MongoClientSettings = MongoClientSettings
    .builder()
    .applyConnectionString(ConnectionString(uri))
    .build()

  val mongoClient: MongoClient = MongoClient(mongoClientSettings)



  test("test partial document update") {

    val codecRegistry: CodecRegistry = fromRegistries(
      fromProviders(classOf[Profile]),
      fromProviders(classOf[Attribute]),
      DEFAULT_CODEC_REGISTRY)

    val coll: MongoCollection[Profile] = mongoClient
      .getDatabase("test").withCodecRegistry(codecRegistry)
      .getCollection[Profile]("test")

    await(coll.deleteOne(Document("_id" -> "1")).toFuture())
    await(coll.deleteOne(Document("_id" -> "2")).toFuture())

    val p1 = Profile("1", Set( //
      Attribute("attr1", "val1", "String", now) //
    ), Set(1, 2, 3))

    val p2 = Profile("2", Set(
      Attribute("attr2", "val2", "String", now),
      Attribute("attr3", "val3", "String", now)
    ), Set(1, 2, 3))

    Await.result(coll.insertOne(p1).toFuture(), 2.seconds).wasAcknowledged() mustBe true
    Await.result(coll.insertOne(p2).toFuture(), 2.seconds).wasAcknowledged() mustBe true

    val filter: Bson = Document.apply("_id" -> p1._id)
    val iOpts = FindOneAndUpdateOptions.apply().upsert(true).returnDocument(ReturnDocument.AFTER)
    val aggs: Seq[Bson] = Seq()


    val bar = Document("$if" -> Document("foo" -> "bar"))

    println(bar.toJson)
    // Aggregates.set(Field("attr", ""))

    // val updatedDoc: Profile = await(coll.findOneAndUpdate(filter, aggs, iOpts).toFuture(), 2.seconds)


  }

  def await[T](in:Future[T]): T = Await.result(in, 2.seconds)
}

case class Profile(_id: String, attributes: Set[Attribute], audiences: Set[Int] = Set())

case class Attribute(name: String, value: String, `type`: String, ts: Long)