package ru.iisuslik.parser

class ParserException(s: String, val pos: Int, val stringNumber : Int): Exception(s) {
    override val message: String?
        get() = "${super.message} in position $pos in line $stringNumber"
}