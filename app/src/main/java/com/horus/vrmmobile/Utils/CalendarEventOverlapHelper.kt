package com.horus.vrmmobile.Utils

import java.util.ArrayList

/**
 * @author Hamzeen. H.
 * This class contains helper method(s) to check
 * overlapping date intervals.
 */
class CalendarEventOverlapHelper {
    /** holds a list of distinct date intervals  */
    private val distinctIntervals: MutableList<DateInterval>?

    init {
        distinctIntervals = ArrayList()
    }

    /**
     * Scans the distinctIntervals list and flags whether the passed interval
     * overlaps with any of the existing interval(s) on the list.
     *
     * @param interval
     * @return
     */
    fun isDistinct(interval: DateInterval): Boolean {
        var result = false
        if (distinctIntervals != null && distinctIntervals.size > 0) {
            for (i in distinctIntervals.indices) {
                if (overlaps(interval, distinctIntervals[i])) {
                    result = true
                }
            }
        }
        distinctIntervals!!.add(interval)
        return result
    }

    /**
     * This method looks for any overlapping date intervals
     * from the passed array and returns the result as a String.
     * @param intervals
     * @return
     */
    fun findOverlap(intervals: ArrayList<DateInterval>): Boolean {
        for (element in intervals) {
            if (isDistinct(element)) {
                return true
            }
        }
        return false
    }

    /**
     * This method flags whether a given two date intervals
     * overlap.
     * @param left
     * @param right
     * @return true, if overlaps & false, otherwise.
     */
    fun overlaps(left: DateInterval, right: DateInterval): Boolean {
        val result = true
        if (left.end!!.before(right.start) && left.end!!.before(right.end)) {
            return false
        }
        return if (left.start!!.after(right.start) && left.start!!.after(right.end)) {
            false
        } else result
    }
}