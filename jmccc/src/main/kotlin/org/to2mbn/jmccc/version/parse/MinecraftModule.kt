package org.to2mbn.jmccc.version.parse

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.LocalDateTime

val minecraftModule: Module = SimpleModule("org.to2mbn.jmccc.MinecraftModule")
		.addSerializer(LocalDateTime::class.java, DateSerializer())
		.addDeserializer(LocalDateTime::class.java, DateDeserializer())
