import me.tomassetti.python.Python3Lexer
import org.antlr.v4.runtime.Token
import java.util.*
import javax.swing.SwingUtilities

/**
 * Created by federico on 03/07/16.
 */

fun main(args: Array<String>) {
    val l = Python3Lexer(org.antlr.v4.runtime.ANTLRInputStream("def foo():"))
    val tokens = LinkedList<Token>()
    while (!l._hitEOF) {
        tokens.add(l.nextToken())
    }
}