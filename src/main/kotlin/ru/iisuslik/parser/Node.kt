package ru.iisuslik.parser

import kotlin.math.pow

open class Node {
    open fun calculate(): Int {
        return 0
    }

    companion object {
        fun getTree(node: Node, prefix: String, isTail: Boolean): String {
            if (node is VNode) {
                return prefix + (if (isTail) "└── " else "├── ") + node.value + '\n'
            } else if (node is OpNode) {
                var res = prefix + (if (isTail) "└── " else "├── ") + node.op + '\n'
                res += getTree(node.right, prefix + (if (isTail) "    " else "│   "), false)
                res += getTree(node.left, prefix + (if (isTail) "    " else "│   "), true)
                return res
            }
            return ""
        }
    }
}

class VNode(val value: Int) : Node() {
    override fun toString(): String {
        return value.toString()
    }

    override fun calculate(): Int {
        return value
    }
}

class OpNode(val op: Char, val left: Node, val right: Node) : Node() {
    override fun toString(): String {
        return "($left$op$right)"
    }

    override fun calculate(): Int {
        val res1 = left.calculate()
        val res2 = right.calculate()
        return when (op) {
            '+' -> res1 + res2
            '-' -> res1 - res2
            '*' -> res1 * res2
            '/' -> res1 / res2
            '^' -> res1.toDouble().pow(res2).toInt()
            else -> {
                throw ParserException("Wrong operation", -1)
            }
        }
    }
}