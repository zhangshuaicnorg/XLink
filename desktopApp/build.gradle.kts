import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

    implementation("io.coil-kt.coil3:coil-compose:3.0.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0")

    implementation("net.java.dev.jna:jna:5.15.0")
    implementation("net.java.dev.jna:jna-platform:5.15.0")

    implementation(files("../lib/JavaTools.jar"))
    implementation("dev.chrisbanes.haze:haze:1.5.2")

}

compose.desktop {
    application {
        mainClass = "org.minecraft.xlink.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            windows {
                iconFile = File(".pack_res/XLink_logo.ico")
            }

            packageName = "XLink"
            packageVersion = "1.0.0"
        }
    }
}