import kotlin.math.abs

val testCodes = """
    029A
    980A
    179A
    456A
    379A
""".trimIndent()

val realCodes = """
    413A
    480A
    682A
    879A
    083A
""".trimIndent()

//val codes = testCodes.lines()
val codes = realCodes.lines()

data class Field(val x: Int, val y: Int, val key: Char) {
    fun manhattanDistance(other: Field): Int {
        return abs(other.x - x ) + abs(other.y - y)
    }

    fun neighbours(others: List<Field>): List<Field> {
        return others.filter { it.manhattanDistance(this) == 1 }
    }
}

val numericKeyPad = listOf(
    Field(0, 0, '7'),
    Field(1, 0, '8'),
    Field(2, 0, '9'),
    Field(0, 1, '4'),
    Field(1, 1, '5'),
    Field(2, 1, '6'),
    Field(0, 2, '1'),
    Field(1, 2, '2'),
    Field(2, 2, '3'),
    Field(1, 3, '0'),
    Field(2, 3, 'A')
).associateBy { it.key }

fun toMotionSequence(keySequence: String, keyPad: Map<Char, Field>): String {
    return keySequence.windowed(2).map {
        val startField = keyPad.getValue(it[0])
        val endField = keyPad.getValue(it[1])
        if (startField == endField) {
            ""
        } else if (startField.x == endField.x) {
            val yDist = endField.y - startField.y
            if (yDist > 0) {
                "v"
            } else {
                "^"
            }
        } else {
            val xDist = endField.x - startField.x
            if (xDist > 0) {
                ">"
            } else {
                "<"
            }
        }
    }.joinToString("") + "A"
}

val numericPathMap = mutableMapOf<String, Set<String>>()
numericKeyPad.values.forEach { field ->
    numericPathMap["${field.key}${field.key}"] = setOf("A")
}
numericKeyPad.values.forEach { field ->
    var others = numericKeyPad.values - field
    val fieldsToCheck = field.neighbours(others).map { "${field.key}${it.key}" to it }.toMutableList()
    while (fieldsToCheck.isNotEmpty()) {
        val (path, fieldToCheck) = fieldsToCheck.removeFirst()
        others = others - fieldToCheck
        val keyCombination = "${field.key}${fieldToCheck.key}"
        val motionSequence = toMotionSequence(path, numericKeyPad)
        if (numericPathMap.containsKey(keyCombination)) {
            val foundPaths = numericPathMap.getValue(keyCombination)
            if (!foundPaths.contains(motionSequence)) {
                if (foundPaths.first().length == motionSequence.length) {
                    numericPathMap[keyCombination] = foundPaths + motionSequence
                } else if (foundPaths.first().length > motionSequence.length) {
                    numericPathMap[keyCombination] = setOf(motionSequence)
                }
            }
        } else {
            numericPathMap[keyCombination] = setOf(motionSequence)
        }
        fieldsToCheck.addAll(fieldToCheck.neighbours(others).map { path + it.key to it })
    }
}

val motionKeyPad = listOf(
    Field(1, 0, '^'),
    Field(2, 0, 'A'),
    Field(0, 1, '<'),
    Field(1, 1, 'v'),
    Field(2, 1, '>')
).associateBy { it.key }

val motionPathMap = mutableMapOf<String, Set<String>>()
motionKeyPad.values.forEach { field ->
    motionPathMap["${field.key}${field.key}"] = setOf("A")
}
motionKeyPad.values.forEach { field ->
    var others = motionKeyPad.values - field
    val fieldsToCheck = field.neighbours(others).map { "${field.key}${it.key}" to it }.toMutableList()
    while (fieldsToCheck.isNotEmpty()) {
        val (path, fieldToCheck) = fieldsToCheck.removeFirst()
        others = others - fieldToCheck
        val keyCombination = "${field.key}${fieldToCheck.key}"
        val motionSequence = toMotionSequence(path, motionKeyPad)
        if (motionPathMap.containsKey(keyCombination)) {
            val foundPaths = motionPathMap.getValue(keyCombination)
            if (!foundPaths.contains(motionSequence)) {
                if (foundPaths.first().length == motionSequence.length) {
                    motionPathMap[keyCombination] = foundPaths + motionSequence
                } else if (foundPaths.first().length > motionSequence.length) {
                    motionPathMap[keyCombination] = setOf(motionSequence)
                }
            }
        } else {
            motionPathMap[keyCombination] = setOf(motionSequence)
        }
        fieldsToCheck.addAll(fieldToCheck.neighbours(others).map { path + it.key to it })
    }
}

val solutionsNumericKeyPad = codes.map { code ->
    code to "A$code".windowed(2).fold(listOf("")) { list, keys ->
        list.flatMap { numericPathMap.getValue(keys).map { path -> it + path } }
    }
}


fun codeScore(code: String): Int {
    return code.filter { it in "0123456789" }.toInt()
}

val lengthforLevel = mutableMapOf<Pair<String,Int>,Long>()
fun shortestCombination(motionSequence: String, keypads: Int): Long {
    return if (keypads == 1) {
        motionSequence.length.toLong()
    } else {
        "A$motionSequence".windowed(2).map { keys ->
            if (lengthforLevel.containsKey(keys to keypads - 1)) {
                lengthforLevel.getValue(keys to keypads - 1)
            } else {
                val solution = motionPathMap.getValue(keys).map { shortestCombination(it, keypads - 1) }.min()
                lengthforLevel[keys to keypads - 1] = solution
                solution
            }
        }.sum()
    }
}

val lengthsAfter2 = motionPathMap.map { (code, solutions) -> code to solutions.map { shortestCombination(it, 2) } }.toMap()
val solutionLength = solutionsNumericKeyPad.map { (code, solutions) ->
    code to solutions.map { s ->
        "A$s".windowed(2).map { lengthsAfter2.getValue(it).min() }.sum()
    }
}.map { (code, length) -> length.min() * codeScore(code)}.sum()

println("Complexity sum part 1 $solutionLength")

val lengthsAfter25 = motionPathMap.map { (code, solutions) -> code to solutions.map { shortestCombination(it, 25) } }.toMap()
val solutionLength25 = solutionsNumericKeyPad.map { (code, solutions) ->
    code to solutions.map { solution ->
        "A$solution".windowed(2).map { lengthsAfter25.getValue(it).min() }.sum()
    }
}.map { (code, length) -> length.min() * codeScore(code)}.sum()
println("Complexity sum part 2 $solutionLength25")