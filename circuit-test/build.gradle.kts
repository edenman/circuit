// Copyright (C) 2024 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.plugin.parcelize)
  alias(libs.plugins.compose)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  // region KMP Targets
  androidTarget { publishLibraryVariants("release") }
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  js(IR) {
    moduleName = property("POM_ARTIFACT_ID").toString()
    browser()
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    moduleName = property("POM_ARTIFACT_ID").toString()
    browser()
  }
  // endregion

  targets.configureEach {
    compilations.configureEach {
      compilerOptions.configure { freeCompilerArgs.add("-Xexpect-actual-classes") }
    }
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      dependencies {
        api(projects.circuitFoundation)
        api(libs.compose.runtime)
        api(libs.coroutines)
        api(libs.turbine)
        api(libs.molecule.runtime)
      }
    }

    commonTest { dependencies { implementation(libs.coroutines.test) } }

    val commonJvmTest =
      maybeCreate("commonJvmTest").apply {
        dependencies {
          implementation(libs.junit)
          implementation(libs.truth)
          implementation(libs.testing.testParameterInjector)
          implementation(project(":internal-test-utils"))
        }
      }
    val jvmTest by getting { dependsOn(commonJvmTest) }
    val androidUnitTest by getting { dependsOn(commonJvmTest) }
    // We use a common folder instead of a common source set because there is no commonizer
    // which exposes the browser APIs across these two targets.
    jsMain { kotlin.srcDir("src/browserMain/kotlin") }
    val wasmJsMain by getting { kotlin.srcDir("src/browserMain/kotlin") }
  }
}

android { namespace = "com.slack.circuit.test" }
