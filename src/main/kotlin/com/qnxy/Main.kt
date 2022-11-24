package com.qnxy

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.json.JsonMapper
import com.qnxy.parser.Parser
import java.io.FileNotFoundException
import java.nio.charset.Charset

/**
 *
 * @author ck
 * 2022/11/24
 */
class Main

private val json = JsonMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .writerWithDefaultPrettyPrinter()

fun main() {

    val inputStream = Main::class.java.classLoader.getResourceAsStream("test.txt") ?: throw FileNotFoundException("test.txt")
    val codeStr = inputStream.readBytes().toString(Charset.defaultCharset())

    val parser = Parser(codeStr)
    val parse = parser.parse()

    json.writeValueAsString(parse).also { println(it) }
}