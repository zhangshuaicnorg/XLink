package org.minecraft.xlink

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
// 配色方案（可全局替换）
// ─────────────────────────────────────────────
private object AppColors {
    val primary       = Color(0xFF2196F3)   // 主蓝
    val primaryDark   = Color(0xFF1565C0)
    val accent        = Color(0xFF00BCD4)   // 青
    val accentLight   = Color(0xFFB2EBF2)
    val surface       = Color(0xFFF8FAFC)
    val surfaceCard   = Color(0xFFFFFFFF)
    val textPrimary   = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)
    val textHint      = Color(0xFF94A3B8)
    val divider       = Color(0xFFE2E8F0)
    val successGreen  = Color(0xFF10B981)
    val warningAmber  = Color(0xFFF59E0B)
    val gradientStart = Color(0xFF1565C0)
    val gradientEnd   = Color(0xFF00ACC1)
    val tagBg         = Color(0xFFE3F2FD)
    val tagText       = Color(0xFF1565C0)
}

@Composable
fun InfoText(isEnter: Boolean) {
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
            AboutScreen(
                appName = "XLink",
                version = "1.0.0",
                buildDate = "2026-08-15",
                websiteUrl = "https://xlink.dev",
                githubUrl = "https://github.com/xlink-project/xlink",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
    LaunchedEffect(Unit) {
        isVisible = isEnter
    }
}


// ─────────────────────────────────────────────
// 入口 Composable
// ─────────────────────────────────────────────
@Composable
fun AboutScreen(
    appName: String = "YourApp",
    version: String = "1.0.0",
    buildDate: String = "2026-08-15",
    websiteUrl: String = "https://yourapp.dev",
    githubUrl: String = "https://github.com/you/yourapp",
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 48.dp)
        ) {
            // ── 顶部渐变 Banner ──
            HeroBanner(appName = appName)

            // ── 简介 ──
            IntroSection(modifier = Modifier.padding(horizontal = 24.dp))

            // ── 核心特性卡片 ──
            FeatureGrid(modifier = Modifier.padding(horizontal = 24.dp))

            // ── 连接模式对比 ──
            ConnectionModeSection(modifier = Modifier.padding(horizontal = 24.dp))

            // ── 技术原理 ──
            TechPrincipleSection(modifier = Modifier.padding(horizontal = 24.dp))

            // ── FAQ ──
            FaqSection(modifier = Modifier.padding(horizontal = 24.dp))

            // ── 版本 & 链接 ──
            FooterSection(
                version = version,
                buildDate = buildDate,
                websiteUrl = websiteUrl,
                githubUrl = githubUrl,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 1. 顶部 Banner
// ─────────────────────────────────────────────
@Composable
private fun HeroBanner(appName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(AppColors.gradientStart, AppColors.gradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo 占位（替换成你的 Icon/Image）
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = appName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "轻量级虚拟局域网 · 开箱即用",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 2. 简介
// ─────────────────────────────────────────────
@Composable
private fun IntroSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(top = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionTitle(icon = Icons.Default.Info, title = "简介")
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("本软件基于 ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = AppColors.primary)) {
                        append("WinTun")
                    }
                    append(" 虚拟网卡技术，将分布在不同网络环境中的 ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = AppColors.primary)) {
                        append("N 台主机")
                    }
                    append(" 组建成一个逻辑上的局域网。所有节点如同连接在同一个交换机上，可以直接通过虚拟 IP 互访，")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = AppColors.accent)) {
                        append("无需关闭系统防火墙")
                    }
                    append("，无需路由器端口映射，无需任何网络基础。")
                },
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 标签
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("WinTun", "P2P 直连", "Relay 保底", "零配置", "免防火墙").forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.tagBg
                    ) {
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            color = AppColors.tagText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 3. 核心特性（2×2 网格）
// ─────────────────────────────────────────────
@Composable
private fun FeatureGrid(modifier: Modifier = Modifier) {
    val features = listOf(
        FeatureItem(
            icon = Icons.Default.Lan,
            title = "虚拟局域网",
            desc = "基于 WinTun 驱动创建虚拟网卡，N 台跨网段主机组网，如同在同一 LAN 内。"
        ),
        FeatureItem(
            icon = Icons.Default.SwapHoriz,
            title = "P2P 直连优先",
            desc = "优先尝试 NAT 穿透建立点对点直连，延迟最低、带宽不受服务器限制。"
        ),
        FeatureItem(
            icon = Icons.Default.Cloud,
            title = "Relay 自动保底",
            desc = "当 P2P 打洞失败时，自动切换至 Relay 中继模式，确保连接永不中断。"
        ),
        FeatureItem(
            icon = Icons.Default.Shield,
            title = "无需关闭防火墙",
            desc = "通过正确的驱动层注册与防火墙规则协商，全程兼容 Windows Defender 防火墙。"
        )
    )

    Column(modifier = modifier.padding(top = 20.dp)) {
        SectionTitle(icon = Icons.Default.Star, title = "核心特性")
        Spacer(modifier = Modifier.height(12.dp))

        // 2列网格
        features.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    FeatureCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 如果最后一行只有1个，补占位
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val desc: String
)

@Composable
private fun FeatureCard(item: FeatureItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(IntrinsicSize.Max),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.desc,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = AppColors.textSecondary
            )
        }
    }
}

