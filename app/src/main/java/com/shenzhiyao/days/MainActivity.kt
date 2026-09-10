package com.shenzhiyao.days

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.shenzhiyao.days.data.DayEvent
import com.shenzhiyao.days.domain.DayCalculator
import com.shenzhiyao.days.ui.EventEditor
import com.shenzhiyao.days.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val repo get() = (application as DaysApplication).repository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent { MaterialTheme { DaysApp(intent.getLongExtra(EXTRA_EVENT_ID, 0)) } }
    }

    @Composable private fun DaysApp(initialId: Long) {
        val events by repo.observeEvents().collectAsState(initial = emptyList())
        var editing by remember { mutableStateOf<Long?>(initialId.takeIf { it > 0 }) }
        var creating by remember { mutableStateOf(false) }
        if (editing != null || creating) {
            EventEditor(eventId = editing, onClose = { editing = null; creating = false }, onSaved = { WidgetUpdater.updateAll(this) })
        } else Scaffold(
            topBar = { TopAppBar(title = { Text("倒数日") }) },
            floatingActionButton = { FloatingActionButton(onClick={ creating=true }) { Icon(Icons.Default.Add, "新建") } }
        ) { padding ->
            if (events.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment=Alignment.Center) { Text("还没有事件，点击 + 创建") }
            else LazyColumn(Modifier.padding(padding), contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
                items(events, key={it.id}) { event ->
                    val day = remember(event) { DayCalculator.calculate(event) }
                    Card(Modifier.fillMaxWidth().clickable { editing=event.id }) {
                        Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically) {
                            Text(event.emoji, style=MaterialTheme.typography.headlineMedium)
                            Column(Modifier.padding(start=14.dp).weight(1f)) { Text(event.title, style=MaterialTheme.typography.titleMedium); Text(day.target.toString(), color=Color.Gray) }
                            Text(if(day.headline=="就是今天") day.headline else "${day.headline} ${day.value} 天", style=MaterialTheme.typography.titleMedium)
                            IconButton(onClick={ lifecycleScope.launch { repo.delete(event).forEach { File(it).delete() }; WidgetUpdater.updateAll(this@MainActivity) } }) { Icon(Icons.Default.Delete,"删除") }
                        }
                    }
                }
            }
        }
    }
    companion object { const val EXTRA_EVENT_ID = "event_id" }
}
