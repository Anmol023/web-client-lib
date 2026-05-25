import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.encoding.Base64

plugins {
    id("io.github.anmol023.dependency-version-management") version "0.0.3"
    `maven-publish`
    signing
    `java-library`
}

group = "io.github.anmol023"
version = "0.0.4"
description = "A reactive web client library for Spring Boot"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-aspects")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xannotation-default-target=param-property"
            )
        }
    }

    bootJar { enabled = false }

    named<Jar>("sourcesJar") {
        enabled = false
    }
    named<Jar>("javadocJar") {
        enabled = false
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = "web-client-lib"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("web-client-lib")
                description.set(project.description)
                url.set("https://github.com/Anmol023/web-client-lib")
                organization {
                    name.set("Anmol023")
                    url.set("https://github.com/Anmol023/")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/Anmol023/web-client-lib/issues")
                }
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://github.com/Anmol023/web-client-lib/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("Anmol023")
                        name.set("Anmol023")
                        url.set("https://github.com/Anmol023")
                    }
                }
                scm {
                    url.set("https://github.com/Anmol023/web-client-lib")
                    connection.set("scm:git:git://github.com/Anmol023/web-client-lib.git")
                    developerConnection.set("scm:git:ssh://git@github.com:Anmol023/web-client-lib.git")
                }
            }
        }
    }
}

signing {
    val signingKey = (findProperty("signingKey") as String?)?.let { String(Base64.decode(it)) }
    val signingPassword = findProperty("signingPassword") as String?
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}

gradle.taskGraph.whenReady {
    val isPublishing = allTasks.any { it.name.contains("publish", ignoreCase = true) }
    if(isPublishing){
        require(findProperty("signingKey") != null) { "signingKey is required for publishing" }
        require(findProperty("signingPassword") != null) { "signingPassword is required for publishing" }
    }
}