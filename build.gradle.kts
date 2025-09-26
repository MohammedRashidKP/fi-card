plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.closemates.games"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

}


kotlin {
    // Force Kotlin compiler to use Java 17 bytecode
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}