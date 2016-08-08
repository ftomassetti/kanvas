
import me.tomassetti.antlr.SandyLexer
import me.tomassetti.antlr.SandyParser
import me.tomassetti.kanvas.AntlrAutoCompletionSuggester
import me.tomassetti.kanvas.EditorContextImpl
import me.tomassetti.kanvas.TokenTypeImpl
import me.tomassetti.kanvas.sandyLanguageSupport
import kotlin.test.assertEquals
import org.junit.Test as test

class CompletionTest {

    @test fun emptyFile() {
        val code = ""
        assertEquals(setOf(TokenTypeImpl(SandyLexer.VAR), TokenTypeImpl(SandyLexer.ID)), AntlrAutoCompletionSuggester(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .suggestions(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)))
    }

    @test fun afterVar() {
        val code = "var"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.ID)), AntlrAutoCompletionSuggester(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .suggestions(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)))
    }

    @test fun afterEquals() {
        val code = "var a ="
        assertEquals(setOf(TokenTypeImpl(SandyLexer.INTLIT), TokenTypeImpl(SandyLexer.DECLIT), TokenTypeImpl(SandyLexer.MINUS)
                , TokenTypeImpl(SandyLexer.LPAREN), TokenTypeImpl(SandyLexer.ID)), AntlrAutoCompletionSuggester(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .suggestions(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)))
    }

    @test fun afterLiteral() {
        val code = "var a = 1"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.NEWLINE), TokenTypeImpl(SandyLexer.EOF), TokenTypeImpl(SandyLexer.PLUS), TokenTypeImpl(SandyLexer.MINUS), TokenTypeImpl(SandyLexer.DIVISION), TokenTypeImpl(SandyLexer.ASTERISK)),
                AntlrAutoCompletionSuggester(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .suggestions(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)))
    }

    @test fun incompleteAddition() {
        val code = "var a = 1 +"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.LPAREN), TokenTypeImpl(SandyLexer.ID), TokenTypeImpl(SandyLexer.MINUS), TokenTypeImpl(SandyLexer.INTLIT), TokenTypeImpl(SandyLexer.DECLIT)), AntlrAutoCompletionSuggester(SandyParser.ruleNames,SandyLexer.VOCABULARY,SandyParser._ATN)
                .suggestions(EditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)))
    }

}
