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

for (i in 1..program.size) {
    val wantedOutput = program.subList(0, i)
    var found = false
    var a = 0.toBigInteger()
    while (!found) {
//        println(a)
        val startA = (a shl ((i - 1) * 3)) or partialResult
//        println("$a ${startA.toString(2)}")
        cpuState = CPUState(startA, startB, startC, 0, emptyList())
        val reachedStates = mutableSetOf(cpuState)
        while (cpuState.pc < program.lastIndex) {
            val operation = forOpcode(program[cpuState.pc])
            val operand = program[cpuState.pc + 1]
            cpuState = operation.execute(cpuState, operand)
            if (reachedStates.contains(cpuState.copy(output = emptyList()))) {
                // endless loop
//                println("endless?")
                break
            }
            reachedStates.add(cpuState.copy(output = emptyList()))
            val currentOutput = cpuState.output
            if (currentOutput.isNotEmpty()) {
                if (currentOutput.size > wantedOutput.size) {
//                    println("too long")
                    break
                } else if (currentOutput.size < wantedOutput.size && currentOutput != wantedOutput.subList(
                        0,
                        currentOutput.size
                    )
                ) {
//                    println("wrong output $currentOutput")
                    break
                } else if (currentOutput.size == wantedOutput.size) {
                    if (currentOutput == wantedOutput) {
                        partialResult = (a shl ((i - 1) * 3)) or partialResult
//                        println("found $currentOutput with a=$a")
                        found = true
                        break
                    } else {
//                        println("wrong output $currentOutput")
                        break
                    }
                }
            }
        }
        a = a.inc()
    }
    println("found $i output digits")
}
println("\nProgram duplicates at $partialResult")