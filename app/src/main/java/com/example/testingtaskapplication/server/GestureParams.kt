package com.example.testingtaskapplication.server

data class GestureParams(val length: Int, val direction: String, val speed: Int)

fun generateGestureParams(): GestureParams {
    val length = (100..500).random()
    val direction = listOf("up", "down").random()
    val speed = (50..200).random()
    return GestureParams(length, direction, speed)
}