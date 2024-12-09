import kotlin.io.path.Path
import kotlin.io.path.readText

val testInput = "2333133121414131402"
val fullInput = Path("input.txt").readText()
val input = fullInput


val memory = mutableListOf<Long>()

for (i in 0..input.lastIndex) {
    if(i % 2 == 0) {
        repeat(input[i].digitToInt()) {
            memory.add((i / 2).toLong())
        }
    } else {
        repeat(input[i].digitToInt()) {
            memory.add(-1L)
        }
    }
}

val compactedMemory = memory.toList().toMutableList()
var firstFreeBlockIndex = compactedMemory.indexOfFirst { it == -1L }
for (i in firstFreeBlockIndex..compactedMemory.lastIndex) {
    val lastNonFreeIndex = compactedMemory.indexOfLast { it != -1L }
    if (lastNonFreeIndex > firstFreeBlockIndex) {
        //swap
        val file = compactedMemory.removeAt(lastNonFreeIndex)
        compactedMemory.add(lastNonFreeIndex, -1)
        compactedMemory.removeAt(firstFreeBlockIndex)
        compactedMemory.add(firstFreeBlockIndex, file)
    } else {
        break
    }
    firstFreeBlockIndex = compactedMemory.drop(i).indexOfFirst { it == -1L } + i
}

val checkSum = compactedMemory.mapIndexed { index, file -> (if (file > -1) index * file else 0) }.sum()
println("Compacted: $checkSum")

val freeBlockIndices = mutableMapOf<Int, List<Int>>()
var freeLength = 0
for (i in 1..memory.lastIndex) {
    if (memory[i] == -1L) {
        if (memory[i - 1] == -1L) {
            freeLength += 1
        } else {
            freeLength = 1
        }
    } else {
        if (memory[i - 1] == -1L) {
            freeBlockIndices.compute(freeLength) {_, indexList -> ((indexList ?: emptyList()) + (i - freeLength)).sorted() }
        }
    }
}

val defragmentedMemory = memory.toList().toMutableList()

var blockLength = 1
for (i in memory.lastIndex - 1 downTo 0) {
    if (memory[i + 1] == memory[i]) {
        blockLength += 1
    } else {
        if (memory[i + 1] != -1L) {
            val firstFreeBlockEntry =
                freeBlockIndices.filterKeys { it >= blockLength }.minByOrNull { (_, v) -> v.min() }
            if (firstFreeBlockEntry != null) {
                val firstFreeBlockIndex = firstFreeBlockEntry.value.firstOrNull()
                if (firstFreeBlockIndex != null && firstFreeBlockIndex < i) {
                    repeat(blockLength) {
                        defragmentedMemory.removeAt(firstFreeBlockIndex)
                    }
                    repeat(blockLength) {
                        defragmentedMemory.add(firstFreeBlockIndex, memory[i + 1])
                    }
                    repeat(blockLength) {
                        defragmentedMemory.removeAt(i + 1)
                    }
                    repeat(blockLength) {
                        defragmentedMemory.add(i + 1, -1)
                    }
                    freeBlockIndices.computeIfPresent(firstFreeBlockEntry.key) { _, list -> list.drop(1) }
                    if (firstFreeBlockEntry.key > blockLength) {
                        freeBlockIndices.compute(firstFreeBlockEntry.key - blockLength) { _, list ->
                            ((list ?: emptyList()) + (firstFreeBlockEntry.value.first() + blockLength)).sorted()
                        }
                    }
                }
            }
            blockLength = 1
        } else {
            blockLength = 1
        }
    }
}

val checksum2 = defragmentedMemory.mapIndexed { index, file -> (if (file > -1) index * file else 0)}.sum()
println("Checksum defragmented: $checksum2")



