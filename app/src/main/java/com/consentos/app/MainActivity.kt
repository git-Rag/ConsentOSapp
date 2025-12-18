package com.consentos.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.consentos.app.data.DataStoreConsentRepository
import com.consentos.app.navigation.ConsentDestination
import com.consentos.app.ui.BlockedScreen
import com.consentos.app.ui.ConsentGateScreen
import com.consentos.app.ui.ConsentRulesScreen
import com.consentos.app.ui.IntroScreen
import com.consentos.app.data.ConsentRule

private const val DATA_STORE_NAME = "consent_os_prefs"
private val Context.consentDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class MainActivity : ComponentActivity() {

    private val viewModel: ConsentViewModel by viewModels {
        val repo = DataStoreConsentRepository(applicationContext.consentDataStore)
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ConsentViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val incomingUrl = extractIncomingUrl(intent)

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    var initialRoute by remember { mutableStateOf(ConsentDestination.Intro.route) }
                    var blockedDomain by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(incomingUrl) {
                        if (incomingUrl != null) {
                            viewModel.handleIncomingUrl(incomingUrl) { action ->
                                when (action) {
                                    is ConsentFlowAction.ShowGate -> {
                                        initialRoute = ConsentDestination.ConsentGate.route +
                                            "?domain=${Uri.encode(action.domain)}"
                                        navController.navigate(initialRoute) {
                                            popUpTo(0)
                                        }
                                    }
                                    is ConsentFlowAction.NavigateBlocked -> {
                                        blockedDomain = action.domain
                                        navController.navigate(ConsentDestination.Blocked.route) {
                                            popUpTo(0)
                                        }
                                    }
                                    is ConsentFlowAction.OpenInBrowser -> {
                                        openInDefaultBrowser(action.url)
                                        finish()
                                    }
                                }
                            }
                        }
                    }

                    val rulesState: List<ConsentRule> = viewModel.consentRules.value

                    NavHost(
                        navController = navController,
                        startDestination = if (incomingUrl == null) {
                            ConsentDestination.Intro.route
                        } else {
                            ConsentDestination.ConsentGate.route + "?domain=${Uri.encode(viewModel.currentDomain ?: incomingUrl)}"
                        }
                    ) {
                        composable(ConsentDestination.Intro.route) {
                            IntroScreen(
                                onViewRules = {
                                    navController.navigate(ConsentDestination.Rules.route)
                                }
                            )
                        }
                        composable(
                            route = ConsentDestination.ConsentGate.route + "?domain={domain}",
                            arguments = listOf(
                                navArgument("domain") {
                                    type = NavType.StringType
                                    nullable = false
                                }
                            )
                        ) { backStackEntry ->
                            val domain = backStackEntry.arguments?.getString("domain") ?: ""
                            ConsentGateScreen(
                                domain = domain,
                                onAllowOnce = {
                                    viewModel.onAllowOnce { action ->
                                        when (action) {
                                            is ConsentFlowAction.OpenInBrowser -> {
                                                openInDefaultBrowser(action.url)
                                                finish()
                                            }
                                            else -> Unit
                                        }
                                    }
                                },
                                onAlwaysAllow = {
                                    viewModel.onAlwaysAllow { action ->
                                        when (action) {
                                            is ConsentFlowAction.OpenInBrowser -> {
                                                openInDefaultBrowser(action.url)
                                                finish()
                                            }
                                            else -> Unit
                                        }
                                    }
                                },
                                onDeny = {
                                    viewModel.onDeny { action ->
                                        if (action is ConsentFlowAction.NavigateBlocked) {
                                            blockedDomain = action.domain
                                            navController.navigate(ConsentDestination.Blocked.route) {
                                                popUpTo(0)
                                            }
                                        }
                                    }
                                },
                                onViewRules = {
                                    navController.navigate(ConsentDestination.Rules.route)
                                }
                            )
                        }
                        composable(ConsentDestination.Blocked.route) {
                            val domain = blockedDomain ?: viewModel.currentDomain.orEmpty()
                            BlockedScreen(
                                domain = domain.ifEmpty { "This website" },
                                onViewRules = {
                                    navController.navigate(ConsentDestination.Rules.route)
                                },
                                onClose = {
                                    finish()
                                }
                            )
                        }
                        composable(ConsentDestination.Rules.route) {
                            ConsentRulesScreen(
                                rules = rulesState,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle new links while the activity is already running
        intent?.let {
            val url = extractIncomingUrl(it)
            if (url != null) {
                viewModel.handleIncomingUrl(url) { action ->
                    when (action) {
                        is ConsentFlowAction.OpenInBrowser -> {
                            openInDefaultBrowser(action.url)
                            finish()
                        }
                        is ConsentFlowAction.NavigateBlocked -> {
                            // For simplicity, just finish; the next launch will show blocked state
                            finish()
                        }
                        is ConsentFlowAction.ShowGate -> {
                            // no-op here; normal flow will show gate on next start
                        }
                    }
                }
            }
        }
    }

    private fun extractIncomingUrl(intent: Intent?): String? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()
            Intent.ACTION_SEND -> {
                val shared = intent.getStringExtra(Intent.EXTRA_TEXT)
                shared?.let { extractFirstUrl(it) }
            }
            else -> null
        }
    }

    private fun extractFirstUrl(text: String): String? {
        val regex = "(https?://\\S+)".toRegex()
        return regex.find(text)?.value
    }

    private fun openInDefaultBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}


