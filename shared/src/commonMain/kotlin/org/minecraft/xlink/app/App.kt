package org.minecraft.xlink.app

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.javatools.net.p2pvpn.event.EventType
import org.javatools.net.tcp.NetHeadBuilder
import org.javatools.net.token.TOKEN32
import org.minecraft.xlink.*
import org.minecraft.xlink.modifier.shake
import org.minecraft.xlink.state.JoinTipState.*
import org.minecraft.xlink.state.LoadState.*
import org.minecraft.xlink.state.OverlayState.*
import org.minecraft.xlink.state.isSuccess
import org.minecraft.xlink.update.UpdateFile
import org.minecraft.xlink.update.launchUpdater
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App(
    showInfo: MutableState<Boolean>
):(@Composable () -> Unit) {
    var overlay: @Composable () -> Unit = {}
    var state by remember { mutableStateOf(LOADING) }
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 这个状态是最下面那个覆盖层显示内容的状态
            var overlayState by remember { mutableStateOf(None) }
            // 主面板
            Crossfade(
                targetState = state
            ) { currentState ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentState) {
                        UPDATE -> {
                            val updateFileListSnap by remember { mutableStateOf(mutableStateListOf<UpdateFile>()) }
                            var updateTick by remember { mutableStateOf(0) }
                            LaunchedEffect(Unit) {
                                launch(Dispatchers.IO) {
                                    updateFileListSnap.addAll(updateFileList)
                                    val downloadFile = File("download")
                                    downloadFile.delete()
                                    downloadFile.mkdirs()

                                    for (file in updateFileListSnap.toTypedArray()) {
                                        val localFile = File(downloadFile, file.file.path)
                                        file.downloadProgress = -1f

                                        if (localFile.parentFile != null && !localFile.parentFile.exists()) {
                                            localFile.parentFile.mkdirs()
                                        }
                                        localFile.createNewFile()

                                        val downloadToken = TOKEN32()
                                        connection?.fetch(
                                            downloadToken.token,
                                            NetHeadBuilder.factory("update::Download")
                                                .put("filepath", file.file.path)
                                                .put("token", downloadToken.token)
                                                .build(),
                                            null,
                                        ) { head, input ->
                                            val totalSize = head.getInt("len")
                                            var readLen = 0
                                            val buffer = ByteArray(1024 * 8)
                                            val fos = FileOutputStream(localFile)
                                            var len = 0;
                                            var lastReported = -1f
                                            while (true) {
                                                if (readLen + buffer.size < totalSize) {
                                                    len = input.read(buffer)
                                                    fos.write(buffer, 0, len)
                                                    readLen += len
                                                } else {
                                                    fos.write(input.readNBytes(totalSize - readLen))
                                                    readLen = totalSize
                                                    fos.flush()
                                                    file.downloadProgress = 1f
                                                    updateTick++
                                                    break
                                                }
                                                val progress = readLen.toFloat() / totalSize
                                                if (progress - lastReported >= 0.01f) {
                                                    lastReported = progress
                                                    file.downloadProgress = progress
                                                    updateTick++
                                                }
                                            }
                                            fos.close()
                                        }
                                        withContext(Dispatchers.Main) {
                                            updateFileListSnap.remove(file)
                                        }
                                    }
                                }
                            }
                            if(updateFileListSnap.isEmpty()) {
                                Button(
                                    onClick = {
                                        launchUpdater(File("download"), "XLink.exe")
                                    }
                                ) {
                                    Text("更新完成，点击重启")
                                }
                            }else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.Update, contentDescription = "Update")
                                            Text("共有 ${updateFileListSnap.size} 个文件需要更新")
                                        }
                                    }
                                    itemsIndexed(
                                        updateFileListSnap,
                                        key = { _, item -> item.file.path }
                                    ) { _, item ->
                                        
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                                .animateItem()
                                        ) {
                                            Text(
                                                text = item.file.name,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            )
                                            LinearProgressIndicator(
                                                progress = {
                                                    updateTick
                                                    val p = item.downloadProgress
                                                    if (p < 0f) 0f else if (p > 1f) 1f else p
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                        }
                        LOADING -> {
                            closeVirtualNet()
                            Column {
                                Text(
                                    "连接中...",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                )
                                // 圆形加载进度
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                LaunchedEffect(Unit) {
                                    thread {
                                        val tmpState = connect {
                                            state = LOADING
                                        }
                                        println("tmpState: $tmpState")
                                        // 先判断要不要update
                                        if(tmpState == SUCCESS) {
                                            val updateNeeded = checkForUpdate()
                                            println("updateNeeded = $updateNeeded")
                                            state = when (updateNeeded) {
                                                UpdateState.ERROR -> {
                                                    ERROR
                                                }
                                                UpdateState.UPDATE -> {
                                                    UPDATE
                                                }
                                                UpdateState.NONE_UPDATE -> {
                                                    SUCCESS
                                                }
                                            }
                                        }else {
                                            state = tmpState
                                        }
                                    }
                                }
                            }
                        }
                        SUCCESS -> {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                // 涵盖创建房间，加入房间和打开文件系统
                                Column (
                                    modifier = Modifier
                                        .width(500.dp)
                                        .background(Color.White)
                                ) {
                                    // 房间号
                                    Column (
                                        modifier = Modifier
                                            .border(
                                                1.dp,
                                                Color(0xFFEFEFEF),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .background(Color.White)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        Surface (
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            color = Color.White
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .align(Alignment.CenterHorizontally)
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                                    .background(Color.White),
                                                text = "我的会话"
                                            )
                                        }
                                        Box (
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFEFEFEF))
                                        ) {
                                            Box (
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.Blue),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column (
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier
                                                        .background(Color.White)
                                                ) {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        modifier = Modifier
                                                            .align(Alignment.CenterHorizontally)
                                                            .fillMaxWidth(),
                                                        text = "邀请码",
                                                        textAlign = TextAlign.Center
                                                    )
                                                    // 大号字体，粗体字，居中
                                                    Text(
                                                        modifier = Modifier
                                                            .align(Alignment.CenterHorizontally)
                                                            .fillMaxWidth(),
                                                        text = getGroupId(),
                                                        textAlign = TextAlign.Center,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    var isCopied by remember { mutableStateOf(false) }
                                                    var currentJob by remember { mutableStateOf<Job?>(null) }

                                                    val scope = rememberCoroutineScope()
                                                    Button(
                                                        onClick = {
                                                            currentJob?.cancel()
                                                            isCopied = true
                                                            currentJob = scope.launch {
                                                                delay(2000)
                                                                isCopied = false
                                                            }
                                                            copy(getGroupId())
                                                        }
                                                    ) {
                                                        Crossfade(targetState = isCopied) {
                                                            if (it) {
                                                                var tarScale by remember { mutableStateOf(0f) }
                                                                val animationScale by animateFloatAsState(
                                                                    targetValue = tarScale,
                                                                    animationSpec = spring(dampingRatio = 0.3f, stiffness = 200f)
                                                                )
                                                                Icon(
                                                                    modifier = Modifier
                                                                        .scale(animationScale),
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "已复制"
                                                                )
                                                                LaunchedEffect(Unit) {
                                                                    tarScale = 1f
                                                                }
                                                            }else {
                                                                Icon(
                                                                    Icons.Default.ContentCopy,
                                                                    contentDescription = "复制"
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    var isConnecting by remember { mutableStateOf(false) }

                                    Column (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "加入会话(✔自己也要复制自己的邀请码并加入)",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            )
                                        }
                                        Column (
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFEFEFEF))
                                                .padding(16.dp)
                                        ) {
                                            var text by remember { mutableStateOf("") }
                                            var enableJoin by remember { mutableStateOf(true) }
                                            OutlinedTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                value = text,
                                                onValueChange = {
                                                    text = it
                                                },
                                                enabled = enableJoin,
                                                placeholder = { Text("请输入邀请码") }
                                            )

                                            var joiningLoadingProgressVisible by remember { mutableStateOf(false) }

                                            AnimatedVisibility(visible = joiningLoadingProgressVisible) {
                                                LinearProgressIndicator(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                )
                                            }

                                            var joinTipVisible by remember { mutableStateOf(false) }
                                            var joinTipState by remember { mutableStateOf(NONE) }
                                            AnimatedVisibility(visible = joinTipVisible) {
                                                Column {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.Start,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            when (joinTipState) {
                                                                SUCCESS_JOIN_WITH_P2P -> "Wintun启动成功 虚拟IP: "
                                                                FAILED -> "加入失败"
                                                                NOT_GROUP_ID -> "邀请码无效"
                                                                NONE -> ""
                                                                NONE_EMPTY -> "邀请码不能为空"
                                                                GROUP_NOT_EXISTS -> "会话不存在"
                                                            },
                                                            color = when (joinTipState) {
                                                                SUCCESS_JOIN_WITH_P2P -> Color.Green
                                                                FAILED -> Color.Red
                                                                NOT_GROUP_ID -> Color.Red
                                                                NONE -> Color.Transparent
                                                                NONE_EMPTY -> Color.Red
                                                                GROUP_NOT_EXISTS -> Color.Red
                                                            }
                                                        )
                                                        if(joinTipState.isSuccess()) {
                                                            SelectionContainer {
                                                                Text(
                                                                    " ${getVirtualNetIP()} ",
                                                                    color = Color.Green
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Row (
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                AnimatedVisibility(
                                                    isConnecting,
                                                    enter = fadeIn() + slideInHorizontally { it / 2 },
                                                    exit = fadeOut() + slideOutHorizontally { it / 2 }
                                                ) {
                                                    Button(
                                                        modifier = Modifier
                                                            .padding(end = 8.dp),
                                                        onClick = {
                                                            thread {
                                                                closeVirtualNet()
                                                                enableJoin = true
                                                                isConnecting = false
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(Color(0xFFE54C58))
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "关闭连接")
                                                    }
                                                }
                                                var isShaking by remember { mutableStateOf(false) }

                                                Button(
                                                    modifier = Modifier
                                                        .shake(
                                                            enabled = isShaking,
                                                            onAnimationEnd = {
                                                                isShaking = false
                                                            }
                                                        ),
                                                    enabled = enableJoin,
                                                    onClick = {
                                                        thread {
                                                            if (joinTipState != NONE) return@thread
                                                            when (joinTipState) {
                                                                NONE -> {
                                                                    if (text.isEmpty()) {
                                                                        joinTipState = NONE_EMPTY
                                                                    } else if (!text.isGroupID()) {
                                                                        joinTipState = NOT_GROUP_ID
                                                                    } else {
                                                                        // 尝试连接
                                                                        enableJoin = false
                                                                        joiningLoadingProgressVisible = true
                                                                        val result = postToVirtualNet(
                                                                            serverAddress,
                                                                            port = 8081,
                                                                            groupID = text,
                                                                            lis = {
                                                                                // 如果断开，则重置
                                                                                println(it.eventType)
                                                                                if (it.eventType == EventType.DISCONNECTED) {
                                                                                    joinTipState = NONE
                                                                                    enableJoin = true
                                                                                    joinTipVisible = false
                                                                                    isConnecting = false

                                                                                    if (overlayState == UserInfos) {
                                                                                        overlayState = None
                                                                                    }
                                                                                }
                                                                            }
                                                                        )
                                                                        joinTipState = result
                                                                        joiningLoadingProgressVisible = false
                                                                        if(result.isSuccess()){
                                                                            joinTipVisible = true
                                                                            isConnecting = true
                                                                        }
                                                                    }
                                                                    if (!joinTipState.isSuccess()) {
                                                                        isShaking = true
                                                                        enableJoin = false
                                                                        // 2秒后收起
                                                                        if (joinTipVisible) return@thread
                                                                        joinTipVisible = true
                                                                        thread {
                                                                            Thread.sleep(2000)
                                                                            joinTipVisible = false
                                                                            joinTipState = NONE
                                                                            enableJoin = true
                                                                        }
                                                                    }
                                                                }
                                                                else -> {}
                                                            }
                                                        }
                                                    },
                                                ) {
                                                    Text("加入")
                                                }
                                            }
                                        }
                                    }

                                    // 显示成员列表
                                    AnimatedVisibility(
                                        visible = isConnecting
                                    ) {
                                        Column (
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0xFFEFEFEF)),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Group, contentDescription = "Group")
                                                Text(
                                                    modifier = Modifier
                                                        .weight(1f),
                                                    text = "查看成员列表"
                                                )
                                                IconButton(
                                                    onClick = {
                                                        overlayState = UserInfos
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Default.ArrowCircleRight,
                                                        contentDescription = "打开会话列表"
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 文件互传


                                }
                            }
                        }

                        ERROR -> {
                            Column {
                                Text(
                                    "ERROR",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Button(
                                    onClick = {
                                        state = LOADING
                                    },
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row {
                                        Icon(Icons.Default.Error, contentDescription = "Error")
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            var overlayVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = overlayState) {
                if(overlayState == None) {
                    overlayVisible = false
                } else {
                    overlayVisible = true
                }
            }
            if(showInfo.value) {
                overlayState = Info
            } else {
                if(overlayState == Info) {
                    overlayState = None
                }
            }

            overlay = {
                // 覆盖层
                AnimatedVisibility(
                    visible = overlayVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .pointerInput(Unit) { // 防止穿透
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                ) {
                    Box (
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x77000000))
                            .onClick {
                                overlayState = None
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFFFFF))
                        ) {
                            Box (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x00FFFFFF))
                            ) {
                                IconButton(
                                    onClick = {
                                        if (overlayState == Info) {
                                            showInfo.value = false
                                        }
                                        overlayState = None
                                    }
                                ) {
                                    Icon(Icons.Default.ArrowCircleLeft, contentDescription = "Close")
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Crossfade(targetState = overlayState) { state ->
                                when (state) {
                                    None -> {}
                                    Info -> {
                                        InfoText(showInfo.value)
                                    }

                                    UserInfos -> {
                                        UserList(overlayState == UserInfos)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return overlay
}

