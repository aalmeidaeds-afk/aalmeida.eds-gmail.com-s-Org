package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusGreen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

const val DIRETORIA_MASTER_LOGIN = "ADMIN"
const val DIRETORIA_MASTER_PASS = "ADMIN100"

fun normalizeCpfDigits(input: String): String = input.filter { it.isDigit() }

fun formatCpfMask(digitsOrFormatted: String): String {
    val digits = normalizeCpfDigits(digitsOrFormatted).take(11)
    return when {
        digits.length == 11 -> "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9, 11)}"
        else -> digitsOrFormatted.trim()
    }
}

enum class TipoAcessoClube(
    val titulo: String,
    val subtitulo: String,
    val perfilFirestore: String,
    val cargoPadrao: String
) {
    MEMBRO(
        titulo = "MEMBRO",
        subtitulo = "Acesso Individual por CPF",
        perfilFirestore = "Associado",
        cargoPadrao = "Integrante Escudado"
    ),
    DIRETORIA(
        titulo = "DIRETORIA",
        subtitulo = "Gestão e Acesso Total",
        perfilFirestore = "Diretoria",
        cargoPadrao = "Diretoria / Comando"
    )
}

data class PendingRegistrationData(
    val isNewRegistration: Boolean = false,
    val tipoAcesso: TipoAcessoClube = TipoAcessoClube.MEMBRO,
    val isDiretoriaMasterAuthenticated: Boolean = false,
    val loginIdentificador: String = "",
    val senhaClube: String = "",
    val nomeCompleto: String = "",
    val nomeGuerra: String = "",
    val cpf: String = "",
    val celular: String = "",
    val cargoDiretoria: String = "Integrante Escudado",
    val tipoSanguineo: String = "O+",
    val regional: String = "Sede Nacional - SP"
)

