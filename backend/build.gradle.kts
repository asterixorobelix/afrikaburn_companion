val ktor_version = "3.1.3"
val kotlin_version = "2.1.21"
val logback_version = "1.5.18"
val exposed_version = "0.61.0"

plugins {
    kotlin("jvm") version "2.1.21"
    id("io.ktor.plugin") version "3.2.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "io.asterixorobelix.afrikaburn"
version = "0.0.1"

application {
    mainClass.set("io.asterixorobelix.afrikaburn.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    
    // Content & Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    
    // Security
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-rate-limit-jvm:$ktor_version")
    implementation("com.auth0:java-jwt:4.5.0")
    
    // Database (Exposed ORM)
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.zaxxer:HikariCP:7.0.2")
    
    // Database Drivers
    implementation("com.h2database:h2:2.3.232")         // Development
    implementation("org.postgresql:postgresql:42.7.7")  // Production
    
    // HTTP Client for AI APIs
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.mockk:mockk:1.14.2")
}

// Fat JAR for deployment
tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.asterixorobelix.afrikaburn.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map { 
        if (it.isDirectory) it else zipTree(it) 
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Detekt configuration
detekt {
    toolVersion = "1.23.1"
    config.from(file("detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
