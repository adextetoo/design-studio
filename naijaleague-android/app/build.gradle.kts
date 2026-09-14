plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ng.naijaleague.fantasy"
    compileSdk = 35

    defaultConfig {
        applicationId = "ng.naijaleague.fantasy"
        // API 26 is the floor because the brand's display face is a VARIABLE font:
        // the wdth 118 / wght 900 instance that makes Archivo read as Druk Wide
        // needs fontVariationSettings, which arrived in API 26. It still covers
        // the cheap-handset reality this product is built for.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en")

        // Where the operator console's API lives.
        //
        // A PROPERTY, NEVER A COMMITTED HOST. There is no deployment of that API
        // yet — the earlier repository ships a docker-compose for running it
        // locally and nothing else — so hard-coding anything here would bake
        // one developer's machine into the APK. Set it per build:
        //
        //     ./gradlew assembleRelease -Png.naijaleague.catalogueUrl=https://api.example.ng
        //
        // or in a local.properties / ~/.gradle/gradle.properties that is not in
        // this repository. Empty is a supported state, not a broken one: the app
        // ships a complete researched catalogue and says on the Profile screen
        // that it is not connected to a console.
        buildConfigField(
            "String",
            "CATALOGUE_BASE_URL",
            "\"${project.findProperty("ng.naijaleague.catalogueUrl") ?: ""}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            // The emulator reaches the host machine's localhost at 10.0.2.2, so
            // this is where `npm run dev` in packages/api actually is. Cleartext
            // to that address is permitted by the debug network security config
            // and by nothing else — see src/debug/res/xml/network_security_config.xml.
            buildConfigField(
                "String",
                "CATALOGUE_BASE_URL",
                "\"${project.findProperty("ng.naijaleague.catalogueUrl") ?: "http://10.0.2.2:4000"}\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)

    debugImplementation(libs.androidx.ui.tooling)

    // The rules engine is pure Kotlin, so it is tested on the JVM with no emulator.
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}
