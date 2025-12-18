package com.consentos.app.navigation

sealed class ConsentDestination(val route: String) {
    data object Intro : ConsentDestination("intro")
    data object ConsentGate : ConsentDestination("consent_gate")
    data object Blocked : ConsentDestination("blocked")
    data object Rules : ConsentDestination("rules")
}


