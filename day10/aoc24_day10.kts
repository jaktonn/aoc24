import kotlin.io.path.Path
import kotlin.io.path.readLines

val testMap = """
    89010123
    78121874
    87430965
    96549874
    45678903
    32019012
    01329801
    10456732
""".trimIndent()

//val map = testMap.lines()
val map = Path("./map.txt").readLines()

data class Position(val x: Int, val y: Int) {
    fun nextPositions() : List<Position> {
        val current = map[y][x].digitToInt()
        val nexts = mutableListOf<Position>()
        if (x > 0 && map[y][x-1].digitToInt() == current+1) {
            nexts.add(Position(x-1, y))
        }
        if (y > 0 && map[y-1][x].digitToInt() == current+1) {
            nexts.add(Position(x, y-1))
        }
        if (y < map.lastIndex && map[y+1][x].digitToInt() == current+1) {
            nexts.add(Position(x, y+1))
        }
        if (x < map[0].lastIndex && map[y][x+1].digitToInt() == current+1) {
            nexts.add(Position(x+1, y))
        }
        return nexts
    }
}

fun findReachablePeaks(current: Position): Set<Position> {
    return if (map[current.y][current.x].digitToInt() == 9) {
        setOf(current)
    } else {
        val nexts = current.nextPositions()
        nexts.flatMap { findReachablePeaks(it) }.toSet()
    }
}

fun findDistinctPaths(current: Position): Int {
    return if (map[current.y][current.x].digitToInt() == 9) {
        1
    } else {
        val nexts = current.nextPositions()
        nexts.sumOf { findDistinctPaths(it) }
    }
}

val trailheads = mutableListOf<Position>()
map.forEachIndexed { y, row ->
    row.forEachIndexed { x, c ->
        if (c.digitToInt() == 0) {
            trailheads.add(Position(x, y))
        }
    }
}

val score = trailheads.map { findReachablePeaks(it).size }.sum()
println("Trailhead score $score")

val distinctPaths = trailheads.map { findDistinctPaths(it) }.sum()
println("Distinct paths $distinctPaths")



