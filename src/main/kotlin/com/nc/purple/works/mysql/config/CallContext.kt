package com.nc.purple.works.mysql.config
import reactor.core.publisher.Mono
import reactor.util.context.ContextView

object CallContext {
    private const val CALLER_KEY = "CALLER_INFO"

    fun <T> wrapWithCallerInfo(mono: Mono<T>): Mono<T> {
        val stackTrace = Thread.currentThread().stackTrace

        // ✅ 비즈니스 로직이 실행된 첫 번째 위치 저장
        val targetPackage = "com.nc.purple.service"

        val caller = stackTrace
            .firstOrNull {
                it.className.startsWith(targetPackage) &&
                        !it.className.contains("\$") &&
                        !it.className.contains("reactor.") &&
                        !it.className.contains("proxy") &&
                        !it.className.contains("config.")
            }
            ?.let { "${it.className}.${it.methodName} (Line: ${it.lineNumber})" }
            ?: "Caller Not Found"

        return mono.contextWrite { context -> context.put(CALLER_KEY, caller) } // ✅ Reactor Context에 저장
    }

    fun getCallerInfo(contextView: ContextView): String? {
        return contextView.getOrDefault(CALLER_KEY, "Caller Not Found")
    }
}

