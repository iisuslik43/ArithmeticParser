package ru.iisuslik.parser

import com.xenomachina.argparser.ArgParser

fun main(args: Array<String>) {
    val argParser = ParserArgs(ArgParser(args))
    try {
        val parser = KekParser(getStringFromFile(argParser.filename))
        println(parser.stringRest())
    } catch (e: ParserException) {
        println(e.message)
    }
}
