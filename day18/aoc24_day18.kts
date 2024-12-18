import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.min

val testInput = """
    5,4
    4,2
    4,5
    3,0
    2,1
    6,3
    2,4
    1,5
    0,6
    3,3
    2,6
    5,1
    1,2
    5,5
    2,5
    6,5
    1,4
    0,4
    6,4
    1,1
    6,1
    1,0
    0,5
    1,6
    2,0
""".trimIndent()


//val input = testInput.lines()
val input = Path("./input.txt").readLines()
//val fieldSize = 7
val fieldSize = 71
//val bytes = 12
val bytes = 1024

data class Field(val x: Int, val y: Int)

val obstacles = input.take(bytes).map {
    val (x, y) = it.split(",").map { it.toInt() }
    Field(x, y)
}

data class FieldWithScore(val field: Field, val score: Int) {
    constructor(entry: Map.Entry<Field, Int>): this(entry.key, entry.value)

    fun nextFields(): List<FieldWithScore> {
        return listOf(
            FieldWithScore(field = Field(x = field.x - 1, y = field.y), score + 1 ),
            FieldWithScore(field = Field(x = field.x + 1, y = field.y), score + 1),
            FieldWithScore(field = Field(x = field.x, y = field.y - 1), score + 1),
            FieldWithScore(field = Field(x = field.x, y = field.y + 1), score + 1)
        ).filter { it.field.x in 0..<fieldSize && it.field.y in 0..<fieldSize }
    }
}

val start = Field(0, 0)
val end = Field(fieldSize - 1, fieldSize - 1)

val unvisited = (0..<fieldSize).flatMap { x ->
    (0..<fieldSize).map { y ->
        val field = Field(x, y)
        val score = if(field == start) 0 else Int.MAX_VALUE
        field to score
    }
}.toMap().toMutableMap()

var field = FieldWithScore(unvisited.entries.minBy { it.value })
while (field.field != end) {
    unvisited.remove(field.field)
    val nexts = field.nextFields().filterNot { obstacles.contains(it.field) }
    nexts.forEach {
        unvisited.computeIfPresent(it.field) { _, v -> min(v, it.score) }
    }
    field = FieldWithScore(unvisited.entries.minBy { it.value })
}

println("Path to exit after $bytes bytes have fallen: ${field.score}")

var obstacles2 = mutableSetOf<Field>()
obstacles2.addAll(obstacles)

for (i in bytes..input.lastIndex) {
    val (x, y) = input[i].split(",").map { it.toInt() }
    obstacles2.add(Field(x, y))

    val unvisited2 = (0..<fieldSize).flatMap { x ->
        (0..<fieldSize).map { y ->
            val field = Field(x, y)
            val score = if(field == start) 0 else Int.MAX_VALUE
            field to score
        }
    }.toMap().toMutableMap()

    var field = FieldWithScore(unvisited2.entries.minBy { it.value })
    while (field.field != end && field.score < Integer.MAX_VALUE) {
        unvisited2.remove(field.field)
        val nexts = field.nextFields().filterNot { obstacles2.contains(it.field) }
        nexts.forEach {
            unvisited2.computeIfPresent(it.field) { _, v -> min(v, it.score) }
        }
        field = FieldWithScore(unvisited2.entries.minBy { it.value })
    }
    if (field.score == Integer.MAX_VALUE) {
        println("Blocking obstacle at $x,$y")
        break;
    }
}

