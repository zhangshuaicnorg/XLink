package org.minecraft.xlink

data class Peer(
    val virtualAddress: String,
    var p2PState: P2PState = P2PState.CONNECTING,
    var delay: Double = -1.0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(virtualAddress == (other as? Peer)?.virtualAddress) return true
        return false
    }
    override fun hashCode(): Int {
        var result = virtualAddress.hashCode()
        result = 31 * result + p2PState.hashCode()
        return result
    }
    fun transferP2PState(p2PState: P2PState): Peer = Peer(virtualAddress, p2PState)
}
