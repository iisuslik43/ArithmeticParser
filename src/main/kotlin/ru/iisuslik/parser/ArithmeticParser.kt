package ru.iisuslik.parser

import com.xenomachina.argparser.ArgParser
import java.io.File

val binOp = mapOf('+' to 0, '-' to 0, '*' to 1, '/' to 1, '^' to 2)
val specialSymbols = setOf(' ', '(', ')', '\n')

class ArithmeticParser(str: String) {
    var pos = 0
    var rest = getTokens(str)

    fun next() {
        if (rest.isEmpty())
            return
        pos += rest.first().length()
        rest = rest.drop(1)
    }

    fun parseExpr(): Node {
        var prevNode = parseU()
        while (!rest.isEmpty()) {
            val token = rest.first()
            if (token is NumToken) {
                throw ParserException("Unexpect num", pos)
            } else if (token is OpToken) {
                if (token.op == ')') {
                    next()
                    return prevNode
                }
                next()
                val node = parseU()
                prevNode = OpNode(token.op, prevNode, node)
            }
        }
        return prevNode
    }

    private fun parseU(): Node {
        var prevNode = parseS()
        while (!rest.isEmpty()) {
            val token = rest[0]
            if (token is NumToken) {
                throw ParserException("Unexpected num", pos)
            } else if (token is OpToken) {
                if (token.op == ')' || binOp[token.op] == 0) {
                    return prevNode
                }
                next()
                val node = parseS()
                prevNode = OpNode(token.op, prevNode, node)
            }
        }
        return prevNode
    }

    private fun parseS(): Node {
        var prevNode = parseP()
        if (rest.isEmpty())
            return prevNode
        val token = rest.first()
        if (token is NumToken) {
            throw ParserException("Unexpected num", pos)
        } else if (token is OpToken) {
            if (binOp[token.op] != 2) {
                return prevNode
            }
            next()
            val node = parseS()
            prevNode = OpNode(token.op, prevNode, node)
        }
        return prevNode
    }

    fun parseP(): Node {
        if (rest.isEmpty()) {
            throw ParserException("Empty Expression", pos)
        }
        val firstToken = rest.first()
        if (firstToken is OpToken) {
            if (binOp.containsKey(firstToken.op) || firstToken.op == ')') {
                throw ParserException("Empty Expression", pos)
            }
            next()
            return parseExpr()
        } else if (firstToken is NumToken) {
            next()
            return VNode(firstToken.value)
        }
        throw ParserException("Strange token", pos)
    }


    fun check(s: String): Boolean {
        val str = s.filter { it == '(' || it == ')' }
        var count = 0
        for (i in 0 until str.length) {
            if (str[i] == '(') {
                count++
            } else {
                count--
            }
            if (count <= 0 && i != str.length - 1) {
                return false
            }
        }
        return true
    }

    private fun getTokens(s: String): List<ArithmToken> {
        val res = mutableListOf<ArithmToken>()
        var curToken: NumToken? = null
        for (i in 0 until s.length) {
            val c = s[i]
            if (c.isDigit()) {
                if (curToken == null) {
                    curToken = NumToken(c - '0')
                } else {
                    curToken.value = curToken.value * 10 + (c - '0')
                }
            } else {
                if (!binOp.containsKey(c) && !specialSymbols.contains(c)) {
                    throw ParserException("Symbol $c isn`t operation or number", i)
                }
                if (curToken != null) {
                    res.add(curToken)
                    curToken = null
                }
                if (!c.isWhitespace()) {
                    res.add(OpToken(c))
                }
            }
        }
        if (curToken != null) {
            res.add(curToken)
        }
        return res
    }
}


fun getStringFromFile(fileName: String): String {
    return File(fileName).readText()
}

fun parse(s: String): Pair<List<ArithmToken>, Node> {
    val parser = ArithmeticParser(s)
    if (s.isEmpty()) {
        throw ParserException("Empty Expression", s.length)
    }
    if (s.first() == '(' && s.last() == ')') {
        if (parser.check(s)) {
            val res = parser.parseP()
            return Pair(parser.rest, res)
        }
    }
    val res = parser.parseExpr()
    return Pair(parser.rest, res)
}

fun parseFromFile(filename: String) {
    val s = getStringFromFile(filename)
    val (_, node) = try {
        parse(s)
    } catch (e: ParserException) {
        System.err.println(e.message + ", position" + e.pos)
        return
    }
    printAllAboutNode(node)
}



fun printAllAboutNode(node: Node) {
    println("\nFirst appearance\n")
    println(node)
    println("\nSecond appearance\n")
    println(Node.getTree(node, "", true))
    println("Calculation result: ${node.calculate()}")
}

fun main(args: Array<String>) {
    val argParser = ParserArgs(ArgParser(args))
    parseFromFile(argParser.filename)
}