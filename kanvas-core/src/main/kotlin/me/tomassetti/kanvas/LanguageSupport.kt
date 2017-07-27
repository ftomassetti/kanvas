package me.tomassetti.kanvas

import me.tomassetti.antlr.None
import me.tomassetti.kolasu.model.Node
import me.tomassetti.kolasu.model.Point
import me.tomassetti.kolasu.parsing.Parser
import me.tomassetti.kolasu.parsing.ParsingResult
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
import java.io.InputStream
import java.util.*

data class ParserData(val ruleNames: Array<String>, val vocabulary: Vocabulary, val atn: ATN)

enum class IssueType {
    WARNING,
    ERROR
}

data class Issue(val type : IssueType, val message: String,
                 val line: Int, val offset: Int, val length: Int)

interface Validator<RootNode: Node> {
    fun validate(parsingResult: ParsingResult<RootNode>, context: Context) : List<Issue>
}

interface Context {
    fun register(name: String, data: Object?)
    fun get(name: String) : Object?
}

open class SimpleContext : Context {
    val map = HashMap<String, Object?>()

    override fun register(name: String, data: Object?) {
        map[name] = data
    }

    override fun get(name: String): Object? {
        return map[name]
    }
}

interface ContextCreator {
    fun create() : Context
}

interface LanguageSupport<RootNode: Node> {
    val syntaxScheme : SyntaxScheme
    val antlrLexerFactory: AntlrLexerFactory
    val parserData: ParserData?
    val propositionProvider: PropositionProvider
    val validator: Validator<RootNode>
    val contextCreator: ContextCreator
    val parser: Parser<RootNode>
}

/**
 * Information provided to contextualize autocompletion
 */
data class AutocompletionSurroundingInformation(val cachedAstRoot: Node?,
                                  val preecedingTokens: List<Token>,
                                  val rulesStack: List<Int>,
                                  val pointInCode: Point)

interface PropositionProvider {
    fun fromTokenType(autocompletionSurroundingInformation: AutocompletionSurroundingInformation,
                      tokenType: Int, context: Context) : List<String>
}

class DefaultLanguageSupport(val languageSupport: LanguageSupport<*>) : PropositionProvider {
    override fun fromTokenType(autocompletionSurroundingInformation: AutocompletionSurroundingInformation,
                               tokenType: Int, context: Context): List<String> {
        val res = LinkedList<String>()
        var proposition : String? = languageSupport.parserData!!.vocabulary.getLiteralName(tokenType)
        if (proposition != null) {
            if (proposition.startsWith("'") && proposition.endsWith("'")) {
                proposition = proposition.substring(1, proposition.length - 1)
            }
            res.add(proposition)
        }
        return res
    }
}

class EverythingOkValidator<RootNode:Node> : Validator<RootNode> {
    override fun validate(parsingResult: ParsingResult<RootNode>, context: Context): List<Issue> = emptyList()
}

abstract class BaseLanguageSupport<RootNode:Node> : LanguageSupport<RootNode> {

    override val propositionProvider: PropositionProvider
        get() = DefaultLanguageSupport(this)
    override val syntaxScheme: SyntaxScheme
        get() = DefaultSyntaxScheme()
    override val validator: Validator<RootNode>
        get() = EverythingOkValidator()
    override val contextCreator: ContextCreator
        get() = object : ContextCreator {
            override fun create(): Context = SimpleContext()
        }
}

class DefaultSyntaxScheme : SyntaxScheme(false) {
    override fun getStyle(index: Int): Style {
        val style = Style()
        style.foreground = Color.WHITE
        return style
    }
}

class DummyParser : Parser<Node> {
    override fun parse(inputStream: InputStream, withValidation: Boolean): ParsingResult<Node> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

object noneLanguageSupport : BaseLanguageSupport<Node>() {

    override val parser: Parser<Node>
        get() = DummyParser()

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
    private val extensionsMap = HashMap<String, LanguageSupport<*>>()

    fun register(extension : String, languageSupport: LanguageSupport<*>) {
        extensionsMap[extension] = languageSupport
    }
    fun languageSupportForExtension(extension : String) : LanguageSupport<*>
            = extensionsMap.getOrDefault(extension, noneLanguageSupport)
    fun languageSupportForFile(file : File) : LanguageSupport<*>
            = languageSupportForExtension(file.extension)
}