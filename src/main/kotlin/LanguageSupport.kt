package me.tomassetti.kanvas

import me.tomassetti.lexers.None
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.Lexer
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import java.awt.Color
import java.io.File
import java.util.*

interface LanguageSupport {
    val syntaxScheme : SyntaxScheme
    val antlrLexerFactory: AntlrLexerFactory
}

object noneLanguageSupport : LanguageSupport {
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

}

object languageSupportRegistry {
    private val extensionsMap = HashMap<String, LanguageSupport>()

    fun register(extension : String, languageSupport: LanguageSupport) {
        extensionsMap[extension] = languageSupport
    }
    fun languageSupportForExtension(extension : String) : LanguageSupport = extensionsMap.getOrDefault(extension, noneLanguageSupport)
    fun languageSupportForFile(file : File) : LanguageSupport = languageSupportForExtension(file.extension)
}