object PendingRegistrationStore {
    var pendingData: PendingRegistrationData? = null
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    val auth = FirebaseAuth.getInstance()
    if (auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Configuração Google Sign-In ausente: default_web_client_id não encontrado")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activity = context as? Activity
            val result = if (activity != null) {
                credentialManager.getCredential(activity, request)
            } else {
                credentialManager.getCredential(context, request)
            }
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Credencial inesperada recebida")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Falha na autenticação Google")
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Ignore normal composition cancellation when AuthScreen swaps in
        } catch (e: Exception) {
            Log.w("Auth", "Clear credential state warning: ${e.message}")
        } finally {
            FirebaseAuth.getInstance().signOut()
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    initialSelectedFlow: TipoAcessoClube = TipoAcessoClube.MEMBRO,
    externalErrorMessage: String? = null,
    onSelectFlow: (TipoAcessoClube) -> Unit = {},
    onStartManagedSignIn: ((PendingRegistrationData) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember(externalErrorMessage) { mutableStateOf(externalErrorMessage) }

    // Quem está acessando: MEMBRO (CPF individual) ou DIRETORIA (ADMIN / ADMIN100 Acesso Total)
    var tipoAcessoSelecionado by remember(initialSelectedFlow) { mutableStateOf(initialSelectedFlow) }

    // 0 = Login de Acesso, 1 = Criar Registro de Membro (Formação de Login por CPF e Senha)
    var authTab by remember { mutableIntStateOf(0) }

    // Campos de Login para MEMBRO (CPF + Senha)
    var loginCpfMembro by remember { mutableStateOf("") }
    var loginSenhaMembro by remember { mutableStateOf("") }

    // Campos de Login para DIRETORIA (ADMIN + ADMIN100)
    var loginDiretoriaUser by remember { mutableStateOf("") }
    var loginDiretoriaPass by remember { mutableStateOf("") }
    var mostrarSenhaLogin by remember { mutableStateOf(false) }

    // Campos de Registro de Novo Membro (CPF como Login + Senha)
    var regNomeCompleto by remember { mutableStateOf("") }
    var regNomeGuerra by remember { mutableStateOf("") }
    var regCpf by remember { mutableStateOf("") }
    var regCelular by remember { mutableStateOf("") }
    var regSenhaAcesso by remember { mutableStateOf("") }
    var regConfirmarSenha by remember { mutableStateOf("") }
    var mostrarSenhaRegistro by remember { mutableStateOf(false) }
    var regTipoSanguineo by remember { mutableStateOf("O+") }
    var regRegional by remember { mutableStateOf("Sede Nacional - SP") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFF1A1410),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Header com Brasão Oficial Iron Side Road Rider MC
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_motoclube_1791663228603),
                    contentDescription = "Banner Moto Clube Iron Side",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x220E1013),
                                    Color(0xEE0E1013)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_logo_ironside_oficial_1791667987118),
                        contentDescription = "Brasão Oficial Iron Side Road Rider MC",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IRON SIDE • ROAD RIDER MC",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            text = "Os melhores são de ferro • Acesso por CPF & Admin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Seletor Principal: QUEM ESTÁ ACESSANDO? [MEMBRO] vs [DIRETORIA]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUEM ESTÁ ACESSANDO?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA)
                                MaterialTheme.colorScheme.secondary
                            else
                                StatusGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA)
                                    "DIRETORIA"
                                else
                                    "MEMBRO (CPF)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA)
                                    MaterialTheme.colorScheme.onSecondary
                                else
                                    Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccessRoleSelectorCard(
                            modifier = Modifier.weight(1f),
                            title = "🏍️ MEMBRO",
                            subtitle = "Acesso pelo CPF (RLS)",
                            icon = Icons.Default.Person,
                            selected = tipoAcessoSelecionado == TipoAcessoClube.MEMBRO,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                tipoAcessoSelecionado = TipoAcessoClube.MEMBRO
                                onSelectFlow(TipoAcessoClube.MEMBRO)
                                errorMessage = null
                            },
                            testTag = "select_role_membro"
                        )
                        AccessRoleSelectorCard(
                            modifier = Modifier.weight(1f),
                            title = "🛡️ DIRETORIA",
                            subtitle = "Acesso Total Comando",
                            icon = Icons.Default.AdminPanelSettings,
                            selected = tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = {
                                tipoAcessoSelecionado = TipoAcessoClube.DIRETORIA
                                onSelectFlow(TipoAcessoClube.DIRETORIA)
                                authTab = 0
                                errorMessage = null
                            },
                            testTag = "select_role_diretoria"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Card de Login / Registro conforme quem está acessando
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA) {
                        // MODO DIRETORIA: ACESSO RESTRITO
                        IronSideFormHeader(
                            title = "ACESSO DA DIRETORIA",
                            subtitle = "Informe suas credenciais de comando para continuar"
                        )

                        OutlinedTextField(
                            value = loginDiretoriaUser,
                            onValueChange = { loginDiretoriaUser = it },
                            label = { Text("Login da Diretoria *") },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("diretoria_login_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = loginDiretoriaPass,
                            onValueChange = { loginDiretoriaPass = it },
                            label = { Text("Senha da Diretoria *") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { mostrarSenhaLogin = !mostrarSenhaLogin }) {
                                    Icon(
                                        imageVector = if (mostrarSenhaLogin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar ou ocultar senha"
                                    )
                                }
                            },
                            visualTransformation = if (mostrarSenhaLogin) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("diretoria_password_input"),
                            singleLine = true
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ ACESSO TOTAL LIBERADO: Gestão de todos os associados, mudança de status (Pendente → Aprovado → Ativo), geração de Mensalidades PIX em lote, publicação de Eventos/Comboios e Comunicados Oficiais.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        // MODO MEMBRO: ACESSO PELO CPF OU REGISTRO DE NOVO MEMBRO POR CPF
                        TabRow(
                            selectedTabIndex = authTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = authTab == 0,
                                onClick = {
                                    authTab = 0
                                    errorMessage = null
                                },
                                text = { Text("🔑 Entrar (CPF)", style = MaterialTheme.typography.labelLarge, maxLines = 1) },
                                modifier = Modifier.testTag("auth_tab_login")
                            )
                            Tab(
                                selected = authTab == 1,
                                onClick = {
                                    authTab = 1
                                    errorMessage = null
                                },
                                text = { Text("📝 Novo Registro", style = MaterialTheme.typography.labelLarge, maxLines = 1) },
                                modifier = Modifier.testTag("auth_tab_register")
                            )
                        }

                        if (authTab == 0) {
                            // ABA 0 (MEMBRO): ENTRAR COM CPF E SENHA
                            IronSideFormHeader(
                                title = "ACESSO DO MEMBRO POR CPF",
                                subtitle = "Acesso restrito aos dados do seu CPF"
                            )

                            OutlinedTextField(
                                value = loginCpfMembro,
                                onValueChange = { loginCpfMembro = it },
                                label = { Text("CPF do Membro *") },
                                placeholder = { Text("Ex: 123.456.789-00") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_identifier_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = loginSenhaMembro,
                                onValueChange = { loginSenhaMembro = it },
                                label = { Text("Senha de Acesso do Membro *") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { mostrarSenhaLogin = !mostrarSenhaLogin }) {
                                        Icon(
                                            imageVector = if (mostrarSenhaLogin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Mostrar ou ocultar senha"
                                        )
                                    }
                                },
                                visualTransformation = if (mostrarSenhaLogin) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input"),
                                singleLine = true
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🔒 ISOLAMENTO POR CPF: Como MEMBRO, você visualiza e gerencia exclusivamente os seus próprios dados cadastrais, sua Carteirinha Digital, suas motos/manutenções/abastecimentos e suas mensalidades vinculadas ao seu CPF.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            // ABA 1 (MEMBRO): CRIAR REGISTRO DE MEMBRO VINCULADO AO CPF
                            IronSideFormHeader(
                                title = "CRIAR REGISTRO DE MEMBRO (CPF & SENHA)",
                                subtitle = "Formação de Login pelo CPF e Senha Pessoal de Acesso"
                            )

                            Text(
                                text = "1. Identificação Pessoal & CPF de Acesso",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = regCpf,
                                onValueChange = { regCpf = it },
                                label = { Text("CPF (Será seu Login de Acesso) *") },
                                placeholder = { Text("000.000.000-00") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_cpf_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = regNomeCompleto,
                                onValueChange = { regNomeCompleto = it },
                                label = { Text("Nome Completo *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_nome_completo_input"),
                                singleLine = true
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = regNomeGuerra,
                                    onValueChange = { regNomeGuerra = it },
                                    label = { Text("Nome Guerra *") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("register_nome_guerra_input"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = regCelular,
                                    onValueChange = { regCelular = it },
                                    label = { Text("WhatsApp *") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = regRegional,
                                onValueChange = { regRegional = it },
                                label = { Text("Regional / Cidade") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                            Text(
                                text = "2. Formação de Senha para o seu CPF",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = regSenhaAcesso,
                                    onValueChange = { regSenhaAcesso = it },
                                    label = { Text("Criar Senha *") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { mostrarSenhaRegistro = !mostrarSenhaRegistro }) {
                                            Icon(
                                                imageVector = if (mostrarSenhaRegistro) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Mostrar senha"
                                            )
                                        }
                                    },
                                    visualTransformation = if (mostrarSenhaRegistro) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("register_password_input"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = regConfirmarSenha,
                                    onValueChange = { regConfirmarSenha = it },
                                    label = { Text("Confirmar Senha *") },
                                    visualTransformation = if (mostrarSenhaRegistro) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("register_confirm_password_input"),
                                    singleLine = true
                                )
                            }

                            Text(
                                text = "Tipo Sanguíneo (Carteirinha & SOS Emergência):",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val tiposSangue = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(tiposSangue) { ts ->
                                    FilterChip(
                                        selected = regTipoSanguineo == ts,
                                        onClick = { regTipoSanguineo = ts },
                                        label = { Text(ts) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA) {
                                // Validação estrita para DIRETORIA
                                val userClean = loginDiretoriaUser.trim().uppercase()
                                val passClean = loginDiretoriaPass.trim().uppercase()
                                if (userClean != DIRETORIA_MASTER_LOGIN || passClean != DIRETORIA_MASTER_PASS) {
                                    errorMessage = "Acesso Diretoria negado! Credenciais de comando inválidas."
                                    return@Button
                                }
                                PendingRegistrationStore.pendingData = PendingRegistrationData(
                                    isNewRegistration = false,
                                    tipoAcesso = TipoAcessoClube.DIRETORIA,
                                    isDiretoriaMasterAuthenticated = true,
                                    loginIdentificador = DIRETORIA_MASTER_LOGIN,
                                    senhaClube = DIRETORIA_MASTER_PASS,
                                    cargoDiretoria = "Diretoria / Acesso Total"
                                )
                            } else {
                                // Modo MEMBRO: Acesso e isolamento através do CPF
                                if (authTab == 1) {
                                    val digitsCpf = normalizeCpfDigits(regCpf)
                                    if (digitsCpf.length < 11) {
                                        errorMessage = "Informe um CPF válido (11 dígitos) para criar seu registro de membro."
                                        return@Button
                                    }
                                    if (regNomeCompleto.isBlank() || regNomeGuerra.isBlank()) {
                                        errorMessage = "Informe seu Nome Completo e Nome de Guerra."
                                        return@Button
                                    }
                                    if (regSenhaAcesso.length < 4) {
                                        errorMessage = "Crie uma senha de acesso com pelo menos 4 caracteres."
                                        return@Button
                                    }
                                    if (regConfirmarSenha.isNotBlank() && regSenhaAcesso != regConfirmarSenha) {
                                        errorMessage = "A confirmação de senha não confere com a senha criada."
                                        return@Button
                                    }
                                    val formattedCpf = formatCpfMask(digitsCpf)
                                    PendingRegistrationStore.pendingData = PendingRegistrationData(
                                        isNewRegistration = true,
                                        tipoAcesso = TipoAcessoClube.MEMBRO,
                                        isDiretoriaMasterAuthenticated = false,
                                        loginIdentificador = formattedCpf,
                                        senhaClube = regSenhaAcesso,
                                        nomeCompleto = regNomeCompleto.trim(),
                                        nomeGuerra = regNomeGuerra.trim(),
                                        cpf = formattedCpf,
                                        celular = regCelular.trim(),
                                        cargoDiretoria = "Integrante Escudado",
                                        tipoSanguineo = regTipoSanguineo,
                                        regional = regRegional.trim()
                                    )
                                } else {
                                    val digitsCpf = normalizeCpfDigits(loginCpfMembro)
                                    if (digitsCpf.length < 11) {
                                        errorMessage = "Para entrar como MEMBRO, digite os 11 números do seu CPF registrado."
                                        return@Button
                                    }
                                    if (loginSenhaMembro.isBlank()) {
                                        errorMessage = "Digite sua senha de membro para acessar os dados do seu CPF."
                                        return@Button
                                    }
                                    val formattedCpf = formatCpfMask(digitsCpf)
                                    PendingRegistrationStore.pendingData = PendingRegistrationData(
                                        isNewRegistration = false,
                                        tipoAcesso = TipoAcessoClube.MEMBRO,
                                        isDiretoriaMasterAuthenticated = false,
                                        loginIdentificador = formattedCpf,
                                        senhaClube = loginSenhaMembro,
                                        cpf = formattedCpf,
                                        cargoDiretoria = "Integrante Escudado"
                                    )
                                }
                            }

                            val pending = PendingRegistrationStore.pendingData
                            if (onStartManagedSignIn != null && pending != null) {
                                errorMessage = null
                                onStartManagedSignIn(pending)
                            } else {
                                isLoading = true
                                errorMessage = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = coroutineScope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.primary,
                            contentColor = if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA)
                                MaterialTheme.colorScheme.onSecondary
                            else
                                MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA)
                                    Icons.Default.AdminPanelSettings
                                else if (authTab == 0)
                                    Icons.Default.Shield
                                else
                                    Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when {
                                    tipoAcessoSelecionado == TipoAcessoClube.DIRETORIA ->
                                        "Entrar na DIRETORIA (Acesso Total)"
                                    authTab == 0 ->
                                        "Acessar Meus Dados pelo CPF"
                                    else ->
                                        "Criar Registro de Membro com CPF"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DIRETORIA (Acesso Restrito) • MEMBRO (Isolamento por CPF) • Firebase Auth",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AccessRoleSelectorCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (selected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selecionado",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) accentColor else Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
