package timetracker
import scala.collection.SortedMap
import scala.util.control.Breaks._
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

class TimeStart(val name: String, val start: Long)

object Timetracker extends App {
  def now() = System.currentTimeMillis

  val terminal = new DefaultTerminalFactory().createTerminal()
  val screen = new TerminalScreen(terminal)
  screen.startScreen()

  var terminalSize = screen.getTerminalSize
  val textGraphics = screen.newTextGraphics()
  screen.setCursorPosition(null)
  screen.refresh()

  val start = now()

  var currentTimes = List(new TimeStart("Nothing", now()), new TimeStart("Something", now() + 10))

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
      // Draw
      textGraphics.putString(0, terminalSize.getRows - 1, "Logging time for blahblah")
      currentTimes.zipWithIndex.foreach { case(time, idx) => {
        textGraphics.putString(0, idx, s"${time.name} - ${time.start}")
      }}
      screen.refresh()
      Thread.`yield`()
    }
  }

  if (screen != null) {
    screen.close()
  }
}
