plugins {
    java
    application
    id("stellatedroller.jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.spongepowered.math)

    implementation(platform(libs.lwjgl.bom))
    runtimeOnly(platform(libs.lwjgl.bom))
    for (lwjgl in listOf(
        libs.lwjgl.asProvider(), libs.lwjgl.glfw, libs.lwjgl.jemalloc, libs.lwjgl.nanovg,
        libs.lwjgl.nuklear, libs.lwjgl.opengl
    )) {
        implementation(lwjgl)
        runtimeOnly(variantOf(lwjgl) {
            classifier("natives-linux")
        })
    }

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.jupiter.engine)
}

application {
    applicationName = "StellatedRoller"
    mainClass.set("net.octyl.stellatedroller.StellatedRoller")
    applicationDefaultJvmArgs = listOf("-Xmx100M")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
