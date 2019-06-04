package timetracker
import scala.util.control.Breaks._
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.input.{KeyStroke, KeyType}


object Timetracker extends App {
  val terminal = new DefaultTerminalFactory().createTerminal()
  val screen = new TerminalScreen(terminal)
  screen.startScreen()

  var terminalSize = screen.getTerminalSize
  screen.setCursorPosition(null)
  screen.refresh()

  breakable {
    while (true) {
      val keyStroke = screen.pollInput
      if(keyStroke != null && (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF)) {
        break
      }

      val newSize = screen.doResizeIfNecessary()
      terminalSize = if (newSize != null) {
        newSize
      } else {
        terminalSize
      }
      screen.refresh()
      Thread.`yield`()
    }
  }

  if (screen != null) {
    screen.close()
  }
}
