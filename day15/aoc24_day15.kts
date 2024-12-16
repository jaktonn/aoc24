import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText

val smallMap = """
    ########
    #..O.O.#
    ##@.O..#
    #...O..#
    #.#.O..#
    #...O..#
    #......#
    ########
""".trimIndent()

val largerMap = """
    ##########
    #..O..O.O#
    #......O.#
    #.OO..O.O#
    #..O@..O.#
    #O#..O...#
    #O..O..O.#
    #.OO.O.OO#
    #....O...#
    ##########
""".trimIndent()

val largerDirections = """
    <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
    vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
    ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
    <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
    ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
    ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
    >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
    <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
    ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
    v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
""".trimIndent()

//val map = largerMap.lines()
val map = Path("./map.txt").readLines()
val smallDirections = "<^^>>>vv<v>>v<<".trimIndent()
//val directions = smallDirections.map(Direction::fromChar)
//val directions = largerDirections.filterNot { it == '\n' }.map(Direction::fromChar)
val directions = Path("./directions.txt").readText().filterNot { it == '\n' }.map(Direction::fromChar)

enum class Direction {
    UP,
    LEFT,
    RIGHT,
    DOWN;

    companion object {
        fun fromChar(char: Char) : Direction {
            return when (char) {
                '^' -> UP
                'v' -> DOWN
                '<' -> LEFT
                '>' -> RIGHT
                else -> throw IllegalArgumentException("Not a direction")
            }
        }
    }
}

data class Position(val x: Int, val y: Int) {
    fun move(direction: Direction): Position {
        return when(direction) {
            Direction.UP -> Position(x, y - 1)
            Direction.DOWN -> Position(x, y + 1)
            Direction.LEFT -> Position(x - 1, y)
            Direction.RIGHT -> Position(x + 1, y)
        }
    }

    fun gpsCoordinate(): Long {
        return y * 100L + x
    }
}

val boxes = mutableSetOf<Position>()
val walls = mutableSetOf<Position>()
var robotPosition: Position? = null
map.forEachIndexed { y, row ->
    row.forEachIndexed { x, char ->
        if (char == '#') {
            walls.add(Position(x, y))
        } else if (char == 'O') {
            boxes.add(Position(x, y))
        } else if (char == '@') {
            robotPosition = Position(x, y)
        }
    }
}

class Robot(var position: Position) {
    fun tryMove(direction: Direction) {
        val nextPosition = position.move(direction)
        if (!walls.contains(nextPosition)) {
            if (boxes.contains(nextPosition)) {
                val boxesToPush = mutableListOf(nextPosition)
                while(boxes.contains(boxesToPush.last().move(direction))) {
                    boxesToPush.add(boxesToPush.last().move(direction))
                }
                if (!walls.contains(boxesToPush.last().move(direction))) {
                    boxes.removeAll(boxesToPush.toSet())
                    boxes.addAll(boxesToPush.map { it.move(direction) }.toSet())
                    position = nextPosition
                }
            } else {
                position = nextPosition
            }
        }
    }
}

val robot = Robot(robotPosition!!)
directions.forEach { robot.tryMove(it)}
val sumOfGpsCoordinates = boxes.sumOf { it.gpsCoordinate() }
println("Sum of GPS coordinates $sumOfGpsCoordinates")


