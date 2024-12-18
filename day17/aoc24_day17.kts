import java.math.BigInteger

val testInput = """
    Register A: 729
    Register B: 0
    Register C: 0

    Program: 0,1,5,4,3,0
""".trimIndent()

val fullInput = """
    Register A: 61156655
    Register B: 0
    Register C: 0

    Program: 2,4,1,5,7,5,4,3,1,6,0,3,5,5,3,0
""".trimIndent()

val testInputB = """
    Register A: 2024
    Register B: 0
    Register C: 0

    Program: 0,3,5,4,3,0
""".trimIndent()

data class CPUState(val a: BigInteger, val b: BigInteger, val c: BigInteger, val pc: Int, val output: List<Int>) {
    fun combo(op: Int): BigInteger {
        return when (op) {
            0,1,2,3 -> op.toBigInteger()
            4 -> a
            5 -> b
            6 -> c
            else -> throw IllegalArgumentException("Invalid operand $op")
        }
    }
}

enum class Operation(val opCode: Int) {
    ADV(0) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            val power = cpuState.combo(op)
            val newA = cpuState.a shr power.toInt()
            return cpuState.copy(a = newA, pc = cpuState.pc + 2)
        }
    },
    BXL(1) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            val newB = cpuState.b xor op.toBigInteger()
            return cpuState.copy(b = newB, pc = cpuState.pc + 2)
        }
    },
    BST(2) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            val newB = cpuState.combo(op) and 7.toBigInteger()
            return cpuState.copy(b = newB, pc = cpuState.pc + 2)
        }
    },
    JNZ(3) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            return if (cpuState.a == 0.toBigInteger()) {
                cpuState.copy(pc = cpuState.pc +  2)
            } else {
                cpuState.copy(pc = op)
            }
        }
    },
    BXC(4) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            val newB = cpuState.b xor cpuState.c
            return cpuState.copy(b = newB, pc = cpuState.pc + 2)
        }
    },
    OUT(5) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            return cpuState.copy(pc = cpuState.pc + 2, output = cpuState.output + (cpuState.combo(op) and 7.toBigInteger()).toInt())
        }
    },
    BDV(6) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            val power = cpuState.combo(op)
            val newB = cpuState.a shr power.toInt()
            return cpuState.copy(b = newB, pc = cpuState.pc + 2)
        }
    },
    CDV(7) {
        override fun execute(cpuState: CPUState, op: Int): CPUState {
            val power = cpuState.combo(op)
            val newC = cpuState.a shr power.toInt()
            return cpuState.copy(c = newC, pc = cpuState.pc + 2)
        }
    };

    abstract fun execute(cpuState: CPUState, op: Int): CPUState
}

fun forOpcode(opcode: Int): Operation {
    return Operation.entries.first { it.opCode == opcode }
}

//val input = testInput.lines()
//val input = testInputB.lines()
val input = fullInput.lines()

val startA = input[0].substringAfter(':').trim().toBigInteger()
val startB = input[1].substringAfter(':').trim().toBigInteger()
val startC = input[2].substringAfter(':').trim().toBigInteger()
val program = input[4].substringAfter(':').trim().split(',').map { it.toInt() }

var cpuState = CPUState(startA, startB, startC, 0, emptyList())
while (cpuState.pc < program.lastIndex) {
    val operation = forOpcode(program[cpuState.pc])
    val operand = program[cpuState.pc + 1]
    cpuState = operation.execute(cpuState, operand)
}
println(cpuState.output.joinToString(","))

var partialResult = 0.toBigInteger()

// as the program shifts a to the right by 3 bits on each loop and has to be 0 at the end of the program,
// we're looking for program.size * 3 bits here
// strategy here is, we try finding the last output first by shifting our search value to the left the
// needed number of bits. Once found, we try finding the last two outputs by using the value from the previous
// run and try the next 3 lower bits, and so on until we found the full input.
for (i in program.lastIndex downTo 0) {
    val wantedOutput = program.subList(i, program.size)
    var found = false
    var a = 0.toBigInteger()
    while (!found) {
        val startA = (a shl (i * 3)) + partialResult
        cpuState = CPUState(startA, startB, startC, 0, emptyList())
        while (cpuState.pc < program.lastIndex) {
            val operation = forOpcode(program[cpuState.pc])
            val operand = program[cpuState.pc + 1]
            cpuState = operation.execute(cpuState, operand)
        }
        if (cpuState.output.size == program.size && cpuState.output.subList(i, program.size) == wantedOutput) {
            found = true
            partialResult = (a shl (i * 3)) + partialResult
        }
        a = a.inc()
    }
    println("found ${program.size - i} output digits")
}
println("\nProgram duplicates at $partialResult")
