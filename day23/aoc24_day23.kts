import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    kh-tc
    qp-kh
    de-cg
    ka-co
    yn-aq
    qp-ub
    cg-tb
    vc-aq
    tb-ka
    wh-tc
    yn-cg
    kh-ub
    ta-co
    de-co
    tc-td
    tb-wq
    wh-td
    ta-ka
    td-qp
    aq-cg
    wq-ub
    ub-vc
    de-ta
    wq-aq
    wq-vc
    wh-yn
    ka-de
    kh-ta
    co-tc
    wh-qp
    tb-vc
    td-yn
""".trimIndent()

//val input = testInput.lines()
val input = Path("./input.txt").readLines()

val connectionMap = mutableMapOf<String, MutableSet<String>>()
input.forEach { line ->
    val computers = line.split('-').map { it.trim() }
    computers.forEach {
        connectionMap.compute(it) {_, connected ->
            (connected ?: mutableSetOf()).also { computerSet -> computerSet.addAll(computers - it) }
        }
    }
}

val triGroups = mutableSetOf<Set<String>>()

connectionMap.entries.forEach { (computer, connections) ->
    val connectionList = connections.toList()
    for (i in connectionList.indices) {
        val toCheck = connectionList[i]
        val rest = connectionList.drop(i + 1)
        val triGroupCandidates = rest.filter { connectionMap.getValue(it).contains(toCheck) }
        triGroupCandidates.forEach {
            triGroups.add(setOf(computer, toCheck, it))
        }
    }
}

val setsInQuestion = triGroups.filter { it.any { computerName -> computerName.startsWith('t')} }
println("Connected computer sets containing 't' ${setsInQuestion.size}")

val networks = mutableSetOf<Set<String>>()
connectionMap.entries.forEach { (computer, connections) ->
    val connectionConnections = connections.map { connectionMap.getValue(it).intersect(connections) + it }
    connectionConnections.forEach { connectionConnection ->
        if (connectionConnections.count { it == connectionConnection } == connectionConnection.size) {
            networks.add(connectionConnection + computer)
        }
    }
}

val lanParty = networks.maxBy { it.size }.toList().sorted()
println("LAN Party password ${lanParty.joinToString(",")}")
