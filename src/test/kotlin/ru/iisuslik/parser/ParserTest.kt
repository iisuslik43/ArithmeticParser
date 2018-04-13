package ru.iisuslik.parser

import org.junit.Assert.*
import org.junit.Test

class ParserTest {
    @Test
    fun parseNum() {
        assertEquals("12", parse("0012").second.toString())
        assertEquals("", parse("0012").first)
    }

    @Test
    fun parsePlus() {
        val (rest, node) = parse("2+2")
        assertEquals("", rest)
        assertEquals("(2+2)", node.toString())
        printAllAboutNode(node)
    }

    @Test
    fun manyPlusMinus() {
        val (rest, node) = parse("2+2-5-6+7")
        assertEquals("", rest)
        assertEquals("((((2+2)-5)-6)+7)", node.toString())
        assertEquals(0, node.calculate())
    }

    @Test
    fun branchesInTheMiddle() {
        val (rest, node) = parse("1+(2-3)-4")
        assertEquals("", rest)
        assertEquals(-4, node.calculate())
    }

    @Test
    fun allOperations() {
        val (rest, node) = parse("1+2*3-4/2+2*2^2")
        assertEquals("", rest)
        assertEquals(13, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun multiply() {
        val (rest, node) = parse("1*2+3")
        assertEquals("", rest)
        assertEquals(5, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun divide() {
        val (rest, node) = parse("48/2/3+1")
        assertEquals("", rest)
        assertEquals(9, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun powerWorks() {
        val (rest, node) = parse("3*2^2^3+1")
        assertEquals("", rest)
        assertEquals(769, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun harderBrackets() {
        val (rest, node) = parse("2^(3 + 7 * (3 - 2 - 1)) + 1")
        assertEquals("", rest)
        assertEquals(9, node.calculate())
        printAllAboutNode(node)
    }

    @Test(expected = ParserException::class)
    fun emptyString() {
        val (rest, node) = parse("")
        assertEquals("", rest)
        printAllAboutNode(node)
    }

    @Test(expected = ParserException::class)
    fun strangeBrackets() {
        val (rest, node) = parse("2+()-2")
        assertEquals("", rest)
        printAllAboutNode(node)
    }

    @Test(expected = ParserException::class)
    fun wrongSymbolExceptionHappened() {
        val (rest, node) = parse("2&2-2")
        assertEquals("", rest)
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
        assertEquals("", rest)
        assertEquals(354630, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun bracketTest1() {
        val (rest, node) = parse("(2 + 2)*2")
        assertEquals("", rest)
        assertEquals(8, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun bracketTest2() {
        val (rest, node) = parse("(2+2)*(2+3)")
        assertEquals("", rest)
        assertEquals(20, node.calculate())
        printAllAboutNode(node)
    }

    @Test
    fun uselessBrackets() {
        val (rest, node) = parse("(2+2)")
        assertEquals("", rest)
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
        assertEquals("", rest)
        assertEquals(5, node.calculate())
        assertEquals("(2+3)", node.toString())
        printAllAboutNode(node)
    }
}