// ─────────────────────────────────────────────
// 4. 连接模式对比（可切换 Tab）
// ─────────────────────────────────────────────
@Composable
private fun ConnectionModeSection(modifier: Modifier = Modifier) {
    var selectedMode by remember { mutableIntStateOf(0) } // 0=P2P, 1=Relay

    Column(modifier = modifier.padding(top = 20.dp)) {
        SectionTitle(icon = Icons.AutoMirrored.Filled.CompareArrows, title = "连接模式")
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Tab 切换
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.surface)
                        .padding(4.dp)
                ) {
                    listOf("P2P 直连", "Relay 中继").forEachIndexed { index, label ->
                        val isSelected = selectedMode == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) AppColors.primary
                                    else Color.Transparent
                                )
                                .clickable { selectedMode = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else AppColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 内容区（带动画）
                Crossfade(
                    targetState = selectedMode,
                    animationSpec = tween(300, easing = EaseInOutCubic),
                    label = "modeSwitch"
                ) { mode ->
                    when (mode) {
                        0 -> ModeDetail(
                            color = AppColors.successGreen,
                            items = listOf(
                                "延迟" to "最低（通常 < 30ms）",
                                "带宽" to "不受服务器限制，取决于双方上行带宽",
                                "原理" to "通过 STUN/TURN 进行 NAT 穿透，建立 UDP/TCP 直连",
                                "适用" to "双方不在对称 NAT / 严格防火墙之后"
                            )
                        )
                        1 -> ModeDetail(
                            color = AppColors.warningAmber,
                            items = listOf(
                                "延迟" to "略高（多一跳中继）",
                                "带宽" to "受限于 Relay 服务器带宽",
                                "原理" to "数据经 Relay 服务器转发，对两端网络无要求",
                                "适用" to "P2P 打洞失败时的保底方案，确保 100% 连通"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeDetail(
    color: Color,
    items: List<Pair<String, String>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = 6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)) {
                            append("$label：")
                        }
                        withStyle(SpanStyle(color = AppColors.textSecondary)) {
                            append(value)
                        }
                    },
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// 5. 技术原理（简化流程图用文字表达）
// ─────────────────────────────────────────────
@Composable
private fun TechPrincipleSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = 20.dp)) {
        SectionTitle(icon = Icons.Default.Memory, title = "技术原理")
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 流程步骤
                val steps = listOf(
                    "安装 WinTun 虚拟网卡驱动，创建虚拟网络适配器",
                    "节点启动后向协调服务器注册，获取虚拟 IP",
                    "尝试 NAT 穿透（STUN）建立 P2P 直连通道",
                    "若 P2P 失败，自动回退至 Relay 中继转发",
                    "流量经虚拟网卡路由，操作系统层面如同局域网"
                )

                steps.forEachIndexed { index, step ->
                    Row(verticalAlignment = Alignment.Top) {
                        // 序号圆
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AppColors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = step,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = AppColors.textPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 连接线
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .offset(x = 11.dp)
                                .width(2.dp)
                                .height(12.dp)
                                .background(AppColors.divider)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 6. FAQ（可展开/折叠）
// ─────────────────────────────────────────────
@Composable
private fun FaqSection(modifier: Modifier = Modifier) {
    val faqs = listOf(
        FaqItem(
            q = "需要付费吗？",
            a = "XLink 完全开源免费，甚至可以自己部署服务器。"
        ),
        FaqItem(
            q = "需要关闭 Windows 防火墙吗？",
            a = "不需要。软件通过 WinTun 驱动在系统网络栈中正确注册虚拟适配器，Windows 防火墙会自动识别并放行虚拟网段流量。"
        ),
        FaqItem(
            q = "什么是 WinTun？",
            a = "WinTun 是一个用于 Windows 的虚拟网卡驱动，用于创建虚拟网络适配器。XLink 使用 WinTun 来创建虚拟网卡，实现局域网联机。"
        ),
        FaqItem(
            q = "最多支持多少台设备同时组网？",
            a = "理论上无硬性上限，实际取决于 Relay 服务器承载能力。P2P 直连模式下，节点数量增加不会影响已有直连对的带宽和延迟。"
        ),
        FaqItem(
            q = "什么是 NAT 穿透？",
            a = "NAT 穿透是指通过技术手段突破网络地址转换（NAT）的限制，实现内部网络设备之间的通信。XLink 使用 STUN/TURN 技术进行 NAT 穿透，建立 P2P 直连通道。"
        ),
        FaqItem(
            q = "如果我是对称 NAT 那么我是不是无法连接？",
            a = "不会，如果对称 NAT 导致 P2P 打洞失败，会自动回退至 Relay 中继转发。"
        ),
        FaqItem(
            q = "什么是 STUN/TURN？",
            a = "STUN 是一种网络协议，用于帮助 NAT 穿透，通过向 STUN 服务器发送请求，获取自己的公网 IP 和端口。TURN 是一种中继服务器，当 STUN 失败时，可以使用 TURN 服务器进行中继，实现 NAT 穿透。XLink 使用 STUN/TURN 技术进行 NAT 穿透，建立 P2P 直连通道。"
        ),
        FaqItem(
            q = "什么是 P2P？",
            a = "P2P 是 Peer to Peer 的缩写，即点对点。P2P 模式下，节点之间直接建立连接，实现高速低延迟通信。XLink 使用 P2P 模式，在节点之间建立直接连接，实现局域网联机。"
        ),
        FaqItem(
            q = "虚拟 IP 如何分配？",
            a = "加入网络时由协调服务器自动分配（默认 10.8.x.x）。"
        ),
        FaqItem(
            q = "支持跨操作系统吗？",
            a = "当前版本仅支持 Windows（依赖 WinTun 驱动）。macOS / Linux 版本在规划中。"
        ),
        FaqItem(
            q = "支持 Minecraft 远程连接功能吗？",
            a = "支持，获取到好友的邀请码，填写邀请码并加入，好友也需要加入同样邀请码的会话，然后就可以通过服务器给你分配到 IP 进行局域网联机了。"
        ),
        FaqItem(
            q = "我已经加入好友的会话，现在应该怎么联机？",
            a = "让好友把服务器分配给他的虚拟 IP 发给你，你只需要通过 IP:端口 在游戏中直接连接就可以啦。"
        ),
        FaqItem(
            q = "如何部署服务端？",
            a = "从 github 主页获取到服务器主体（通常是一个 jar 文件），然后在服务器上运行它即可。"
        ),
        FaqItem(
            q = "我没有公网服务器，如何部署服务端？",
            a = "可以通过内网穿透工具，如 ngrok 或 frp，将本地服务器暴露到公网。"
        ),
        FaqItem(
            q = "我既然都有内网穿透工具了，我为什么还要用 XLink？",
            a = "内网穿透工具往往不是免费的，流量额度有限，但是如果借助内网穿透实现了 XLink，由于服务端只是交换 IP，少部分情况下会触发 Relay， 所以只会花费很少的内网穿透流量。"
        ),
        FaqItem(
            q = "会影响我正常上网吗？",
            a = "完全不影响，与正常适配器之前不会产生冲突，虚拟适配器会在断开连接或者关闭程序时自动消除。"
        )


    )

    Column(modifier = modifier.padding(top = 20.dp)) {
        SectionTitle(icon = Icons.AutoMirrored.Filled.Help, title = "常见问题")
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                faqs.forEachIndexed { index, faq ->
                    FaqRow(faq = faq)
                    if (index < faqs.lastIndex) {
                        HorizontalDivider(
                            color = AppColors.divider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class FaqItem(val q: String, val a: String)

@Composable
private fun FaqRow(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = faq.q,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = AppColors.textHint,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(250)) + fadeIn(tween(250)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
        ) {
            Text(
                text = faq.a,
                fontSize = 13.sp,
                lineHeight = 21.sp,
                color = AppColors.textSecondary,
                modifier = Modifier.padding(top = 10.dp, start = 28.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 7. 底部：版本信息 & 链接
// ─────────────────────────────────────────────
@Composable
private fun FooterSection(
    version: String,
    buildDate: String,
    websiteUrl: String,
    githubUrl: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(color = AppColors.divider, modifier = Modifier.padding(bottom = 20.dp))

        Text(
            text = "版本 $version · 构建于 $buildDate",
            fontSize = 12.sp,
            color = AppColors.textHint
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 官网按钮
            OutlinedButton(
                onClick = { browse(websiteUrl) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AppColors.primary)
            ) {
                Icon(Icons.Default.Language, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("官网", fontSize = 13.sp)
            }

            // GitHub 按钮
            OutlinedButton(
                onClick = { browse(githubUrl) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AppColors.primary)
            ) {
                Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("GitHub", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "© 2026 YourApp. All rights reserved.",
            fontSize = 11.sp,
            color = AppColors.textHint
        )
    }
}

// ─────────────────────────────────────────────
// 通用小组件
// ─────────────────────────────────────────────
@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
    }
}

// 旋转扩展用（ExpandMore 箭头旋转动画）
private fun Modifier.rotate(degrees: Float): Modifier = this.then(
    Modifier.graphicsLayer { rotationZ = degrees }
)

