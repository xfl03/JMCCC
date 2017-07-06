package org.to2mbn.jmccc.test


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.to2mbn.jmccc.version.parse.minecraftModule
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class DateAdapterTest {

	val om = ObjectMapper().registerModule(minecraftModule)

	fun date(datetime: String) = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

	fun parseDate(input: String) = (
			om.readValue<Map<String, LocalDateTime>>(
					"""
						{"value":"${input}"}
					""",
					object : TypeReference<Map<String, LocalDateTime>>() {})
			).get("value")


	fun testParse(datetime: String, input: String) = assertEquals(parseDate(input), date(datetime))

	@Test
	fun test_parse_iso_8601_with_colon() = testParse("2016-05-10T10:17:16-01:00", "2016-05-10T10:17:16-01:00")

	@Test
	fun test_parse_iso_8601_without_colon() = testParse("2017-06-08T04:08:37+00:00", "2017-06-08T04:08:37+0000")

	@Test
	fun test_parse_iso_8601_with_Z() = testParse("2017-06-08T04:08:37+00:00", "2017-06-08T04:08:37Z")

	@Test
	fun test_parse_en_us() = testParse("2017-07-05T20:00:01+00:00", "Jul 5, 2017 8:00:01 PM")

	@Test
	fun test_parse_en_us_with_comma() = testParse("2017-07-05T20:00:01+00:00", "Jul 5, 2017, 8:00:01 PM")

}
