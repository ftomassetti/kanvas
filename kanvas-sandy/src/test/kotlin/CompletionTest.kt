
import me.tomassetti.antlr.SandyLexer
import me.tomassetti.antlr.SandyParser
import me.tomassetti.kanvas.*
import me.tomassetti.kolasu.model.Node
import org.antlr.v4.runtime.Token
import kotlin.test.assertEquals
import org.junit.Test as test

class MyEditorContextImpl(val code: String, val antlrLexerFactory: AntlrLexerFactory, val ast: Node? = null) : EditorContext {
    override fun cachedAst(): Node? {
        return ast
    }

    override fun preceedingTokens(): List<Token> {
        val lexer = antlrLexerFactory.create(code)
        return lexer.toList()
    }
}

class CompletionTest {

    fun tokenSuggested(code: String) = AutoCompletionContextProvider(SandyParser.ruleNames, SandyParser.VOCABULARY, SandyParser._ATN, debugging = false)
            .autoCompletionContext(MyEditorContextImpl(code, sandyLanguageSupport.antlrLexerFactory)).proposals.map { it.first }.toSet()

    @test fun emptyFile() {
        val code = ""
        assertEquals(setOf(TokenTypeImpl(SandyLexer.VAR), TokenTypeImpl(SandyLexer.ID)), tokenSuggested(code))
    }

    @test fun afterVar() {
        val code = "var"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.ID)), tokenSuggested(code))
    }

    @test fun afterEquals() {
        val code = "var a ="
        assertEquals(setOf(TokenTypeImpl(SandyLexer.INTLIT), TokenTypeImpl(SandyLexer.DECLIT), TokenTypeImpl(SandyLexer.MINUS)
                , TokenTypeImpl(SandyLexer.LPAREN), TokenTypeImpl(SandyLexer.ID)), tokenSuggested(code))
    }

    @test fun afterLiteral() {
        val code = "var a = 1"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.NEWLINE), TokenTypeImpl(SandyLexer.EOF), TokenTypeImpl(SandyLexer.PLUS),
                TokenTypeImpl(SandyLexer.MINUS), TokenTypeImpl(SandyLexer.DIVISION), TokenTypeImpl(SandyLexer.ASTERISK)),
                tokenSuggested(code))
    }

    @test fun incompleteAddition() {
        val code = "var a = 1 +"
        assertEquals(setOf(TokenTypeImpl(SandyLexer.LPAREN), TokenTypeImpl(SandyLexer.ID), TokenTypeImpl(SandyLexer.MINUS),
                TokenTypeImpl(SandyLexer.INTLIT), TokenTypeImpl(SandyLexer.DECLIT)), tokenSuggested(code))
    }

}
