package ru.iisuslik.parser

import com.xenomachina.argparser.ArgParser
import java.io.File

val binOp = mapOf('+' to 0, '-' to 0, '*' to 1, '/' to 1, '^' to 2)
val specialSymbols = setOf(' ', '(', ')')

fun parseExpr(s: List<Token>): Pair<List<Token>, Node> {
    var (rest, prevNode) = parseU(s)
    while (!rest.isEmpty()) {
        val token = rest.first()
        if (token is NumToken) {
            throw ParserException("Unexpect num", rest.size)
        } else if (token is OpToken) {
            if (token.op == ')') {
                return Pair(rest.drop(1), prevNode)
            }
            val (newRest, node) = parseU(rest.drop(1))
            rest = newRest
            prevNode = OpNode(token.op, prevNode, node)
        }
    }
    return Pair(rest, prevNode)
}

fun parseU(s: List<Token>): Pair<List<Token>, Node> {
    var (rest, prevNode) = parseS(s)
    while (!rest.isEmpty()) {
        val token = rest[0]
        if (token is NumToken) {
            throw ParserException("Unexpected num", rest.size)
        } else if (token is OpToken) {
            if (token.op == ')' || binOp[token.op] == 0) {
                return Pair(rest, prevNode)
            }
            val (newRest, node) = parseS(rest.drop(1))
            rest = newRest
            prevNode = OpNode(token.op, prevNode, node)
        }
    }
    return Pair(rest, prevNode)
}

fun parseS(s: List<Token>): Pair<List<Token>, Node> {
    var (rest, prevNode) = parseP(s)
    if (rest.isEmpty())
        return Pair(rest, prevNode)
    val token = rest.first()
    if (token is NumToken) {
        throw ParserException("Unexpected num", rest.size)
    } else if (token is OpToken) {
        if (binOp[token.op] != 2) {
            return Pair(rest, prevNode)
        }
        val (newRest, node) = parseS(rest.drop(1))
        rest = newRest
        prevNode = OpNode(token.op, prevNode, node)
    }
    return Pair(rest, prevNode)
}

fun parseP(s: List<Token>): Pair<List<Token>, Node> {
    val firstToken = s.first()

    if (s.isEmpty()) {
        throw ParserException("Empty Expression", s.size)
    }
    if (firstToken is OpToken) {
        if (binOp.containsKey(firstToken.op) || firstToken.op == ')') {
            throw ParserException("Empty Expression", s.size)
        }
        return parseExpr(s.drop(1))
    } else if (firstToken is NumToken) {
        return Pair(s.drop(1), VNode(firstToken.value))
    }
    throw ParserException("Strange token", -1)
}


fun getStringFromFile(fileName: String): String {
    return File(fileName).readText()
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

fun getTokens(s: String): List<Token> {
    val res = mutableListOf<Token>()
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
                throw ParserException("Symbol $c isn`t operation or number", s.length - i)
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

fun parse(s: String): Pair<List<Token>, Node> {
    if (s.isEmpty()) {
        throw ParserException("Empty Expression", s.length)
    }
    if (s.first() == '(' && s.last() == ')') {
        if (check(s)) {
            return parseP(getTokens(s))
        }
    }
    return parseExpr(getTokens(s))
}

fun parseFromFile(filename: String) {
    val s = getStringFromFile(filename)
    val (_, node) = try {
        parse(s)
    } catch (e: ParserException) {
        System.err.println(e.message + ", position" + (s.length - e.restLength))
        return
    }
    printAllAboutNode(node)
}

fun main(args: Array<String>) {
    val argParser = ParserArgs(ArgParser(args))
    parseFromFile(argParser.filename)
}

fun printAllAboutNode(node: Node) {
    println("\nFirst appearance\n")
    println(node)
    println("\nSecond appearance\n")
    println(Node.getTree(node, "", true))
    println("Calculation result: ${node.calculate()}")
}

class ParserArgs(parser: ArgParser) {
    val filename: String by parser.storing("--file", help = "choose port that server will listen to")
}