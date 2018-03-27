package com.talentreef.kafka.streams

import java.util.regex.Pattern

import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.{GlobalKTable, KStream, KTable, Materialized}
import org.apache.kafka.streams.processor.{ProcessorSupplier, StateStore}
import org.apache.kafka.streams.state.{KeyValueStore, StoreBuilder}
import org.apache.kafka.streams.{Consumed, StreamsBuilder, Topology}

import scala.collection.JavaConverters._

/**
  * Wraps the Java class StreamsBuilder and delegates method calls to the underlying Java object.
  */
class KafkaStreamsBuilder(inner: StreamsBuilder = new StreamsBuilder) {

  import scala.language.implicitConversions
  private implicit def toS[K, V](stream: KStream[K, V]) = new KStreamS(stream)
  private implicit def toS[K, V](table: KTable[K, V]) = new KTableS(table)

  def stringStream(topic: String): KStreamS[String, String] =
    stream(topic)

  def stream[K, V](topic: String)(implicit canBeConsumed: CanBeConsumed[K, V]): KStreamS[K, V] =
    inner.stream[K, V](topic, canBeConsumed.consumed())

  def stream[K, V](topics: List[String])(implicit canBeConsumed: CanBeConsumed[K, V]): KStreamS[K, V] =
    inner.stream[K, V](topics.asJava, canBeConsumed.consumed())

  def stream[K, V](topicPattern: Pattern)(implicit canBeConsumed: CanBeConsumed[K, V]): KStreamS[K, V] =
    inner.stream[K, V](topicPattern, canBeConsumed.consumed())

  def table[K, V](topic: String)(implicit canBeConsumed: CanBeConsumed[K, V]): KTableS[K, V] =
    inner.table[K, V](topic, canBeConsumed.consumed())

  def table[K, V](topic: String, materialized: Materialized[K, V, KeyValueStore[Bytes, Array[Byte]]])
    (implicit canBeConsumed: CanBeConsumed[K, V]): KTableS[K, V] =
    inner.table[K, V](topic, canBeConsumed.consumed(), materialized)

  def globalTable[K, V](topic: String)(implicit canBeConsumed: CanBeConsumed[K, V]): GlobalKTable[K, V] =
    inner.globalTable(topic, canBeConsumed.consumed())

  def globalTable[K, V](topic: String, materialized: Materialized[K, V, KeyValueStore[Bytes, Array[Byte]]])
    (implicit canBeConsumed: CanBeConsumed[K, V]): GlobalKTable[K, V] =
    inner.globalTable(topic, canBeConsumed.consumed(), materialized)

  def addStateStore(builder: StoreBuilder[_ <: StateStore]): StreamsBuilder = inner.addStateStore(builder)

  def addGlobalStore(storeBuilder: StoreBuilder[_ <: StateStore],
                     topic: String,
                     sourceName: String,
                     consumed: Consumed[_, _],
                     processorName: String,
                     stateUpdateSupplier: ProcessorSupplier[_, _]): StreamsBuilder =
    inner.addGlobalStore(storeBuilder, topic, sourceName, consumed, processorName, stateUpdateSupplier)

  def build(): Topology = inner.build()
}
