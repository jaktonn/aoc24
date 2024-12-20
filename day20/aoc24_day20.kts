import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.abs

val testMap = """
    ###############
    #...#...#.....#
    #.#.#.#.#.###.#
    #S#...#.#.#...#
    #######.#.#.###
    #######.#.#...#
    #######.#.###.#
    ###..E#...#...#
    ###.#######.###
    #...###...#...#
    #.#####.#.###.#
    #.#...#.#.#...#
    #.#.#.#.#.#.###
    #...#...#...###
    ###############
""".trimIndent()

//val map = testMap.lines()
val map = Path("./map.txt").readLines()

data class Field(val x: Int, val y: Int) {
    fun getPath(): List<Field> {
        return listOf(Field(x - 1, y), Field(x + 1, y), Field(x, y - 1), Field(x, y + 1))
            .filter { map[it.y][it.x] != '#' }
    }

    fun getCheats(): List<Field> {
        return listOf(Field(x - 2, y), Field(x + 2, y), Field(x, y - 2), Field(x, y + 2))
            .filter { it.x in map[0].indices && it.y in map.indices }
            .filter { map[it.y][it.x] != '#' }
    }

    fun dist(other: Field): Int {
        return abs(other.x - x) + abs(other.y - y)
    }

}

var startX = 0
var endX = 0
var startY = 0
var endY = 0

for (y in map.indices) {
    for (x in map[0].indices) {
        if (map[y][x] == 'S') {
            startX = x
            startY = y
        } else if (map[y][x] == 'E') {
            endX = x
            endY = y
        }
    }
}

val start = Field(startX, startY)
val end = Field(endX, endY)

val path = mutableListOf(start)
while(path.last() != end) {
    val current = path.last()
    val next = current.getPath().let {
        if (it.size == 2) {
            it.first { field -> field != path[path.size - 2] }
        } else {
            it.first()
        }
    }
    path.add(next)
}

data class Cheat(val from: Field, val to: Field, val saved: Int)
val cheats = mutableListOf<Cheat>()

path.forEachIndexed { startPicos, field ->
    val potentialCheats = field.getCheats()
    potentialCheats.forEach {
        val targetPicos = path.indexOf(it)
        if (targetPicos - startPicos > 2) {
            cheats.add(Cheat(field, it, targetPicos - startPicos - 2))
        }
    }
}

println("Cheats saving over 100 picos: ${cheats.filter { it.saved >= 100 }.size}")

val longCheatCounts = mutableMapOf<Int, Long>()
path.forEachIndexed { index, field ->
    val remainingPath = path.drop(index + 1)
    val cheatEnds = remainingPath.filter { it.dist(field) <= 20 }
    cheatEnds.forEach { cheatEnd ->
        val saved = path.indexOf(cheatEnd) - index - cheatEnd.dist(field)
        longCheatCounts.compute(saved) {_, v -> (v ?: 0L) + 1}
    }
}
println("Loooong cheats saving over 100 picos: ${longCheatCounts.filterKeys { it >= 100 }.values.sum()}")




