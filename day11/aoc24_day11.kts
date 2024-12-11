val testStones = "125 17"
val input = "6571 0 5851763 526746 23 69822 9 989"
val stoneList = input

val lookupTable = mutableMapOf<Pair<Long, Int>, Long>()

data class Stone(val value: Long) {

    constructor(s: String): this(s.toLong())

    fun blink(): List<Stone> {
        return when {
            value == 0L -> listOf(Stone(1L))
            value.toString().length % 2 == 0 -> listOf(
                Stone(value.toString().dropLast(value.toString().length/2)),
                Stone(value.toString().drop(value.toString().length/2)),
            )
            else -> listOf(Stone(value * 2024L))
        }
    }

    fun blinkRecursive(times: Int): Long {
        return if (times == 0) {
            1L
        } else if (lookupTable[value to times] != null) {
            lookupTable.getValue(value to times)
        } else {
            val newStones = blink()
            val stoneSum = newStones.asSequence().map { it.blinkRecursive(times - 1 ) }.sum()
            lookupTable[value to times] = stoneSum
            stoneSum
        }
    }
}

val stones = stoneList.split(' ').map { Stone(it.trim()) }

val sum = stones.asSequence().map { it.blinkRecursive(25) }.toList()
println("Number of stones after blinking 25 times: ${sum.sum()}")

val sum75 = stones.asSequence().map { it.blinkRecursive(75) }.toList()
println("Number of stones after blinking 75 times: ${sum75.sum()}")