
import me.tomassetti.antlr.SandyLexer
import me.tomassetti.antlr.SandyParser
import me.tomassetti.kanvas.AutoCompletionContextProvider
import me.tomassetti.kanvas.EditorContextImpl
import me.tomassetti.kanvas.TokenTypeImpl
import me.tomassetti.kanvas.sandyLanguageSupport
import kotlin.test.assertEquals
import org.junit.Test as test

class CompletionTest {

    @test fun emptyFile() {
        val code = ""
        assertEquals(setOf(TokenTypeImpl(SandyLexer.VAR), TokenTypeImpl(SandyLexer.ID)), AutoCompletionContextProvider(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .autoCompletionContext(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)).proposals)
    }

    @test fun afterVar() {
        val code = "var"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.ID)), AutoCompletionContextProvider(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .autoCompletionContext(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)).proposals)
    }

    @test fun afterEquals() {
        val code = "var a ="
        assertEquals(setOf(TokenTypeImpl(SandyLexer.INTLIT), TokenTypeImpl(SandyLexer.DECLIT), TokenTypeImpl(SandyLexer.MINUS)
                , TokenTypeImpl(SandyLexer.LPAREN), TokenTypeImpl(SandyLexer.ID)), AutoCompletionContextProvider(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .autoCompletionContext(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)).proposals)
    }

    @test fun afterLiteral() {
        val code = "var a = 1"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.NEWLINE), TokenTypeImpl(SandyLexer.EOF), TokenTypeImpl(SandyLexer.PLUS), TokenTypeImpl(SandyLexer.MINUS), TokenTypeImpl(SandyLexer.DIVISION), TokenTypeImpl(SandyLexer.ASTERISK)),
                AutoCompletionContextProvider(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .autoCompletionContext(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)).proposals)
    }

    @test fun incompleteAddition() {
        val code = "var a = 1 +"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.LPAREN), TokenTypeImpl(SandyLexer.ID), TokenTypeImpl(SandyLexer.MINUS), TokenTypeImpl(SandyLexer.INTLIT), TokenTypeImpl(SandyLexer.DECLIT)), AutoCompletionContextProvider(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .autoCompletionContext(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)).proposals)
    }

}
