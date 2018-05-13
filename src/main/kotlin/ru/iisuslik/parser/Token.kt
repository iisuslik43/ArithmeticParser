package ru.iisuslik.parser

enum class KeyWord() {
    IF,
    ELSE,
    WHILE,
    READ,
    WRITE,
    RETURN,
    FUN
}

val splitTokens = setOf("(", ")", ";", ",", "{", "}")
val operations = setOf("+", "-", "*", "/", "%", "==", "!=", ">", ">=", "<", "<=", "&&", "||", "=")


open class Token(sNumber: Int, position: Int, str: String) {
    val s = str
    val pos = position
    val sNumb = sNumber
    var isOk = true
    fun length(): Int {
        return s.length
    }

    override fun toString(): String = "$sNumb, $pos, ${pos + length() - 1}"
}

open class LToken(sNumber: Int, pos: Int, str: String) : Token(sNumber, pos, str)

class NToken(sNumber: Int, pos: Int, str: String) : LToken(sNumber, pos, str) {
    val num = fromDouble(s)
    private fun fromDouble(s: String): Double {
        val maybeDouble = s.toDoubleOrNull()
        if (maybeDouble == null) {
            isOk = false
            return 0.0
        }
        return maybeDouble
    }

    override fun toString(): String = "Num($num, ${super.toString()})"

}


class BToken(sNumber: Int, pos: Int, str: String) : LToken(sNumber, pos, str) {
    val bool = checkBool(s)

    private fun checkBool(s: String): Boolean {
        if (s != "true" && s != "false") {
            isOk = false
            return false
        }
        return s.toBoolean()
    }

    override fun toString(): String = "Boolean($bool, ${super.toString()})"

}

class KToken(sNumber: Int, position: Int, str: String) : Token(sNumber, position, str) {
    val keyWord = parseKeyWord(str)
    private fun parseKeyWord(str: String): KeyWord {
        try {
            return KeyWord.valueOf(str.toUpperCase())
        } catch (e: IllegalArgumentException) {
            isOk = false
            return KeyWord.ELSE
        }
    }

    override fun toString(): String = "$keyWord(${super.toString()})"
}


class SToken(sNumber: Int, position: Int, str: String) : Token(sNumber, position, str) {
    val split = checkSplit(s)
    private fun checkSplit(s: String): String {
        if (s in splitTokens) {
            return s
        }
        isOk = false
        return ""
    }

    override fun toString(): String = "SplitSymbol(\'$split\', ${super.toString()})"
}


class IToken(sNumber: Int, position: Int, str: String) : Token(sNumber, position, str) {
    val ident = checkIdent(s)
    private fun checkIdent(s: String): String {
        val regex = "[a-z_]\\w*".toRegex()
        if (regex.matchEntire(s) != null) {
            return s
        }
        isOk = false
        return ""
    }
    override fun toString(): String = "Ident(\"$ident\", ${super.toString()})"
}

class OToken(sNumber: Int, position: Int, str: String) : Token(sNumber, position, str) {
    val op = checkOperation(s)
    private fun checkOperation(s: String): String {
        if (s in operations) {
            return s
        }
        isOk = false
        return ""
    }

    override fun toString(): String = "Operation(\"$op\", ${super.toString()})"
}