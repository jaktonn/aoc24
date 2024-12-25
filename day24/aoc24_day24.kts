import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.io.path.Path
import kotlin.io.path.readLines

val testWires = """
    x00: 1
    x01: 0
    x02: 1
    x03: 1
    x04: 0
    y00: 1
    y01: 1
    y02: 1
    y03: 1
    y04: 1
""".trimIndent()

val testGates = """
    ntg XOR fgs -> mjb
    y02 OR x01 -> tnw
    kwq OR kpj -> z05
    x00 OR x03 -> fst
    tgd XOR rvg -> z01
    vdt OR tnw -> bfw
    bfw AND frj -> z10
    ffh OR nrd -> bqk
    y00 AND y03 -> djm
    y03 OR y00 -> psh
    bqk OR frj -> z08
    tnw OR fst -> frj
    gnj AND tgd -> z11
    bfw XOR mjb -> z00
    x03 OR x00 -> vdt
    gnj AND wpb -> z02
    x04 AND y00 -> kjc
    djm OR pbm -> qhw
    nrd AND vdt -> hwm
    kjc AND fst -> rvg
    y04 OR y02 -> fgs
    y01 AND x02 -> pbm
    ntg OR kjc -> kwq
    psh XOR fgs -> tgd
    qhw XOR tgd -> z09
    pbm OR djm -> kpj
    x03 XOR y03 -> ffh
    x00 XOR y04 -> ntg
    bfw OR bqk -> z06
    nrd XOR fgs -> wpb
    frj XOR qhw -> z04
    bqk OR frj -> z07
    y03 OR x01 -> nrd
    hwm AND bqk -> z03
    tgd XOR rvg -> z12
    tnw OR pbm -> gnj
""".trimIndent()

//val startWires = testWires.lines()
val startWires = Path("./wires.txt").readLines()
val wireMap = startWires.associate {
    val (wire, value) = it.split(":").map(String::trim)
    wire to value.toByte()
}.toMutableMap()



data class Gate(val inWire1: String, val inWire2: String, val opName: String, val op: (b1: Byte, b2: Byte) -> Byte, val outWire: String) {
    companion object {
        fun fromLine(line: String): Gate {
            val functionElements = line.substringBefore("->").split(" ").map(String::trim)
            val outWire = line.substringAfter("->").trim()
            val operation = when(functionElements[1]) {
                "AND" -> Byte::and
                "OR" -> Byte::or
                "XOR" -> Byte::xor
                else -> throw IllegalArgumentException("unknown logical operation")
            }
            return Gate(functionElements[0], functionElements[2], functionElements[1], operation, outWire)
        }
    }
}

//val gates = testGates.lines()
val gates = Path("./gates.txt").readLines()
val gateList = gates.map { Gate.fromLine(it) }

val zMap = mutableMapOf<String, Byte>()

val gatesToCheck = gateList.toMutableList()
while(gatesToCheck.isNotEmpty()) {
    val gate = gatesToCheck.removeFirst()
    if (wireMap.containsKey(gate.inWire1) && wireMap.containsKey(gate.inWire2)) {
        val outValue = gate.op.invoke(wireMap.getValue(gate.inWire1), wireMap.getValue(gate.inWire2))
        if (gate.outWire.startsWith("z")) {
            zMap[gate.outWire] = outValue
        } else {
            wireMap[gate.outWire] = outValue
        }
    } else {
        gatesToCheck.addLast(gate)
    }
}

val result = zMap.entries.sortedBy { it.key }.foldIndexed(0L) { i, acc, (_, bit) -> acc or (bit.toLong() shl i) }
println(result)

val gatesByOut = gateList.associateBy { it.outWire }
val zWires = gateList.filter { it.outWire.startsWith("z") }.map { it.outWire }.sorted()

data class GateTreeNode(val gate: Gate, var gatesBefore: Set<GateTreeNode>) {
    fun buildRecursive() {
        gatesBefore = listOf(gatesByOut[gate.inWire1], gatesByOut[gate.inWire2]).filterNotNull().map {
            GateTreeNode(it, emptySet()).also { it.buildRecursive() } }.toSet()
    }

    fun getReadableTerm(): String {
        val terms = if (gatesBefore.isNotEmpty()) {
            gatesBefore.map { it ->
                "( ${it.gate.outWire}=${it.getReadableTerm()} )"
            }
        } else {
            listOf(gate.inWire1, gate.inWire2)
        }

        return "${terms[0]} ${gate.opName} ${terms[1]}"
    }
}


val gateMap = gatesByOut.toMutableMap()
val wirefunc = mutableMapOf<String, String>()

// SUMXX = XORXX XOR CARRYX-1
// CARRYXX = ANDXX OR CARRYANDX
// CARRYANDXX = CARRYX-1 AND XORXX

