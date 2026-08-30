plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ru.reeg.moexmonitor.core.di"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // :core:di знает про все core-модули, потому что именно здесь собирается
    // корневой Dagger-граф. Наружу (в фичи) он отдаёт их через api.
    api(projects.core.model)
    api(projects.core.common)
    api(projects.core.data)
    api(projects.core.network)
    api(projects.core.database)

    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.fragment.ktx)

    api(libs.dagger)
    ksp(libs.dagger.compiler)

    testImplementation(libs.junit)
}
