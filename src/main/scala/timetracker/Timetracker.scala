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
  var shouldExit = false

  sys.addShutdownHook {
    if (screen != null) {
      screen.close()
    }
    // Can we write out to a file here?
  }

  while (!shouldExit) {
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
        shouldExit = true
      } else {
        val (newScreen, newTimes, exit) = currentScreen.handleInput(screen, keyStroke, currentTimes)
        currentScreen = newScreen
        currentTimes = newTimes
        shouldExit = exit
      }
    }

    currentScreen.render(screen, terminalSize, currentTimes)

    screen.refresh()
    Thread.`yield`()
  }

  if (screen != null) {
    screen.close()
  }
}
