package ru.iisuslik.parser

import org.junit.Assert.*
import org.junit.Test

class KekParserTest {
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
        assertFalse(NToken(0, 0, "0.00.1").isOk)
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
        println(KekParser("return 2;").stringRest())
        assertEquals("RETURN(0, 0, 5); Num(2.0, 0, 7, 7); SplitSymbol(';', 0, 8, 8); ",
                KekParser("return 2;").stringRest())
    }

    @Test
    fun hard() {
        println(KekParser("read x; if y + 1 == x then write y else write x").stringRest())
    }

    @Test
    fun twoLines() {
        val s = """
            |x
            |if
            """.trimMargin()
        println(KekParser(s).stringRest())
        assertEquals("Ident(\"x\", 0, 0, 0); IF(1, 0, 1); ", KekParser(s).stringRest())
    }

    @Test
    fun twoWhitespaces() {
        println(KekParser("if  x").stringRest())
        assertEquals("IF(0, 0, 1); Ident(\"x\", 0, 4, 4); ", KekParser("if  x").stringRest())
    }

    @Test
    fun tab() {
        println(KekParser("if x").stringRest())
        assertEquals("IF(0, 0, 1); Ident(\"x\", 0, 3, 3); ", KekParser("if x").stringRest())
    }

    @Test(expected = ParserException::class)
    fun wrongToken() {
        println(KekParser("if =! x"))
    }

    @Test
    fun noWhiteSpaces() {
        println(KekParser("x==1").stringRest())
        assertEquals("Ident(\"x\", 0, 0, 0); Operation(\"==\", 0, 1, 2); Num(1.0, 0, 3, 3); ", KekParser("x==1").stringRest())
    }

    @Test
    fun brackets() {
        println(KekParser("if (x)").stringRest())
        assertEquals("IF(0, 0, 1); SplitSymbol('(', 0, 3, 3); Ident(\"x\", 0, 4, 4); SplitSymbol(')', 0, 5, 5); ",
                KekParser("if (x)").stringRest())
    }

    @Test
    fun fromFile() {
        println(KekParser(getStringFromFile("./src/test/resources/ltest")).stringRest())
    }

    @Test
    fun minus() {
        println(KekParser("2 - 2").stringRest())
    }

    @Test
    fun eq1() {
        println(KekParser("2 = 2").stringRest())
    }

    @Test
    fun eq2() {
        println(KekParser("2 == 2").stringRest())
    }

    @Test
    fun comments() {
        println(KekParser("//s").stringRest())
    }

    @Test
    fun veryHard() {
        val s = """function = fun (x, y) {
    if (x == y) {
        c = x + y;
        write(c);
        return false;
    }
    else {
        return true;
    }
};

function2 = fun (f) {
    return f(2, 3);
};

x = 43;

while(x) {
    function2(function);
}

return 42; """
        println(KekParser(s).stringRest())
        println(KekParser(s).parseL())
        println(KekParser(s).parseL().getTree())
    }

    @Test
    fun number() {
        println(KekParser("-975.31e-2").stringRest())
    }

    @Test
    fun ifElse() {
        val s = """
    if (x == y) {
        c = x + y;
        write(c);
        return false;
    }
    else {
        return true;
    }"""
        println(KekParser(s).parseL().getTree())
    }


    @Test
    fun brackets2() {
        println(KekParser("return (2 + 2) * 2;").parseL().getTree())
    }

    @Test
    fun bigExpr() {
        println(KekParser("x=2+func(3+a(x),2);").parseL().getTree())
    }

    @Test
    fun whileTest() {
        val s = """
    while (f(x * y, 43)) {
        c = 73e+23;
        write(c);
    }
    """
        println(KekParser(s).parseL().getTree())
    }

    @Test
    fun writeRead() {
        val s = """
        write(func(2+3) + 6);
        x = read;
    """
        println(KekParser(s).parseL().getTree())
    }

    @Test
    fun hardComments() {
        val s = """
    // asd asd232 asdadg
    return 0;
    x = x + 2;//asd
    """
        println(KekParser(s).parseL().getTree())
    }

}