import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    ............
    ........0...
    .....0......
    .......0....
    ....0.......
    ......A.....
    ............
    ............
    ........A...
    .........A..
    ............
    ............
""".trimIndent()

//val map = testInput.lines()
val map = Path("./map.txt").readLines()

data class Position(val x: Int, val y: Int) {
    operator fun plus(vector2: Vector2): Position {
        return Position(x + vector2.deltaX, y + vector2.deltaY)
    }

    fun inside(): Boolean {
        return (x in map.indices && y in map[0].indices)
    }

    fun dist(otherPosition: Position): Vector2 {
        return Vector2(otherPosition.x - x, otherPosition.y - y)
    }
}

data class Vector2(val deltaX: Int, val deltaY: Int) {
    fun negate(): Vector2 {
        return Vector2(-deltaX, -deltaY)
    }
}

var antennaMap = mutableMapOf<Char, List<Position>>()
map.forEachIndexed { y, row ->
    row.forEachIndexed { x, ch ->
        if (ch != '.') {
            antennaMap.compute(ch) { _, positions ->
                (positions ?: emptyList()) + Position(x, y)
            }
        }
    }
}

// Part 1
val antinodes = mutableSetOf<Position>()
antennaMap.values.forEach { antennas ->
    for (i in 0..<antennas.lastIndex) {
        val antenna = antennas[i]
        val otherAntennas = antennas.drop(i+1)
        otherAntennas.forEach { other ->
            val dist = antenna.dist(other)
            antinodes.add(other.plus(dist))
            antinodes.add(antenna.plus(dist.negate()))
        }
    }
}
println("Antinodes with first model ${antinodes.count { it.inside() }}")

// Part 2
val moreAntinodes = mutableSetOf<Position>()
antennaMap.values.forEach { antennas ->
    for (i in 0..<antennas.lastIndex) {
        val antenna = antennas[i]
        val otherAntennas = antennas.drop(i + 1)
        otherAntennas.forEach { other ->
            moreAntinodes.add(antenna)
            moreAntinodes.add(other)
            val dist = antenna.dist(other)
            var nextAntinode = other.plus(dist)
            while (nextAntinode.inside()) {
                moreAntinodes.add(nextAntinode)
                nextAntinode += dist
            }
            nextAntinode = antenna.plus(dist.negate())
            while (nextAntinode.inside()) {
                moreAntinodes.add(nextAntinode)
                nextAntinode += dist.negate()
            }
        }
    }
}
println("Antinodes with second model ${moreAntinodes.count()}")



