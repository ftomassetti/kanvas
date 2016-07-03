import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.*
import javax.swing.plaf.metal.MetalTabbedPaneUI

private val BACKGROUND = Color(39, 40, 34)
private val BACKGROUND_DARKER = Color(23, 24, 20)
private val BACKGROUND_LIGHTER = Color(109, 109, 109)

private fun makeTextPanel(font: Font) : Component {
    val editor = JEditorPane()
    editor.font = font
    editor.background = BACKGROUND
    editor.foreground = Color.WHITE
    return editor
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

private fun createAndShowGUI() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val font = Font.createFont(Font.TRUETYPE_FONT, Object().javaClass.getResourceAsStream("/CutiveMono-Regular.ttf"))
            .deriveFont(24.0f)

    val frame = JFrame("Kanvas")
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