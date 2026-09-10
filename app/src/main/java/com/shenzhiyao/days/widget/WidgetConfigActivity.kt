package com.shenzhiyao.days.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.shenzhiyao.days.DaysApplication
import com.shenzhiyao.days.data.*
import com.shenzhiyao.days.image.BackgroundImageStore
import com.shenzhiyao.days.ui.WidgetPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class WidgetConfigActivity:ComponentActivity(){
    private var widgetId=AppWidgetManager.INVALID_APPWIDGET_ID
    private val repo get()=(application as DaysApplication).repository
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);widgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);setResult(Activity.RESULT_CANCELED);if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){finish();return};setContent{MaterialTheme{Config()}}}
    @Composable private fun Config(){
        val events by repo.observeEvents().collectAsState(initial=emptyList());var selected by remember{mutableStateOf<DayEvent?>(null)}
        var type by remember{mutableStateOf(BackgroundType.SYSTEM)};var uri by remember{mutableStateOf<Uri?>(null)};var mask by remember{mutableFloatStateOf(.4f)};var alpha by remember{mutableFloatStateOf(.88f)};var text by remember{mutableStateOf(TextColorMode.AUTO)};var aspect by remember{mutableStateOf(WidgetAspect.WIDE)};var zoom by remember{mutableFloatStateOf(1f)};var ox by remember{mutableFloatStateOf(0f)};var oy by remember{mutableFloatStateOf(0f)}
        val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){uri=it}
        Scaffold(topBar={TopAppBar(title={Text(if(selected==null)"选择 Widget 事件" else "设置这个 Widget")})}){pad->Column(Modifier.padding(pad).padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            if(selected==null){if(events.isEmpty())Text("请先打开“日子”创建事件")else LazyColumn{items(events){e->Card(Modifier.fillMaxWidth().padding(4.dp).clickable{selected=e;type=e.backgroundType;mask=e.maskStrength;alpha=e.backgroundAlpha;text=e.textColorMode;aspect=e.widgetAspect}){Row(Modifier.padding(18.dp)){Text(e.emoji);Spacer(Modifier.width(12.dp));Text(e.title)}}}}}
            else selected?.let{e->
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){BackgroundType.entries.forEach{v->FilterChip(type==v,{type=v},{Text(mapOf(BackgroundType.SYSTEM to "系统",BackgroundType.SOLID to "纯色",BackgroundType.TRANSLUCENT to "半透明",BackgroundType.PHOTO to "照片")[v]!!)})}}
                if(type==BackgroundType.PHOTO)OutlinedButton(onClick={picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}){Text("为这个 Widget 选择照片")}
                if(type==BackgroundType.PHOTO){Text("背景遮罩 ${(mask*100).toInt()}%");Slider(mask,{mask=it},valueRange=0f..0.7f)}else{Text("背景透明度 ${(alpha*100).toInt()}%");Slider(alpha,{alpha=it})}
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){TextColorMode.entries.forEach{v->FilterChip(text==v,{text=v},{Text(mapOf(TextColorMode.AUTO to "自动",TextColorMode.WHITE to "白色",TextColorMode.BLACK to "黑色")[v]!!)})}}
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){WidgetAspect.entries.forEach{v->FilterChip(aspect==v,{aspect=v},{Text(if(v==WidgetAspect.WIDE)"2×1" else "2×2")})}}
                WidgetPreview(e.title,e.targetDate,e.emoji,e.backgroundPath,uri,mask,text,aspect,zoom,ox,oy){z,x,y->zoom=z;ox=x;oy=y}
                Button(onClick={save(e,type,uri,mask,alpha,text,aspect,zoom,ox,oy)},modifier=Modifier.fillMaxWidth()){Text("完成")}
            }
        }}
    }
    private fun save(e:DayEvent,type:BackgroundType,uri:Uri?,mask:Float,alpha:Float,text:TextColorMode,aspect:WidgetAspect,zoom:Float,ox:Float,oy:Float){lifecycleScope.launch{try{val path=if(type==BackgroundType.PHOTO&&uri!=null)withContext(Dispatchers.IO){BackgroundImageStore.import(this@WidgetConfigActivity,uri,aspect,zoom,ox,oy)}else if(type==BackgroundType.PHOTO)e.backgroundPath else null;repo.save(WidgetBinding(widgetId,e.id,type,e.backgroundColor,alpha,path,mask,text,aspect));DaysWidget().update(this@WidgetConfigActivity,AppWidgetId(widgetId));setResult(Activity.RESULT_OK,Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId));finish()}catch(t:Throwable){Toast.makeText(this@WidgetConfigActivity,t.message?:"配置失败",Toast.LENGTH_LONG).show()}}}
}
