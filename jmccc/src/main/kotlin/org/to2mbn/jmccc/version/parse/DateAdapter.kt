package org.to2mbn.jmccc.version.parse

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

private val DATE_ISO_8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
private val DATE_EN_US = DateTimeFormatter.ofPattern("MMM d, y[,] h:mm:ss a", Locale.US)
private const val IDX_COLON = 22

internal class DateSerializer : StdSerializer<LocalDateTime>(LocalDateTime::class.java) {
	override fun serialize(value: LocalDateTime, gen: JsonGenerator, provider: SerializerProvider) =
			value.format(DATE_ISO_8601).let {
				gen.writeString("${it.substring(0 until IDX_COLON)}:${it.substring(IDX_COLON)}")
			}
}

internal class DateDeserializer : StdDeserializer<LocalDateTime>(LocalDateTime::class.java) {
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
			p.getText().let {
				try {
					LocalDateTime.parse(it, DATE_EN_US)
				} catch(e1: DateTimeParseException) {
					try {
						LocalDateTime.parse(it, DATE_ISO_8601)
					} catch(e2: DateTimeParseException) {
						try {
							LocalDateTime.parse(it.replace("Z", "+00:00").removeRange(IDX_COLON..IDX_COLON), DATE_ISO_8601)
						} catch(e3: DateTimeParseException) {
							throw InvalidFormatException(p, "Invalid date ${it}", it, Date::class.java).apply {
								addSuppressed(e1)
								addSuppressed(e2)
								addSuppressed(e3)
							}
						}
					}
				}
			}
}
