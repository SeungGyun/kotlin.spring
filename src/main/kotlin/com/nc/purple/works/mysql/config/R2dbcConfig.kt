package com.nc.purple.works.mysql.config


import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.proxy.ProxyConnectionFactory
import io.r2dbc.proxy.core.QueryExecutionInfo
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ValidationDepth
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.r2dbc.core.DatabaseClient
import java.time.Duration
import java.time.ZoneId
import java.util.function.Consumer
import java.util.regex.Pattern


@Configuration
class R2dbcConfig {
    private val log: Logger = LoggerFactory.getLogger(R2dbcConfig::class.java)

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val connectionFactory = MySqlConnectionFactory.from(
            MySqlConnectionConfiguration.builder()
                .host("localhost")
                .port(62222)
                .username("pp")
                .password("ppw")
                .database("ngp_web")
                .serverZoneId(ZoneId.systemDefault())
                .useClientPrepareStatement()
                .tcpKeepAlive(true)
                .build()
        )

        val connectionPool = ConnectionPool(
            ConnectionPoolConfiguration.builder(connectionFactory)
                .name("r2dbcPool")
                .maxIdleTime(Duration.ofSeconds(10))
                .maxLifeTime(Duration.ofSeconds(60))
                .maxSize(10)
                .initialSize(2)
                .validationDepth(ValidationDepth.REMOTE)
                .validationQuery("SELECT 1")
                .build()
        )

        return ProxyConnectionFactory.builder(connectionPool)
            .onBeforeQuery(sqlLoggingListener())  // ✅ SQL 로그 출력
            .onEachQueryResult { executionInfo ->  // ✅ SQL 실행 후 개별 결과를 가져옴
                when (val result = executionInfo.currentMappedResult) {
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val mapResult = result as Map<String, Any?>
                        log.info(formatResultAsTable(listOf(mapResult)))  // ✅ 테이블 형태로 출력
                    }

                    is List<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val listResult = result as List<Map<String, Any?>>
                        log.info(formatResultAsTable(listResult))  // ✅ 리스트인 경우 여러 개 출력
                    }

