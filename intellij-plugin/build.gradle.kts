plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "io.testseer"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("io.testseer:testseer-v01:0.1.0-SNAPSHOT")

    intellijPlatform {
        intellijIdeaCommunity("2024.2.5")
        bundledPlugin("com.intellij.java")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellijPlatform {
    pluginConfiguration {
        description = "TestSeer: API integration test plan generation (real PSI adapter)."
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "252.*"
        }
    }
}
