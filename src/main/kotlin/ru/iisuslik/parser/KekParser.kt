package ru.iisuslik.parser

import com.xenomachina.argparser.ArgParser
import com.xenomachina.text.clear

val blankSymbols = setOf(' ', '\n', '\t')
val operationsFirstSymbols = setOf('+', '-', '*', '/', '%', '=', '!', '>', '>', '<', '<', '&', '|')

class KekParser(s: String) {
    var rest = getTokens(s)

    private fun next(): Token {
        if (rest.isEmpty()) {
            throw ParserException("Unexpected end of file", -1, -1)
        }
        val res = rest.first()
        rest = rest.drop(1).toMutableList()
        return res
    }

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
        throw ParserException("No such token in $pos: \"$s\"", pos, stringNumber)
    }

    private fun clearBuilder(sb: StringBuilder, tokens: MutableList<Token>,
                             stringNumber: Int, pos: Int) {
        if (!sb.isEmpty()) {
            val token = getToken(stringNumber, pos, sb.toString())
            tokens.add(token)
            sb.clear()
        }
    }

    private fun getTokens(str: String): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        val sb = StringBuilder()
        var stringNumber = 0
        var pos = 0
        val sa = str.split('\n')
        for (s in sa) {
            var i = 0
            while (i in s.indices) {
                val c = s[i]
                if (i + 1 in s.indices && c == '/' && s[i + 1] == '/') {
                    break
                }
                if (c in blankSymbols) {
                    clearBuilder(sb, tokens, stringNumber, pos)
                    pos = i + 1
                } else if (c.toString() in splitTokens) {
                    if (!sb.isEmpty()) {
                        tokens.add(getToken(stringNumber, pos, sb.toString()))
                        sb.clear()
                    }
                    tokens.add(SToken(stringNumber, i, c.toString()))
                    pos = i + 1
                } else if (c in operationsFirstSymbols) {
                    if (i > 0 && (c == '+' || c == '-') && s[i - 1] == 'e') {
                        sb.append(c)
                    } else {
                        clearBuilder(sb, tokens, stringNumber, pos)
                        if (i + 1 in s.indices && OToken(stringNumber, pos,
                                        "${s[i]}${s[i + 1]}").isOk) {
                            tokens.add(OToken(stringNumber, i, "${s[i]}${s[i + 1]}"))
                            i++
                        } else {
                            if (!OToken(stringNumber, i, "${s[i]}").isOk) {
                                throw ParserException("No such token in $pos: \"$s\"", pos, stringNumber)
                            }
                            tokens.add(OToken(stringNumber, i, "${s[i]}"))
                        }
                        pos = i + 1
                    }
                } else {
                    sb.append(c)
                }
                i++
            }
            clearBuilder(sb, tokens, stringNumber, pos)
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

    fun parseL(): MainNode {
        val program = mutableListOf<StNode>()
        while (!rest.isEmpty()) {
            program.addAll(parseSt())
        }
        return MainNode(NodeList(program))
    }

    private fun parseSt(lastInFor: Boolean = false): List<StNode> {
        var first = rest.first()
        if (first is KToken) {
            return when (first.keyWord) {
                KeyWord.IF -> listOf(parseIf())
                KeyWord.RETURN -> listOf(parseReturn())
                KeyWord.WHILE -> listOf(parseWhile())
                KeyWord.WRITE -> listOf(parseWrite())
                KeyWord.READ -> listOf(parseRead())
                KeyWord.FOR -> parseFor()
                else -> throw ParserException("Strange keyword ${first.keyWord} in statement",
                        first.pos, first.sNumb)
            }
        }
        if (rest.size >= 2 && first is IToken && rest[1] is SToken && rest[1].s == "(") {
            val res = parseCall()
            if(!lastInFor)
                skipSplit(";")
            return listOf(CallStNode(res))
        }

        if (rest.size >= 2) {
            first = next()
            val second = next()
            if (!rest.isEmpty() && first is IToken && second is OToken && second.op == "=") {
                val res = ANode(INode(first), second, parseExpr())
                if(!lastInFor)
                    skipSplit(";")
                return listOf(res)
            }
        }
        throw ParserException("Strange tokens in statement", first.pos, first.sNumb)
    }

    private fun parseStList(): MutableList<StNode> {
        val statements = mutableListOf<StNode>()
        while (rest.first() !is SToken || (rest.first() as SToken).split != "}") {
            statements.addAll(parseSt())
        }
        return statements
    }

    private fun parseExpr(prior: Int = 6): ExprNode {
        if(prior == 0) {
            return ExprNode(parseV())
        }
        val left = parseExpr(prior - 1)
        var op: OToken? = null
        var expr: ExprNode? = null
        if (!rest.isEmpty()) {
            val first = rest.first()
            if (first is OToken && operations.contains(first.op) && operations[first.op] == prior) {
                op = next() as OToken
                expr = parseExpr(prior)
            }
        }
        return ExprNode(ExprVNode(left), op, expr)
    }


    private fun parseV(): VNode {
        val first = rest.first()
        if (first is KToken) {
            return when (first.keyWord) {
                KeyWord.FUN -> parseFun()
                else -> throw ParserException("Strange keyword ${first.keyWord} in expression",
                        first.pos, first.sNumb)
            }
        }
        if (first is BToken) {
            return BoolNode(next() as BToken)
        }
        if (first is NToken) {
            return NumNode(next() as NToken)
        }
        if (first is IToken) {
            if (rest.size >= 2) {
                val second = rest[1]
                if (second is SToken && second.split == "(") {
                    return parseCall()
                }
            }
            return INode(next() as IToken)
        }
        if (first is SToken && first.split == "(") {
            skipSplit("(")
            val res = parseExpr()
            skipSplit(")")
            return ExprVNode(res)
        }
        throw ParserException("Unexpected token in expression + ${first.pos}", first.pos, first.sNumb)
    }

    private fun parseCall(): CallNode {
        val name = next()
        skipSplit("(")
        val args = parseArgList()
        skipSplit(")")
        return CallNode(INode(name as IToken), NodeList(args))
    }

    private fun parseRead(): ReadNode {
        val readWord = next() as KToken
        skipSplit("(")
        val next = next()
        if (next !is IToken) {
            throw ParserException("Unexpected token in read arg", next.pos, next.sNumb)
        }
        val res = ReadNode(readWord, INode(next))
        skipSplit(")")
        skipSplit(";")
        return res
    }

    private fun parseFun(): FunNode {
        val funWord = next()
        skipSplit("(")
        val args = parseArgList()
        skipSplit(")")
        val next = rest.first()
        if (next is OToken && next.op == "->") {
            next()
            val body = listOf<StNode>(RetNode(KToken(next.sNumb, next.pos, "return"), ExprNode(parseV())))
            return FunNode(funWord as KToken, NodeList(args), NodeList(body))
        }
        skipSplit("{")
        val body = parseStList()
        skipSplit("}")
        return FunNode(funWord as KToken, NodeList(args), NodeList(body))
    }

    private fun parseArgList(): List<ExprNode> {
        val args = mutableListOf<ExprNode>()
        while (!isSplit(")")) {
            args.add(parseExpr())
            if (isSplit(",")) {
                skipSplit(",")
            }
        }
        return args
    }

    private fun parseIf(): IfNode {
        val ifToken = next()
        skipSplit("(")
        val condition = parseExpr()
        skipSplit(")")
        skipSplit("{")
        val body = parseStList()
        skipSplit("}")
        var elseWord: KToken? = null
        var elseBody: NodeList<StNode>? = null
        if (!rest.isEmpty()) {
            val first = rest.first()
            if (first is KToken && first.keyWord == KeyWord.ELSE) {
                elseWord = next() as KToken
                skipSplit("{")
                elseBody = NodeList(parseStList())
                skipSplit("}")
            }
        }
        return IfNode(ifToken as KToken, condition, NodeList(body), elseWord, elseBody)
    }

    private fun parseWhile(): WhileNode {
        val whileToken = next()
        skipSplit("(")
        val condition = parseExpr()
        skipSplit(")")
        skipSplit("{")
        val body = parseStList()
        skipSplit("}")
        return WhileNode(whileToken as KToken, condition, NodeList(body))
    }

    private fun parseFor() : List<StNode> {
        val res = mutableListOf<StNode>()
        val forWord = next()
        skipSplit("(")
        val init = parseSt().first()
        println(stringRest())
        val condition = parseExpr()
        skipSplit(";")
        val findNext = parseSt(true).first()
        skipSplit(")")
        skipSplit("{")
        val body = parseStList()
        body.add(findNext)
        skipSplit("}")
        res.add(init)
        res.add(WhileNode(KToken(-1, -1, "while"), condition, NodeList(body)))
        return res
    }

    private fun parseReturn(): StNode {
        val returnWord = next() as KToken
        if (!rest.isEmpty()) {
            val next = rest.first()
            if (next is KToken && next.keyWord == KeyWord.IF) {
                val ifToken = next()
                skipSplit("(")
                val condition = parseExpr()
                skipSplit(")")
                skipSplit("{")
                val ifReturn = RetNode(KToken(-1, -1, "return"), ExprNode(parseV()))
                skipSplit("}")
                if (!rest.isEmpty()) {
                    val first = rest.first()
                    if (first is KToken && first.keyWord == KeyWord.ELSE) {
                        val elseWord = next() as KToken
                        skipSplit("{")
                        val elseReturn = RetNode(KToken(-1, -1, "return"), ExprNode(parseV()))
                        skipSplit("}")
                        return IfNode(ifToken as KToken, condition, NodeList(listOf(ifReturn)), elseWord, NodeList(listOf(elseReturn)))
                    }
                }
                return IfNode(ifToken as KToken, condition, NodeList(listOf(ifReturn)))
            }
        }
        val res = RetNode(returnWord, parseExpr())
        skipSplit(";")
        return res
    }

    private fun skipSplit(expected: String) {
        val split = next()
        if (split !is SToken) {
            throw ParserException("Strange token ${split}", split.pos, split.sNumb)
        } else if (split.split != expected) {
            throw ParserException("Strange split token ${split.split}", split.pos, split.sNumb)
        }
    }

    private fun isSplit(expected: String): Boolean {
        val first = rest.first()
        return first is SToken && first.split == expected
    }

    private fun parseWrite(): WriteNode {
        val writeToken = next()
        skipSplit("(")
        val res = WriteNode(writeToken as KToken, parseExpr())
        skipSplit(")")
        skipSplit(";")
        return res
    }

}

fun main(args: Array<String>) {
    val argParser = ParserArgs(ArgParser(args))
    try {
        val parser = KekParser(getStringFromFile(argParser.filename))
        println(parser.parseL().getTree())
    } catch (e: ParserException) {
        println(e.message)
    }
}

class ParserArgs(parser: ArgParser) {
    val filename: String by parser.storing("--file",
            help = "choose port that server will listen to")
}