import kotlin.io.path.Path
import kotlin.io.path.readLines

val testRulesInput = """
    47|53
    97|13
    97|61
    97|47
    75|29
    61|13
    75|53
    29|13
    97|29
    53|29
    61|53
    97|53
    61|29
    47|13
    75|47
    97|75
    47|61
    75|61
    47|29
    75|13
    53|13
""".trimIndent()

val testPagesInput = """
    75,47,61,53,29
    97,61,53,29,13
    75,29,13
    75,97,47,61,53
    61,13,29
    97,13,75,29,47
""".trimIndent()

val fullRulesInput = Path("./rules.txt").readLines()
//val rules = testRulesInput.lines()
val rules = fullRulesInput

val successorRules = rules.map { it.split('|') }.groupBy({it[0].toInt()}, {it[1].toInt()})
val predecessorRules = rules.map { it.split('|') }.groupBy({it[1].toInt()}, {it[0].toInt()})

fun checkLine(pages: List<Int>): Boolean {
    for (i in pages.indices) {
        val pagesBefore = pages.subList(0, i)
        val pagesAfter = pages.subList(i+1, pages.size)
        if (pagesBefore.any { successorRules.get(pages[i])?.contains(it) ?: false}) {
            return false
        }
        if (pagesAfter.any { predecessorRules.get(pages[i])?.contains(it)?: false}) {
            return false
        }
    }
    return true
}

fun findCorrectPageOrder(reordered: List<Int>, remaining: List<Int>): List<Int> {
    if (remaining.isEmpty()) {
        return reordered
    }
    val candidates = remaining.filter {
        val successors = remaining - it
        predecessorRules.get(it)?.none { predessor -> successors.contains(predessor)} ?: true &&
                successorRules.get(it)?.none { successor -> reordered.contains(successor)} ?: true
    }
    return candidates.map { findCorrectPageOrder(reordered + it, remaining - it) }.filter { it.isNotEmpty() }.first()
}

val fullPagesInput = Path("./pages.txt").readLines()
//val pages = testPagesInput.lines()
val pages = fullPagesInput

val (correctPages, wrongPages) = pages.map { it.split(',').map { it.toInt() }}.partition { checkLine(it) }
val sumOfMiddles = correctPages.sumOf { it[it.size/2] }
println("Part 1 $sumOfMiddles")

val reordered = wrongPages.map { findCorrectPageOrder(emptyList(), it) }
val sumOfReorderedMiddles = reordered.sumOf { it[it.size/2] }
println("Part 2 $sumOfReorderedMiddles")
