package me.tomassetti.kanvas

import org.fife.ui.autocomplete.*
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.*
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import javax.swing.*
import javax.swing.plaf.metal.MetalTabbedPaneUI
import javax.swing.plaf.synth.SynthScrollBarUI
import javax.swing.text.*

private val BACKGROUND = Color(39, 40, 34)
private val BACKGROUND_SUBTLE_HIGHLIGHT = Color(49, 50, 44)
private val BACKGROUND_DARKER = Color(23, 24, 20)
private val BACKGROUND_LIGHTER = Color(109, 109, 109)

class TextPanel(textArea: RSyntaxTextArea, var file : File?) : RTextScrollPane(textArea) {
    val text : String
        get() = textArea.text
    var title : String
        get() = tabbedPane().getTitleAt(index())
        set(value) {
            tabbedPane().setTitleAt(index(), value)
        }
    private fun tabbedPane() = this.parent as MyTabbedPane
    private fun index() = tabbedPane().indexOfComponent(this)
    fun close() {
        tabbedPane().removeTabAt(index())
    }

    fun  changeLanguageSupport(languageSupport: LanguageSupport) {
        (textArea.document as RSyntaxDocument).setSyntaxStyle(AntlrTokenMaker(languageSupport.antlrLexerFactory))
        (textArea as RSyntaxTextArea).syntaxScheme = languageSupport.syntaxScheme
    }
}

private fun makeTextPanel(font: Font, languageSupport: LanguageSupport, initialContenxt: String = "", file: File? = null) : TextPanel {
    val textArea = RSyntaxTextArea(20, 60)

    (textArea.document as RSyntaxDocument).setSyntaxStyle(AntlrTokenMaker(languageSupport.antlrLexerFactory))

    textArea.syntaxScheme = languageSupport.syntaxScheme
    textArea.text = initialContenxt
    textArea.isCodeFoldingEnabled = true
    textArea.font = font
    textArea.background = BACKGROUND
    textArea.foreground = Color.WHITE
    textArea.currentLineHighlightColor = BACKGROUND_SUBTLE_HIGHLIGHT
    val textPanel = TextPanel(textArea, file)

    val provider = createCompletionProvider(languageSupport)
    val ac = AutoCompletion(provider)
    ac.install(textArea)

    textPanel.viewportBorder = BorderFactory.createEmptyBorder()
    textPanel.verticalScrollBar.ui = object : SynthScrollBarUI() {
        override fun configureScrollBarColors() {
            super.configureScrollBarColors()
        }
    }
    return textPanel
}

private abstract class AbstractCompletionProviderBase : CompletionProviderBase() {
    private val seg = Segment()

    override fun getCompletionsAt(comp: JTextComponent?, p: Point?): MutableList<Completion>? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParameterizedCompletions(tc: JTextComponent?): MutableList<ParameterizedCompletion>? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    protected fun isValidChar(ch: Char): Boolean {
        return Character.isLetterOrDigit(ch) || ch == '_'
    }

    override fun getAlreadyEnteredText(comp: JTextComponent): String {
        val doc = comp.document

        val dot = comp.caretPosition
        val root = doc.defaultRootElement
        val index = root.getElementIndex(dot)
        val elem = root.getElement(index)
        var start = elem.startOffset
        var len = dot - start
        try {
            doc.getText(start, len)
        } catch (ble: BadLocationException) {
            ble.printStackTrace()
            return EMPTY_STRING
        }

        val segEnd = seg.offset + len
        start = segEnd - 1
        while (start >= seg.offset && seg.array != null && isValidChar(seg.array[start])) {
            start--
        }
        start++

        len = segEnd - start
        return if (len == 0) EMPTY_STRING else String(seg.array, start, len)
    }
}

