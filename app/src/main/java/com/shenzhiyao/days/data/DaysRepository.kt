package com.shenzhiyao.days.data

import kotlinx.coroutines.flow.Flow

class DaysRepository(private val events: EventDao, private val widgets: WidgetBindingDao) {
    fun observeEvents(): Flow<List<DayEvent>> = events.observeAll()
    suspend fun events() = events.getAll()
    suspend fun event(id: Long) = events.get(id)
    suspend fun save(event: DayEvent) = events.save(event)
    suspend fun binding(id: Int) = widgets.get(id)
    suspend fun bindings() = widgets.all()
    suspend fun save(binding: WidgetBinding) = widgets.save(binding)
    suspend fun removeWidget(id: Int): Pair<String?, List<String>> {
        val old = widgets.get(id)?.backgroundPath
        widgets.delete(id)
        return old to widgets.all().mapNotNull { it.backgroundPath }
    }
    suspend fun delete(event: DayEvent): List<String> {
        val paths = buildList {
            event.backgroundPath?.let(::add)
            widgets.forEvent(event.id).mapNotNullTo(this) { it.backgroundPath }
        }.distinct()
        widgets.deleteForEvent(event.id)
        events.delete(event)
        return paths
    }
}
