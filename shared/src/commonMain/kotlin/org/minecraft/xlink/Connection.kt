package org.minecraft.xlink


import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.launch
import org.javatools.net.p2pvpn.client.PeerState
import org.javatools.net.p2pvpn.client.VpnClient
import org.javatools.net.p2pvpn.event.EventType
import org.javatools.net.p2pvpn.event.VpnEventListener
import org.javatools.net.tcp.NetHeadBuilder
import org.javatools.net.tcp.TCPChannel
import org.jetbrains.skia.Pattern
import java.io.IOException
import java.net.InetSocketAddress


const val serverAddress: String = "8.148.79.60"
//val serverAddress: String = "127.0.0.1"


var connection: TCPChannel? = null
fun connect(onDisconnect: () -> Unit): LoadState {
    println("connecting...")
    if (connection != null) {
        connection?.disconnect()
    }
    connection = connectServer(serverAddress, 8080)
    if (connection == null) return LoadState.ERROR
    val result = connection!!.fetch("can sign", NetHeadBuilder.factory().path("can sign").build(), null)
    val canSign: Boolean = result.head!!.getBoolean("can sign")
    connection!!.addDisconnectListener { onDisconnect() }

    return if (canSign) {
        // 防止重连不刷新邀请码
        groupID = null
        LoadState.SUCCESS
    } else {
        LoadState.ERROR
    }
}

fun connectServer(address: String, port: Int): TCPChannel? {
    try {
        val channel = TCPChannel(address, port)
        return channel
    }catch (e: IOException) {
        return null
    }
}

var groupID: String? = null

fun getGroupId(): String {
    if(groupID != null) return groupID!!
    if(connection != null) {
        groupID = connection!!.fetch(
            "get group id",
            NetHeadBuilder.factory().path("get group id").build(),
            null
        ).head!!.getString("GroupID")
    }
    return groupID ?: ""
}

val groupIDMatch: Pattern = Pattern.compile("[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}")
fun String.isGroupID(): Boolean {
    return groupIDMatch.matcher(this).matches()
}

var vpn: VpnClient? = null
var joinedGroupID: String? = null

fun postToVirtualNet(
    address: String,
    port: Int,
    groupID: String,
    lis: VpnEventListener
): JoinTipState {
    // 先尝试服务器校验groupID是否存在
    if(connection != null) {
        val result = connection!!.fetch(
            "check group id result",
            NetHeadBuilder.factory().path("check group id").put("groupID", groupID).build(),
            null
        )
        if(!result.head!!.getBoolean("checked")) return JoinTipState.GROUP_NOT_EXISTS
    }
    println("开始尝试组网...")
    vpn = VpnClient()

    // 运行时事件监听（start完成后持续生效）
    vpn?.addListener(lis)

    vpn?.virtualIp

    // 启动并等待结果
    val result = vpn?.joinRoom(address, port, groupID)?.get()

    return if(result != null && result.isSuccess) {
        joinedGroupID = groupID
        vpn?.addListener { vpnEvent ->
            when(vpnEvent.eventType) {
                EventType.CONNECTING -> {}
                EventType.CONNECTED -> {}
                EventType.DISCONNECTED -> {}
                EventType.JOINING_ROOM -> {}
                EventType.ROOM_JOINED -> {}
                EventType.ROOM_JOIN_FAILED -> {}
                EventType.ADAPTER_CREATED -> {}
                EventType.ADAPTER_FAILED -> {}
                EventType.PEER_JOINED -> {}
                EventType.PEER_LEFT -> {}
                EventType.P2P_CONNECTED -> {}
                EventType.P2P_DEGRADED_TO_RELAY -> {}
                EventType.PUNCH_FAILED -> {}
                EventType.SEND_FAILED -> {}
                EventType.HEARTBEAT_TIMEOUT -> {}
                EventType.ERROR -> {}
                EventType.SERVER_STARTED -> {}
                EventType.SERVER_STOPPED -> {
                    repeat(10) {
                        println("----")
                    }
                    closeVirtualNet()
                    repeat(10) {
                        println("----")
                    }
                }
                EventType.NODE_REGISTERED -> {}
                EventType.NODE_ONLINE -> {}
                EventType.NODE_OFFLINE -> {}
                EventType.ROOM_CREATED -> {}
                EventType.ROOM_DESTROYED -> {}
                EventType.PACKET_FORWARDED -> {}
            }
        }
        JoinTipState.SUCCESS_JOIN_WITH_P2P

    } else {
        JoinTipState.FAILED
    }
}

