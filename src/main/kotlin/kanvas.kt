
import me.tomassetti.python.None
import org.antlr.v4.runtime.Lexer
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.*
import java.util.*
import javax.swing.*
import javax.swing.plaf.metal.MetalTabbedPaneUI
import javax.swing.plaf.synth.SynthScrollBarUI

private val BACKGROUND = Color(39, 40, 34)
private val BACKGROUND_SUBTLE_HIGHLIGHT = Color(49, 50, 44)
private val BACKGROUND_DARKER = Color(23, 24, 20)
private val BACKGROUND_LIGHTER = Color(109, 109, 109)

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
            override fun create(code: String): Lexer = None(org.antlr.v4.runtime.ANTLRInputStream(code))
        }

}

object LanguageSupportRegistry {
    private val extensionsMap = HashMap<String, LanguageSupport>()

    fun register(extension : String, languageSupport: LanguageSupport) {
        extensionsMap[extension] = languageSupport
    }
    fun languageSupportForExtension(extension : String) : LanguageSupport = extensionsMap.getOrDefault(extension, noneLanguageSupport)
}

private fun makeTextPanel(font: Font, languageSupport: LanguageSupport) : Component {
    val textArea = RSyntaxTextArea(20, 60)

    (textArea.document as RSyntaxDocument).setSyntaxStyle(AntlrTokenMaker(languageSupport.antlrLexerFactory))

    textArea.syntaxScheme = languageSupport.syntaxScheme
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

private fun addTab(tabbedPane: MyTabbledPane, title: String, font: Font, languageSupport: LanguageSupport) {
    val panel1 = makeTextPanel(font, languageSupport)
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

    val font = Font.createFont(Font.TRUETYPE_FONT, Object().javaClass.getResourceAsStream("/CutiveMono-Regular.ttf"))
            .deriveFont(24.0f)

    val frame = JFrame(APP_TITLE)
    frame.background = BACKGROUND_DARKER
    frame.contentPane.background = BACKGROUND_DARKER
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    val tabbedPane = MyTabbledPane()

    val menuBar = JMenuBar()
    val fileMenu = JMenu("File")
    menuBar.add(fileMenu)
    val open = JMenuItem("Open")
    fileMenu.add(open)
    val new = JMenuItem("New")
    new.addActionListener { addTab(tabbedPane, "<UNNAMED>", font, noneLanguageSupport) }
    fileMenu.add(new)
    val save = JMenuItem("Save")
    fileMenu.add(save)
    val saveAs = JMenuItem("Save as")
    fileMenu.add(saveAs)
    val close = JMenuItem("Close")
    fileMenu.add(close)
    frame.jMenuBar = menuBar

    //addTab(tabbedPane, "My Tab", font, noneLanguageSupport)
    //addTab(tabbedPane, "Other Tab", font, noneLanguageSupport)
    //addTab(tabbedPane, "Another one", font, noneLanguageSupport)

    frame.contentPane.add(tabbedPane)

    frame.pack()
    if (frame.width < 500) {
        frame.size = Dimension(500, 500)
    }
    frame.isVisible = true
}

fun main(args: Array<String>) {
    SwingUtilities.invokeLater { createAndShowGUI() }
}