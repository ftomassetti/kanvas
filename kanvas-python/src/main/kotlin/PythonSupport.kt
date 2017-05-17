package me.tomassetti.kanvas

import me.tomassetti.antlr.Python3
import me.tomassetti.kolasu.model.Node
import me.tomassetti.kolasu.parsing.Parser
import org.antlr.v4.runtime.Lexer
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import java.awt.Color

object pythonSyntaxScheme : SyntaxScheme(true) {
    override fun getStyle(index: Int): Style {
        val style = Style()
        val color = when (index) {
            Python3.DEF -> Color.GREEN
            Python3.IMPORT -> Color.GREEN
            Python3.PASS -> Color.GREEN
            Python3.NAME -> Color.WHITE
            Python3.COLON -> Color.LIGHT_GRAY
            Python3.DOT -> Color.LIGHT_GRAY
            Python3.OPEN_BRACE -> Color.LIGHT_GRAY
            Python3.CLOSE_BRACE -> Color.LIGHT_GRAY
            Python3.OPEN_PAREN -> Color.LIGHT_GRAY
            Python3.CLOSE_PAREN -> Color.LIGHT_GRAY
            Python3.STRING_LITERAL -> Color.YELLOW
            Python3.LONG_STRING_LITERAL_START -> Color.YELLOW
            Python3.IN_LONG_STRING -> Color.YELLOW
            Python3.COMMENT -> Color.MAGENTA
            else -> null
        }
        if (color != null) {
            style.foreground = color
        }
        return style
    }
}

object pythonLanguageSupport : BaseLanguageSupport<Node>() {
    override val parser: Parser<Node>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val syntaxScheme: SyntaxScheme
        get() = pythonSyntaxScheme
    override val antlrLexerFactory: AntlrLexerFactory
        get() = object : AntlrLexerFactory {
            override fun create(code: String): Lexer = Python3(org.antlr.v4.runtime.ANTLRInputStream(code))
        }
    override val parserData: ParserData?
        get() = null
}
