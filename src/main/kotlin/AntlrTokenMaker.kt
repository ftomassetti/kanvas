import me.tomassetti.python.Python3
import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.TokenImpl
import org.fife.ui.rsyntaxtextarea.TokenMakerBase
import java.util.*
import javax.swing.text.Segment

class AntlrTokenMaker() : TokenMakerBase() {

    fun toList(text: Segment, startOffset: Int, antlrTokens:List<org.antlr.v4.runtime.Token>) : Token?{
        if (antlrTokens.isEmpty()) {
            return null
        } else {
            val at = antlrTokens[0]
            val t = TokenImpl(text, text.offset + at.startIndex, text.offset + at.startIndex + at.text.length - 1, startOffset + at.startIndex, at.type, 0)
            /*if (at.type != -1) {
                println("TYPE " + at.type + " " + Python3.ruleNames[at.type - 3])
            }*/
            t.nextToken = toList(text, startOffset, antlrTokens.subList(1, antlrTokens.size))
            return t
        }
    }

    override fun getTokenList(text: Segment?, initialTokenType: Int, startOffset: Int): Token {
        //resetTokenList()
        if (text == null) {
            throw IllegalArgumentException()
        }
        val lexer = Python3(org.antlr.v4.runtime.ANTLRInputStream(text.toString()))
        val tokens = LinkedList<org.antlr.v4.runtime.Token>()
        while (!lexer._hitEOF) {
            tokens.add(lexer.nextToken())
        }
        return toList(text, startOffset, tokens) as Token
    }

}
