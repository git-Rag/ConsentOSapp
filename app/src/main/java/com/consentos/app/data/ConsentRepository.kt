package com.consentos.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ConsentRepository {
    suspend fun getDecisionFor(domain: String): ConsentDecision?
    suspend fun setDecision(domain: String, decision: ConsentDecision)
    fun getAllRules(): Flow<List<ConsentRule>>
}

class DataStoreConsentRepository(
    private val dataStore: DataStore<Preferences>
) : ConsentRepository {

    private fun keyFor(domain: String) = preferencesKey<String>("consent_rule_$domain")

    override suspend fun getDecisionFor(domain: String): ConsentDecision? {
        val prefs = dataStore.edit {  } // ensure we can read current snapshot
        val value = prefs[keyFor(domain)] ?: return null
        return runCatching { ConsentDecision.valueOf(value) }.getOrNull()
    }

    override suspend fun setDecision(domain: String, decision: ConsentDecision) {
        dataStore.edit { prefs ->
            // We persist only stable, repeatable decisions
            if (decision == ConsentDecision.ALLOW_ONCE) {
                prefs.remove(keyFor(domain))
            } else {
                prefs[keyFor(domain)] = decision.name
            }
        }
    }

    override fun getAllRules(): Flow<List<ConsentRule>> {
        return dataStore.data.map { prefs ->
            prefs.asMap()
                .mapNotNull { (key, value) ->
                    val name = key.name
                    if (!name.startsWith("consent_rule_")) return@mapNotNull null
                    val domain = name.removePrefix("consent_rule_")
                    val decisionString = value as? String ?: return@mapNotNull null
                    val decision = runCatching { ConsentDecision.valueOf(decisionString) }.getOrNull()
                        ?: return@mapNotNull null
                    ConsentRule(domain = domain, decision = decision)
                }
        }
    }
}


