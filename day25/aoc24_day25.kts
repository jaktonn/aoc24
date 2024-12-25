import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    #####
    .####
    .####
    .####
    .#.#.
    .#...
    .....

    #####
    ##.##
    .#.##
    ...##
    ...#.
    ...#.
    .....

    .....
    #....
    #....
    #...#
    #.#.#
    #.###
    #####

    .....
    .....
    #.#..
    ###..
    ###.#
    ###.#
    #####

    .....
    .....
    .....
    #....
    #.#..
    #.#.#
    #####
""".trimIndent()

//val input = testInput.lines()
val input = Path("./input.txt").readLines()

val keys = mutableSetOf<List<Int>>()
val locks = mutableSetOf<List<Int>>()

fun pivot(matrix: List<String>): List<String> {
    val newLines = mutableListOf<String>()
    for (x in matrix[0].indices) {
        var line = ""
        for (y in matrix.indices) {
            line += matrix[y][x]
        }
        newLines.add(line)
    }
    return newLines
}

input.filter { it.isNotEmpty() }.windowed(7, 7).forEach { window ->
    val pivoted = pivot(window)
    val combination = pivoted.map { it.count { c -> c == '#' } - 1 }
    if (window[0] == "#####") {
        //lock
        locks.add(combination)
    } else {
        // key
        keys.add(combination)
    }
}

fun fits(lock: List<Int>, key: List<Int>): Boolean {
    return lock.foldIndexed(true){ i, acc, pin -> acc and (key[i] + pin <= 5)}
}

val uniqueCombinations = locks.fold(0) {acc, lock -> acc + keys.count { key -> fits(lock, key) } }
println("Unique lock - key combinations: $uniqueCombinations")