package ru.iisuslik.parser

import com.sun.org.apache.xpath.internal.operations.Bool
import com.xenomachina.argparser.ArgParser
import java.io.File

val binOp = mapOf('+' to 0, '-' to 0, '*' to 1, '/' to 1, '^' to 2)

fun parseExpr(s: String): Pair<String, Node> {
    var (rest, prevNode) = parseU(s)
    while (!rest.isEmpty()) {
        val op = rest[0]
        if (binOp[op] != 0) {
            if (op == ')') {
                return Pair(rest.drop(1), prevNode)
            }
            throw ParserException("Symbol $op isn`t operation or number", rest.length)
        }
        val (newRest, node) = parseU(rest.drop(1))
        rest = newRest
        prevNode = OpNode(op, prevNode, node)
    }
    return Pair(rest, prevNode)
}

fun parseU(s: String): Pair<String, Node> {
    var (rest, prevNode) = parseS(s)
    while (!rest.isEmpty()) {
        val op = rest[0]
        if (binOp[op] != 1) {
            if (op == ')' || binOp[op] == 0) {
                return Pair(rest, prevNode)
            }
            throw ParserException("Symbol $op isn`t operation or number", rest.length)
        }
        val (newRest, node) = parseS(rest.drop(1))
        rest = newRest
        prevNode = OpNode(op, prevNode, node)
    }
    return Pair(rest, prevNode)
}

fun parseS(s: String): Pair<String, Node> {
    var (rest, prevNode) = parseP(s)
    if (rest == "")
        return Pair(rest, prevNode)
    val op = rest[0]
    if (binOp[op] != 2) {
        if (op == ')' || binOp[op] == 0 || binOp[op] == 1) {
            return Pair(rest, prevNode)
        }
        throw ParserException("Symbol $op isn`t operation or number", rest.length)
    }
    val (newRest, node) = parseS(rest.drop(1))
    rest = newRest
    prevNode = OpNode(op, prevNode, node)

    return Pair(rest, prevNode)
}

fun parseP(s: String): Pair<String, Node> {
    if (s.isEmpty() || s.first() == ')') {
        throw ParserException("Empty Expression", s.length)
    }
    return if (s.first() == '(') {
        parseExpr(s.drop(1))
    } else {
        parseV(s)
    }
}

fun check(s: String): Boolean {
    val str = s.filter { it == '(' || it == ')' }
    var count = 0
    for(i in 0 until str.length) {
        if(str[i] == '(') {
            count++
        } else {
            count--
        }
        if(count <= 0 && i != str.length - 1) {
            return false
        }
    }
    return true
}

fun parseV(s: String): Pair<String, Node> {
    var res = 0
    for (i in 0 until s.length) {
        if (s[i].isDigit()) {
            res = res * 10 + (s[i] - '0')
        } else {
            return Pair(s.drop(i), VNode(res))
        }
    }
    return Pair("", VNode(res))
}


fun getStringFromFile(fileName: String): String {
    return File(fileName).readText()
}

fun parse(s: String): Pair<String, Node> {
    if (s.isEmpty()) {
        throw ParserException("Empty Expression", s.length)
    }
    if(s.first() == '(' && s.last() == ')') {
        if (check(s)) {
            println("kek")
            return parseP(s)
        }
    }
    return parseExpr(s)
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