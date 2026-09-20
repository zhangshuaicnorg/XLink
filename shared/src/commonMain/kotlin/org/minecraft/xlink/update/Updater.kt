package org.minecraft.xlink.update

import net.sf.json.JSONObject
import org.javatools.io.FileHash
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.exitProcess

fun parseJSONManifest(node: JSONObject, parent: File?, list: MutableList<UpdateFile>) {
    val fileName: String = node.getString("filename")
    if(node.contains("hash")) {
        val hash: String = node.getString("hash")
        // 是个文件
        list.add(UpdateFile(File(parent, fileName), hash, node.getLong("size")))
    } else {
        // 是个文件夹
        val files = node.getJSONArray("files")
        for (index in 0 until files.size) {
            parseJSONManifest(files.getJSONObject(index), File(parent, fileName), list)
        }
    }
}

fun checkAllUpdateFileAndCleanManifest(manifest: CopyOnWriteArrayList<UpdateFile>): Boolean {
    var needUpdate = false
    val toRemove = mutableListOf<UpdateFile>()

    for (file in manifest.toList()) {            // 遍历的是迭代器快照，不会 CME
        if (!file.file.exists() || FileHash(file.file).sha256() != file.hashcode) {
            needUpdate = true
        } else {
            toRemove += file
        }
    }
    manifest.removeAll(toRemove.toSet())        // 批量删，一次写入
    return needUpdate
}

/**
 * 下载完成后调用此函数，然后退出自身
 */
fun launchUpdater(tempDir: File, appExeName: String) {
    val updaterExe = File("updater.exe")
    require(updaterExe.exists()) { "找不到 updater.exe" }

    ProcessBuilder(
        updaterExe.absolutePath,
        tempDir.absolutePath,   // 参数1: 临时目录
        appExeName              // 参数2: exe名称
    ).directory(updaterExe.parentFile)
        .inheritIO()
        .start()

    // 短暂延迟让 updater 启动，然后主程序退出
    Thread.sleep(300)
    println("[App] 退出，交由 updater 完成替换")
    exitProcess(0)
}

data class UpdateFile(
    var file: File,
    val hashcode: String,
    val size: Long,
    var downloadProgress: Float = -1f
) {
    override fun toString(): String {
        return "UpdateFile(file=$file, hashcode='$hashcode')"
    }
}

