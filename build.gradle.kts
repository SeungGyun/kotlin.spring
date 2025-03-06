plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.2.3" // ✅ 최신 Spring Boot 버전
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.nc.purple"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// ✅ Spring WebFlux (비동기 웹 애플리케이션)
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// ✅ Kotlin & JSON 지원
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// ✅ Reactor + 코루틴 지원
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// ✅ R2DBC - MySQL 최신 드라이버 (MariaDB R2DBC 사용)
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.mariadb:r2dbc-mariadb:1.1.4") // 최신 R2DBC MySQL 드라이버

	// ✅ 로깅 지원
	implementation("io.github.microutils:kotlin-logging:2.1.21")

	// ✅ 테스트 관련 라이브러리
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
