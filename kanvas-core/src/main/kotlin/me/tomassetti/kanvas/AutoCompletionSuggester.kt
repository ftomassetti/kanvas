package me.tomassetti.kanvas

import me.tomassetti.antlr4c3.ParserStack
import me.tomassetti.antlr4c3.api.completionsWithContextIgnoringSemanticPredicates
import me.tomassetti.kanvas.Debugging.*
import me.tomassetti.kolasu.model.Node
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.atn.*
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.*

interface TokenType {
    val type : Int
}

interface EditorContext {
    fun preceedingTokens() : List<Token>
    fun cachedAst() : Node?
    fun incompleteNode() : Node?
}

data class AutoCompletionContext(val preecedingTokens: List<Token>,
                                 val proposals: Set<Pair<TokenType, ParserStack>>,
                                 val cachedAst: Node?,
                                 val incompleteNode: Node?) {
    fun isIncomplete(node: Node?) : Boolean = if (incompleteNode == null || node == null) {
        false
    } else {
        incompleteNode == node || isIncomplete(incompleteNode?.parent)
    }
}

/**
 * The goal of this is to find the type of tokens that can be used in a given context
 */
interface AutoCompletionSuggester {
    fun autoCompletionContext(editorContext: EditorContext) : AutoCompletionContext
}

data class TokenTypeImpl(override val type: Int) : TokenType

class EditorContextImpl(val code: String, val antlrLexerFactory: AntlrLexerFactory,
                        val textPanel: TextPanel) : EditorContext {

    override fun incompleteNode(): Node? {
        return textPanel.incompleteNode
    }

    override fun cachedAst(): Node? {
        return textPanel.cachedRoot
    }

    override fun preceedingTokens(): List<Token> {
        val lexer = antlrLexerFactory.create(code)
        return lexer.toList()
    }

}

typealias CompletionOption = Pair<TokenType, ParserStack>
typealias CompletionOptions = Set<CompletionOption>

class AutoCompletionContextProvider(val ruleNames: Array<String>,
                                    val vocabulary: Vocabulary, val atn: ATN,
                                    val languageName: String = "MyLanguage_${ruleNames[0]}",
                                    val debugging: Debugging = NONE) : AutoCompletionSuggester {

    override fun autoCompletionContext(editorContext: EditorContext): AutoCompletionContext {
        val preceedingTokens = editorContext.preceedingTokens()
        val completionOptionsRaw = completionsWithContextIgnoringSemanticPredicates(
                preceedingTokens.map { it.type }, atn, vocabulary, ruleNames, languageName)

        val completionOptions = completionOptionsRaw.tokens.keys.map { tokenKind ->
            val parserStack = completionOptionsRaw.tokensContext[tokenKind]!!
            Pair<TokenType, ParserStack>(me.tomassetti.kanvas.TokenTypeImpl(tokenKind), parserStack)
        }.toSet()
        return AutoCompletionContext(preceedingTokens, completionOptions, editorContext.cachedAst(), editorContext.incompleteNode())
    }

}

fun String.toInputStream() = ByteArrayInputStream(this.toByteArray(StandardCharsets.UTF_8))

val CARET_TOKEN_TYPE = -10

enum class Debugging {
    NONE,
    AT_CARET,
    ALL
}

fun Lexer.toList() : List<Token> {
    val res = LinkedList<Token>()
    do {
        var next = this.nextToken()
        if (next.channel == 0) {
            if (next.type < 0) {
                next = CommonToken(CARET_TOKEN_TYPE)
            }
            res.add(next)
        }
    } while (next.type >= 0)
    return res
}
