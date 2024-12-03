import kotlin.io.path.Path
import kotlin.io.path.readText

val input = "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))"

val fullInput = Path("input.txt").readText()

val extractRegex = "(mul\\(\\d{1,3},\\d{1,3}\\))".toRegex()
val instructionRegex = "mul\\((\\d{1,3}),(\\d{1,3})\\)".toRegex()

val groups = extractRegex.findAll(fullInput).toList()
val sum = groups.map { it.value }
    .flatMap { instructionRegex.findAll(it).toList().map { i -> i.groupValues[1].toLong() * i.groupValues[2].toLong() } }
    .sum()

println("Part 1: $sum")

val fullInstruction = fullInput
val validInstructions = mutableListOf<String>()
validInstructions.add(fullInstruction.substring(0, fullInstruction.indexOf("don't")))
var tail = fullInstruction.substring(fullInstruction.indexOf("don't") + 4)
do {
    val doIndex = tail.indexOf("do()")
    val dontIndex = tail.indexOf("don't()", doIndex)
    val end = if (dontIndex > -1)  dontIndex+6 else tail.length - 1
    if (doIndex > -1 && end > 0) {
        validInstructions.add(tail.substring(doIndex + 4, end))
    }
    tail = tail.substring(end)
} while (doIndex > -1)

val doGroups = validInstructions.flatMap { extractRegex.findAll(it).toList().map { it.value } }
val sum2 = doGroups
    .flatMap { instructionRegex.findAll(it).toList().map { i -> i.groupValues[1].toLong() * i.groupValues[2].toLong() } }
    .sum()
println("Part2: $sum2")

