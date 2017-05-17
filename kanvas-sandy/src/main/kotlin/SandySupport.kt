package me.tomassetti.kanvas

import me.tomassetti.antlr.SandyLexer
import me.tomassetti.antlr.SandyParser
import me.tomassetti.kolasu.model.Node
import me.tomassetti.kolasu.parsing.Parser
import org.antlr.v4.runtime.Lexer
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import java.awt.Color

object sandySyntaxScheme : SyntaxScheme(true) {
    override fun getStyle(index: Int): Style {
        val style = Style()
        val color = when (index) {
            SandyLexer.VAR -> Color.GREEN
            SandyLexer.ASSIGN -> Color.GREEN
            SandyLexer.ASTERISK, SandyLexer.DIVISION, SandyLexer.PLUS, SandyLexer.MINUS -> Color.WHITE
            SandyLexer.INTLIT, SandyLexer.DECLIT -> Color.BLUE
            SandyLexer.UNMATCHED -> Color.RED
            SandyLexer.ID -> Color.MAGENTA
            SandyLexer.LPAREN, SandyLexer.RPAREN -> Color.WHITE
            else -> null
        }
        if (color != null) {
            style.foreground = color
        }
        return style
    }
}

object sandyLanguageSupport : BaseLanguageSupport<Node>() {
    override val parser: Parser<Node>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val syntaxScheme: SyntaxScheme
        get() = sandySyntaxScheme
    override val antlrLexerFactory: AntlrLexerFactory
        get() = object : AntlrLexerFactory {
            override fun create(code: String): Lexer = SandyLexer(org.antlr.v4.runtime.ANTLRInputStream(code))
        }
    override val parserData: ParserData?
        get() = ParserData(SandyParser.ruleNames, SandyLexer.VOCABULARY, SandyParser._ATN)
}
