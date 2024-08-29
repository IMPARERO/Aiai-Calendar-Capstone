plugins {
    alias(libs.plugins.android).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.ksp).apply(false)
}

//ext {
//    compose_version = '1.5.4'
//}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:7.0.0")
    }
}




tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
