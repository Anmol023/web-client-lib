import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.encoding.Base64

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.gradleup.nmcp") version "0.1.4"
    `maven-publish`
    signing
}

group = "io.github.anmol023"
version = "0.0.1"
description = "A reactive web client library for Spring Boot"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-aspects")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val sourceJar by tasks.registering(Jar::class) {
    description = "Generates a JAR containing the source code"
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    description = "Generates Javadoc and packages it into a JAR"
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
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
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = "web-client-lib"
            version = project.version.toString()

            from(components["java"])
            artifact(sourceJar)
            artifact(javadocJar)

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
    val signingPassword = findProperty("signingPassword")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey as String, signingPassword as String)
    }
    sign(publishing.publications["mavenJava"])
}

gradle.taskGraph.whenReady {
    if (hasTask(":publishAllPublicationsToCentralPortal")) {
        require(findProperty("signingKey") != null) { "signingKey is required for publishing" }
        require(findProperty("signingPassword") != null) { "signingPassword is required for publishing" }
    }
}

nmcp {
    centralPortal {
        username = findProperty("ossrhUsername") as String? ?: ""
        password = findProperty("ossrhPassword") as String? ?: ""
        publishingType = "AUTOMATIC"
    }
}