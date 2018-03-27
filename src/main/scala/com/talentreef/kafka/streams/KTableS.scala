package com.talentreef.kafka.streams

import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.common.utils.Bytes
import FunctionConversions._

/**
 * Wraps the Java class KTable and delegates method calls to the underlying Java object.
 */
class KTableS[K, V](val inner: KTable[K, V]) {

  def filter(predicate: (K, V) => Boolean): KTableS[K, V] = {
    new KTableS(inner.filter(predicate(_, _)))
  }

  def filter(predicate: (K, V) => Boolean,
    materialized: Materialized[K, V, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, V] = {
    new KTableS(inner.filter(predicate.asPredicate, materialized))
  }

  def filterNot(predicate: (K, V) => Boolean): KTableS[K, V] = {
    new KTableS(inner.filterNot(predicate(_, _)))
  }

  def filterNot(predicate: (K, V) => Boolean,
    materialized: Materialized[K, V, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, V] = {
    new KTableS(inner.filterNot(predicate.asPredicate, materialized))
  }

  def mapValues[VR](mapper: V => VR): KTableS[K, VR] = {
    new KTableS(inner.mapValues[VR](mapper.asValueMapper))
  }

  def mapValues[VR](mapper: V => VR,
    materialized: Materialized[K, VR, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, VR] = {
    new KTableS(inner.mapValues[VR](mapper.asValueMapper, materialized))
  }

  def toStream: KStreamS[K, V] = new KStreamS(inner.toStream)

  def toStream[KR](mapper: (K, V) => KR): KStreamS[KR, V] = {
    new KStreamS(inner.toStream[KR](mapper.asKeyValueMapper))
  }

  def groupBy[KR, VR](selector: (K, V) => (KR, VR))(implicit canBeSerialized: CanBeSerialized[KR, VR]): KGroupedTableS[KR, VR] = {
    new KGroupedTableS(inner.groupBy(selector.asKeyValueMapper, canBeSerialized.serialized()))
  }

  def join[VO, VR](other: KTableS[K, VO],
    joiner: (V, VO) => VR): KTableS[K, VR] = {

    new KTableS(inner.join[VO, VR](other.inner, joiner.asValueJoiner))
  }

  def join[VO, VR](other: KTableS[K, VO],
    joiner: (V, VO) => VR,
    materialized: Materialized[K, VR, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, VR] = {

    new KTableS(inner.join[VO, VR](other.inner, joiner.asValueJoiner, materialized))
  }

  def leftJoin[VO, VR](other: KTableS[K, VO],
    joiner: (V, VO) => VR): KTableS[K, VR] = {

    new KTableS(inner.leftJoin[VO, VR](other.inner, joiner.asValueJoiner))
  }

  def leftJoin[VO, VR](other: KTableS[K, VO],
    joiner: (V, VO) => VR,
    materialized: Materialized[K, VR, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, VR] = {

    new KTableS(inner.leftJoin[VO, VR](other.inner, joiner.asValueJoiner, materialized))
  }

  def outerJoin[VO, VR](other: KTableS[K, VO],
    joiner: (V, VO) => VR): KTableS[K, VR] = {

    new KTableS(inner.outerJoin[VO, VR](other.inner, joiner.asValueJoiner))
  }

  def outerJoin[VO, VR](other: KTableS[K, VO],
    joiner: (V, VO) => VR,
    materialized: Materialized[K, VR, KeyValueStore[Bytes, Array[Byte]]]): KTableS[K, VR] = {

    new KTableS(inner.outerJoin[VO, VR](other.inner, joiner.asValueJoiner, materialized))
  }

  def queryableStoreName: String =
    inner.queryableStoreName
}
