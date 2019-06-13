package timetracker
import scala.collection.SortedMap
import scala.util.control.Breaks._
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import timetracker.modes.{AppState, ViewMode, InputMode, TimeStart}


object Timetracker extends App {
  def now() = System.currentTimeMillis

  val terminal = new DefaultTerminalFactory().createTerminal()
  val screen = new TerminalScreen(terminal)
  screen.startScreen()

  var terminalSize = screen.getTerminalSize
  val textGraphics = screen.newTextGraphics()
  screen.setCursorPosition(null)
  screen.refresh()

  var currentScreen: AppState = new ViewMode()

  var currentTimes = List(new TimeStart("Nothing", now()))

  sys.addShutdownHook {
    if (screen != null) {
      screen.close()
    }
    // Can we write out to a file here?
  }

  breakable {
    while (true) {
      val newSize = screen.doResizeIfNecessary()
      terminalSize = if (newSize != null) {
        screen.clear()
        newSize
      } else {
        terminalSize
      }

      val keyStroke = screen.pollInput
      if (keyStroke != null) {
        if (keyStroke.getKeyType() == KeyType.EOF) {
          break
        }
        val (newScreen, newTimes) = currentScreen.handleInput(screen, keyStroke, currentTimes)
        currentScreen = newScreen
        currentTimes = newTimes
      }

      currentScreen.render(screen, terminalSize, currentTimes)

      screen.refresh()
      Thread.`yield`()
    }
  }

  if (screen != null) {
    screen.close()
  }
}
