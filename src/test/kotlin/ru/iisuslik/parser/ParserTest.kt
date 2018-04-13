package ru.iisuslik.parser

import org.junit.Assert.*
import org.junit.Test

class ParserTest {
    @Test
    fun parseNum() {
        assertEquals("12", parse("0012").second.toString())
        assertEquals(emptyList<Token>(), parse("0012").first)
    }

    @Test
    fun parsePlus() {
        println(getTokens("2 +  2"))
        val (rest, node) = parse("2+2")
        assertEquals(emptyList<Token>(), rest)
        assertEquals("(2+2)", node.toString())
        printAllAboutNode(node)
    }

    @Test
    fun manyPlusMinus() {
        val (rest, node) = parse("2+2-5-6+7")
        assertEquals(emptyList<Token>(), rest)
        assertEquals("((((2+2)-5)-6)+7)", node.toString())
        assertEquals(0, node.calculate())
    }

    @Test
    fun branchesInTheMiddle() {
        val (rest, node) = parse("1+(2-3)-4")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(-4, node.calculate())
    }

    @Test
    fun allOperations() {
        val (rest, node) = parse("1+2*3-4/2+2*2^2")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(13, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun multiply() {
        val (rest, node) = parse("1*2+3")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(5, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun divide() {
        val (rest, node) = parse("48/2/3+1")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(9, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun powerWorks() {
        val (rest, node) = parse("3*2^2^3+1")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(769, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun harderBrackets() {
        val (rest, node) = parse("2^(3 + 7 * (3 - 2 - 1)) + 1")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(9, node.calculate())
        printAllAboutNode(node)
    }

    @Test(expected = ParserException::class)
    fun emptyString() {
        val (rest, node) = parse("")
        assertEquals(emptyList<Token>(), rest)
        printAllAboutNode(node)
    }

    @Test(expected = ParserException::class)
    fun strangeBrackets() {
        val (rest, node) = parse("2+()-2")
        assertEquals(emptyList<Token>(), rest)
        printAllAboutNode(node)
    }

    @Test(expected = ParserException::class)
    fun wrongSymbolExceptionHappened() {
        val (rest, node) = parse("2&2-2")
        assertEquals(emptyList<Token>(), rest)
        printAllAboutNode(node)
    }

    @Test(expected = ParserException::class)
    fun badWhitespaces() {
        val (rest, node) = parse("(2 + 2) + 2 2")
        assertEquals(emptyList<Token>(), rest)
        printAllAboutNode(node)
    }

    @Test
    fun wrongSymbolPosition() {
        var happend = false
        try {
            parse("2&2-2")
        } catch (e: ParserException) {
            happend = true
            assertEquals(4, e.restLength)
        }
        assertTrue(happend)
    }

    @Test
    fun bigNumbers() {
        val (rest, node) = parse("434343 + 43411 - 123124")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(354630, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun bracketTest1() {
        val (rest, node) = parse("(2 + 2)*2")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(8, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun bracketTest2() {
        val (rest, node) = parse("(2+2)*(2+3)")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(20, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun uselessBrackets() {
        val (rest, node) = parse("(2+2)")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(4, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun fromTestFile() {
        parseFromFile("./src/test/resources/testFile")
    }

    @Test
    fun manyBrackets() {
        val (rest, node) = parse("(((((2+3)))))")
        assertEquals(emptyList<Token>(), rest)
        assertEquals(5, node.calculate())
        assertEquals("(2+3)", node.toString())
        printAllAboutNode(node)
    }
}