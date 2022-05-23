package com.horus.vrmmobile.Utils

/**
 * Created by mparraga on 3/9/2018.
 */
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Stack
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

abstract class  TaskIterator<T>(list: List<T>) : Thread() {
    private val listDown = Stack<T>()
    private var count: AtomicInteger? = null
    var simultaneousWork = 2
    var eventNext: String = ""
    var eventEnd: String = ""

    init {
        EventBus.getDefault().register(this)
        eventNext = UUID.randomUUID().toString()
        eventEnd = UUID.randomUUID().toString()
        listDown.addAll(list)
    }

    override fun run() {
        init()
    }

    private fun init() {
        if (listDown.size == 0) {
            EventBus.getDefault().post(Event(eventEnd))
            EventBus.getDefault().unregister(this)
            actionEnd()
            return
        }
        count = AtomicInteger(listDown.size)
        var i = 0
        while (i < simultaneousWork && listDown.size > 0) {
            actionItem(listDown.pop())
            i++
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEventListener(event: Event) {
        if (event.eventName.equals(eventNext)) {
            if (listDown.size != 0) {
                actionItem(listDown.pop())
            }

            if (count!!.decrementAndGet() == 0) {
                EventBus.getDefault().post(Event(eventEnd))
                EventBus.getDefault().unregister(this)
                actionEnd()
            }
        }
    }

    abstract fun actionEnd()
    abstract fun actionItem(item: T)
}
