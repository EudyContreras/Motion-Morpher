package com.eudycontreras.motionmorpherlibrary

import com.eudycontreras.motionmorpherlibrary.extensions.groupByAnd
import com.eudycontreras.motionmorpherlibrary.globals.approximate
import com.eudycontreras.motionmorpherlibrary.properties.Bounds
import org.junit.Test
import java.util.*


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 03 2019
 */
 
 
public class ChoreographerTests {

    @Test fun grouping_fuzzy_floats_works() {

        var nodes = LinkedList<ValueHolder<Float>>()
        nodes.addAll(arrayOf(
            ValueHolder(20f),
            ValueHolder(10f),
            ValueHolder(12f),
            ValueHolder(8f),
            ValueHolder(22f),
            ValueHolder(30f),
            ValueHolder(10f),
            ValueHolder(15f),
            ValueHolder(25f),
            ValueHolder(18f),
            ValueHolder(30f),
            ValueHolder(34f),
            ValueHolder(33f),
            ValueHolder(59f),
            ValueHolder(29f)
        ))

        nodes.sortBy { it.value }

        val nodesGroups = nodes.groupByAnd( { it.value }, { it, other ->
            approximate(
                it.value,
                other.value,
                other.value * 0.3f
            )
        })

        assert(nodesGroups.size == 6)
    }

    @Test fun grouping_overlapping_bounds_works() {

        var nodes = LinkedList<Bounds>()
        nodes.addAll(arrayOf(
            Bounds(0, 0, 30f, 30f),
            Bounds(30, 10, 30f, 30f),
            Bounds(60, 20, 30f, 30f),
            Bounds(0, 30, 30f, 30f),
            Bounds(0, 60, 30f, 30f),
            Bounds(0, 70, 30f, 30f),
            Bounds(0, 90, 30f, 30f),
            Bounds(0, 120, 30f, 30f),
            Bounds(0, 150, 30f, 30f),
            Bounds(0, 180, 30f, 30f),
            Bounds(0, 210, 30f, 30f)
        ))

        nodes.sortBy { it.y }

        val nodesGroups = nodes.groupByAnd( { it.y }, { it, other -> it.overlapsVertically(other) })

        assert(nodesGroups.size == 8)
    }

    inner class ValueHolder<T>(var value: T)
}