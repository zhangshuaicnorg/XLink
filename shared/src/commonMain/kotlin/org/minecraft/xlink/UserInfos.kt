package org.minecraft.xlink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.javatools.net.ping.PingServer
import org.minecraft.xlink.state.P2PState
import org.minecraft.xlink.state.P2PState.*

@Composable
fun UserList(isEnter: Boolean) {
    var isVisible by remember { mutableStateOf(!isEnter) }
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically {it / 2},
        exit = slideOutVertically()
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            UserListScreen()
        }
    }
    LaunchedEffect(Unit) {
        isVisible = isEnter
    }
}

@Composable
fun UserListScreen() {
    val users = remember { mutableStateMapOf<String, Peer>() }
    LaunchedEffect(Unit) {
        registerUserLis(users)
    }
    val self = Peer(getVirtualNetIP() ?: "", P2PState.SELF)
    
    // 将 map 转换为列表，并按照虚拟IP最后一段数字升序排序
    val userList by remember {
        derivedStateOf {
            users.keys.sortedBy { ip ->
                // 提取IP地址最后一段数字进行排序 (10.8.0.x)
                ip.split(".").lastOrNull()?.toIntOrNull() ?: 0
            }
        }
    }
    
    LazyColumn (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.padding(top = 16.dp, bottom = 2.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(19.2f.dp))
                        Icon(Icons.Default.Wifi, contentDescription = "LAN")
                        Text(
                            "已有${users.size + 1}个用户加入会话",
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
        item {
            PeerShow(0, self)
        }
        itemsIndexed(
            items = userList,
            key = { _, userAddress -> userAddress } // 设置唯一 key
        ) { index, userAddress ->
            PeerShow(index, users[userAddress]!!)
        }
    }
}

val port = 34577
val pinger: PingServer = PingServer(port)

@Composable
fun LazyItemScope.PeerShow(index: Int, peer: Peer) {
//    var version by remember { mutableStateOf(0L) }
//    LaunchedEffect(Unit) {
//        if (peer.p2PState == SELF) return@LaunchedEffect
//        while (true) {
//            delay(1000)
//            println("Pinging ${peer.virtualAddress}")
//            pinger.ping(peer.virtualAddress, port, 500)
//            version++
//        }
//    }
    // 使用 animateItem() 实现进场动画，不影响布局计算
    Card (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .animateItem(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 圆形序号
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = "PEER: ${peer.virtualAddress}"
            )
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                when (peer.p2PState) {
                    CONNECTING -> {
                        Icon(Icons.Default.CastConnected, contentDescription = "Info")
                        Text("连接中")
                    }

                    P2P -> {
                        Icon(Icons.Default.Share, contentDescription = "Info")
                        Text("P2P")
                    }

                    DELAY -> {
                        Icon(Icons.Default.Hub, contentDescription = "Info")
                        Text("RELAY")
                    }

                    SELF -> {
                        Icon(Icons.Default.Computer, contentDescription = "Info")
                        Text("本机")
                    }
                    DISCONNECTED -> {
                        Icon(Icons.Default.WifiTetheringOff, contentDescription = "Info")
                        Text("已断开")
                    }
                }
            }
        }
    }
}
