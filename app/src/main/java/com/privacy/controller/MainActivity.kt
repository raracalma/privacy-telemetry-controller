package com.privacy.controller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rikka.shizuku.Shizuku

private const val SHIZUKU_REQUEST_CODE = 1001

private data class InstalledApp(
    val name: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val permissions: List<String>
)

class MainActivity : ComponentActivity() {

    private var shizukuMessage by mutableStateOf("Verificando Shizuku...")
    private var shizukuAuthorized by mutableStateOf(false)

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                shizukuAuthorized =
                    grantResult == PackageManager.PERMISSION_GRANTED

                shizukuMessage = if (shizukuAuthorized) {
                    "Shizuku autorizado"
                } else {
                    "Autorização não concedida"
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addRequestPermissionResultListener(permissionListener)
        updateShizukuStatus()

        val apps = loadInstalledApps()

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrivacyDashboard(
                        apps = apps,
                        shizukuMessage = shizukuMessage,
                        shizukuAuthorized = shizukuAuthorized,
                        requestShizuku = { requestShizukuPermission() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateShizukuStatus()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun updateShizukuStatus() {
        try {
            when {
                !Shizuku.pingBinder() -> {
                    shizukuAuthorized = false
                    shizukuMessage = "Shizuku não está iniciado"
                }

                Shizuku.checkSelfPermission() ==
                    PackageManager.PERMISSION_GRANTED -> {
                    shizukuAuthorized = true
                    shizukuMessage = "Shizuku conectado e autorizado"
                }

                else -> {
                    shizukuAuthorized = false
                    shizukuMessage = "Shizuku ativo, autorização pendente"
                }
            }
        } catch (e: Exception) {
            shizukuAuthorized = false
            shizukuMessage = "Não foi possível verificar Shizuku"
        }
    }

    private fun requestShizukuPermission() {
        try {
            if (!Shizuku.pingBinder()) {
                shizukuMessage = "Inicie o Shizuku primeiro"
                return
            }

            if (Shizuku.isPreV11()) {
                shizukuMessage = "Versão do Shizuku incompatível"
                return
            }

            if (Shizuku.checkSelfPermission() ==
                PackageManager.PERMISSION_GRANTED) {
                shizukuAuthorized = true
                shizukuMessage = "Shizuku já está autorizado"
                return
            }

            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
            shizukuMessage = "Aguardando autorização..."
        } catch (e: Exception) {
            shizukuMessage = "Erro ao solicitar autorização"
        }
    }

    private fun loadInstalledApps(): List<InstalledApp> {
        return runCatching {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA
            ).map { app ->
                val permissions = runCatching {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(
                        app.packageName,
                        PackageManager.GET_PERMISSIONS
                    ).requestedPermissions?.toList() ?: emptyList()
                }.getOrDefault(emptyList())

                InstalledApp(
                    name = packageManager.getApplicationLabel(app).toString(),
                    packageName = app.packageName,
                    isSystemApp =
                        (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    permissions = permissions
                )
            }.sortedBy { it.name.lowercase() }
        }.getOrDefault(emptyList())
    }
}

@Composable
private fun PrivacyDashboard(
    apps: List<InstalledApp>,
    shizukuMessage: String,
    shizukuAuthorized: Boolean,
    requestShizuku: () -> Unit
) {
    var selectedApp by remember { mutableStateOf<InstalledApp?>(null) }
    var search by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }

    val filteredApps = remember(apps, search, showSystemApps) {
        apps.filter {
            (showSystemApps || !it.isSystemApp) &&
                (it.name.contains(search, true) ||
                    it.packageName.contains(search, true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "PRIVACY TELEMETRY",
            style = MaterialTheme.typography.labelLarge
        )

        Text(
            "Sua privacidade,\nno seu controle.",
            style = MaterialTheme.typography.headlineLarge
        )

        Card {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Integração Shizuku",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(shizukuMessage)

                Button(
                    onClick = requestShizuku,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !shizukuAuthorized
                ) {
                    Text(
                        if (shizukuAuthorized)
                            "Shizuku autorizado"
                        else
                            "Autorizar Shizuku"
                    )
                }
            }
        }

        if (selectedApp != null) {
            AppDetails(
                app = selectedApp!!,
                onBack = { selectedApp = null }
            )
        } else {
            Card {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Gerenciador de aplicativos",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Encontrados: ${apps.size}")

                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Buscar aplicativo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = {
                            showSystemApps = !showSystemApps
                        }
                    ) {
                        Text(
                            if (showSystemApps)
                                "Ocultar aplicativos do sistema"
                            else
                                "Incluir aplicativos do sistema"
                        )
                    }

                    Text("Resultados: ${filteredApps.size}")

                    filteredApps.take(100).forEach { app ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedApp = app }
                        ) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    app.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    app.packageName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    if (app.isSystemApp)
                                        "Aplicativo do sistema"
                                    else
                                        "Aplicativo instalado",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    "Toque para ver detalhes e permissões",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            "Privacy Telemetry Controller • 0.3.0",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun AppDetails(
    app: InstalledApp,
    onBack: () -> Unit
) {
    Card {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                app.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(app.packageName)

            Text(
                if (app.isSystemApp)
                    "Aplicativo do sistema"
                else
                    "Aplicativo instalado"
            )

            Divider()

            Text(
                "Permissões declaradas: ${app.permissions.size}",
                style = MaterialTheme.typography.titleMedium
            )

            if (app.permissions.isEmpty()) {
                Text("Nenhuma permissão declarada foi encontrada.")
            } else {
                app.permissions.forEach { permission ->
                    Text(
                        "• $permission",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Divider()

            Text(
                "Esta tela mostra as permissões declaradas pelo aplicativo. " +
                    "Elas não significam necessariamente que o acesso esteja " +
                    "concedido ou sendo utilizado."
            )

            Text(
                "O controle AppOps será conectado na próxima etapa."
            )

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar aos aplicativos")
            }
        }
    }
}
