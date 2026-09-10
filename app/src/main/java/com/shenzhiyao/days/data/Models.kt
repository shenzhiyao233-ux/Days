package com.shenzhiyao.days.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DayMode { COUNTDOWN, COUNTUP }
enum class BackgroundType { SYSTEM, SOLID, TRANSLUCENT, PHOTO }
enum class TextColorMode { AUTO, WHITE, BLACK }
enum class WidgetAspect { WIDE, SQUARE }

@Entity(tableName = "events")
data class DayEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetDate: String,
    val mode: DayMode = DayMode.COUNTDOWN,
    val repeatYearly: Boolean = false,
    val includeToday: Boolean = false,
    val emoji: String = "📅",
    val sortOrder: Int = 0,
    val backgroundType: BackgroundType = BackgroundType.SYSTEM,
    val backgroundColor: Long = 0xFFF3F4F6,
    val backgroundAlpha: Float = .88f,
    val backgroundPath: String? = null,
    val maskStrength: Float = .4f,
    val textColorMode: TextColorMode = TextColorMode.AUTO,
    val widgetAspect: WidgetAspect = WidgetAspect.WIDE
)

@Entity(tableName = "widget_bindings")
data class WidgetBinding(
    @PrimaryKey val appWidgetId: Int,
    val eventId: Long,
    val backgroundType: BackgroundType,
    val backgroundColor: Long,
    val backgroundAlpha: Float,
    val backgroundPath: String?,
    val maskStrength: Float,
    val textColorMode: TextColorMode,
    val widgetAspect: WidgetAspect
)
