package org.minecraft.xlink

import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

// 定义 DwmApi 接口，映射到 dwmapi.dll
interface DwmApi : StdCallLibrary {
    companion object {
        val INSTANCE: DwmApi = Native.load("dwmapi", DwmApi::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }

    // DwmSetWindowAttribute 函数签名
    // hwnd: 窗口句柄
    // dwAttribute: 要设置的属性
    // pvAttribute: 属性值的指针
    // cbAttribute: 属性值的大小
    fun DwmSetWindowAttribute(
        hwnd: WinDef.HWND,
        dwAttribute: WinDef.DWORD,
        pvAttribute: WinDef.LPVOID,
        cbAttribute: WinDef.DWORD
    ): WinNT.HRESULT
}

// DWMWINDOWATTRIBUTE 枚举值，用于设置窗口圆角偏好
const val DWMWA_WINDOW_CORNER_PREFERENCE = 33

// DWM_WINDOW_CORNER_PREFERENCE 枚举值
const val DWMWCP_DEFAULT = 0     // 让系统决定
const val DWMWCP_DONOTROUND = 1  // 不圆角
const val DWMWCP_ROUND = 2       // 圆角
const val DWMWCP_ROUNDSMALL = 3  // 小圆角