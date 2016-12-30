package me.tomassetti.kanvas

import me.tomassetti.antlr.None
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.atn.ATN
import org.fife.ui.autocomplete.BasicCompletion
import org.fife.ui.autocomplete.Completion
import org.fife.ui.autocomplete.CompletionProvider
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import java.awt.Color
import java.io.File
import java.util.*

data class ParserData(val ruleNames: Array<String>, val vocabulary: Vocabulary, val atn: ATN)

interface LanguageSupport {
    val syntaxScheme : SyntaxScheme
    val antlrLexerFactory: AntlrLexerFactory
    val parserData: ParserData?
    val propositionProvider: PropositionProvider
}

interface PropositionProvider {
    fun fromTokenType(completionProvider: CompletionProvider, preecedingTokens: List<Token>, tokenType: Int) : List<Completion>
}

class DefaultLanguageSupport(val languageSupport: LanguageSupport) : PropositionProvider {
    override fun fromTokenType(completionProvider: CompletionProvider, preecedingTokens: List<Token>, tokenType: Int): List<Completion> {
        val res = LinkedList<Completion>()
        var proposition : String? = languageSupport.parserData!!.vocabulary.getLiteralName(tokenType)
        if (proposition != null) {
            if (proposition.startsWith("'") && proposition.endsWith("'")) {
                proposition = proposition.substring(1, proposition.length - 1)
            }
            res.add(BasicCompletion(completionProvider, proposition))
        }
        return res
    }
}

abstract class BaseLanguageSupport : LanguageSupport {

    override val propositionProvider: PropositionProvider
        get() = DefaultLanguageSupport(this)
}

object noneLanguageSupport : BaseLanguageSupport() {

    override val syntaxScheme: SyntaxScheme
        get() = object : SyntaxScheme(false) {
            override fun getStyle(index: Int): Style {
                val style = Style()
                style.foreground = Color.WHITE
                return style
            }
        }
    override val antlrLexerFactory: AntlrLexerFactory
        get() = object : AntlrLexerFactory {
            override fun create(code: String): Lexer = None(ANTLRInputStream(code))
        }

    override val parserData: ParserData?
        get() = null

    override fun toString(): String {
        return "default language support"
    }
}

object languageSupportRegistry {
    private val extensionsMap = HashMap<String, LanguageSupport>()

    fun register(extension : String, languageSupport: LanguageSupport) {
        extensionsMap[extension] = languageSupport
    }
    fun languageSupportForExtension(extension : String) : LanguageSupport = extensionsMap.getOrDefault(extension, noneLanguageSupport)
    fun languageSupportForFile(file : File) : LanguageSupport = languageSupportForExtension(file.extension)
}