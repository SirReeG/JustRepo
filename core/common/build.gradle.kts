plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ru.reeg.moexmonitor.core.common"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.core.ktx)

    // Rx и Dagger отдаём наружу через api: они есть в публичных сигнатурах
    // (SchedulerProvider, @Inject-конструкторы), поэтому нужны всем модулям выше.
    api(libs.rxjava)
    api(libs.rxandroid)
    api(libs.dagger)
    ksp(libs.dagger.compiler)

    testImplementation(libs.junit)
}