fun getExpected(func: String, level: Int): Set<String> {
    return if (func == "XOR") {
        when (level) {
            0 -> emptySet()
            1 -> setOf("XOR01", "AND00")
            else -> setOf("XOR${level.toString().padStart(2, '0') }", "CARRY${(level - 1).toString().padStart(2, '0') }")
        }
    } else if (func == "OR") {
        setOf("AND${level.toString().padStart(2, '0') }", "CARRYAND${level.toString().padStart(2, '0') }" )
    } else {
        when (level) {
            0 -> emptySet()
            1 -> setOf("XOR01", "AND00")
            else -> setOf("CARRY${(level - 1).toString().padStart(2, '0')}", "XOR${level.toString().padStart(2, '0') }")
        }
    }
}


fun functionName(func: String, level: Int, outwire: String, inFunctions: Set<String>): String {
   // println("$func $level $outwire $inFunctions")
    return if (func == "XOR") {
        if (getExpected(func, level) == inFunctions) {
            "SUM${level.toString().padStart(2, '0')}"
        } else {
            "ERROR${level.toString().padStart(2, '0')}"
        }
    } else if (func == "OR") {
        if (inFunctions == getExpected(func, level)) {
            "CARRY${level.toString().padStart(2, '0')}"
        } else {
            "ERROR${level.toString().padStart(2, '0')}"
        }
    } else if (func == "AND") {
        if (inFunctions == getExpected(func, level)) {
            "CARRYAND${level.toString().padStart(2, '0')}"
        } else {
            "ERROR${level.toString().padStart(2, '0')}"
        }
    } else {
        "ERROR${level.toString().padStart(2, '0')}"
    }
}


fun findWireFunction(wire: String): String {
    return if (wirefunc.containsKey(wire)) {
        wirefunc.getValue(wire)
    } else {
        val gate = gateMap.getValue(wire)
        if (gate.inWire1[0] in listOf('x', 'y') && gate.inWire2[0] in listOf('x', 'y')) {
            val level = gate.inWire1.substring(1)
            wirefunc[wire] = gate.opName + level
            gate.opName + level
        } else {
            val functions = listOf(gate.inWire1, gate.inWire2).map { findWireFunction(it) }.toSet()
            val level = functions.maxOf { it.takeLast(2).toInt() }
            val name = functionName(gate.opName, level, gate.outWire, functions)
            wirefunc[wire] = name
            name
        }
    }
}


val toSwap = mutableListOf<String>()
var correct = false
do {
    for (i in 1..zWires.lastIndex - 1) {
        val wire = zWires[i]
        findWireFunction(wire)
    }
    if (wirefunc.filterKeys { it.startsWith("z") }.all { (wire, func) -> func == "SUM${wire.takeLast(2)}" }) {
        correct = true
    } else {
        val lowestError = wirefunc.filterKeys { it.startsWith("z") }.filter { (wire, func) -> func != "SUM${wire.takeLast(2)}" }.minBy { (wire, _) -> wire.takeLast(2).toInt() }
        if (lowestError.value.startsWith("ERROR")) {
            val gate = gateMap.getValue(lowestError.key)
            val expected = getExpected(gate.opName, lowestError.key.takeLast(2).toInt())
            val (correct, wrong) = listOf(gate.inWire2, gate.inWire1).map { it to wirefunc.getValue(it) }.partition { expected.contains(it.second) }
            val wanted = (expected - correct.first().second)
            val swapWith = wirefunc.entries.first { (_, func) -> func == wanted.first() }
            val fixedGate1 = gateMap.getValue(wrong.first().first).copy(outWire = swapWith.key)
            val fixedGate2 = gateMap.getValue(swapWith.key).copy(outWire = wrong.first().first)
            gateMap[fixedGate1.outWire] = fixedGate1
            gateMap[fixedGate2.outWire] = fixedGate2
            toSwap.add(wrong.first().first)
            toSwap.add(swapWith.key)
        } else {
            val wanted = "SUM${lowestError.key.takeLast(2)}"
            val swapWith = wirefunc.entries.first { (_, func) -> func == wanted }
            toSwap.add(lowestError.key)
            toSwap.add(swapWith.key)
            val fixedGate1 = gateMap.getValue(lowestError.key).copy(outWire = swapWith.key)
            val fixedGate2 = gateMap.getValue(swapWith.key).copy(outWire = lowestError.key)
            gateMap[swapWith.key] = fixedGate1
            gateMap[lowestError.key] = fixedGate2
        }
        wirefunc.clear()
    }
} while (!correct)

println("These wires must be swapped: ${toSwap.sorted().joinToString(",")}")