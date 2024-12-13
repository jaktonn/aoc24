import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    Button A: X+94, Y+34
    Button B: X+22, Y+67
    Prize: X=8400, Y=5400

    Button A: X+26, Y+66
    Button B: X+67, Y+21
    Prize: X=12748, Y=12176

    Button A: X+17, Y+86
    Button B: X+84, Y+37
    Prize: X=7870, Y=6450

    Button A: X+69, Y+23
    Button B: X+27, Y+71
    Prize: X=18641, Y=10279
""".trimIndent()

data class Solution(
    val a: Long,
    val b: Long
) {
    val cost by lazy {
        a * 3 + b
    }
}

data class ClawMachine(
    val aPlusX: Long,
    val aPlusY: Long,
    val bPlusX: Long,
    val bPlusY: Long,
    val priceX: Long,
    val priceY: Long
) {

    fun findTokensToSolve(): Long? {
        val solutions = mutableListOf<Solution>()
        for (a in 0L..100L) {
            val x = a * aPlusX
            val y = a * aPlusY
            if (x > priceX || y > priceY) {
                break
            }
            if (
                    (priceX - x) % bPlusX == 0L &&
                    (priceY - y) % bPlusY == 0L
                ) {
                val b = (priceX - x) / bPlusX
                if (b in 0..100 && priceY == y + b * bPlusY ) {
                    solutions.add(Solution(a, b))
                }
            }
        }
        return solutions.minOfOrNull { it.cost }
    }

    fun solveMathematical(): Long? {
        return if ((priceY * aPlusX - priceX * aPlusY) % (aPlusX * bPlusY - bPlusX * aPlusY) == 0L) {
            val b = (priceY * aPlusX - priceX * aPlusY) / (aPlusX * bPlusY - bPlusX * aPlusY)
            val a = (priceX - b * bPlusX) / aPlusX
            if (a >= 0 && b >= 0) {
                Solution(a, b).cost
            } else {
                null
            }
        } else {
            null
        }
    }

    fun corrected(): ClawMachine {
        return ClawMachine(
            aPlusX = aPlusX,
            bPlusX=bPlusX,
            aPlusY = aPlusY,
            bPlusY = bPlusY,
            priceX = priceX + 10000000000000L,
            priceY = priceY + 10000000000000L
        )
    }

}

//val input = testInput.lines()
val input = Path("./machines.txt").readLines()

val machines = input.filter { it.isNotBlank() }.windowed(3, 3)
    .map { machineLines ->
        val factorsA = machineLines[0].substringAfter("A: ").split(',')
        val aPlusX = factorsA[0].substringAfter('+').toLong()
        val aPlusY = factorsA[1].substringAfter('+').toLong()
        val factorsB = machineLines[1].substringAfter("B: ").split(',')
        val bPlusX = factorsB[0].substringAfter('+').toLong()
        val bPlusY = factorsB[1].substringAfter('+').toLong()
        val prizePositions = machineLines[2].substringAfter(": ").split(',')
        val priceX = prizePositions[0].substringAfter('=').toLong()
        val priceY = prizePositions[1].substringAfter('=').toLong()
        ClawMachine(aPlusX, aPlusY, bPlusX, bPlusY, priceX, priceY)
    }

val costsToWin = machines.map { it.findTokensToSolve()?.toLong() }.filterNotNull().sum()
println("Winning all costs $costsToWin")

val fixedMachines = machines.map { it.corrected() }
val costsToWin2 = fixedMachines.map { it.solveMathematical() }.filterNotNull().sum()
println("Costs to win part 2 $costsToWin2")
