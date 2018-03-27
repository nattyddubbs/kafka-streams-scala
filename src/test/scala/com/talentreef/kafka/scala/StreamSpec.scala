package com.talentreef.kafka.scala

import com.talentreef.kafka.streams.{CanSerde, KafkaStreamsBuilder}
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
import org.scalatest.{Matchers, WordSpec}
import net.manub.embeddedkafka.ConsumerExtensions._
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.JoinWindows

class StreamSpec extends WordSpec with Matchers with EmbeddedKafkaStreamsAllInOne {
  implicit val stringSerializer = CanSerde.StringCanSerde.serde.serializer()

  "KStreamS" should {

    "pipe from one topic to another" in {
      val inputTopic = "input-topic"
      val outputTopic = "output-topic"
      val throughTopic = "through-topic"
      // your code for building the stream goes here e.g.
      val streamBuilder = new KafkaStreamsBuilder()

      // From one stream to another.
      streamBuilder.stringStream(inputTopic).through(throughTopic).to(outputTopic)

      runStreamsWithStringConsumer(
        topicsToCreate = Seq(inputTopic, outputTopic, throughTopic),
        topology = streamBuilder.build()
      ) { consumer =>
        // Publish a message.
        publishToKafka(inputTopic, key = "hello", message = "world")

        val toTopicRecord = consumer.consumeLazily(outputTopic).head
        toTopicRecord.key() shouldEqual "hello"
        toTopicRecord.value() shouldEqual "world"

        val throughTopicRecord = consumer.consumeLazily(throughTopic).head
        throughTopicRecord.key() shouldEqual "hello"
        throughTopicRecord.value() shouldEqual "world"
      }
    }

    "filter one topic to another" in {
      val inputTopic = "filtered-input-topic"
      val outputTopic = "filtered-topic"
      // your code for building the stream goes here e.g.
      val streamBuilder = new KafkaStreamsBuilder()

      // Filter some messages.
      streamBuilder.stringStream(inputTopic).filter({
        (k: String, v: String) =>
          k.startsWith("o")
      }).to(outputTopic)

      runStreamsWithStringConsumer(
        topicsToCreate = Seq(inputTopic, outputTopic),
        topology = streamBuilder.build()
      ) { consumer =>
        publishToKafka(inputTopic, key = "hello", message = "world")
        consumer.consumeLazily(outputTopic).isEmpty shouldBe true
      }
    }

    "update key from stream to another" in {
      val inputTopic = "keyed-input-topic"
      val outputTopic = "keyed-topic"
      // your code for building the stream goes here e.g.
      val streamBuilder = new KafkaStreamsBuilder()

      // make the key equal to the value.
      streamBuilder.stringStream(inputTopic).selectKey({
        (k: String, v: String) =>
          v
      }).to(outputTopic)

      runStreamsWithStringConsumer(
        topicsToCreate = Seq(inputTopic, outputTopic),
        topology = streamBuilder.build()
      ) { consumer =>
        publishToKafka(inputTopic, key = null: String, message = "world")
        val toTopicRecord = consumer.consumeLazily(outputTopic).head
        toTopicRecord.key() shouldEqual "world"
        toTopicRecord.value() shouldEqual "world"
      }
    }

    "map from one type to another in another stream" in {
      implicit val deser = Serdes.Integer().asInstanceOf[Serde[Int]].deserializer()

      val inputTopic = "mapped-input-topic"
      val outputTopic = "mapped-topic"
      // your code for building the stream goes here e.g.
      val streamBuilder = new KafkaStreamsBuilder()

      // Map from one type to another.
      streamBuilder.stringStream(inputTopic).map({
        (k: String, v: String) =>
          (k.length, v.length)
      }).to(outputTopic)

      runStreams(
        topicsToCreate = Seq(inputTopic, outputTopic),
        topology = streamBuilder.build()
      )(withConsumer { consumer: KafkaConsumer[Int, Int] =>
        publishToKafka(inputTopic, key = "hello", message = "world")
        val toTopicRecord = consumer.consumeLazily(outputTopic).head
        toTopicRecord.key() shouldEqual 5
        toTopicRecord.value() shouldEqual 5
      })
    }

    "join streams of the same type" in {
      implicit val deser = Serdes.Integer().asInstanceOf[Serde[Int]].deserializer()

      val inputTopic = "join-input-topic"
      val otherInputTopic = "join-input-2-topic"
      val outputTopic = "joined-output-topic"
      // your code for building the stream goes here e.g.
      val streamBuilder = new KafkaStreamsBuilder()

      // Map from one type to another.
      val s1 = streamBuilder.stringStream(inputTopic)
      val s2 = streamBuilder.stringStream(otherInputTopic)

      // Join them.
      s1.join(s2, {
        (left: String, right: String) =>
          s"$left:$right"
      }, JoinWindows.of(500)).to(outputTopic)

      // Join these two into an output topic.

      runStreams(
        topicsToCreate = Seq(inputTopic, otherInputTopic, outputTopic),
        topology = streamBuilder.build()
      )(withStringConsumer { consumer =>

        val expectedResults: Map[String, String] = Map(
          "leo" -> "revenant:inception",
          "bill" -> "even:though"
        )

        publishToKafka(inputTopic, key = "leo", message = "revenant")
        publishToKafka(otherInputTopic, key = "leo", message = "inception")

        publishToKafka(inputTopic, key = "bill", message = "even")
        publishToKafka(otherInputTopic, key = "bill", message = "though")

        val records = consumer.consumeLazily(outputTopic).toList

        var count = 0

        records.foreach { record: ConsumerRecord[String, String] =>
          expectedResults.contains(record.key) shouldBe true
          expectedResults(record.key()) shouldBe record.value()
          count += 1
        }

        count shouldBe 2
      })
    }

    "flatMap streams to iterable type" in {

      val inputTopic = "flatMap-input-topic"
      val outputTopic = "flatMap-output-topic"
      // your code for building the stream goes here e.g.
      val streamBuilder = new KafkaStreamsBuilder()

      // Map from one type to another.
      val s1 = streamBuilder.stringStream(inputTopic)

      // Join them.
      s1.flatMap{
        (k: String, v: String) => v.map{k -> _.toString}
      }.to(outputTopic)

      // Join these two into an output topic.

      runStreams(
        topicsToCreate = Seq(inputTopic, outputTopic),
        topology = streamBuilder.build()
      )(withStringConsumer { consumer =>

        publishToKafka(inputTopic, key = "what", message = "isgoingon")

        val records: List[String] = consumer.consumeLazily(outputTopic).toList.map{r => r.value}
        val characters: List[String] = "isgoingon".toCharArray.map(_.toString).toList

        for(i <- records.indices){
          records(i) shouldBe characters(i)
        }

        records.size shouldBe 9
      })
    }

    "split streams based on a predicate" in {

      val inputTopic = "split-input-topic"
      val outputTopic1 = "split-output-small-topic"
      val outputTopic2 = "split-output-large-topic"
      // your code for building the stream goes here e.g.
      val streamBuilder = new KafkaStreamsBuilder()

      // Map from one type to another.
      // Join them.
      val (s1, s2) = streamBuilder.stringStream(inputTopic).split{
        (_: String, v: String) => v.length > 4
      }

      s1.to(outputTopic1)
      s2.to(outputTopic2)

      // Join these two into an output topic.

      runStreams(
        topicsToCreate = Seq(inputTopic, outputTopic1, outputTopic2),
        topology = streamBuilder.build()
      )(withStringConsumer { consumer =>

        publishToKafka(inputTopic, key = "what", message = "isgoingon")
        publishToKafka(inputTopic, key = "why", message = "isgo")

        val s1Records = consumer.consumeLazily(outputTopic1)
        val s2Records = consumer.consumeLazily(outputTopic1)

        s1Records.head.value == "isgoingon"
        s1Records.head.value == "isgo"

      })
    }
  }
}