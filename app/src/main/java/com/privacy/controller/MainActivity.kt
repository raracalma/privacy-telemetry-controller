package com.privacy.controller

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrivacyDashboard(
                        shizukuStatus = {
                            when {
                                !Shizuku.pingBinder() -> "Shizuku não está ativo"
                                Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED ->
                                    "Shizuku conectado e autorizado"
                                else -> "Shizuku ativo, autorização pendente"
                            }
                        },
                        openShizukuSettings = {
                            runCatching {
                                startActivity(packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api"))
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
    shizukuStatus: () -> String,
    openShizukuSettings: () -> Unit
) {
    val showDetails = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PRIVACY CONTROLLER", style = MaterialTheme.typography.labelLarge)
        Text("Sua privacidade,\nno seu controle.", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Painel local para revisar proteções e preparar controles avançados do Android.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estado do Shizuku", style = MaterialTheme.typography.titleMedium)
                Text(shizukuStatus())
                Button(onClick = openShizukuSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir Shizuku")
                }
            }
        }

        FeatureCard(
            title = "Bloqueio de rastreadores",
            description = "Próxima etapa: regras de domínios com contadores reais. Esta versão ainda não filtra tráfego."
        )
        FeatureCard(
            title = "Permissões e AppOps",
            description = "Próxima etapa: listar operações suportadas e permitir mudanças individuais com confirmação."
        )
        FeatureCard(
            title = "Diagnóstico do aparelho",
            description = "Use também as configurações nativas do Android e da Samsung para reduzir diagnósticos e personalização."
        )

        OutlinedButton(onClick = { showDetails.value = !showDetails.value }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showDetails.value) "Ocultar limites" else "O que esta versão ainda não faz")
        }
        if (showDetails.value) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Esta é uma base inicial de interface. Ela não bloqueia conexões, não altera AppOps e não desativa pacotes ainda. " +
                            "O Shizuku não concede root: as operações disponíveis dependem do Android, da versão do sistema e da autorização ADB. " +
                            "Bloqueio de rede por aplicativo exige um mecanismo de VPN local ou integração com firewall compatível."
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Versão 0.1.0 • sem conta • sem servidor próprio", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FeatureCard(title: String, description: String) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
