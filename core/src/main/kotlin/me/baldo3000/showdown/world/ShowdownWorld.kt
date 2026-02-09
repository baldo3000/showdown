package me.baldo3000.showdown.world

import me.baldo3000.showdown.network.Vector2D
import kotlin.math.roundToInt

class ShowdownWorld(val mapSize: Vector2D = Vector2D(16f, 9f), val maxPlayers: Int = 10) : World {
    val availableSpawnLocations: MutableList<Vector2D>

    init {
        availableSpawnLocations = createSpawnLocations(mapSize, maxPlayers).toMutableList()
    }

    override fun isGameFull() = availableSpawnLocations.isEmpty()

    override fun newPLayerSpawnLocation(): Vector2D {
        if (isGameFull()) throw IllegalStateException("Cannot spawn new player: game is full")
        return availableSpawnLocations.removeFirst()
    }


    private fun createSpawnLocations(mapSize: Vector2D, locations: Int): List<Vector2D> {
        return if (locations <= 0) {
            emptyList()
        } else {
            val (w, h) = mapSize
            val margin = (minOf(w, h) * 0.05f).coerceAtMost(minOf(w, h) / 2f)
            val innerW = (w - 2f * margin).coerceAtLeast(0f)
            val innerH = (h - 2f * margin).coerceAtLeast(0f)

            if (innerW <= 0f || innerH <= 0f) {
                // Degenerate: all spawns at center
                List(locations) { Vector2D(w / 2f, h / 2f) }
            } else {
                val pts = mutableListOf<Vector2D>()

                // Corners in order: top-left, top-right, bottom-right, bottom-left
                val corners = listOf(
                    Vector2D(margin, margin),
                    Vector2D(w - margin, margin),
                    Vector2D(w - margin, h - margin),
                    Vector2D(margin, h - margin)
                )
                val cornersToPlace = minOf(locations, 4)
                pts += corners.take(cornersToPlace)
                var remaining = locations - cornersToPlace
                if (remaining > 0) {
                    val shortLen = minOf(innerW, innerH)
                    val longLen = maxOf(innerW, innerH)
                    val totalShort = 2f * shortLen
                    val totalLong = 2f * longLen
                    val countsShort = if (totalShort + totalLong > 0f) {
                        (remaining * (totalShort / (totalShort + totalLong))).roundToInt()
                    } else 0
                    val countsLong = remaining - countsShort

                    fun placeTop(k: Int) {
                        if (k <= 0) return
                        val spacing = innerW / (k + 1f)
                        for (i in 1..k) pts += Vector2D(margin + spacing * i, margin)
                    }

                    fun placeBottom(k: Int) {
                        if (k <= 0) return
                        val spacing = innerW / (k + 1f)
                        for (i in 1..k) pts += Vector2D(w - margin - spacing * i, h - margin)
                    }

                    fun placeRight(k: Int) {
                        if (k <= 0) return
                        val spacing = innerH / (k + 1f)
                        for (i in 1..k) pts += Vector2D(w - margin, margin + spacing * i)
                    }

                    fun placeLeft(k: Int) {
                        if (k <= 0) return
                        val spacing = innerH / (k + 1f)
                        for (i in 1..k) pts += Vector2D(margin, h - margin - spacing * i)
                    }

                    // Split counts between the two edges of each group
                    val shortFirst = countsShort / 2 + countsShort % 2
                    val shortSecond = countsShort - shortFirst
                    val longFirst = countsLong / 2 + countsLong % 2
                    val longSecond = countsLong - longFirst

                    if (innerW <= innerH) {
                        // Top & bottom are short, left & right are long
                        placeTop(shortFirst)
                        placeRight(longFirst)
                        placeBottom(shortSecond)
                        placeLeft(longSecond)
                    } else {
                        // Left & right are short, top & bottom are long
                        placeTop(longFirst)
                        placeRight(shortFirst)
                        placeBottom(longSecond)
                        placeLeft(shortSecond)
                    }
                }

                pts.toList()
            }
        }
    }
}