fun createCompletionProvider(languageSupport: LanguageSupport): CompletionProvider {
    if (languageSupport.parserData == null) {
        return object : AbstractCompletionProviderBase() {

            override fun getCompletionsImpl(comp: JTextComponent?): MutableList<Completion> {
                return LinkedList()
            }

        }
    }
    val cp = object : AbstractCompletionProviderBase() {

        private val autoCompletionSuggester = AntlrAutoCompletionSuggester(languageSupport.parserData!!.ruleNames,
                languageSupport.parserData!!.vocabulary, languageSupport.parserData!!.atn)

        private fun beforeCaret(comp: JTextComponent) : String {
            val doc = comp.document

            val dot = comp.caretPosition
            val root = doc.defaultRootElement
            val currLineIndex = root.getElementIndex(dot)
            val sb = StringBuffer()
            for (i in 0 until currLineIndex) {
                val elem = root.getElement(i)
                var start = elem.startOffset
                val len = elem.endOffset - start
                sb.append(doc.getText(start, len))
            }

            sb.append(beforeCaretOnCurrentLine(doc, dot, currLineIndex))
            return sb.toString()
        }

        private fun beforeCaretOnCurrentLine(doc: Document, dot:Int, currLineIndex: Int) : String {
            val root = doc.defaultRootElement
            val elem = root.getElement(currLineIndex)
            var start = elem.startOffset
            val len = dot - start
            return doc.getText(start, len)
        }

        override fun getCompletionsImpl(comp: JTextComponent): MutableList<Completion>? {
            val retVal = ArrayList<Completion>()
            val code = beforeCaret(comp)
            autoCompletionSuggester.suggestions(EditorContextImpl(code, languageSupport.antlrLexerFactory)).forEach {
                if (it.type != -1) {
                    retVal.addAll(languageSupport.propositionProvider.fromTokenType(this, it.type))
                }
            }

            return retVal

        }

    }
    return cp
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

class MyTabbedPane : JTabbedPane() {

    init {
        this.tabPlacement = SwingConstants.TOP
        val ui = NoInsetTabbedPaneUI()
        this.setUI(ui)
        ui.overrideContentBorderInsetsOfUI()
    }

}


//
// Commands
//

private fun saveAsCommand(tabbedPane : MyTabbedPane) {
    if (tabbedPane.selectedComponent == null) {
        return
    }
    val fc = JFileChooser()
    val res = fc.showOpenDialog(tabbedPane)
    if (res == JFileChooser.APPROVE_OPTION) {
        (tabbedPane.selectedComponent as TextPanel).file = fc.selectedFile
        val languageSupport = languageSupportRegistry.languageSupportForFile(fc.selectedFile)
        fc.selectedFile.writeText((tabbedPane.selectedComponent as TextPanel).text)
        (tabbedPane.selectedComponent as TextPanel).title = fc.selectedFile.name
        (tabbedPane.selectedComponent as TextPanel).changeLanguageSupport(languageSupport)
    }
}

private fun saveCommand(tabbedPane : MyTabbedPane) {
    if (tabbedPane.selectedComponent == null) {
        return
    }
    val file = (tabbedPane.selectedComponent as TextPanel).file
    if (file == null) {
        saveAsCommand(tabbedPane)
    } else {
        file.writeText((tabbedPane.selectedComponent as TextPanel).text)
    }
}

private fun closeCommand(tabbedPane : MyTabbedPane) {
    if (tabbedPane.selectedComponent == null) {
        return
    }
    (tabbedPane.selectedComponent as TextPanel).close()
}

//
// Public API
//

class Kanvas {

    val APP_TITLE = "Kanvas"

    val defaultFont: Font = Font.createFont(Font.TRUETYPE_FONT, Object().javaClass.getResourceAsStream("/CutiveMono-Regular.ttf"))
            .deriveFont(24.0f)

    private val tabbedPane = MyTabbedPane()

    fun addTab(title: String, font: Font = defaultFont, initialContenxt: String = "",
                       languageSupport: LanguageSupport = noneLanguageSupport,
                       file: File? = null) {
        try {
            val panel = makeTextPanel(font, languageSupport, initialContenxt, file)
            tabbedPane.addTab(title, null, panel, "Go to $title")
            tabbedPane.setForegroundAt(tabbedPane.tabCount - 1, Color.white)
            tabbedPane.setBackgroundAt(tabbedPane.tabCount - 1, BACKGROUND_DARKER)
        } catch (e : Exception) {
            JOptionPane.showMessageDialog(tabbedPane, "Error creating tab for language ${languageSupport}: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun openCommand() {
        val fc = JFileChooser()
        val res = fc.showOpenDialog(tabbedPane)
        if (res == JFileChooser.APPROVE_OPTION) {
            addTab(fc.selectedFile.name, defaultFont, fc.selectedFile.readText(Charset.defaultCharset()),
                    languageSupportRegistry.languageSupportForFile(fc.selectedFile),
                    fc.selectedFile)
        }
    }

    private fun createKanvasIcon() : Image {
        val url = ClassLoader.getSystemResource("kanvas-logo.png")
        val kit = Toolkit.getDefaultToolkit()
        val img = kit.createImage(url)
        return img
    }

    fun createAndShowKanvasGUI() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        val xToolkit = Toolkit.getDefaultToolkit()
        val awtAppClassNameField = xToolkit.javaClass.getDeclaredField("awtAppClassName")
        awtAppClassNameField.isAccessible = true
        awtAppClassNameField.set(xToolkit, APP_TITLE)

        val frame = JFrame(APP_TITLE)
        frame.iconImage = createKanvasIcon()
        frame.background = BACKGROUND_DARKER
        frame.contentPane.background = BACKGROUND_DARKER
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        frame.contentPane.add(tabbedPane)

        val menuBar = JMenuBar()
        val fileMenu = JMenu("File")
        menuBar.add(fileMenu)
        val open = JMenuItem("Open")
        open.addActionListener { openCommand() }
        fileMenu.add(open)
        val new = JMenuItem("New")
        new.addActionListener { addTab("<UNNAMED>") }
        fileMenu.add(new)
        val save = JMenuItem("Save")
        save.addActionListener { saveCommand(tabbedPane) }
        fileMenu.add(save)
        val saveAs = JMenuItem("Save as")
        saveAs.addActionListener { saveAsCommand(tabbedPane) }
        fileMenu.add(saveAs)
        val close = JMenuItem("Close")
        close.addActionListener { closeCommand(tabbedPane) }
        fileMenu.add(close)
        frame.jMenuBar = menuBar

        frame.pack()
        if (frame.width < 500) {
            frame.size = Dimension(500, 500)
        }
        frame.isVisible = true
    }
}

fun main(args: Array<String>) {
    val kanvas = Kanvas()
    SwingUtilities.invokeLater { kanvas.createAndShowKanvasGUI() }
}