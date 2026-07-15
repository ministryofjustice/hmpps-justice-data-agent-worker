plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.5.6"
  kotlin("plugin.spring") version "2.4.0"
}

extra["springAiVersion"] = "2.0.0"

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.5.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
  implementation("com.networknt:json-schema-validator:1.4.0")
  implementation("org.springframework.ai:spring-ai-starter-model-anthropic")
  implementation("org.springframework.ai:spring-ai-starter-model-openai")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.44") {
    exclude(group = "io.swagger.core.v3")
  }
}

kotlin {
  jvmToolchain(25)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }

  kotlin {
    compilerOptions {
      javaParameters = true
    }
  }

  dependencyManagement {
    imports {
      mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
  }
}
