package me.tomassetti.kanvas

import me.tomassetti.antlr.StaMacLexer
import me.tomassetti.antlr.StaMacParser
import org.antlr.v4.runtime.Lexer
import kotlin.test.assertEquals
import org.junit.Test as test

class AutoCompletionSuggesterTest {

    private val antlrLexerFactory = object : AntlrLexerFactory {
        override fun create(code: String): Lexer = StaMacLexer(org.antlr.v4.runtime.ANTLRInputStream(code))
    }

    private val ruleNames = StaMacParser.ruleNames
    private val vocabulary = StaMacParser.VOCABULARY
    private val atn = StaMacParser._ATN

    private fun process(code: String) : Set<Pair<TokenType, ParserStack>> {
        val lexer = antlrLexerFactory.create(code)
        val preceedingTokens = lexer.toList()
        val collector = Collector()
        process(ruleNames, vocabulary, atn.states[0],
                MyTokenStream(preceedingTokens), collector, ParserStack(ruleNames, vocabulary))
        return collector.collected()
    }

    @test fun emptyCode() {
        val collected = process("")
        assertEquals(1, collected.size)
        assertEquals(StaMacLexer.SM, collected.first().first.type)
        println(collected.first().second.describe())
    }

    @test fun afterSMToken() {
        val collected = process("statemachine")
        assertEquals(1, collected.size)
        assertEquals(StaMacLexer.ID, collected.first().first.type)
    }

    @test fun afterSmNameToken() {
        val collected = process("statemachine mySm")
        assertEquals(5, collected.size)
        val tokenTypes = collected.map { it.first.type }.toSet()
        assertEquals(true, tokenTypes.contains(StaMacLexer.INPUT))
        assertEquals(true, tokenTypes.contains(StaMacLexer.VAR))
        assertEquals(true, tokenTypes.contains(StaMacLexer.EVENT))
        assertEquals(true, tokenTypes.contains(StaMacLexer.START))
        assertEquals(true, tokenTypes.contains(StaMacLexer.STATE))
    }

    @test fun afterEventToken() {
        val collected = process("""statemachine mySm
                                   event""")
        assertEquals(1, collected.size)
        assertEquals(StaMacLexer.ID, collected.first().first.type)
        println(collected.first().second.describe())
    }

    @test fun afterInputToken() {
        val collected = process("""statemachine mySm
                                   input""")
        assertEquals(1, collected.size)
        assertEquals(StaMacLexer.ID, collected.first().first.type)
        println(collected.first().second.describe())
    }
}