fun closeVirtualNet() {
    vpn?.leaveRoom()
    vpn?.close()
    joinedGroupID = null
}

fun getVirtualNetIP(): String? {
    return vpn?.virtualIp
}

fun registerUserLis(users: SnapshotStateMap<String, Peer>) {
    val peersByIp = vpn?.peersByIp
    if (peersByIp != null) {
        for ((ip, peer) in peersByIp) {
            val p2pState: P2PState = when (peer.state) {
                PeerState.DISCOVERED, PeerState.PUNCHING -> P2PState.CONNECTING
                PeerState.P2P_CONNECTED -> P2PState.P2P
                PeerState.RELAYING -> P2PState.DELAY
                PeerState.DISCONNECTED -> P2PState.DISCONNECTED
            }
            users[ip] = Peer(ip, p2pState)
        }
    }
    vpn?.addListener { vpnEvent ->
        when (vpnEvent.eventType) {
            EventType.CONNECTING -> {

            }
            EventType.CONNECTED -> {

            }
            EventType.DISCONNECTED -> {

            }
            EventType.JOINING_ROOM -> {

            }
            EventType.ROOM_JOINED -> {

            }
            EventType.ROOM_JOIN_FAILED -> {

            }
            EventType.ADAPTER_CREATED -> {

            }
            EventType.ADAPTER_FAILED -> {

            }
            EventType.PEER_JOINED -> {
                val data = vpnEvent.getData<Array<Any>>()
                val peerVirtualAddress = data[0] as String
                repeat(10) {
                    println("----")
                }
                println("peerVirtualAddress join: $peerVirtualAddress")
                repeat(10) {
                    println("----")
                }
                val peerInetSocketAddress: InetSocketAddress = data[1] as InetSocketAddress
                // 触发重组 - 确保在主线程更新状态
                kotlinx.coroutines.MainScope().launch {
                    users[peerVirtualAddress] = Peer(peerVirtualAddress)
                }
            }
            EventType.PEER_LEFT -> {
                val peerVirtualAddress = vpnEvent.getData<String>()
                println("peerVirtualAddress left: $peerVirtualAddress")
                // 重组 - 确保在主线程更新状态
                kotlinx.coroutines.MainScope().launch {
                    users.remove(peerVirtualAddress)
                }
            }
            EventType.P2P_CONNECTED -> {
                val peerVirtualAddress = vpnEvent.getData<String>()
                val peer = users[peerVirtualAddress]
                // 确保在主线程更新状态
                kotlinx.coroutines.MainScope().launch {
                    users.remove(peerVirtualAddress)
                    users[peerVirtualAddress] = peer?.transferP2PState(P2PState.P2P) ?: Peer(peerVirtualAddress, P2PState.P2P)
                }
                println("P2P_CONNECTED: $peerVirtualAddress")
            }
            EventType.P2P_DEGRADED_TO_RELAY -> {
                val peerVirtualAddress = vpnEvent.getData<String>()
                val peer = users[peerVirtualAddress]
                // 确保在主线程更新状态
                kotlinx.coroutines.MainScope().launch {
                    users.remove(peerVirtualAddress)
                    users[peerVirtualAddress] = peer?.transferP2PState(P2PState.DELAY) ?: Peer(peerVirtualAddress, P2PState.DELAY)
                }
                println("P2P_DEGRADED_TO_RELAY: $peerVirtualAddress")
            }
            EventType.PUNCH_FAILED -> {

            }
            EventType.SEND_FAILED -> {

            }
            EventType.HEARTBEAT_TIMEOUT -> {

            }
            EventType.ERROR -> {

            }
            EventType.SERVER_STARTED -> {

            }
            EventType.SERVER_STOPPED -> {

            }
            EventType.NODE_REGISTERED -> {

            }
            EventType.NODE_ONLINE -> {

            }
            EventType.NODE_OFFLINE -> {

            }
            EventType.ROOM_CREATED -> {

            }
            EventType.ROOM_DESTROYED -> {

            }
            EventType.PACKET_FORWARDED -> {

            }
        }
    }

}



