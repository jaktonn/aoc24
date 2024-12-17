import kotlin.io.path.Path
import kotlin.io.path.readLines

val smallMap = """
    ###############
    #.......#....E#
    #.#.###.#.###.#
    #.....#.#...#.#
    #.###.#####.#.#
    #.#.#.......#.#
    #.#.#####.###.#
    #...........#.#
    ###.#.#####.#.#
    #...#.....#.#.#
    #.#.#.###.#.#.#
    #.....#...#.#.#
    #.###.#.#.#.#.#
    #S..#.....#...#
    ###############
""".trimIndent()

val mediumMap = """
    #################
    #...#...#...#..E#
    #.#.#.#.#.#.#.#.#
    #.#.#.#...#...#.#
    #.#.#.#.###.#.#.#
    #...#.#.#.....#.#
    #.#.#.#.#.#####.#
    #.#...#.#.#.....#
    #.#.#####.#.###.#
    #.#.#.......#...#
    #.#.###.#####.###
    #.#.#...#.....#.#
    #.#.#.#####.###.#
    #.#.#.........#.#
    #.#.#.#########.#
    #S#.............#
    #################
""".trimIndent()

//val map = smallMap.lines()
//val map = mediumMap.lines()
val map = Path("./map.txt").readLines()

enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, -1),
    WEST(-1, 0),
    EAST(1, 0),
    SOUTH(0, 1);

    fun turnScore(turnTo: Direction): Long {
        return when {
            this == turnTo -> 0L
            this == NORTH && turnTo == SOUTH -> 2000L
            this == SOUTH && turnTo == NORTH -> 2000L
            this == EAST && turnTo == WEST -> 2000L
            this == WEST && turnTo == EAST -> 2000L
            else -> 1000L
        }
    }
}

data class Field(val x: Int, val y: Int) {
    fun reachable(): List<Pair<Direction, Field>> {
        val neighbours = Direction.entries.map { it to this.neighbour(it) }
        return neighbours.filter { map[it.second.y][it.second.x] != '#' }
    }

    fun neighbour(direction: Direction): Field {
        return Field(x + direction.dx, y + direction.dy)
    }
}

data class PathStep(val field: Field, val direction: Direction, val score: Long) {
    fun nextSteps(): List<PathStep> {
        val nexts = field.reachable()
        return nexts.map { PathStep(it.second, it.first, score + direction.turnScore(it.first)+1L) }
    }
}

var shortest = Long.MAX_VALUE
var pathCounter = 0
class Path(val step: PathStep, val previous: Path?, var nexts: List<Path> = emptyList()) {
    fun searchTheEnd(visited: Set<Field>): Boolean {
        return if (map[step.field.y][step.field.x] == 'E') {
            if (shortest > step.score) {
                shortest = step.score
            }
            pathCounter++
            print("\b".repeat(pathCounter.toString().length))
            print(pathCounter)
            true
        } else if (visited.contains(step.field) || step.score > shortest) {
            false
        } else {
            val nextSteps = step.nextSteps().filter { it.field != previous?.step?.field }.filterNot { visited.contains(it.field)}
            if (nextSteps.isEmpty()) {
                false
            } else {
                val pathsToEnd = nextSteps.map { Path(it, this, emptyList()) }.filter { it.searchTheEnd(visited + step.field) }
                nexts = pathsToEnd
                return pathsToEnd.isNotEmpty()
            }
        }
    }

    fun findLeaves(): List<Path> {
        return if(nexts.isEmpty()) {
            listOf(this)
        } else {
            nexts.flatMap { it.findLeaves() }
        }
    }

    fun visitedFields(): List<Field> {
        return if (this.previous == null) {
            listOf(this.step.field)
        } else {
            listOf(this.step.field) + previous.visitedFields()
        }
    }
}

var start: Field? = null
var end: Field? = null
for (y in map.indices) {
    for (x in map[y].indices) {
        if (map[y][x] == 'S') {
            start = Field(x, y)
        } else if (map[y][x] == 'E') {
            end = Field(x, y)
        }
    }
}

val firstStep = PathStep(start!!, Direction.EAST, 0L)
val path = Path(firstStep, null)

path.searchTheEnd(emptySet())
println("\n*****")
val pathLeaves = path.findLeaves().sortedBy { it.step.score }
val shortestPath = pathLeaves.first()
println("Shortest path score: ${shortestPath.step.score}")

val shortestPaths = pathLeaves.filter { it.step.score== shortestPath.step.score }
println(shortestPaths.size)
val visitedFields = shortestPaths.flatMap { it.visitedFields() }.toSet()
println("Tiles on best paths ${visitedFields.size}")








