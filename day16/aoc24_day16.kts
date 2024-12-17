import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.min

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

data class PathStep(val field: Field, val direction: Direction, val score: Long, val visited: Set<Field>) {
    fun nextSteps(): List<PathStep> {
        val nexts = field.reachable().filterNot { visited.contains(field) }
        return nexts.map { PathStep(it.second, it.first, score + direction.turnScore(it.first)+1L, visited + this.field) }
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

val firstStep = PathStep(start!!, Direction.EAST, 0L, emptySet())
val finalSteps = mutableListOf<PathStep>()
var currentShortest = Long.MAX_VALUE
val lowestVisitedScores = mutableMapOf<Pair<Field, Direction>, Long>()
var currentSteps = listOf(firstStep)
while (currentSteps.isNotEmpty()) {
    currentSteps = currentSteps.flatMap { step ->
        val nextSteps = if (map[step.field.y][step.field.x] == 'E') {
            finalSteps.add(step)
            currentShortest = min(step.score, currentShortest)
            println("found solution length ${step.score}")
            listOf(null)
        } else if (step.score > (lowestVisitedScores[step.field to step.direction] ?: Long.MAX_VALUE)) {
            listOf(null)
        } else {
            step.nextSteps()
        }
        lowestVisitedScores.compute(step.field to step.direction) {_, v -> min(v ?: Long.MAX_VALUE, step.score)}
        nextSteps.filterNotNull()
    }
}

println("Shortest path $currentShortest")
val visitedFields = finalSteps.filter { it.score == currentShortest }.flatMap { it.visited }.toSet().size + 1
println("Visited fields $visitedFields")
