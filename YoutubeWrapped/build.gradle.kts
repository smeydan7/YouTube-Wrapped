import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktorVersion = "2.3.4"

compose.desktop {
    application {
        mainClass = "MainKt" // Make sure this is correct
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "YouTubeWrapped"
            packageVersion = "1.3.0"
        }
    }
}

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "org.example"
version = "0.3.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.ui:ui:1.5.1")
    implementation("org.jetbrains.compose.foundation:foundation:1.5.1")
    implementation("org.jetbrains.compose.material3:material3:1.5.1")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.5.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.8.0")

    // http client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    //
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    

    implementation("io.github.ehsannarmani:compose-charts:0.1.2")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")


}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)

}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}