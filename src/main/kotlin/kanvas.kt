import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.Toolkit
import javax.swing.*
import javax.swing.plaf.ColorUIResource
import javax.swing.plaf.metal.MetalTabbedPaneUI
import javax.swing.plaf.synth.SynthScrollBarUI

private val BACKGROUND = Color(39, 40, 34)
private val BACKGROUND_SUBTLE_HIGHLIGHT = Color(49, 50, 44)
private val BACKGROUND_DARKER = Color(23, 24, 20)
private val BACKGROUND_LIGHTER = Color(109, 109, 109)

private fun makeTextPanel(font: Font) : Component {
    val textArea = RSyntaxTextArea(20, 60)
    textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVA
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