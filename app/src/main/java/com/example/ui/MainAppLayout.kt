package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.auth.FluxoAutenticadoClube
import com.example.data.Associado
import com.example.ui.theme.LocalClubIdentityConfig
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusYellow
import kotlinx.coroutines.launch

data class MainNavDestination(
    val index: Int,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTagId: String
)

val PRIMARY_MOBILE_DESTINATIONS = listOf(
    MainNavDestination(
        index = 0,
        label = "Início",
        subtitle = "Painel & Simulador",
        icon = Icons.Default.Home,
        testTagId = "inicio"
    ),
    MainNavDestination(
        index = 1,
        label = "Veículos",
        subtitle = "Garagem & Manutenção",
        icon = Icons.Default.DirectionsBike,
        testTagId = "veiculos"
    ),
    MainNavDestination(
        index = 2,
        label = "Eventos",
        subtitle = "Comboios & Comunicados",
        icon = Icons.Default.CalendarMonth,
        testTagId = "eventos"
    ),
    MainNavDestination(
        index = 3,
        label = "Carteirinha",
        subtitle = "ID Digital & QR LGPD",
        icon = Icons.Default.Badge,
        testTagId = "carteirinha"
    ),
    MainNavDestination(
        index = 4,
        label = "Perfil",
        subtitle = "Cadastro, CNH & PIX",
        icon = Icons.Default.Person,
        testTagId = "perfil"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    associado: Associado?,
    authenticatedFlow: FluxoAutenticadoClube = FluxoAutenticadoClube.MEMBRO,
    selectedDestination: Int,
    onSelectDestination: (Int) -> Unit,
    onSelectQuickShortcut: (destination: Int, subTab: Int) -> Unit,
    onAlternarModoAcesso: () -> Unit,
    onOpenEmergencySos: () -> Unit,
    onSignOut: () -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val isDiretoriaAtiva = authenticatedFlow.isDiretoriaTotal ||
        associado?.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    var mostrarModalCredencialAdmin by remember { mutableStateOf(false) }
    var modalAdminUser by remember { mutableStateOf("") }
    var modalAdminPass by remember { mutableStateOf("") }
    var modalAdminErro by remember { mutableStateOf<String?>(null) }

    val handleRequestToggleRole = {
        if (isDiretoriaAtiva) {
            onAlternarModoAcesso()
        } else {
            modalAdminUser = ""
            modalAdminPass = ""
            modalAdminErro = null
            mostrarModalCredencialAdmin = true
        }
    }

    if (mostrarModalCredencialAdmin) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { mostrarModalCredencialAdmin = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (modalAdminUser.trim().uppercase() == DIRETORIA_MASTER_LOGIN &&
                            modalAdminPass.trim().uppercase() == DIRETORIA_MASTER_PASS
                        ) {
                            mostrarModalCredencialAdmin = false
                            onAlternarModoAcesso()
                        } else {
                            modalAdminErro = "Credencial inválida! Verifique o login e a senha da Diretoria."
                        }
                    }
                ) {
                    Text("Liberar Acesso Total")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { mostrarModalCredencialAdmin = false }) {
                    Text("Cancelar")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IronSideLogoBadge(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Acesso Total Diretoria")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Informe as credenciais de comando para habilitar o Acesso Total da Diretoria:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = modalAdminUser,
                        onValueChange = { modalAdminUser = it },
                        label = { Text("Login Diretoria") },
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = modalAdminPass,
                        onValueChange = { modalAdminPass = it },
                        label = { Text("Senha Diretoria") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true
                    )
                    if (modalAdminErro != null) {
                        Text(
                            text = modalAdminErro!!,
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktopOrExpanded = maxWidth >= 700.dp

        if (isDesktopOrExpanded) {
            // Layout Desktop / Tablet Expandido: Menu Lateral Fixo (PermanentNavigationDrawer)
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier
                            .width(290.dp)
                            .fillMaxHeight()
                            .testTag("desktop_sidebar_menu"),
                        drawerContainerColor = MaterialTheme.colorScheme.surface
                    ) {
                        IronSideSidebarContent(
                            associado = associado,
                            authenticatedFlow = authenticatedFlow,
                            selectedDestination = selectedDestination,
                            onSelectDestination = onSelectDestination,
                            onSelectQuickShortcut = onSelectQuickShortcut,
                            onAlternarModoAcesso = handleRequestToggleRole,
                            onOpenEmergencySos = onOpenEmergencySos,
                            onSignOut = onSignOut
                        )
                    }
                }
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        IronSideTopBar(
                            associado = associado,
                            authenticatedFlow = authenticatedFlow,
                            showMenuButton = false,
                            onOpenDrawer = {},
                            onAlternarModoAcesso = handleRequestToggleRole,
                            onOpenEmergencySos = onOpenEmergencySos,
                            onSignOut = onSignOut
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        content()
                    }
                }
            }
        } else {
            // Layout Dispositivos Móveis: Barra Inferior Responsiva (5 abas) + Menu Gaveta Opcional
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .width(295.dp)
                            .fillMaxHeight()
                            .testTag("mobile_drawer_menu"),
                        drawerContainerColor = MaterialTheme.colorScheme.surface
                    ) {
                        IronSideSidebarContent(
                            associado = associado,
                            authenticatedFlow = authenticatedFlow,
                            selectedDestination = selectedDestination,
                            onSelectDestination = { dest ->
                                onSelectDestination(dest)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onSelectQuickShortcut = { dest, subTab ->
                                onSelectQuickShortcut(dest, subTab)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onAlternarModoAcesso = {
                                coroutineScope.launch { drawerState.close() }
                                handleRequestToggleRole()
                            },
                            onOpenEmergencySos = {
                                onOpenEmergencySos()
                                coroutineScope.launch { drawerState.close() }
                            },
                            onSignOut = {
                                coroutineScope.launch { drawerState.close() }
                                onSignOut()
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        IronSideTopBar(
                            associado = associado,
                            authenticatedFlow = authenticatedFlow,
                            showMenuButton = true,
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onAlternarModoAcesso = handleRequestToggleRole,
                            onOpenEmergencySos = onOpenEmergencySos,
                            onSignOut = onSignOut
                        )
                    },
                    bottomBar = {
                        IronSideMobileBottomBar(
                            selectedDestination = selectedDestination,
                            onSelectDestination = onSelectDestination
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IronSideTopBar(
    associado: Associado?,
    authenticatedFlow: FluxoAutenticadoClube = FluxoAutenticadoClube.MEMBRO,
    showMenuButton: Boolean,
    onOpenDrawer: () -> Unit,
    onAlternarModoAcesso: () -> Unit,
    onOpenEmergencySos: () -> Unit,
    onSignOut: () -> Unit
) {
    val isDiretoria = authenticatedFlow.isDiretoriaTotal ||
        associado?.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    val clubIdentity = LocalClubIdentityConfig.current
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        navigationIcon = {
            if (showMenuButton) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("open_sidebar_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Abrir Menu Lateral",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IronSideLogoBadge(size = 34.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = clubIdentity.nomeClube,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isDiretoria) {
                            "DIRETORIA • ${clubIdentity.lemaClube}"
                        } else {
                            "MEMBRO • ${associado?.numeroAssociado ?: "MC-000001"}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            Surface(
                color = if (isDiretoria) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clickable { onAlternarModoAcesso() }
                    .testTag("toggle_access_role_chip")
            ) {
                Text(
                    text = if (isDiretoria) "🛡️ DIR." else "🏍️ MEMBRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDiretoria) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            Button(
                onClick = onOpenEmergencySos,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(34.dp)
                    .padding(end = 2.dp)
                    .testTag("top_bar_sos_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "Modo Emergência SOS",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onSignOut,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sair da conta",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

@Composable
private fun IronSideMobileBottomBar(
    selectedDestination: Int,
    onSelectDestination: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("mobile_bottom_navigation_bar")
    ) {
        PRIMARY_MOBILE_DESTINATIONS.forEach { dest ->
            val isSelected = selectedDestination == dest.index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectDestination(dest.index) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .testTag("bottom_nav_${dest.index}")
            )
        }
    }
}

@Composable
private fun IronSideSidebarContent(
    associado: Associado?,
    authenticatedFlow: FluxoAutenticadoClube = FluxoAutenticadoClube.MEMBRO,
    selectedDestination: Int,
    onSelectDestination: (Int) -> Unit,
    onSelectQuickShortcut: (destination: Int, subTab: Int) -> Unit,
    onAlternarModoAcesso: () -> Unit,
    onOpenEmergencySos: () -> Unit,
    onSignOut: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isAtivo = associado?.statusAssociado == "Ativo" || associado?.statusAssociado == "Aprovado"
    val isDiretoria = authenticatedFlow.isDiretoriaTotal ||
        associado?.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    val clubIdentity = LocalClubIdentityConfig.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Cabeçalho Oficial do Clube + Resumo do Associado
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onSelectDestination(4) }
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IronSideLogoBadge(size = 54.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = clubIdentity.nomeClube,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = clubIdentity.lemaClube,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = associado?.nomeGuerra ?: "COMANDANTE",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${associado?.numeroAssociado ?: "MC-000001"} • ${if (isDiretoria) "DIRETORIA" else "MEMBRO"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = if (isAtivo) StatusGreen else StatusYellow,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = (associado?.statusAssociado ?: "ATIVO").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    color = if (isDiretoria) MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAlternarModoAcesso() }
                        .testTag("sidebar_toggle_role_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDiretoria) "🛡️ Acesso: DIRETORIA" else "🏍️ Acesso: MEMBRO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alternar ⇄",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Seção Principal (Mesmos 5 itens da barra móvel: Início, Veículos, Eventos, Carteirinha, Perfil)
        Text(
            text = "NAVEGAÇÃO PRINCIPAL",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        PRIMARY_MOBILE_DESTINATIONS.forEach { dest ->
            val selected = selectedDestination == dest.index
            NavigationDrawerItem(
                label = {
                    Column {
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = dest.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label
                    )
                },
                selected = selected,
                onClick = { onSelectDestination(dest.index) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("sidebar_nav_${dest.testTagId}")
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )

        // 3. Módulos Operacionais & Atalhos Desktop (Garagem, Financeiro PIX, Diretoria e Estrada)
        Text(
            text = "ATALHOS DOS 3 PILARES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
            label = { Text("Manutenção & Abastecimento") },
            icon = { Icon(Icons.Default.Build, contentDescription = null) },
            selected = false,
            onClick = { onSelectQuickShortcut(1, 1) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sidebar_shortcut_manutencao")
        )

        NavigationDrawerItem(
            label = { Text("Mensalidades PIX") },
            icon = { Icon(Icons.Default.Payments, contentDescription = null) },
            selected = false,
            onClick = { onSelectQuickShortcut(4, 3) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sidebar_shortcut_pix")
        )

        NavigationDrawerItem(
            label = {
                Text(
                    if (isDiretoria) "Painel Admin & Aprovação (TOTAL)"
                    else "Painel Membro & Status Aprovação"
                )
            },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
            selected = false,
            onClick = { onSelectQuickShortcut(4, 4) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sidebar_shortcut_diretoria")
        )

        NavigationDrawerItem(
            label = { Text("Identidade do Clube (Logo/Cores/PIX)") },
            icon = { Icon(Icons.Default.Palette, contentDescription = null) },
            selected = false,
            onClick = { onSelectQuickShortcut(4, 5) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sidebar_shortcut_identidade_clube")
        )

        NavigationDrawerItem(
            label = { Text("Relatórios Diretoria (Ficha PDF & CRLV)") },
            icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
            selected = false,
            onClick = { onSelectQuickShortcut(4, 6) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sidebar_shortcut_relatorios_pdf")
        )

        NavigationDrawerItem(
            label = { Text("Estrada & Busca Nacional SOS") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Sos,
                    contentDescription = null,
                    tint = if (selectedDestination == 5) Color.White else MaterialTheme.colorScheme.tertiary
                )
            },
            selected = selectedDestination == 5,
            onClick = onOpenEmergencySos,
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                selectedIconColor = Color.White,
                selectedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sidebar_nav_estrada_sos")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Botão de Emergência Rápida + Logout no Rodapé do Menu Lateral
        Button(
            onClick = onOpenEmergencySos,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sidebar_emergency_sos_button")
        ) {
            Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ACIONAR EMERGÊNCIA 1-TOQUE", color = Color.White, fontWeight = FontWeight.Bold)
        }

        NavigationDrawerItem(
            label = { Text("Encerrar Sessão") },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair") },
            selected = false,
            onClick = onSignOut,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("sidebar_sign_out_button")
        )
    }
}
