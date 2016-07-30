
import org.antlr.v4.runtime.Lexer
import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.TokenImpl
import org.fife.ui.rsyntaxtextarea.TokenMakerBase
import java.util.*
import javax.swing.text.Segment

interface AntlrLexerFactory {
    fun create(code: String) : Lexer
}

class AntlrTokenMaker(val antlrLexerFactory : AntlrLexerFactory) : TokenMakerBase() {

    fun toList(text: Segment, startOffset: Int, antlrTokens:List<org.antlr.v4.runtime.Token>) : Token?{
        if (antlrTokens.isEmpty()) {
            return null
        } else {
            val at = antlrTokens[0]
            val t = TokenImpl(text, text.offset + at.startIndex, text.offset + at.startIndex + at.text.length - 1, startOffset + at.startIndex, at.type, 0)
            t.nextToken = toList(text, startOffset, antlrTokens.subList(1, antlrTokens.size))
            return t
        }
    }

    override fun getTokenList(text: Segment?, initialTokenType: Int, startOffset: Int): Token {
        if (text == null) {
            throw IllegalArgumentException()
        }
        val lexer = antlrLexerFactory.create(text.toString())
        val tokens = LinkedList<org.antlr.v4.runtime.Token>()
        while (!lexer._hitEOF) {
            tokens.add(lexer.nextToken())
        }
        return toList(text, startOffset, tokens) as Token
    }

}
