package com.consentos.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.consentos.app.data.ConsentDecision
import com.consentos.app.data.ConsentRule

@Composable
fun IntroScreen(
    onViewRules: () -> Unit
) {
    ConsentScaffold(
        title = "Consent OS",
        subtitle = "A pause before you open links"
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Consent is a conscious decision, not a checkbox.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Consent OS adds a small pause before websites open from your apps.\n\n" +
                        "When a link is tapped, Consent OS shows who you are about to interact with " +
                        "and asks for your explicit decision before anything loads.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column {
                PrimaryActionButton(
                    text = "View my consent rules",
                    onClick = onViewRules
                )
                Spacer(modifier = Modifier.height(8.dp))
                TertiaryTextButton(
                    text = "This screen appears when no link is active",
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

@Composable
fun ConsentGateScreen(
    domain: String,
    onAllowOnce: () -> Unit,
    onAlwaysAllow: () -> Unit,
    onDeny: () -> Unit,
    onViewRules: () -> Unit
) {
    ConsentScaffold(
        title = "Consent gate",
        subtitle = "Before you open this website"
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = domain,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This website may collect personal data such as identifiers, usage patterns, or device information.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Consent OS pauses the link so you can make a conscious choice before continuing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                PrimaryActionButton(
                    text = "Allow once",
                    onClick = onAllowOnce
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryActionButton(
                    text = "Always allow for this domain",
                    onClick = onAlwaysAllow
                )
                Spacer(modifier = Modifier.height(8.dp))
                TertiaryTextButton(
                    text = "Deny and stay here",
                    onClick = onDeny
                )
                Spacer(modifier = Modifier.height(16.dp))
                TertiaryTextButton(
                    text = "View my consent rules",
                    onClick = onViewRules
                )
            }
        }
    }
}

@Composable
fun BlockedScreen(
    domain: String,
    onViewRules: () -> Unit,
    onClose: () -> Unit
) {
    ConsentScaffold(
        title = "Link blocked",
        subtitle = "You chose to block this website"
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = domain,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Consent OS respected your decision and stopped this link from opening.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You stay in control of which sites can reach you and when.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                PrimaryActionButton(
                    text = "View my consent rules",
                    onClick = onViewRules
                )
                Spacer(modifier = Modifier.height(8.dp))
                TertiaryTextButton(
                    text = "Close",
                    onClick = onClose
                )
            }
        }
    }
}

@Composable
fun ConsentRulesScreen(
    rules: List<ConsentRule>,
    onBack: () -> Unit
) {
    ConsentScaffold(
        title = "Consent rules",
        subtitle = "Domains and your decisions",
        onBack = onBack
    ) { _ ->
        if (rules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You haven't made any decisions yet. Rules will appear here after you allow or deny websites.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                items(rules) { rule ->
                    ConsentRuleRow(rule = rule)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ConsentRuleRow(rule: ConsentRule) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = rule.domain,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when (rule.decision) {
                    ConsentDecision.ALLOW_ONCE -> "One-time decision"
                    ConsentDecision.ALWAYS_ALLOW -> "Always allow"
                    ConsentDecision.DENY -> "Denied"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


