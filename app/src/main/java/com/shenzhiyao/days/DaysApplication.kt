package com.shenzhiyao.days

import android.app.Application
import com.shenzhiyao.days.data.DaysDatabase
import com.shenzhiyao.days.data.DaysRepository

class DaysApplication : Application() {
    val database by lazy { DaysDatabase.create(this) }
    val repository by lazy { DaysRepository(database.eventDao(), database.widgetBindingDao()) }
}
