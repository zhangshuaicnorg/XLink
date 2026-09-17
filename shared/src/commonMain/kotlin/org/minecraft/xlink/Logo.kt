package org.minecraft.xlink

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import xlink.shared.generated.resources.Res
import xlink.shared.generated.resources.XLink_logo
import xlink.shared.generated.resources.XLink_logo_ico

@Composable
fun xLinkLogoPainter(): Painter {
    return painterResource(Res.drawable.XLink_logo)
}
@Composable
fun xLinkLogoIconPainter(): Painter {
    return painterResource(Res.drawable.XLink_logo_ico)
}
