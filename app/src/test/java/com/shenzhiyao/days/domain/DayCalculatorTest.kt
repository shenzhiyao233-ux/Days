package com.shenzhiyao.days.domain

import com.shenzhiyao.days.data.DayEvent
import com.shenzhiyao.days.data.DayMode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DayCalculatorTest {
    private val today = LocalDate.of(2026, 9, 8)
    private fun event(date: String, mode: DayMode = DayMode.COUNTDOWN, repeat: Boolean = false, include: Boolean = false) = DayEvent(title="测试", targetDate=date, mode=mode, repeatYearly=repeat, includeToday=include)
    @Test fun today() = assertEquals("就是今天", DayCalculator.calculate(event("2026-09-08"), today).headline)
    @Test fun tomorrow() = assertEquals(1, DayCalculator.calculate(event("2026-09-09"), today).value)
    @Test fun yesterday() = assertEquals(1, DayCalculator.calculate(event("2026-09-07"), today).value)
    @Test fun includeToday() = assertEquals(2, DayCalculator.calculate(event("2026-09-09", include=true), today).value)
    @Test fun crossMonth() = assertEquals(1, DayCalculator.calculate(event("2026-10-01"), LocalDate.of(2026,9,30)).value)
    @Test fun crossYear() = assertEquals(1, DayCalculator.calculate(event("2027-01-01"), LocalDate.of(2026,12,31)).value)
    @Test fun leapYear() = assertEquals(2, DayCalculator.calculate(event("2028-03-01"), LocalDate.of(2028,2,28)).value)
    @Test fun yearlyRepeat() = assertEquals(LocalDate.of(2027,1,1), DayCalculator.calculate(event("2020-01-01", repeat=true), today).target)
    @Test fun countUp() = assertEquals(9, DayCalculator.calculate(event("2026-08-31", DayMode.COUNTUP, include=true), today).value)
}
