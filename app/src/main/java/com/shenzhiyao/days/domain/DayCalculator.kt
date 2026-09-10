package com.shenzhiyao.days.domain

import com.shenzhiyao.days.data.DayEvent
import com.shenzhiyao.days.data.DayMode
import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DayDisplay(val value: Long, val headline: String, val target: LocalDate)

object DayCalculator {
    fun calculate(event: DayEvent, today: LocalDate = LocalDate.now()): DayDisplay {
        val stored = LocalDate.parse(event.targetDate)
        val target = if (event.repeatYearly) nextOccurrence(stored, today) else stored
        val raw = ChronoUnit.DAYS.between(today, target)
        return when (event.mode) {
            DayMode.COUNTDOWN -> when {
                raw > 0 -> DayDisplay(raw + if (event.includeToday) 1 else 0, "还有", target)
                raw == 0L -> DayDisplay(if (event.includeToday) 1 else 0, "就是今天", target)
                else -> DayDisplay(-raw + if (event.includeToday) 1 else 0, "已经过去", target)
            }
            DayMode.COUNTUP -> {
                val elapsed = ChronoUnit.DAYS.between(stored, today)
                DayDisplay((elapsed + if (event.includeToday) 1 else 0).coerceAtLeast(0), "第", stored)
            }
        }
    }

    private fun nextOccurrence(date: LocalDate, today: LocalDate): LocalDate {
        fun inYear(year: Int): LocalDate = try { date.withYear(year) }
        catch (_: DateTimeException) { LocalDate.of(year, 2, 28) } // 2月29日在非闰年按2月28日处理
        val thisYear = inYear(today.year)
        return if (thisYear < today) inYear(today.year + 1) else thisYear
    }
}
