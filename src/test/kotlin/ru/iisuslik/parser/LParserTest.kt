package ru.iisuslik.parser

import org.junit.Assert.*
import org.junit.Test

class LParserTest {
    @Test
    fun keyWordMatch() {
        assertTrue(KToken(0, 0, "else").isOk)
        assertEquals(KeyWord.ELSE, KToken(0, 0, "else").keyWord)
        assertFalse(KToken(0, 0, "elsse").isOk)
        println(KToken(0, 0, "else"))
    }

    @Test
    fun identMatch() {
        assertTrue(IToken(0, 0, "_asd").isOk)
        assertTrue(IToken(0, 0, "a_a23sd2_").isOk)
        assertTrue(IToken(0, 0, "x").isOk)
        assertFalse(IToken(0, 0, "2sd_asd").isOk)
        println(IToken(0, 0, "_asd"))
    }

    @Test
    fun numMatch() {
        assertEquals(0.1, NToken(0, 0, "0.1").num, 0.001)
        assertEquals(1.0, NToken(0, 0, "1").num, 0.001)
        assertFalse(NToken(0,0, "0.00.1").isOk)
        println(NToken(0, 0, "0.1"))
    }

    @Test
    fun boolMatch() {
        assertTrue(BToken(0, 0, "true").isOk)
        assertTrue(BToken(0, 0, "false").isOk)
        assertFalse(BToken(0, 0, "False").isOk)
        assertFalse(BToken(0, 0, "tasdasfas").isOk)
        println(BToken(0, 0, "true"))
    }

    @Test
    fun operationMatch() {
        assertTrue(OToken(0, 0, "==").isOk)
        assertTrue(OToken(0, 0, "+").isOk)
        assertTrue(OToken(0, 0, "&&").isOk)
        println(OToken(0, 0, "&&"))
    }

    @Test
    fun splitMatch() {
        assertTrue(SToken(0, 0, "(").isOk)
        assertTrue(SToken(0, 0, ")").isOk)
        assertTrue(SToken(0, 0, ";").isOk)
        assertTrue(SToken(0, 0, ",").isOk)
        println(SToken(0, 0, ","))
    }

    @Test
    fun simple() {
        println(LParser("return 2;").stringRest())
        assertEquals("Ident(\"return\", 0, 0, 5); Num(2.0, 0, 7, 7); SplitSymbol(';', 0, 8, 8); ",
                LParser("return 2;").stringRest())
    }

    @Test
    fun hard() {
        println(LParser("read x; if y + 1 == x then write y else write x").stringRest())
    }

    @Test
    fun twoLines() {
        val s = """
            |x
            |if
            """.trimMargin()
        println(LParser(s).stringRest())
        assertEquals("Ident(\"x\", 0, 0, 0); KeyWord_IF(1, 0, 1); ", LParser(s).stringRest())
    }
    @Test
    fun twoWhitespaces() {
        println(LParser("if  x").stringRest())
        assertEquals("KeyWord_IF(0, 0, 1); Ident(\"x\", 0, 4, 4); ", LParser("if  x").stringRest())
    }
    @Test
    fun tab() {
        println(LParser("if x").stringRest())
        assertEquals("KeyWord_IF(0, 0, 1); Ident(\"x\", 0, 3, 3); ", LParser("if x").stringRest())
    }

    @Test(expected = ParserException::class)
    fun wrongToken() {
        println(LParser("if =! x"))
    }

    @Test
    fun noWhiteSpaces() {
        println(LParser("x==1").stringRest())
        assertEquals("Ident(\"x\", 0, 0, 0); Operation(\"==\", 0, 1, 2); Num(1.0, 0, 3, 3); ", LParser("x==1").stringRest())
    }

    @Test
    fun brackets() {
        println(LParser("if (x)").stringRest())
        assertEquals("KeyWord_IF(0, 0, 1); SplitSymbol('(', 0, 3, 3); Ident(\"x\", 0, 4, 4); SplitSymbol(')', 0, 5, 5); ",
                LParser("if (x)").stringRest())
    }

    @Test
    fun fromFile() {
        println(LParser(getStringFromFile("./src/test/resources/ltest")).stringRest())
    }

    @Test
    fun minus() {
        println(LParser("2 - 2").stringRest())
    }

    @Test
    fun eq1() {
        println(LParser("2 = 2").stringRest())
    }

    @Test
    fun eq2() {
        println(LParser("2 == 2").stringRest())
    }
}