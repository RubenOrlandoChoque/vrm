package com.horus.vrmmobile.Utils

import java.util.Date

/**
 * @author Hamzeen. H.
 * This class holds the definition for a Date Interval.
 */
class DateInterval {
    /**
     * Returns the start date of the interval.
     * @return
     */
    var start: Date? = null
        private set
    /**
     * Returns the end date of the interval.
     * @return
     */
    var end: Date? = null
        private set

    /**
     * Constructor.
     * @param startDate
     * @param endDate
     */
    constructor(startDate: Date, endDate: Date) {
        start = startDate
        end = endDate
    }

    /**
     * A copy constructor
     * @param interval
     */
    constructor(interval: DateInterval) {
        this.start = interval.start
        this.end = interval.end
    }
}