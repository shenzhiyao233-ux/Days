package com.shenzhiyao.days.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface EventDao {
    @Query("SELECT * FROM events ORDER BY sortOrder, id") fun observeAll(): Flow<List<DayEvent>>
    @Query("SELECT * FROM events ORDER BY sortOrder, id") suspend fun getAll(): List<DayEvent>
    @Query("SELECT * FROM events WHERE id=:id") suspend fun get(id: Long): DayEvent?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(event: DayEvent): Long
    @Delete suspend fun delete(event: DayEvent)
}

@Dao interface WidgetBindingDao {
    @Query("SELECT * FROM widget_bindings WHERE appWidgetId=:id") suspend fun get(id: Int): WidgetBinding?
    @Query("SELECT * FROM widget_bindings WHERE eventId=:eventId") suspend fun forEvent(eventId: Long): List<WidgetBinding>
    @Query("SELECT * FROM widget_bindings") suspend fun all(): List<WidgetBinding>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(binding: WidgetBinding)
    @Query("DELETE FROM widget_bindings WHERE appWidgetId=:id") suspend fun delete(id: Int)
    @Query("DELETE FROM widget_bindings WHERE eventId=:eventId") suspend fun deleteForEvent(eventId: Long)
}

class DbConverters {
    @TypeConverter fun mode(v: String) = DayMode.valueOf(v)
    @TypeConverter fun mode(v: DayMode) = v.name
    @TypeConverter fun background(v: String) = BackgroundType.valueOf(v)
    @TypeConverter fun background(v: BackgroundType) = v.name
    @TypeConverter fun text(v: String) = TextColorMode.valueOf(v)
    @TypeConverter fun text(v: TextColorMode) = v.name
    @TypeConverter fun aspect(v: String) = WidgetAspect.valueOf(v)
    @TypeConverter fun aspect(v: WidgetAspect) = v.name
}

@Database(entities = [DayEvent::class, WidgetBinding::class], version = 1, exportSchema = true)
@TypeConverters(DbConverters::class)
abstract class DaysDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun widgetBindingDao(): WidgetBindingDao
    companion object {
        fun create(context: Context) = Room.databaseBuilder(context, DaysDatabase::class.java, "days.db")
            .fallbackToDestructiveMigrationOnDowngrade().build()
    }
}
