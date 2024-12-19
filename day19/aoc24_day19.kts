import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    r, wr, b, g, bwu, rb, gb, br

    brwrr
    bggr
    gbbr
    rrbgbr
    ubwu
    bwurrg
    brgr
    bbrgwb
""".trimIndent()

//val input = testInput.lines()
val input = Path("./input.txt").readLines()

val towelPatterns = input[0].split(',').map { it.trim() }
val wantedDesigns = input.drop(2)

var possible = 0
wantedDesigns.forEach { design ->
    val remainingDesigns = mutableListOf(design)
    while(remainingDesigns.isNotEmpty()) {
        val remainingDesign = remainingDesigns.removeFirst()
        if (remainingDesign.isEmpty()) {
            possible += 1
            break
        }
        val nextDesigns = towelPatterns.filter { remainingDesign.startsWith(it) }.map { remainingDesign.substring(it.length) }
        remainingDesigns.addAll(nextDesigns)
        remainingDesigns.sortBy { it.length }
    }
}
println("Possible designs: $possible")

val solutionTable = mutableMapOf<String, Long>()
fun findSolution(remainingPattern: String): Long {
    return if (remainingPattern.isEmpty()) {
        1L
    } else if (solutionTable.containsKey(remainingPattern)) {
        solutionTable.getValue(remainingPattern)
    } else {
        val newCandidates = towelPatterns.filter { remainingPattern.startsWith(it) }.map { remainingPattern.substring(it.length) }
        newCandidates.map { it to findSolution(it) }.onEach { solutionTable[it.first] = it.second }.sumOf { it.second }
    }
}

val totalSolutions = wantedDesigns.map { findSolution(it) }.sum()

println("Total possible solutions $totalSolutions")