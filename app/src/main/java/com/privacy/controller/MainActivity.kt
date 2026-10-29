package com.privacy.controller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rikka.shizuku.Shizuku

private data class InstalledApp(
    val name: String,
    val packageName: String,
    val isSystemApp: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apps = runCatching {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { app ->
                    InstalledApp(
                        name = packageManager.getApplicationLabel(app).toString(),
                        packageName = app.packageName,
                        isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }
                .sortedBy { it.name.lowercase() }
        }.getOrDefault(emptyList())

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrivacyDashboard(
                        apps = apps,
                        shizukuStatus = {
                            when {
                                !Shizuku.pingBinder() ->
                                    "Shizuku não está ativo"
                                Shizuku.checkSelfPermission() ==
                                    PackageManager.PERMISSION_GRANTED ->
                                    "Shizuku conectado e autorizado"
                                else ->
                                    "Shizuku ativo, autorização pendente"
                            }
                        },
                        openShizukuSettings = {
                            runCatching {
                                packageManager.getLaunchIntentForPackage(
                                    "moe.shizuku.privileged.api"
                                )?.let { startActivity(it) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyDashboard(
    apps: List<InstalledApp>,
    shizukuStatus: () -> String,
    openShizukuSettings: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    var showApps by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }

    val filteredApps = remember(apps, search, showSystemApps) {
        apps.filter { app ->
            (showSystemApps || !app.isSystemApp) &&
                (app.name.contains(search, ignoreCase = true) ||
                    app.packageName.contains(search, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PRIVACY TELEMETRY", style = MaterialTheme.typography.labelLarge)
        Text(
            "Sua privacidade,\nno seu controle.",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            "Painel local para revisar aplicativos e preparar controles de privacidade do Android.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Estado do Shizuku", style = MaterialTheme.typography.titleMedium)
                Text(shizukuStatus())
                Button(
                    onClick = openShizukuSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abrir Shizuku")
                }
            }
        }

        Card {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Aplicativos instalados", style = MaterialTheme.typography.titleMedium)
                Text("Encontrados: ${apps.size}")
                OutlinedButton(
                    onClick = { showApps = !showApps },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showApps) "Ocultar aplicativos" else "Ver aplicativos")
                }

                if (showApps) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Buscar por nome ou pacote") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showSystemApps = !showSystemApps }) {
                            Text(if (showSystemApps) "Ocultar sistema" else "Incluir sistema")
                        }
                    }
                    Text("Resultados: ${filteredApps.size}")

                    filteredApps.take(100).forEach { app ->
                        HorizontalAppItem(app)
                    }

                    if (filteredApps.size > 100) {
                        Text("Mostrando os primeiros 100 resultados.")
                    }
                }
            }
        }

        FeatureCard(
            title = "Bloqueio de rastreadores",
            description = "Ainda não filtra conexões de rede. Um bloqueador real precisará de VPN local ou firewall compatível."
        )
        FeatureCard(
            title = "Permissões e AppOps",
            description = "A listagem está disponível. Ler e alterar operações de aplicativos será uma próxima etapa, com confirmação."
        )
        FeatureCard(
            title = "Diagnóstico do aparelho",
            description = "As configurações nativas do Android e da Samsung continuam sendo úteis para reduzir diagnósticos e personalização."
        )

        OutlinedButton(
            onClick = { showDetails = !showDetails },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (showDetails) "Ocultar limites" else "O que esta versão ainda não faz")
        }

        if (showDetails) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Esta versão lista aplicativos, mas ainda não bloqueia conexões, " +
                            "não altera AppOps e não desativa pacotes. O Shizuku não concede " +
                            "root: as operações disponíveis dependem do Android e das " +
                            "autorizações concedidas."
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Versão 0.2.0 • processamento local",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun HorizontalAppItem(app: InstalledApp) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(app.name, style = MaterialTheme.typography.titleSmall)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            Text(
                if (app.isSystemApp) "Aplicativo do sistema" else "Aplicativo instalado",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun FeatureCard(title: String, description: String) {
    Card {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
