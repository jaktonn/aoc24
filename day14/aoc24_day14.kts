import kotlin.io.path.Path
import kotlin.io.path.readLines

val testInput = """
    p=0,4 v=3,-3
    p=6,3 v=-1,-3
    p=10,3 v=-1,2
    p=2,0 v=2,-1
    p=0,0 v=1,3
    p=3,0 v=-2,-2
    p=7,6 v=-1,-3
    p=3,0 v=-1,-2
    p=9,3 v=2,3
    p=7,3 v=-1,2
    p=2,4 v=2,-3
    p=9,5 v=-3,-3
""".trimIndent()

//val fieldX = 11
//val fieldY = 7
val fieldX = 101
val fieldY = 103


data class Position(val x: Int, val y: Int) {
    fun inSectorA(): Boolean {
        return x in 0..<fieldX/2 && y in 0 ..<fieldY/2
    }
    fun inSectorB(): Boolean {
        return x > fieldX/2 && y in 0 ..<fieldY/2
    }
    fun inSectorC(): Boolean {
        return x > fieldX/2 && y > fieldY/2
    }
    fun inSectorD(): Boolean {
        return x in 0..<fieldX/2 && y > fieldY/2
    }


}
data class Motion(val deltaX: Int, val deltaY: Int)

data class Robot(val position: Position, val motion: Motion) {
    fun move(seconds: Int): Robot {
        val newX = (position.x + seconds * motion.deltaX).mod(fieldX)
        val newY = (position.y + seconds * motion.deltaY).mod(fieldY)
        return Robot(Position(newX, newY), motion)
    }
}

//val robotDefs = testInput.lines()
val robotDefs = Path("./robots.txt").readLines()

val robots = robotDefs.map { robotDef ->
    val (startDef, motionDef) = robotDef.split("\\s".toRegex())
    val (x, y) = startDef.substringAfter('=').split(',').map { it.toInt() }
    val (deltaX, deltaY) = motionDef.substringAfter('=').split(',').map { it.toInt() }
    Robot(Position(x, y), Motion(deltaX, deltaY))
}

val movedRobots = robots.map { it.move(100) }
// Sectors A, B, C, D, starting top-left and going clockwise

fun List<Robot>.safetyFactor(): Long {
    var robotsInSectorA = 0L
    var robotsInSectorB = 0L
    var robotsInSectorC = 0L
    var robotsInSectorD = 0L

    this.forEach { robot ->
        if (robot.position.inSectorA()) {
            robotsInSectorA++
        } else if (robot.position.inSectorB()) {
            robotsInSectorB++
        } else if (robot.position.inSectorC()) {
            robotsInSectorC++
        } else if (robot.position.inSectorD()) {
            robotsInSectorD++
        }
    }
    return robotsInSectorA * robotsInSectorB * robotsInSectorC * robotsInSectorD
}


val safetyFactor = movedRobots.safetyFactor()
println("Safety factor $safetyFactor")

val safetyFactors = (0..100000).map { iteration -> robots.map { it.move(iteration) }.safetyFactor() }

val minSafetyFactor = safetyFactors.min()
val step = safetyFactors.indexOf(minSafetyFactor)
println("Easter egg found after $step seconds")

val easterEggRobots = robots.map { it.move(step) }
var map = ""
for (y in 0..<fieldY) {
    for (x in 0..fieldX) {
        map += if (easterEggRobots.any { it.position.x == x && it.position.y == y })
            "*"
        else
            "."
    }
    map += "\n"
}
println(map)

