package ru.iisuslik.parser

import kotlin.math.pow

open class ArithmNode {
    open fun calculate(): Int {
        return 0
    }

    companion object {
        fun getTree(arithmNode: ArithmNode, prefix: String, isTail: Boolean): String {
            if (arithmNode is VArithmNode) {
                return prefix + (if (isTail) "└── " else "├── ") + arithmNode.value + '\n'
            } else if (arithmNode is OpArithmNode) {
                var res = prefix + (if (isTail) "└── " else "├── ") + arithmNode.op + '\n'
                res += getTree(arithmNode.right, prefix + (if (isTail) "    " else "│   "), false)
                res += getTree(arithmNode.left, prefix + (if (isTail) "    " else "│   "), true)
                return res
            }
            return ""
        }
    }
}

class VArithmNode(val value: Int) : ArithmNode() {
    override fun toString(): String {
        return value.toString()
    }

    override fun calculate(): Int {
        return value
    }
}

class OpArithmNode(val op: Char, val left: ArithmNode, val right: ArithmNode) : ArithmNode() {
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