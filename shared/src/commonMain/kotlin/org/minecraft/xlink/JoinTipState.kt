package org.minecraft.xlink

enum class JoinTipState {
    NONE, FAILED, SUCCESS_JOIN_WITH_P2P, NOT_GROUP_ID, NONE_EMPTY, GROUP_NOT_EXISTS
}

fun JoinTipState.isSuccess(): Boolean {
    return when(this) {
        JoinTipState.SUCCESS_JOIN_WITH_P2P -> true
        else -> false
    }
}