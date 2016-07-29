
import me.tomassetti.python.Python3
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.Toolkit
import javax.swing.*
import javax.swing.plaf.metal.MetalTabbedPaneUI
import javax.swing.plaf.synth.SynthScrollBarUI

private val BACKGROUND = Color(39, 40, 34)
private val BACKGROUND_SUBTLE_HIGHLIGHT = Color(49, 50, 44)
private val BACKGROUND_DARKER = Color(23, 24, 20)
private val BACKGROUND_LIGHTER = Color(109, 109, 109)

class TurinSyntaxScheme(useDefaults: Boolean) : SyntaxScheme(useDefaults) {
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
            else -> null
        }
        if (color != null) {
            style.foreground = color
        }
        return style
    }
}

private fun makeTextPanel(font: Font) : Component {
    val textArea = RSyntaxTextArea(20, 60)
    //textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVA

    /*(textArea.document as RSyntaxDocument).setTokenMakerFactory(object : TokenMakerFactory() {
        override fun getTokenMakerImpl(key: String?): TokenMaker {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun keySet(): MutableSet<String>? {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    })*/
    (textArea.document as RSyntaxDocument).setSyntaxStyle(AntlrTokenMaker())
    /*(textArea.document as RSyntaxDocument).setSyntaxStyle(object : AbstractTokenMaker() {
        override fun getWordsToHighlight(): TokenMap {
            val tm = TokenMap()
            tm.put("class", TokenTypes.RESERVED_WORD)
            return tm
            //throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getTokenList(text: Segment?, initialTokenType: Int, startOffset: Int): Token {
            val t = TokenImpl()
            t.text = CharArray(69)
            return t
        }

    })*/
    textArea.syntaxScheme = TurinSyntaxScheme(true)
    textArea.isCodeFoldingEnabled = true
    textArea.font = font
    textArea.background = BACKGROUND
    textArea.foreground = Color.WHITE
    textArea.currentLineHighlightColor = BACKGROUND_SUBTLE_HIGHLIGHT
    val sp = RTextScrollPane(textArea)
    sp.viewportBorder = BorderFactory.createEmptyBorder()
    sp.verticalScrollBar.ui = object : SynthScrollBarUI() {
        override fun configureScrollBarColors() {
            super.configureScrollBarColors()
        }
    }
    return sp
}

internal class NoInsetTabbedPaneUI : MetalTabbedPaneUI() {

    override fun installDefaults() {
        super.installDefaults()
        this.tabAreaBackground = Color.RED
        this.selectColor = BACKGROUND_LIGHTER
        this.selectHighlight = BACKGROUND_LIGHTER
        this.highlight = BACKGROUND_LIGHTER
    }

    fun overrideContentBorderInsetsOfUI() {
        if (this.contentBorderInsets != null) {
            this.contentBorderInsets.top = 0
            this.contentBorderInsets.left = 0
            this.contentBorderInsets.right = 0
            this.contentBorderInsets.bottom = 2
        }
    }
}

class MyTabbledPane : JTabbedPane() {

    init {
        this.tabPlacement = SwingConstants.TOP
        val ui = NoInsetTabbedPaneUI()
        this.setUI(ui)
        ui.overrideContentBorderInsetsOfUI()
    }

}

private fun addTab(tabbedPane: MyTabbledPane, title: String, font: Font) {
    val panel1 = makeTextPanel(font)
    tabbedPane.addTab(title, null, panel1, "Go to $title")
    tabbedPane.setForegroundAt(tabbedPane.tabCount - 1, Color.white)
    tabbedPane.setBackgroundAt(tabbedPane.tabCount - 1, BACKGROUND_DARKER)
}

val APP_TITLE = "Kanvas"

private fun createAndShowGUI() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val xToolkit = Toolkit.getDefaultToolkit()
    val awtAppClassNameField = xToolkit.javaClass.getDeclaredField("awtAppClassName")
    awtAppClassNameField.isAccessible = true
    awtAppClassNameField.set(xToolkit, APP_TITLE)

    /*UIManager.put("ScrollBar.thumb", ColorUIResource(Color.RED))
    UIManager.put("ScrollBar.thumbHighlight", ColorUIResource(Color.RED))
    UIManager.put("ScrollBar.thumbShadow", ColorUIResource(Color.RED))
    UIManager.put("ScrollBar.background", ColorUIResource(Color.RED))
    UIManager.put("ScrollBar.foreground", ColorUIResource(Color.RED))
    UIManager.put("Button.foreground", ColorUIResource(Color.RED))

    val defaults = UIManager.getLookAndFeelDefaults().entries
    defaults.forEach { e -> if (e.key.toString().contains("Scroll")) println(e.key) }*/

    val font = Font.createFont(Font.TRUETYPE_FONT, Object().javaClass.getResourceAsStream("/CutiveMono-Regular.ttf"))
            .deriveFont(24.0f)

    val frame = JFrame(APP_TITLE)
    frame.background = BACKGROUND_DARKER
    frame.contentPane.background = BACKGROUND_DARKER
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    val tabbedPane = MyTabbledPane()
    addTab(tabbedPane, "My Tab", font)
    addTab(tabbedPane, "Other Tab", font)
    addTab(tabbedPane, "Another one", font)

    frame.contentPane.add(tabbedPane)

    frame.pack()
    frame.isVisible = true
}

fun main(args: Array<String>) {
    SwingUtilities.invokeLater { createAndShowGUI() }
}