package org.minecraft.xlink

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineExceptionHandler
import org.javatools.net.p2pvpn.wintun.WinTunNative
import org.minecraft.xlink.app.App
import org.minecraft.xlink.app.xLinkLogoPainter

fun isRunningAsAdmin(): Boolean {
    try {
        val p = Runtime.getRuntime().exec("net session")
        val isAdmin = p.waitFor() == 0
        if(isAdmin) {
            // 预加载Wintun
            // 自动加载内部类然后自动初始化
            WinTunNative.INSTANCE
        }
        return isAdmin
    } catch (_: Exception) { return false }
}

fun main() = application {
    val log = java.io.PrintWriter(java.io.FileWriter("app.log", true))
    System.setErr(java.io.PrintStream("app-err.log"))

    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        log.println("${t.name} uncaught: ${e.message}")
        e.printStackTrace(log)
        log.flush()
    }

    if (!isRunningAsAdmin()) {
        throw RuntimeException("Must run as Administrator!")
    }
    val windowState = rememberWindowState(
        width = 600.dp,
        height = 800.dp,
        position = WindowPosition(Alignment.Center)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "XLink",
        undecorated = true,
        resizable = false,
        transparent = true,
        state = windowState,
        icon = xLinkLogoPainter()
    ) {
        // 整个窗口区域（含阴影空间）
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            var overlay: @Composable () -> Unit = {}
            // 实际内容区域，内缩留出阴影空间
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)  // ← 关键2：每边留 5dp 给阴影
                    .clip(RoundedCornerShape(8.dp))
                    .border(1f.dp, Color(0x45787878), RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                val isShowInfo = remember { mutableStateOf(false) }
                WindowDraggableArea(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(1.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "XLink",
                            modifier = Modifier
                                .padding(start = 16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // 自定义标题栏
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp),
                            onClick = {
                                isShowInfo.value = !isShowInfo.value
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color.Black
                            )
                        }
                        IconButton(
                            modifier = Modifier.padding(5.dp),
                            onClick = {
                                // 最小化
                                windowState.isMinimized = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloseFullscreen,
                                contentDescription = "Minimize",
                                tint = Color.Black
                            )
                        }
                        IconButton(
                            modifier = Modifier.padding(5.dp),
                            onClick = { exitApplication() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Black
                            )
                        }
                    }
                }
                // 应用内容区域
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    overlay = App(isShowInfo)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                overlay()
            }
        }
    }
}
