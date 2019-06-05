package timetracker
import scala.collection.SortedMap
import scala.util.control.Breaks._
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

case class TimeStart(name: String, start: Long)

trait AppState
case class ViewMode() extends AppState
case class InputMode() extends AppState


object Timetracker extends App {
  def now() = System.currentTimeMillis

  def collectTimes(times: List[TimeStart], out: SortedMap[String, Long]): SortedMap[String, Long] = {
    times match {
      case Nil => out
      case time :: Nil => {
        val additionalTime = now - time.start
        val existingTime: Long = out.get(time.name).getOrElse(0)
        out + (time.name -> (existingTime + additionalTime))
      }
      case first :: second :: _ => {
        val additionalTime = second.start - first.start
        val existingTime: Long = out.get(first.name).getOrElse(0)
        val updated = out + (first.name -> (existingTime + additionalTime))
        collectTimes(times.tail, updated)
      }
    }
  }

  def msToHours(dur: Long): Double = {
    dur.toDouble / 1000 / 60 / 60
  }

  val terminal = new DefaultTerminalFactory().createTerminal()
  val screen = new TerminalScreen(terminal)
  screen.startScreen()

  var terminalSize = screen.getTerminalSize
  val textGraphics = screen.newTextGraphics()
  screen.setCursorPosition(null)
  screen.refresh()

  val start = now()
  var currentState: AppState = new ViewMode()
  val currentString = new StringBuilder

  var currentTimes = List(new TimeStart("Nothing", now()))

  breakable {
    while (true) {
      val keyStroke = screen.pollInput
      if (keyStroke != null) {
        if (keyStroke.getKeyType() == KeyType.EOF) {
          break
        }
        currentState match {
          case ViewMode() => {
            if (keyStroke.getKeyType() == KeyType.Character) {
              val c = keyStroke.getCharacter()
              if (c == 'i') {
                currentState = new InputMode()
                screen.clear()
              }
            }
          }
          case InputMode() => {
            if (keyStroke.getKeyType() == KeyType.Escape) {
              currentString.clear()
              currentState = new ViewMode()
              screen.clear()
            } else if (keyStroke.getKeyType() == KeyType.Enter) {
              if (currentString != "") {
                currentTimes = currentTimes :+ new TimeStart(currentString.toString, now())
              }
              currentString.clear()
              currentState = new ViewMode()
              screen.clear()
            } else if (keyStroke.getKeyType() == KeyType.Backspace) {
              currentString.deleteCharAt(currentString.length - 1)
              screen.clear()
            } else if (keyStroke.getKeyType() == KeyType.Character) {
              currentString += keyStroke.getCharacter
            }
          }
        }
      }

      val newSize = screen.doResizeIfNecessary()
      terminalSize = if (newSize != null) {
        screen.clear()
        newSize
      } else {
        terminalSize
      }

      // Draw
      currentState match {
        case ViewMode() => {
          textGraphics.putString(0, terminalSize.getRows - 1, s"Logging time for ${currentTimes.reverse.head.name}")
        }
        case InputMode() => {
          textGraphics.putString(0, terminalSize.getRows - 1, s"? ${currentString}")
        }
      }

      val collected = collectTimes(currentTimes, SortedMap())
      collected.zipWithIndex.foreach { case(pair, idx) => {
        val (name, duration) = pair
        textGraphics.putString(0, idx, f"${idx}: ${name} - ${msToHours(duration)}%.2f")
      }}
      screen.refresh()
      Thread.`yield`()
    }
  }

  if (screen != null) {
    screen.close()
  }
}
