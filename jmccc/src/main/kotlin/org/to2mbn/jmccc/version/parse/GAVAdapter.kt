package org.to2mbn.jmccc.version.parse

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.to2mbn.jmccc.version.GAV

internal class GAVSerializer : StdSerializer<GAV>(GAV::class.java) {
	override fun serialize(value: GAV, gen: JsonGenerator, provider: SerializerProvider) = gen.writeString(value.toString())
}

internal class GAVDeserializer : StdDeserializer<GAV>(GAV::class.java) {
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext) = p.getText().let(::GAV)
}
