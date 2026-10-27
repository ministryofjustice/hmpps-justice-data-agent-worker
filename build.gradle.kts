plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.14"
  kotlin("plugin.spring") version "2.4.21"
  kotlin("plugin.jpa") version "2.4.21"
}

extra["springAiVersion"] = "2.0.1"

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.1")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.1")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("com.fasterxml.uuid:java-uuid-generator:5.2.0")
  implementation("org.flywaydb:flyway-core")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("org.springframework.boot:spring-boot-starter-flyway:4.1.1")
  implementation("org.postgresql:r2dbc-postgresql:1.1.2.RELEASE")
  implementation("org.postgresql:postgresql:42.7.14")
  implementation("com.networknt:json-schema-validator:1.4.0")
  implementation("org.springframework.ai:spring-ai-starter-model-anthropic")
  implementation("org.springframework.ai:spring-ai-starter-model-openai")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
  testImplementation("org.testcontainers:postgresql:1.21.4")
  testImplementation("org.testcontainers:r2dbc:1.21.4")
  testImplementation("org.springframework.boot:spring-boot-data-r2dbc-test")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:3.0.1")
  // Localstack
  // implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
  testImplementation("org.testcontainers:localstack:1.21.4")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.47") {
    exclude(group = "io.swagger.core.v3")
  }

  constraints {
    implementation("com.ongres.scram:scram-client:3.3") {
      because("CVE-2026-53712 - remove when r2dbc-postgresql brings scram 3.3+")
    }
    implementation("com.ongres.scram:scram-common:3.3") {
      because("CVE-2026-53712 - remove when r2dbc-postgresql brings scram 3.3+")
    }
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
