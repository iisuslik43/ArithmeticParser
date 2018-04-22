package ru.iisuslik.parser

interface ArithmToken {
    fun length():Int
}

class OpToken(val op: Char) : ArithmToken {
    override fun toString(): String {
        return op.toString()
    }

    override fun length(): Int {
        return 1
    }
}
class NumToken(var value: Int) : ArithmToken {
    override fun toString(): String {
        return value.toString()
    }

    override fun length(): Int {
        return value.toString().length
    }
}
