package org.minecraft.xlink

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun copy(content: String) {
    java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(java.awt.datatransfer.StringSelection(content), null)
}

actual fun browse(url: String) {
    java.awt.Desktop.getDesktop().browse(java.net.URI(url))
}
