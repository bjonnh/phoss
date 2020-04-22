plugins {
    java
    kotlin("jvm") version "1.4-M1"
    kotlin("plugin.serialization") version "1.4-M1"
}

group = "net.nprod"
version = "0.0.1-SNAPSHOT"

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.microutils:kotlin-logging:1.7.9")
    implementation("org.slf4j", "slf4j-log4j12", "1.7.29")
    implementation("org.openscience.cdk", "cdk-bundle", "2.3")
    implementation("org.apache.commons", "commons-compress", "1.20")
    implementation("org.apache.velocity", "velocity-engine-core", "2.2")
    implementation("org.apache.velocity.tools", "velocity-tools-generic", "3.0")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    //implementation("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.7.1") // We keep that until kotlin compiler bug corrected
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_13
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "13"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "13"
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}