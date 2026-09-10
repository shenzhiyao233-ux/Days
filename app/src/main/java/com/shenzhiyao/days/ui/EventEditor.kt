package com.shenzhiyao.days.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shenzhiyao.days.DaysApplication
import com.shenzhiyao.days.data.*
import com.shenzhiyao.days.image.BackgroundImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun EventEditor(eventId: Long?, onClose:()->Unit, onSaved:()->Unit) {
    val context=LocalContext.current; val scope=LocalLifecycleOwner.current.lifecycleScope
    val repo=(context.applicationContext as DaysApplication).repository
    var loaded by remember { mutableStateOf(eventId == null) }; var original by remember { mutableStateOf<DayEvent?>(null) }
    var title by remember{mutableStateOf("")}; var date by remember{mutableStateOf(LocalDate.now().plusDays(1))}; var mode by remember{mutableStateOf(DayMode.COUNTDOWN)}
    var repeat by remember{mutableStateOf(false)}; var include by remember{mutableStateOf(false)}; var emoji by remember{mutableStateOf("📅")}
    var bgType by remember{mutableStateOf(BackgroundType.SYSTEM)}; var bgColor by remember{mutableLongStateOf(0xFFF3F4F6)}; var bgPath by remember{mutableStateOf<String?>(null)}; var mask by remember{mutableFloatStateOf(.4f)}
    var alpha by remember{mutableFloatStateOf(.88f)}; var textMode by remember{mutableStateOf(TextColorMode.AUTO)}; var aspect by remember{mutableStateOf(WidgetAspect.WIDE)}
    var pendingUri by remember{mutableStateOf<Uri?>(null)}; var zoom by remember{mutableFloatStateOf(1f)}; var ox by remember{mutableFloatStateOf(0f)}; var oy by remember{mutableFloatStateOf(0f)}
    LaunchedEffect(eventId) { eventId?.let { repo.event(it) }?.also { e -> original=e; title=e.title; date=LocalDate.parse(e.targetDate); mode=e.mode; repeat=e.repeatYearly; include=e.includeToday; emoji=e.emoji; bgType=e.backgroundType; bgColor=e.backgroundColor; bgPath=e.backgroundPath; mask=e.maskStrength; alpha=e.backgroundAlpha; textMode=e.textColorMode; aspect=e.widgetAspect }; loaded=true }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { pendingUri=it; zoom=1f; ox=0f; oy=0f }
    if(!loaded) return
    Scaffold(topBar={TopAppBar(title={Text(if(eventId==null)"新建事件" else "编辑事件")}, navigationIcon={TextButton(onClick=onClose){Text("返回")}})}) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxSize(), verticalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title,{title=it},label={Text("事件名称")},modifier=Modifier.fillMaxWidth())
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ listOf("🎂","❤️","✈️","🏠","🎓","💼","🌊","🎉","📅").forEach { FilterChip(selected=emoji==it,onClick={emoji=it},label={Text(it)}) } }
            OutlinedButton(onClick={ DatePickerDialog(context,{_,y,m,d->date=LocalDate.of(y,m+1,d)},date.year,date.monthValue-1,date.dayOfMonth).show() }){Text("日期：$date")}
            Row{FilterChip(mode==DayMode.COUNTDOWN,{mode=DayMode.COUNTDOWN},{Text("倒数")});Spacer(Modifier.width(8.dp));FilterChip(mode==DayMode.COUNTUP,{mode=DayMode.COUNTUP},{Text("正数")})}
            SettingSwitch("每年重复",repeat){repeat=it}; SettingSwitch("计算今天",include){include=it}
            Text("Widget 背景",style=MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ BackgroundType.entries.forEach { FilterChip(bgType==it,{bgType=it},{Text(mapOf(BackgroundType.SYSTEM to "系统",BackgroundType.SOLID to "纯色",BackgroundType.TRANSLUCENT to "半透明",BackgroundType.PHOTO to "照片")[it]!!)}) } }
            if(bgType==BackgroundType.SOLID||bgType==BackgroundType.TRANSLUCENT) Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(0xFFF3F4F6,0xFF202124,0xFFDCEAFE,0xFFFFE4E6,0xFFDDF5E3).forEach{c->FilterChip(bgColor==c,{bgColor=c},{Text("●",color=Color(c))})}}
            if(bgType==BackgroundType.PHOTO) OutlinedButton(onClick={picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}){Text("选择背景图片")}
            if(bgType==BackgroundType.PHOTO){ Text("背景遮罩 ${(mask*100).toInt()}%"); Slider(mask,{mask=it},valueRange=0f..0.7f) }
            else { Text("背景透明度 ${(alpha*100).toInt()}%"); Slider(alpha,{alpha=it}) }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){TextColorMode.entries.forEach{FilterChip(textMode==it,{textMode=it},{Text(mapOf(TextColorMode.AUTO to "自动",TextColorMode.WHITE to "白色",TextColorMode.BLACK to "黑色")[it]!!)})}}
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){WidgetAspect.entries.forEach{FilterChip(aspect==it,{aspect=it},{Text(if(it==WidgetAspect.WIDE)"2×1" else "2×2")})}}
            WidgetPreview(title.ifBlank{"事件名称"},date.toString(),emoji,bgPath,pendingUri,mask,textMode,aspect,zoom,ox,oy){z,x,y->zoom=z;ox=x;oy=y}
            Button(onClick={ scope.launch { try { var newPath=bgPath; pendingUri?.let { uri -> newPath=withContext(Dispatchers.IO){BackgroundImageStore.import(context,uri,aspect,zoom,ox,oy)} }; val e=DayEvent(id=original?.id?:0,title=title.trim(),targetDate=date.toString(),mode=mode,repeatYearly=repeat,includeToday=include,emoji=emoji,backgroundType=bgType,backgroundColor=bgColor,backgroundAlpha=alpha,backgroundPath=newPath,maskStrength=mask,textColorMode=textMode,widgetAspect=aspect); repo.save(e); if(original?.backgroundPath!=newPath) BackgroundImageStore.deleteIfUnused(original?.backgroundPath,repo.bindings().mapNotNull{it.backgroundPath}); onSaved(); onClose() } catch(t:Throwable){Toast.makeText(context,t.message?:"保存失败",Toast.LENGTH_LONG).show()} } },enabled=title.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("保存")}
        }
    }
}

