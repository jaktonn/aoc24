import kotlin.io.path.Path
import kotlin.io.path.readLines

val testMap = """
    RRRRIICCFF
    RRRRIICCCF
    VVRRRCCFFF
    VVRCCCJFFF
    VVVVCJJCFE
    VVIVCCJJEE
    VVIIICJJEE
    MIIIIIJJEE
    MIIISIJEEE
    MMMISSJEEE
""".trimIndent()

//val map = testMap.lines()
val map = Path("./map.txt").readLines()

data class Field(val x: Int, val y: Int) {

    val crop = if (x in map[0].indices && y in map.indices) {
        map[y][x]
    } else {
        '.'
    }

    fun leftIsSame(): Boolean {
        return Field(x-1, y).crop == crop
    }

    fun upIsSame(): Boolean {
        return Field(x, y - 1).crop == crop
    }

    fun isNeighbour(other: Field): Boolean {
        return if (other.x == x) {
            other.y == y - 1 || other.y == y + 1
        } else if (other.y == y) {
            other.x == x - 1 || other.x == x + 1
        } else {
            false
        }
    }

    val fence: Set<Fence> by lazy {
        val fence = mutableSetOf<Fence>()
        if (Field(x-1, y).crop != crop) {
            fence.add(Fence(x, y, FencePosition.LEFT))
        }
        if (Field(x+1, y).crop != crop) {
            fence.add(Fence(x, y, FencePosition.RIGHT))
        }
        if (Field(x, y-1).crop != crop) {
            fence.add(Fence(x, y, FencePosition.TOP))
        }
        if (Field(x, y+1).crop != crop) {
            fence.add(Fence(x, y, FencePosition.BOTTOM))
        }
        fence.toSet()
    }
}

enum class FencePosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

data class Fence(val x: Int, val y: Int, val position: FencePosition) {
    fun isConnected(other: Fence): Boolean {
        return if (other.position == position) {
            when (position) {
                FencePosition.LEFT, FencePosition.RIGHT -> other.x == x && (other.y == y - 1 || other.y == y + 1)
                FencePosition.TOP, FencePosition.BOTTOM -> other.y == y && (other.x == x - 1 || other.x == x + 1)
            }
        } else {
            false
        }
    }
}

val groups = mutableListOf<MutableSet<Field>>()
val checkedFields = mutableSetOf<Field>()
for (y in map.indices) {
    for (x in map[0].indices) {
        val field = Field(x, y)
        if (field.leftIsSame()) {
            groups.first { it.contains(Field(x-1, y)) }.add(field)
        } else if (field.upIsSame()) {
            groups.first { it.contains(Field(x, y-1)) }.add(field)
        } else {
            groups.add(mutableSetOf(field))
        }
    }
}
var compactedGroups = groups.map { it.toSet() }.toList().toMutableList()
val newGroups = mutableListOf<Set<Field>>()
do {
    var merged = false
    newGroups.clear()
    while (compactedGroups.isNotEmpty()) {
        val group = compactedGroups.removeFirst()
        val mergedGroup = mutableSetOf<Field>()
        mergedGroup.addAll(group)
        group.forEach { field ->
            val neighbourGroups = compactedGroups.filter { it.any { candidateField -> candidateField.crop == field.crop && candidateField.isNeighbour(field)  } }
            if (neighbourGroups.isNotEmpty()) {
                merged = true
                mergedGroup.addAll(neighbourGroups.flatten())
                compactedGroups.removeAll(neighbourGroups)
            }
        }
        newGroups.add(mergedGroup)
    }
    compactedGroups = newGroups.toList().toMutableList()
} while (merged)

println("Found all groups")

val fenceCosts = newGroups.map { group -> group.map { field -> field.fence.size }.sum() * group.size }.sum()
println("Fence costs: $fenceCosts")

val fences = newGroups.map { group -> group.flatMap { field -> field.fence } }
val groupedFences = fences.map { fence ->
    var currentFenceList = fence.map { listOf(it) }.toMutableList()
    val fenceSegments = mutableListOf<List<Fence>>()
    do {
        var merged = false
        fenceSegments.clear()
        while (currentFenceList.isNotEmpty()) {
            val currentFence = currentFenceList.removeAt(0)
            val mergedFence = mutableListOf<Fence>()
            mergedFence.addAll(currentFence)
            currentFence.forEach { currentFenceField ->
                val connected = currentFenceList.filter { fenceCandidate ->
                    fenceCandidate.any { fenceField ->
                        fenceField.isConnected(currentFenceField)
                    }
                }
                if (connected.isNotEmpty()) {
                    merged = true
                    mergedFence.addAll(connected.flatten())
                    currentFenceList.removeAll(connected)
                }
            }
            fenceSegments.add(mergedFence)
        }
        currentFenceList = fenceSegments.toList().toMutableList()
    } while (merged)
    fenceSegments
}

val fenceCosts2 = newGroups.mapIndexed { index, group -> group.size * groupedFences[index].size }.sum()
println("Fence costs segmented $fenceCosts2")

