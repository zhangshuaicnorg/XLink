package org.minecraft.xlink

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun copy(content: String)
expect fun browse(url: String)