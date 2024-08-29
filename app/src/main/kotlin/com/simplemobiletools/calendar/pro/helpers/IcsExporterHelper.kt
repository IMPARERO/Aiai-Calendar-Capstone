package com.simplemobiletools.calendar.pro.helpers

import android.content.Context
import com.simplemobiletools.calendar.pro.models.Event
import java.io.OutputStream
import biweekly.Biweekly
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.property.Summary
import biweekly.property.DateStart
import biweekly.property.DateEnd
import java.util.Date

class IcsExporterHelper(val context: Context) {

    fun writeEventsToStream(events: List<Event>, outputStream: OutputStream) {
        val ical = ICalendar()
        for (event in events) {
            val vevent = VEvent()
            vevent.setSummary(Summary(event.title))
            vevent.setDateStart(DateStart(Date(event.startTS * 1000)))
            vevent.setDateEnd(DateEnd(Date(event.endTS * 1000)))
            ical.addEvent(vevent)
        }
        Biweekly.write(ical).go(outputStream)
    }
}
