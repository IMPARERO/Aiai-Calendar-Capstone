import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    base
}

base {
    archivesName.set("calendar")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    compileSdk = project.libs.versions.app.build.compileSDKVersion.get().toInt()

    packaging {
        resources.excludes.add ("META-INF/*")
        resources.excludes.add ("META-INF/INDEX.LIST")
        resources.excludes.add ("META-INF/io.netty.versions.properties")
        resources.excludes.add ("META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule")
//        resources.excludes.add ("")
    }

    defaultConfig {
        applicationId = libs.versions.app.version.appId.get()
        minSdk = project.libs.versions.app.build.minimumSDK.get().toInt()
        targetSdk = project.libs.versions.app.build.targetSDK.get().toInt()
        versionCode = project.libs.versions.app.version.versionCode.get().toInt()
        versionName = project.libs.versions.app.version.versionName.get()
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            register("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }


    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true //compose
    }
    composeOptions {//compose
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    flavorDimensions.add("variants")
    productFlavors {
        register("core")
        register("fdroid")
        register("prepaid")
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    compileOptions {
        val currentJavaVersionFromLibs = JavaVersion.valueOf(libs.versions.app.build.javaVersion.get().toString())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = project.libs.versions.app.build.kotlinJVMTarget.get()
    }

    namespace = libs.versions.app.version.appId.get()

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }


}

dependencies {
    implementation(libs.simple.mobile.tools.commons)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.print)
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    //aiai alpha
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation ("androidx.activity:activity-compose:1.8.1")

    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-tooling-preview")
    implementation ("androidx.compose.material3:material3")

    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation ("androidx.compose.ui:ui-tooling")
    debugImplementation ("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    //Koin
    implementation ("io.insert-koin:koin-androidx-compose:3.4.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("net.sf.biweekly:biweekly:0.6.6")
    implementation("org.mnode.ical4j:ical4j:3.2.3")

    implementation("org.codehaus.groovy:groovy:3.0.10")


}
