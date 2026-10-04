plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "quylh.canvas.shadow"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = false
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = providers.gradleProperty("GROUP").get()
                artifactId = "canvasShadow"
                // JitPack supplies VERSION from the requested Git tag/commit.
                version = System.getenv("VERSION")
                    ?: providers.gradleProperty("VERSION_NAME").get()
            }
        }
    }
}
