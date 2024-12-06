import kotlin.io.path.Path
import kotlin.io.path.readLines

val testMap = """
    ....#.....
    .........#
    ..........
    ..#.......
    .......#..
    ..........
    .#..^.....
    ........#.
    #.........
    ......#...
""".trimIndent()

data class Position(val x: Int, val y: Int) {

    fun move(direction: Direction): Position {
        return Position(this.x + direction.deltaX, this.y + direction.deltaY)
    }

    fun outside(): Boolean {
        return x == -1 || x == map[0].length || y == -1 || y == map.size
    }
}

data class Direction(val deltaX: Int, val deltaY: Int) {

    companion object {
        val LEFT = Direction(-1, 0)
        val RIGHT = Direction(1, 0)
        val UP = Direction(0, -1)
        val DOWN = Direction(0, 1)

        fun fromChar(char: Char): Direction {
            return when(char) {
                '^' -> UP
                'v' -> DOWN
                '<' -> LEFT
                '>' -> RIGHT
                else -> throw IllegalArgumentException()
            }
        }
    }

    fun turn(): Direction {
        return when(this) {
            Direction(-1, 0) -> return Direction(0, -1)  // left -> up
            Direction(0, -1) -> return Direction(1, 0)   // up -> right
            Direction(1, 0) -> return Direction(0, 1)  // right -> down
            Direction(0, 1) -> return Direction(-1, 0) // down -> left
            else -> throw IllegalArgumentException()
        }
    }
}

//val map = testMap.lines()
val map = Path("./map.txt").readLines()

val startLine = map.filter { it.indexOfAny("<>^v".toCharArray()) > -1 }.first()
val startRow = map.indexOf(startLine)
val startColumn = startLine.indexOfAny("<>^v".toCharArray())
val startDirection = Direction.fromChar(map[startRow][startColumn])

val visited = mutableListOf(Position(startColumn, startRow) to startDirection)
var direction = startDirection

do  {
    val nextForward = visited.last().first.move(direction)
    if (!nextForward.outside()) {
        if (map[nextForward.y][nextForward.x] == '#') {
            direction = direction.turn()
        } else {
            visited.add(nextForward to direction)
        }
    }
} while (!nextForward.outside())

println("Path length ${visited.map {it.first}.distinct().size}")

val loopCreatorPositions = mutableListOf<Position>()

for(i in 1..visited.lastIndex) {
    // This runs for a pretty long time. There must be a better way to solve this, but no good idea right now
    val newObstacle = visited[i].first
    if (!loopCreatorPositions.contains(newObstacle)) {
        val newPath = mutableListOf(Position(startColumn, startRow) to startDirection)
        var direction = newPath.last().second
        do {
            val nextForward = newPath.last().first.move(direction)
            if (newPath.subList(0, newPath.size-1).contains(newPath.last())) {
                loopCreatorPositions.add(newObstacle)
                break
            }
            if (!nextForward.outside()) {
                if (map[nextForward.y][nextForward.x] == '#' || nextForward == newObstacle) {
                    direction = direction.turn()
                } else {
                    newPath.add(nextForward to direction)
                }
            }
        } while (!nextForward.outside())
    }
}

println("Loop creator candidates ${loopCreatorPositions.distinct().size}")