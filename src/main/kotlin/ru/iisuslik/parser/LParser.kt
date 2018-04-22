package ru.iisuslik.parser

import com.xenomachina.argparser.ArgParser
import com.xenomachina.text.clear

val blankSymbols = setOf(' ', '\n', '\t')
val operationsFirstSymbols = setOf('+', '-', '*', '/', '%', '=', '!', '>', '>', '<', '<', '&', '|')

class LParser(s: String) {
    var rest = getTokens(s)

    private fun getToken(stringNumber: Int, pos: Int, s: String): Token {
        if (KToken(stringNumber, pos, s).isOk) {
            return KToken(stringNumber, pos, s)
        }
        if (BToken(stringNumber, pos, s).isOk) {
            return BToken(stringNumber, pos, s)
        }
        if (NToken(stringNumber, pos, s).isOk) {
            return NToken(stringNumber, pos, s)
        }
        if (IToken(stringNumber, pos, s).isOk) {
            return IToken(stringNumber, pos, s)
        }
        if (OToken(stringNumber, pos, s).isOk) {
            return OToken(stringNumber, pos, s)
        }
        throw ParserException("No such token in $pos: \"$s\"", pos)
    }

    private fun getTokens(str: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val sb = StringBuilder()
        var stringNumber = 0
        var pos = 0
        val sa = str.split('\n')
        for (s in sa) {
            var i = 0
            while (i in s.indices) {
                val c = s[i]
                if (c in blankSymbols) {
                    if (!sb.isEmpty()) {
                        tokens.add(getToken(stringNumber, pos, sb.toString()))
                        sb.clear()
                    }
                    pos = i + 1
                } else if (c.toString() in splitTokens) {
                    if (!sb.isEmpty()) {
                        tokens.add(getToken(stringNumber, pos, sb.toString()))
                        sb.clear()
                    }
                    tokens.add(SToken(stringNumber, i, c.toString()))
                    pos = i + 1
                } else if (c in operationsFirstSymbols) {
                    if (!sb.isEmpty()) {
                        tokens.add(getToken(stringNumber, pos, sb.toString()))
                        sb.clear()
                    }
                    if (i + 1 in s.indices && OToken(stringNumber, pos, "${s[i]}${s[i+1]}").isOk) {
                        tokens.add(OToken(stringNumber, i, "${s[i]}${s[i+1]}"))
                        i++
                    } else {
                        if(!OToken(stringNumber, i, "${s[i]}").isOk) {
                            throw ParserException("No such token in $pos: \"$s\"", pos)
                        }
                        tokens.add(OToken(stringNumber, i, "${s[i]}"))
                    }
                    pos = i + 1
                } else {
                    sb.append(c)
                }
                i++
            }
            if (!sb.isEmpty()) {
                tokens.add(getToken(stringNumber, pos, sb.toString()))
                sb.clear()
            }
            stringNumber++
            pos = 0
        }
        return tokens
    }

    fun stringRest(): String {
        val sb = StringBuilder()
        for (t in rest) {
            sb.append("$t; ")
        }
        return sb.toString()
    }
}

fun main(args: Array<String>) {
    val argParser = ParserArgs(ArgParser(args))
    try {
        val parser = LParser(getStringFromFile(argParser.filename))
        println(parser.stringRest())
    } catch(e: ParserException) {
        println(e.message)
    }
}

class ParserArgs(parser: ArgParser) {
    val filename: String by parser.storing("--file", help = "choose port that server will listen to")
}