@Composable private fun SettingSwitch(label:String,value:Boolean,onChange:(Boolean)->Unit)=Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f));Switch(value,onChange)}

@Composable fun WidgetPreview(title:String,date:String,emoji:String,path:String?,uri:Uri?,mask:Float,textMode:TextColorMode,aspect:WidgetAspect,zoom:Float,ox:Float,oy:Float,onTransform:(Float,Float,Float)->Unit){
    val shape=RoundedCornerShape(24.dp); val color=if(textMode==TextColorMode.BLACK)Color.Black else Color.White
    val context=LocalContext.current
    var bitmap by remember(uri,path){mutableStateOf<android.graphics.Bitmap?>(null)}
    LaunchedEffect(uri,path){bitmap=withContext(Dispatchers.IO){runCatching{
        if(uri!=null){
            val b=android.graphics.BitmapFactory.Options().apply{inJustDecodeBounds=true};context.contentResolver.openInputStream(uri)?.use{android.graphics.BitmapFactory.decodeStream(it,null,b)}
            var s=1;while(maxOf(b.outWidth,b.outHeight)/s>1600)s*=2
            context.contentResolver.openInputStream(uri)?.use{android.graphics.BitmapFactory.decodeStream(it,null,android.graphics.BitmapFactory.Options().apply{inSampleSize=s})}
        }else path?.let(android.graphics.BitmapFactory::decodeFile)
    }.getOrNull()}}
    Box(Modifier.fillMaxWidth().aspectRatio(if(aspect==WidgetAspect.WIDE)2f else 1f).clip(shape).background(Color(0xFF30343B)).pointerInput(uri){detectTransformGestures{_,pan,z,_->onTransform((zoom*z).coerceIn(1f,4f),(ox+pan.x/300).coerceIn(-1f,1f),(oy+pan.y/300).coerceIn(-1f,1f))}}){
        bitmap?.let{androidx.compose.foundation.Image(it.asImageBitmap(),null,Modifier.fillMaxSize().graphicsLayer{scaleX=zoom;scaleY=zoom;translationX=ox*size.width*.25f;translationY=oy*size.height*.25f},contentScale=ContentScale.Crop)}
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=mask)))
        Column(Modifier.padding(16.dp)){Text("$emoji  $title",color=color);Text("158 天",color=color,style=MaterialTheme.typography.displaySmall);Text(date,color=color)}
    }
}
