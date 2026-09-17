plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

            implementation("io.coil-kt.coil3:coil-compose:3.0.0")
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0")

            implementation(files("../lib/JavaTools.jar"))

            implementation("org.jetbrains.compose.material3:material3:1.9.0")

            implementation("net.java.dev.jna:jna:5.15.0")
            implementation("net.java.dev.jna:jna-platform:5.15.0")

            implementation("dev.chrisbanes.haze:haze:1.5.2")

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}