package nl.koenhabets.yahtzeescore

import java.util.*

class MovingAverage(private val size: Int) {
    private val Dataset: Queue<Double> = LinkedList()
    private var sum = 0.0
    fun addData(num: Double) {
        sum += num
        Dataset.add(num)
        if (Dataset.size > size) {
            sum -= Dataset.remove()
        }
    }

    val mean: Double
        get() = sum / size
}