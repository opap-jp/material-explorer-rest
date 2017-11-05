package jp.opap.material.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object JsonSerializers {
  object AppSerializerModule extends SimpleModule {
    this.addSerializer(classOf[Seq[_]], new SeqSerializer())
    this.addSerializer(classOf[Map[_, _]], new MapSerializer())
    this.addSerializer(classOf[LocalDateTime], new LocalDateTimeSerializer())
  }

  class SeqSerializer() extends StdSerializer[Seq[_]](classOf[Seq[_]]) {
    override def serialize(value: Seq[_], gen: JsonGenerator, provider: SerializerProvider): Unit = {
      gen.writeStartArray()
      value.foreach(gen.writeObject)
      gen.writeEndArray()
    }
  }

  class MapSerializer() extends StdSerializer[Map[_, _]](classOf[Map[_, _]]) {
    override def serialize(value: Map[_, _], gen: JsonGenerator, provider: SerializerProvider): Unit = {
      gen.writeStartObject()
      value.foreach(entry => gen.writeObjectField(entry._1.toString, entry._2))
      gen.writeEndObject()
    }
  }

  class LocalDateTimeSerializer() extends StdSerializer[LocalDateTime](classOf[LocalDateTime]) {
    override def serialize(value: LocalDateTime, gen: JsonGenerator, provider: SerializerProvider): Unit = {
      gen.writeString(value.format(DateTimeFormatter.ISO_DATE_TIME))
    }
  }
}
