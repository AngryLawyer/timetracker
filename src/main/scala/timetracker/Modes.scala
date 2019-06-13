package timetracker.modes
import scala.collection.SortedMap
import com.googlecode.lanterna.screen.{TerminalScreen}
import com.googlecode.lanterna.{TerminalSize}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

case class TimeStart(name: String, start: Long)

trait AppState {
  def render(screen: TerminalScreen, terminalSize: TerminalSize, times: List[TimeStart])
  def handleInput(screen: TerminalScreen, keyStroke: KeyStroke, times: List[TimeStart]): Tuple2[AppState, List[TimeStart]]
}

object AppState {
  def now() = System.currentTimeMillis

  def msToHours(dur: Long): Double = {
    dur.toDouble / 1000 / 60 / 60
  }


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

  def renderTimes(screen: TerminalScreen, terminalSize: TerminalSize, times: List[TimeStart]) {
    val textGraphics = screen.newTextGraphics()
    val collected = collectTimes(times, SortedMap())
    collected.zipWithIndex.foreach { case(pair, idx) => {
      val (name, duration) = pair
      textGraphics.putString(0, idx, f"${idx}: ${name} - ${msToHours(duration)}%.2f")
    }}
  }
}

class ViewMode() extends AppState {
  def render(screen: TerminalScreen, terminalSize: TerminalSize, times: List[TimeStart]) {
    AppState.renderTimes(screen, terminalSize, times)
    val textGraphics = screen.newTextGraphics()
    textGraphics.putString(0, terminalSize.getRows - 1, s"Logging time for ${times.reverse.head.name}")
  }

  def handleInput(screen: TerminalScreen, keyStroke: KeyStroke, times: List[TimeStart]): Tuple2[AppState, List[TimeStart]] = {
    if (keyStroke.getKeyType() == KeyType.Character) {
      val c = keyStroke.getCharacter()
      if (c == 'i') {
        screen.clear()
        return (new InputMode(), times)
      }
    }
    (this, times)
  }
}

class InputMode() extends AppState {
  val currentString = new StringBuilder

  def render(screen: TerminalScreen, terminalSize: TerminalSize, times: List[TimeStart]) {
    val textGraphics = screen.newTextGraphics()
    textGraphics.putString(0, terminalSize.getRows - 1, s"? ${currentString}")
    AppState.renderTimes(screen, terminalSize, times)
  }

  def handleInput(screen: TerminalScreen, keyStroke: KeyStroke, times: List[TimeStart]): Tuple2[AppState, List[TimeStart]] = {
    if (keyStroke.getKeyType() == KeyType.Escape) {
      currentString.clear()
      screen.clear()
      return (new ViewMode(), times)
    } else if (keyStroke.getKeyType() == KeyType.Enter) {
      val currentTimes = if (currentString != "") {
        times :+ new TimeStart(currentString.toString, AppState.now())
      } else {
        times
      }
      currentString.clear()
      screen.clear()
      return (new ViewMode(), currentTimes)
    } else if (keyStroke.getKeyType() == KeyType.Backspace) {
      currentString.deleteCharAt(currentString.length - 1)
      screen.clear()
    } else if (keyStroke.getKeyType() == KeyType.Character) {
      currentString += keyStroke.getCharacter
    }
    (this, times)
  }
}
