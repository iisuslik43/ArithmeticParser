package ru.iisuslik.parser

open class Token

class OpToken(val op: Char) : Token() {
    override fun toString(): String {
        return op.toString()
    }
}
class NumToken(var value: Int) : Token() {
    override fun toString(): String {
        return value.toString()
    }
}