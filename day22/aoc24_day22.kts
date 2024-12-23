import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    1
    10
    100
    2024
""".trimIndent()

val testInput2 = """
    1
    2
    3
    2024
""".trimIndent()

//val input = listOf("123")
//val input = testInput.lines()
//val input = testInput2.lines()
val input = Path("./input.txt").readLines()

fun nextValue(value: Long): Long {
    //step 1: multiply by 64, xor, prune
    val afterStep1 = (value shl 6) xor value and 16777215
    // step 2: divide by 32, xor, prune
    val afterStep2 = (afterStep1 shr 5) xor afterStep1 and 16777215
    // step 3: multiply by 2048, xor, prune
    return (afterStep2 shl 11) xor afterStep2 and 16777215
}

val secrets = input.map {
    var secret = it.toLong()
    repeat(2000) {
        secret = nextValue(secret)
    }
    secret
}
println("Sum of secret numbers ${secrets.sum()}")

data class Sequence(val numbers: List<Int>)

val sequenceOccurences = mutableMapOf<Sequence, Int>()
input.forEach { seed ->
    val usedSequences = mutableSetOf<Sequence>()
    var secret = seed.toLong()
    val sequence = mutableListOf<Int>()
    repeat(2000) {
        val nextValue = nextValue(secret)
        val change = (nextValue % 10 - secret % 10).toInt()
        sequence.add(change)
        if (sequence.size == 4) {
            val sequenceInstance = Sequence(sequence.toList())
            if (!usedSequences.contains(sequenceInstance)) {
                usedSequences.add(sequenceInstance)
                sequenceOccurences.compute(sequenceInstance) { _, sum ->
                    (sum ?: 0) + (nextValue % 10).toInt()
                }
            }
            sequence.removeFirst()
        }
        secret = nextValue
    }
}

val mostBananas = sequenceOccurences.entries.maxBy { it.value }
println("Most bananas possible with sequence ${mostBananas.key.numbers}, earns ${mostBananas.value} bananas")


