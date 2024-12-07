import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    190: 10 19
    3267: 81 40 27
    83: 17 5
    156: 15 6
    7290: 6 8 6 15
    161011: 16 10 13
    192: 17 8 14
    21037: 9 7 18 13
    292: 11 6 16 20
""".trimIndent()

data class Equation(
    val result: Long,
    val values: List<Long>
) {
    companion object {
        fun fromLine(line: String): Equation {
            val result = line.substringBefore(':').toLong()
            val values = line.substringAfter(':').trim().split(' ').map { it.toLong() }
            return Equation(result, values)
        }
    }

    fun solvable(): Boolean {
        return if (values.size == 1) {
            values.first() == result
        } else {
            val subtractedSolvable = Equation(result - values.last(), values.subList(0, values.size - 1)).solvable()
            val dividedSolvable = if(result.mod(values.last()) == 0L) {
                Equation(result / values.last(), values.subList(0, values.size - 1)).solvable()
            } else {
                false
            }
            dividedSolvable || subtractedSolvable
        }
    }

    fun solvableWithConcatenation(): Boolean {
        return if (values.size == 1) {
            values.first() == result
        } else {
            val subtractedSolvable = if (result - values.last() >= 0) {
                Equation(result - values.last(), values.subList(0, values.size - 1)).solvableWithConcatenation()
            } else {
                false
            }
            val dividedSolvable = if(result.mod(values.last()) == 0L) {
                Equation(result / values.last(), values.subList(0, values.size - 1)).solvableWithConcatenation()
            } else {
                false
            }

            val resultAsString = result.toString()
            val concatenationSolvable = if (resultAsString.length > values.last().toString().length) {
                val end = resultAsString.drop(resultAsString.length - values.last().toString().length).toLong()
                if (end == values.last()) {
                    val newResult = resultAsString.dropLast(values.last().toString().length).toLong()
                    Equation(newResult, values.subList(0, values.size - 1)).solvableWithConcatenation()
                } else {
                    false
                }
            } else {
                false
            }
            dividedSolvable || subtractedSolvable || concatenationSolvable
        }
    }
}

val fullInput = Path("./input.txt").readLines()

val equations = fullInput.map(Equation.Companion::fromLine)
val (solvables, unsolvables) = equations.partition { it.solvable() }
val sum = solvables.sumOf { it.result }
println("Sum of solveable equations $sum")

val solvableWithConcat = unsolvables.filter { it.solvableWithConcatenation() }
val sum2 = solvableWithConcat.sumOf { it.result }
println("Total solvable sum including || ${sum + sum2}")