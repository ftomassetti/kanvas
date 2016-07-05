import me.tomassetti.python.Python3Lexer
import org.antlr.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream
import org.fife.ui.rsyntaxtextarea.*
import java.util.*
import javax.swing.text.Segment

class AntlrTokenMaker() : TokenMakerBase() {

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
        //resetTokenList()
        if (text == null) {
            throw IllegalArgumentException()
        }
        println("TEXT IS '${text.toString()}' startOffset $startOffset initialTokenType $initialTokenType TEXT OFFSET ${text.offset}")
        val lexer = Python3Lexer(org.antlr.v4.runtime.ANTLRInputStream(text.toString()))
        val tokens = LinkedList<org.antlr.v4.runtime.Token>()
        while (!lexer._hitEOF) {
            tokens.add(lexer.nextToken())
        }
        println("TOKENS "+tokens)
        return toList(text, startOffset, tokens) as Token
    }

    /*override fun getTokenList(text: Segment?, initialTokenType: Int, startOffset: Int): Token {
        if (text == null) {
            throw IllegalArgumentException()
        }
        val lexer = Python3Lexer(org.antlr.v4.runtime.ANTLRInputStream(text.toString()))
        val antlrToken = lexer.nextToken()
        return addToken()
    }

    override fun getWordsToHighlight(): TokenMap {
        val tm = TokenMap()
        return tm
    }*/

}