                    else -> {
                        // ✅ `Game` 같은 엔티티 객체인 경우, Reflection Map 변환
                        val entityMap = result?.let { entityToMap(it) }
                        if (entityMap != null) {
                            log.info(formatResultAsTable(listOf(entityMap)))  // ✅ 테이블 형태로 출력
                        } else {
                            log.info("Unknown Result Type: {}", result.toString())  // ✅ 예외 처리
                        }
                    }
                }
            }
            .build()
    }

    /*
    private fun formatResult(result: Any): String {
        return when (result) {
            is Map<*, *> -> result.entries.joinToString(prefix = "{ ", postfix = " }") { (key, value) ->
                "\"$key\": \"${value ?: "NULL"}\""
            }

            else -> result.toString()
        }
    }
*/
    private fun entityToMap(entity: Any): Map<String, Any?> {
        return entity::class.members
            .filterIsInstance<kotlin.reflect.KProperty1<Any, *>>() // ✅ 프로퍼티만 가져옴
            .associate { it.name to it.get(entity) } // ✅ `name: value` 형태로 변환
    }


    @Bean
    fun databaseClient(connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .bindMarkers(DialectResolver.getDialect(connectionFactory).bindMarkersFactory)
            .build()
    }

    // ✅ 컬러 코드 정의
    private val RESET = "\u001B[0m"   // 기본색 (리셋)
    private val BLUE = "\u001B[34m"   // SQL 키워드 (파란색)
    private val GREEN = "\u001B[32m"  // 바인딩된 값 (녹색)
    private val RED = "\u001B[31m"    // 에러 (빨간색)
    private val YELLOW = "\u001B[33m"
    private val CYAN = "\u001B[36m"

    private fun sqlLoggingListener(): Consumer<QueryExecutionInfo> {
        return Consumer { executionInfo ->
            executionInfo.queries.forEach { queryInfo ->
                var formattedQuery = queryInfo.query

                // ✅ 바인딩된 값 추출 (Kotlin 스타일로 개선)
                val boundValues = queryInfo.bindingsList
                    .flatMap { bindings ->
                        bindings.indexBindings.map { it.boundValue.value } + // 인덱스 바인딩
                                bindings.namedBindings.map { it.boundValue.value } // 이름 바인딩
                    }

                // ✅ `?` 값과 바인딩된 값을 매칭하여 SQL을 완성
                boundValues.forEach { value ->
                    formattedQuery = formattedQuery.replaceFirst(
                        Pattern.quote("?").toRegex(),
                        "$GREEN'$value'$RESET"
                    )
                }

                // ✅ SQL 포맷팅 (줄바꿈 및 정리) & 컬러 적용
                formattedQuery = formattedQuery
                    .replace(
                        Regex("(?i)\\b(SET|WHERE|VALUES|UPDATE|INSERT INTO|SELECT|FROM|DELETE)\\b"),
                        "\n$BLUE$1$RESET"
                    )
                    .replace(",", ",\n    ") // 쉼표 뒤 줄바꿈 및 들여쓰기 추가
                    .replace("(", "(\n    ") // 괄호 열기 후 줄바꿈 및 들여쓰기 추가
                    .replace(")", "\n)") // 괄호 닫기 전 줄바꿈

                //log.info("Called from: {}",  getCallerInfo())
                log.info(
                    "\n---- Executed SQL ----{}\n--Execution END Run Time: {} ms --",
                    formattedQuery,
                    executionInfo.executeDuration.toMillis()
                )
                // 실행 시간 출력
                log.info(
                    "Executed on Thread: {} (ID: {}), Execution Type: {}",
                    executionInfo.threadName,
                    executionInfo.threadId,
                    executionInfo.type
                )
                executionInfo.throwable?.let {
                    log.error("${RED}SQL Execution Error: $RESET", it)
                }
            }
        }
    }

    /* TODO 실행 위치 찾는 부분은 좀더 고민해야함
    private fun getCallerInfo(): String {
        val stackTrace = Thread.currentThread().stackTrace

        // ✅ 프로젝트의 실제 비즈니스 로직 패키지명 (사용자의 프로젝트 패키지로 변경 필요)
        val targetPackage = "com.nc.purple.works"

        return stackTrace
            .reversed() // ✅ 최신 호출을 찾기 위해 역순으로 탐색
            .firstOrNull {
                it.className.startsWith(targetPackage) && // ✅ 비즈니스 코드 실행 위치 찾기
                        !it.className.contains("config.") && // ✅ 설정 관련 클래스 제외
                        !it.className.contains("lambda$") && // ✅ 람다 함수 제외
                        !it.className.contains("\$") && // ✅ 내부 익명 클래스 제외
                        !it.className.contains("reactor.") && // ✅ Reactor 관련 클래스 제외
                        !it.className.contains("proxy") && // ✅ Spring Proxy 관련 클래스 제외
                        !it.className.contains("MethodInvocationSubscriber") && // ✅ R2DBC Proxy 관련 클래스 제외
                        !it.className.contains("io.netty") // ✅ Netty 관련 클래스 제외
            }
            ?.let { "${it.className}.${it.methodName} (Line: ${it.lineNumber})" }
            ?: "Caller Not Found"
    }
*/

    private fun formatResultAsTable(results: List<Map<String, Any?>>): String {
        if (results.isEmpty()) return "${CYAN}No results found.${RESET}"

        val headers = results.first().keys.toList()
        val columnWidths = headers.associateWith { header ->
            maxOf(header.length, results.maxOfOrNull { it[header]?.toString()?.length ?: 0 } ?: 0)
        }

        val separator =
            columnWidths.values.joinToString("+", prefix = "$YELLOW+", postfix = "+$RESET") { "-".repeat(it + 2) }

        val headerRow = headers.joinToString("|", prefix = "$YELLOW|", postfix = "|$RESET") {
            it.padEnd(columnWidths[it]!! + 2)
        }

        val dataRows = results.joinToString("\n") { row ->
            headers.joinToString("|", prefix = "$GREEN|", postfix = "|$RESET") {
                (row[it]?.toString() ?: "NULL").padEnd(columnWidths[it]!! + 2)
            }
        }

        return "\n$separator\n$headerRow\n$separator\n$dataRows\n$separator"
    }


}
