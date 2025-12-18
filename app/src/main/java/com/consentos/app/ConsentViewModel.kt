package com.consentos.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.consentos.app.data.ConsentDecision
import com.consentos.app.data.ConsentRepository
import com.consentos.app.data.ConsentRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URI

sealed class ConsentFlowAction {
    data class ShowGate(val domain: String) : ConsentFlowAction()
    data class NavigateBlocked(val domain: String) : ConsentFlowAction()
    data class OpenInBrowser(val url: String) : ConsentFlowAction()
}

class ConsentViewModel(
    private val repository: ConsentRepository
) : ViewModel() {

    var currentUrl: String? = null
        private set

    var currentDomain: String? = null
        private set

    val consentRules: StateFlow<List<ConsentRule>> =
        repository.getAllRules()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun handleIncomingUrl(
        url: String,
        onAction: (ConsentFlowAction) -> Unit
    ) {
        currentUrl = url
        currentDomain = extractDomain(url)

        val domain = currentDomain
        if (domain == null) {
            // If we can't extract a domain, just show the gate with a generic label.
            onAction(ConsentFlowAction.ShowGate(url))
            return
        }

        viewModelScope.launch {
            when (repository.getDecisionFor(domain)) {
                ConsentDecision.ALWAYS_ALLOW -> onAction(ConsentFlowAction.OpenInBrowser(url))
                ConsentDecision.DENY -> onAction(ConsentFlowAction.NavigateBlocked(domain))
                null, ConsentDecision.ALLOW_ONCE -> {
                    onAction(ConsentFlowAction.ShowGate(domain))
                }
            }
        }
    }

    fun onAllowOnce(onAction: (ConsentFlowAction) -> Unit) {
        currentUrl?.let { onAction(ConsentFlowAction.OpenInBrowser(it)) }
    }

    fun onAlwaysAllow(onAction: (ConsentFlowAction) -> Unit) {
        val domain = currentDomain ?: return
        viewModelScope.launch {
            repository.setDecision(domain, ConsentDecision.ALWAYS_ALLOW)
            currentUrl?.let { onAction(ConsentFlowAction.OpenInBrowser(it)) }
        }
    }

    fun onDeny(onAction: (ConsentFlowAction) -> Unit) {
        val domain = currentDomain ?: return
        viewModelScope.launch {
            repository.setDecision(domain, ConsentDecision.DENY)
            onAction(ConsentFlowAction.NavigateBlocked(domain))
        }
    }

    private fun extractDomain(url: String): String? {
        return runCatching {
            val uri = URI(url)
            uri.host?.removePrefix("www.")
        }.getOrNull()
    }
}


