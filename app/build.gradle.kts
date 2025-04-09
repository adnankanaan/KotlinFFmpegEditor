plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.devtools)
    id("maven-publish")
}

android {
    namespace = "com.webapp.kotlin_webapp_video_editer"
    compileSdk = 35

    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }

    lint {
        targetSdk = 34
    }

    testOptions {
        targetSdk = 34
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()  // optional, but recommended
            withJavadocJar()  // optional, if you generate javadocs
        }
    }
}

dependencies {
    ksp(libs.ksp)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.glide)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.colorpicker)
    implementation(libs.android.image.cropper)
    implementation(libs.face.detection)
    implementation(libs.ffmpeg.kit.full)
    implementation(libs.okhttp)
    implementation(libs.androidx.navigation.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.adnankanaan"
                artifactId = "KotlinFFmpegEditor"
                version = "1.0.6"
            }
        }
    }
}
