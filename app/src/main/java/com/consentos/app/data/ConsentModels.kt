package com.consentos.app.data

enum class ConsentDecision {
    ALLOW_ONCE,
    ALWAYS_ALLOW,
    DENY
}

data class ConsentRule(
    val domain: String,
    val decision: ConsentDecision
)


