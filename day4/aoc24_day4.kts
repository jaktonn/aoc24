import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
MMMSXXMASM
MSAMXMSMSA
AMXSXMAAMM
MSAMASMSMX
XMASAMXAMM
XXAMMXXAMA
SMSMSASXSS
SAXAMASAAA
MAMMMXMMMM
MXMXAXMASX
""".trimIndent()

fun countXmasOccurrences(line: String): Int {
    val windows = line.windowed(4)
    return windows.filter { it == "XMAS" || it == "SAMX"}.size
}

fun columns(lines: List<String>): List<String> {
    val columns = mutableListOf<String>()
    for (column in 0..lines[0].length-1) {
        var colLine = ""
        for (row in 0..lines.size-1) {
            colLine += lines[row][column]
        }
        columns.add(colLine)
    }
    return columns
}

fun shiftStringLeft(line: String, count: Int): String {
    val insert = ".".repeat(count)
    return line.substring(count) + insert
}

fun shiftStringRight(line: String, count: Int): String {
    val insert = ".".repeat(count)
    return insert + line.substring(0, line.length-1-count)
}

fun diagonals(lines: List<String>): List<String> {
    val diagonals = mutableListOf<String>()
    for (column in 0..lines[0].length-1) {
        var diagonalLeft = ""
        var diagonalRight = ""
        for (row in 0..lines.size-1) {
            if (column < lines[row].length-row) {
                diagonalRight += lines[row][column+row]
            }
            if (column - row >= 0) {
                diagonalLeft += lines[row][column - row]
            }
        }
        diagonals.add(diagonalLeft)
        diagonals.add(diagonalRight)
    }
    for (row in 1..lines.size-1) {
        var diagonalLeft = ""
        var diagonalRight = ""
        for (row2 in row..lines.size-1) {
            if (row2-row < lines[row2].length) {
                diagonalRight += lines[row2][row2-row]
            }
            if (row2-row >= 0) {
                diagonalLeft += lines[row2][lines[row2].length-1-row2+row]
            }
        }
        diagonals.add(diagonalRight)
        diagonals.add(diagonalLeft)
    }
    return diagonals
}

fun windowSearch(lines: List<String>): Int {
    var count = 0
    for (row in 0..lines.size-3) {
        for (column in 0..lines[0].length-3) {
            val diagonal1 = lines[row][column].toString() + lines[row+1][column+1].toString() + lines[row+2][column+2].toString()
            val diagonal2 = lines[row][column+2].toString() + lines[row+1][column+1].toString() + lines[row+2][column].toString()
            if ((diagonal1 == "MAS" || diagonal1 == "SAM") && (diagonal2 == "MAS" || diagonal2 == "SAM")) {
                count++
            }
        }
    }
    return count
}

//val input = testInput.lines()
val input = Path("./input.txt").readLines()
val matches = (input + columns(input) + diagonals(input)).map { line -> countXmasOccurrences(line) }.sum()
println(matches)

val xpatternmatches = windowSearch(input)
println("X-pattern-matches $xpatternmatches")