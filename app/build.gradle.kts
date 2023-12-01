plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "kr.ac.wku.albeapp"
    compileSdk = 34

    dataBinding {
        enable = true
    }

    viewBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "kr.ac.wku.albeapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.2.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-auth:22.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // 파이어베이스 연동
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.firebaseui:firebase-ui-storage:8.0.1")

    // 프로필 사진 저장 관련 파이어베이스 라이브러리
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // Glide 이미지 라이브러리 저장
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:compiler:4.13.2")

    // 파이어베이스 사전 설정
    implementation(platform("com.google.firebase:firebase-bom:32.4.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    // 파이어베이스 사전 설정2
    implementation("com.google.firebase:firebase-auth-ktx:22.2.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.multidex:multidex:2.0.1")

    // 파이어베이스 사전 설정3
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-database:20.3.0")

    // 파이어베이스 전화 인증 설정
    implementation("com.google.firebase:firebase-auth:21.0.1")

    // 화면 잡아당기는 라이브러리 추가
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // 백그라운드 서비스 관련
    implementation("androidx.core:core:1.12.0")
}