import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText

val smallMap = """
    #######
    #...#.#
    #.....#
    #..OO@#
    #..O..#
    #.....#
    #######
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

//val map = smallMap.lines()
//val map = largerMap.lines()
val map = Path("./map.txt").readLines()


val smallDirections = "<vv<<^^<<^^"
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

    fun isNeighbour(other: Position): Boolean {
        return if (other.x == x) {
            other.y == y - 1 || other.y == y + 1
        } else if (other.y == y ){
            other.x == x - 1 || other.x == x + 1
        } else {
            false
        }
    }
}

val boxMap = mutableMapOf<Position, Box>()
data class Box(var positions: List<Position>) {
    fun push(direction: Direction) {
        positions = positions.map { it.move(direction) }
    }
    fun gpsCoordinates(): Long {
        return positions.minBy { it.x }.gpsCoordinate()
    }
}

data class BoxTree(val box: Box, val pushed: List<BoxTree>) {

    fun allBoxes(): List<Box> {
        return pushed.flatMap { it.allBoxes() } + box
    }

    fun leaves(): List<Box> {
        return if (pushed.isEmpty()) {
            listOf(box)
        } else {
            pushed.map { it.leaves() }.flatten()
        }
    }

    fun leavePositions(): List<Position> {
        return if (pushed.isEmpty()) {
            box.positions
        } else {
            box.positions.filterNot { pushed.flatMap { p -> p.box.positions }.any { p -> p.isNeighbour(it) } } + pushed.flatMap { it.leavePositions() }
        }
    }
}

fun buildBoxTree(box: Box, direction: Direction): BoxTree {
    val nextPositions = box.positions.map { it.move(direction) }.filterNot { box.positions.contains(it) }
    val nextBoxTrees = nextPositions.map { boxMap[it] }.filterNotNull().toSet().map { buildBoxTree(it, direction) }
    return BoxTree(box, nextBoxTrees)
}



val walls = mutableSetOf<Position>()
var robotPosition: Position? = null
map.forEachIndexed { y, row ->
    row.forEachIndexed { x, char ->
        if (char == '#') {
            walls.add(Position(x * 2, y))
            walls.add(Position(x * 2 + 1, y))
        } else if (char == 'O') {
            val box = Box(listOf(Position(x * 2, y), Position(x * 2 + 1, y)))
            boxMap[box.positions[0]] = box
            boxMap[box.positions[1]] = box
        } else if (char == '@') {
            robotPosition = Position(x * 2, y)
        }
    }
}

class Robot(var position: Position) {
    fun tryMove(direction: Direction) {
        val nextPosition = position.move(direction)
        if (!walls.contains(nextPosition)) {
            if (boxMap.contains(nextPosition)) {
                val boxesToPush = buildBoxTree(boxMap[nextPosition]!!, direction)
                val endPositions = boxesToPush.leavePositions().map { it.move(direction) }.toSet()
                if (!walls.any { endPositions.contains(it) }) {
                    //tree of boxes can be pushed
                    val allBoxes = boxesToPush.allBoxes()
                    allBoxes.forEach { box ->
                        box.positions.forEach { boxMap.remove(it) }
                        box.push(direction)
                        box.positions.forEach { boxMap[it] = box }
                    }
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

val sumOfGpsCoordinates = boxMap.values.toSet().sumOf { it.gpsCoordinates() }
println("Sum of GPS coordinates $sumOfGpsCoordinates")

var printMap = ""
for (y in map.indices) {
    for (x in 0..<map[0].length * 2) {
        if (walls.contains(Position(x, y))) {
            printMap += "#"
        } else if (boxMap.contains(Position(x, y))) {
            val box = boxMap.getValue(Position(x, y))
            printMap += if (box.positions.minBy { it.x } == Position(x, y)) {
                "["
            } else {
                "]"
            }
        } else if (robot.position == Position(x, y)) {
            printMap += "@"
        } else {
            printMap += "."
        }
    }
    printMap += "\n"
}
println(printMap)