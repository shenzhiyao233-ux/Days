package com.shenzhiyao.days.widget

import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.color.ColorProvider as DayNightColorProvider
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.*
import com.shenzhiyao.days.DaysApplication
import com.shenzhiyao.days.MainActivity
import com.shenzhiyao.days.data.*
import com.shenzhiyao.days.domain.DayCalculator
import com.shenzhiyao.days.image.BackgroundImageStore
import kotlinx.coroutines.*
import java.io.File

class DaysWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appId=GlanceAppWidgetManager(context).getAppWidgetId(id)
        val repo=(context.applicationContext as DaysApplication).repository
        val binding=repo.binding(appId); val event=binding?.let{repo.event(it.eventId)}
        provideContent { WidgetContent(context,event,binding) }
    }
}

@Composable private fun WidgetContent(context:Context,event:DayEvent?,binding:WidgetBinding?){
    if(event==null||binding==null){Box(GlanceModifier.fillMaxSize().background(ColorProvider(Color(0xFFF3F4F6))).padding(16.dp),contentAlignment=Alignment.Center){Text("事件已删除\n点击 App 重新配置")};return}
    val day=DayCalculator.calculate(event)
    val file=binding.backgroundPath?.let(::File)?.takeIf{it.isFile}
    val bitmap=if(binding.backgroundType==BackgroundType.PHOTO&&file!=null) runCatching{BitmapFactory.decodeFile(file.absolutePath)}.getOrNull() else null
    val autoWhite=bitmap?.let{averageLuma(it)<145}?:false
    val foreground=when(binding.textColorMode){TextColorMode.WHITE->Color.White;TextColorMode.BLACK->Color.Black;TextColorMode.AUTO->if(bitmap!=null||autoWhite)Color.White else Color(0xFF171717)}
    val base=when(binding.backgroundType){BackgroundType.SYSTEM->DayNightColorProvider(Color(0xFFF5F5F5),Color(0xFF242424));BackgroundType.SOLID->ColorProvider(Color(binding.backgroundColor));BackgroundType.TRANSLUCENT->ColorProvider(Color(binding.backgroundColor).copy(alpha=binding.backgroundAlpha));BackgroundType.PHOTO->ColorProvider(Color(0xFF30343B))}
    val modifier=if(bitmap!=null)GlanceModifier.fillMaxSize().background(ImageProvider(bitmap),ContentScale.Crop) else GlanceModifier.fillMaxSize().background(base)
    val intent=Intent(context,MainActivity::class.java).putExtra(MainActivity.EXTRA_EVENT_ID,event.id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    Box(modifier.cornerRadius(24.dp).clickable(actionStartActivity(intent))){
        if(bitmap!=null) Box(GlanceModifier.fillMaxSize().background(ColorProvider(Color.Black.copy(alpha=binding.maskStrength)))){}
        Column(GlanceModifier.fillMaxSize().padding(14.dp),verticalAlignment=Alignment.Vertical.CenterVertically){
            Text("${event.emoji}  ${event.title}",style=TextStyle(color=ColorProvider(foreground),fontSize=15.sp,fontWeight=FontWeight.Medium),maxLines=1)
            Spacer(GlanceModifier.height(3.dp))
            Text(if(day.headline=="就是今天")day.headline else "${day.headline} ${day.value} 天",style=TextStyle(color=ColorProvider(foreground),fontSize=28.sp,fontWeight=FontWeight.Bold),maxLines=1)
            if(binding.widgetAspect==WidgetAspect.SQUARE) Text(day.target.toString(),style=TextStyle(color=ColorProvider(foreground.copy(alpha=.84f)),fontSize=12.sp))
        }
    }
}

private fun averageLuma(bitmap:android.graphics.Bitmap):Int{var sum=0L;var count=0;val sx=(bitmap.width/20).coerceAtLeast(1);val sy=(bitmap.height/20).coerceAtLeast(1);for(y in 0 until bitmap.height step sy)for(x in 0 until bitmap.width step sx){val c=bitmap.getPixel(x,y);sum+=(android.graphics.Color.red(c)*299+android.graphics.Color.green(c)*587+android.graphics.Color.blue(c)*114)/1000;count++};return(sum/count.coerceAtLeast(1)).toInt()}

class DaysWidgetReceiver : GlanceAppWidgetReceiver(){override val glanceAppWidget=DaysWidget();override fun onDeleted(context:Context,appWidgetIds:IntArray){super.onDeleted(context,appWidgetIds);val repo=(context.applicationContext as DaysApplication).repository;CoroutineScope(Dispatchers.IO).launch{appWidgetIds.forEach{val(old,used)=repo.removeWidget(it);BackgroundImageStore.deleteIfUnused(old,used)}}}}

object WidgetUpdater { fun updateAll(context:Context){CoroutineScope(Dispatchers.IO).launch{DaysWidget().updateAll(context)}} }

class DateChangeReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){val pending=goAsync();CoroutineScope(Dispatchers.IO).launch{try{DaysWidget().updateAll(context)}finally{pending.finish()}}}}
