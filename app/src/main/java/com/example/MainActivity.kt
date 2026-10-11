package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.ClubAuthState
import com.example.auth.ClubAuthViewModel
import com.example.auth.FirebaseAuthInitializer
import com.example.auth.FluxoAutenticadoClube
import com.example.data.ConfiguracaoIdentidadeClube
import com.example.data.MotoClubeRepository
import com.example.data.UiState
import com.example.ui.AuthScreen
import com.example.ui.EstradaPillarScreen
import com.example.ui.GaragemPillarScreen
import com.example.ui.InicioDashboardScreen
import com.example.ui.IronSideLogoBadge
import com.example.ui.MainAppLayout
import com.example.ui.MotoClubePillarScreen
import com.example.ui.MotoClubeViewModel
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialização explícita do Firebase Auth na subida da Activity
        val initializedAuth = FirebaseAuthInitializer.initialize(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation(auth = initializedAuth)
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = FirebaseAuthInitializer.initialize(LocalContext.current)) {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    val authViewModel: ClubAuthViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ClubAuthViewModel(
                    auth = auth,
                    repository = MotoClubeRepository(db)
                )
            }
        }
    )

    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        authViewModel.trySilentAutoSignInIfNeeded(
            context = context,
            credentialManager = credentialManager
        )
    }

    when (val state = authState) {
        is ClubAuthState.Initializing -> {
            AuthTransitionSplash(
                title = "INICIALIZANDO FIREBASE AUTH",
                subtitle = "Verificando sessão segura e políticas de acesso (Membro / Diretoria)..."
            )
        }
        is ClubAuthState.Authenticating -> {
            AuthTransitionSplash(
                title = "AUTENTICANDO SESSÃO (${state.targetFlow.titulo})",
                subtitle = state.statusMessage
            )
        }
        is ClubAuthState.Unauthenticated -> {
            AuthScreen(
                onAuthSuccess = {},
                initialSelectedFlow = state.selectedFlow,
                externalErrorMessage = state.errorMessage,
                onSelectFlow = { authViewModel.selectPreAuthFlow(it) },
                onStartManagedSignIn = { pendingData ->
                    authViewModel.startGoogleSignIn(
                        context = context,
                        credentialManager = credentialManager,
                        pendingData = pendingData
                    )
                }
            )
        }
        is ClubAuthState.Error -> {
            AuthScreen(
                onAuthSuccess = {},
                initialSelectedFlow = state.lastSelectedFlow,
                externalErrorMessage = state.message,
                onSelectFlow = { authViewModel.selectPreAuthFlow(it) },
                onStartManagedSignIn = { pendingData ->
                    authViewModel.startGoogleSignIn(
                        context = context,
                        credentialManager = credentialManager,
                        pendingData = pendingData
                    )
                }
            )
        }
        is ClubAuthState.AuthenticatedMembro -> {
            // Fluxo Autenticado de MEMBRO: Escopo individual por CPF / RLS
            MainFeatureScreen(
                currentUserId = state.userId,
                currentUserEmail = state.email,
                currentUserDisplayName = state.displayName,
                authenticatedFlow = FluxoAutenticadoClube.MEMBRO,
                onSwitchAuthenticatedFlow = { targetFlow ->
                    authViewModel.switchAuthenticatedFlow(targetFlow)
                },
                onManagedSignOut = {
                    authViewModel.signOutSession(context, credentialManager)
                }
            )
        }
        is ClubAuthState.AuthenticatedDiretoria -> {
            // Fluxo Autenticado de DIRETORIA: Acesso Total Administrativo / RLS Admin
            MainFeatureScreen(
                currentUserId = state.userId,
                currentUserEmail = state.email,
                currentUserDisplayName = state.displayName,
                authenticatedFlow = FluxoAutenticadoClube.DIRETORIA,
                onSwitchAuthenticatedFlow = { targetFlow ->
                    authViewModel.switchAuthenticatedFlow(targetFlow)
                },
                onManagedSignOut = {
                    authViewModel.signOutSession(context, credentialManager)
                }
            )
        }
    }
}

@Composable
private fun AuthTransitionSplash(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("auth_initializing_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IronSideLogoBadge(size = 82.dp)
            Spacer(modifier = Modifier.height(18.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MainFeatureScreen(
    currentUserId: String,
    currentUserEmail: String,
    currentUserDisplayName: String,
    authenticatedFlow: FluxoAutenticadoClube = FluxoAutenticadoClube.MEMBRO,
    onSwitchAuthenticatedFlow: ((FluxoAutenticadoClube) -> Unit)? = null,
    onManagedSignOut: (() -> Unit)? = null
) {
    val viewModel: MotoClubeViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MotoClubeViewModel(
                    repository = MotoClubeRepository(db),
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail,
                    currentUserDisplayName = currentUserDisplayName
                )
            }
        }
    )

    // Destinos principais (Barra Móvel e Menu Lateral Desktop):
    // 0 = Início, 1 = Veículos, 2 = Eventos, 3 = Carteirinha, 4 = Perfil, 5 = Estrada & SOS
    var selectedDestination by rememberSaveable { mutableIntStateOf(0) }
    var garagemInitialTab by rememberSaveable { mutableIntStateOf(0) }
    var motoClubeInitialTab by rememberSaveable { mutableIntStateOf(0) }
    var estradaInitialTab by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = selectedDestination != 0) {
        selectedDestination = 0
    }

    val associadoUi by viewModel.associadoState.collectAsStateWithLifecycle()
    val todosAssociadosAdminUi by viewModel.todosAssociadosAdminState.collectAsStateWithLifecycle()
    val veiculosUi by viewModel.veiculosState.collectAsStateWithLifecycle()
    val manutencoesUi by viewModel.manutencoesState.collectAsStateWithLifecycle()
    val abastecimentosUi by viewModel.abastecimentosState.collectAsStateWithLifecycle()
    val eventosUi by viewModel.eventosState.collectAsStateWithLifecycle()
    val comunicadosUi by viewModel.comunicadosState.collectAsStateWithLifecycle()
    val mensalidadesUi by viewModel.mensalidadesState.collectAsStateWithLifecycle()
    val parceirosUi by viewModel.parceirosState.collectAsStateWithLifecycle()
    val configIdentidadeUi by viewModel.configuracaoIdentidadeState.collectAsStateWithLifecycle()
    val firestoreInitReport by viewModel.firestoreInitReportState.collectAsStateWithLifecycle()
    val feedbackMsg by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(feedbackMsg) {
        val msg = feedbackMsg
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val associado = (associadoUi as? UiState.Success)?.data
    val todosAssociadosAdmin = (todosAssociadosAdminUi as? UiState.Success)?.data ?: emptyList()
    val veiculos = (veiculosUi as? UiState.Success)?.data ?: emptyList()
    val manutencoes = (manutencoesUi as? UiState.Success)?.data ?: emptyList()
    val abastecimentos = (abastecimentosUi as? UiState.Success)?.data ?: emptyList()
    val eventos = (eventosUi as? UiState.Success)?.data ?: emptyList()
    val comunicados = (comunicadosUi as? UiState.Success)?.data ?: emptyList()
    val mensalidades = (mensalidadesUi as? UiState.Success)?.data ?: emptyList()
    val parceiros = (parceirosUi as? UiState.Success)?.data ?: emptyList()
    val configIdentidade = (configIdentidadeUi as? UiState.Success)?.data ?: ConfiguracaoIdentidadeClube()

    fun navigateToDestination(destIndex: Int) {
        when (destIndex) {
            1 -> garagemInitialTab = 0 // Veículos
            2 -> motoClubeInitialTab = 2 // Eventos & Avisos
            3 -> motoClubeInitialTab = 0 // Carteirinha Digital
            4 -> motoClubeInitialTab = 1 // Perfil (Cadastro, CNH, Contatos, PIX e Diretoria)
            5 -> estradaInitialTab = 0 // Estrada & SOS
        }
        selectedDestination = destIndex
    }

    val toggleFlowAction = {
        val nextFlow = if (authenticatedFlow == FluxoAutenticadoClube.DIRETORIA) {
            FluxoAutenticadoClube.MEMBRO
        } else {
            FluxoAutenticadoClube.DIRETORIA
        }
        if (onSwitchAuthenticatedFlow != null) {
            onSwitchAuthenticatedFlow(nextFlow)
        } else if (associado != null) {
            val isDir = associado.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
            val novoPerfil = if (isDir) "Associado" else "Diretoria"
            viewModel.alterarStatusAssociado(
                associado.userId,
                associado.statusAssociado,
                novoPerfil
            )
        }
    }

    MyApplicationTheme(clubIdentity = configIdentidade) {
        MainAppLayout(
            associado = associado,
            authenticatedFlow = authenticatedFlow,
            selectedDestination = selectedDestination,
            onSelectDestination = { destIndex -> navigateToDestination(destIndex) },
            onSelectQuickShortcut = { destIndex, subTab ->
                when (destIndex) {
                    1 -> garagemInitialTab = subTab
                    2, 3, 4 -> motoClubeInitialTab = subTab
                    5 -> estradaInitialTab = subTab
                }
                selectedDestination = destIndex
            },
            onAlternarModoAcesso = toggleFlowAction,
            onOpenEmergencySos = {
                estradaInitialTab = 0
                selectedDestination = 5
            },
            onSignOut = {
                onManagedSignOut?.invoke()
            },
            snackbarHostState = snackbarHostState
        ) {
            when (selectedDestination) {
                0 -> InicioDashboardScreen(
                    associado = associado,
                    todosAssociadosAdmin = todosAssociadosAdmin,
                    veiculos = veiculos,
                    manutencoes = manutencoes,
                    abastecimentos = abastecimentos,
                    eventos = eventos,
                    comunicados = comunicados,
                    mensalidades = mensalidades,
                    parceiros = parceiros,
                    configIdentidade = configIdentidade,
                    firestoreInitReport = firestoreInitReport,
                    onRunFirestoreInitScript = { viewModel.executarInicializacaoFirestore() },
                    onNavigateToPillar = { destIdx -> navigateToDestination(destIdx) },
                    onOpenAdminApprovalPanel = {
                        motoClubeInitialTab = 4
                        selectedDestination = 4
                    },
                    onOpenClubIdentitySettings = {
                        motoClubeInitialTab = 5
                        selectedDestination = 4
                    },
                    onAlternarModoAcesso = toggleFlowAction,
                    onAlterarStatusAssociado = { uid, st, pf -> viewModel.alterarStatusAssociado(uid, st, pf) },
                    onOpenEmergencyModal = {
                        estradaInitialTab = 0
                        selectedDestination = 5
                    },
                    onConfirmarPresenca = { ev -> viewModel.confirmarPresenca(ev) }
                )
                1 -> GaragemPillarScreen(
                    veiculos = veiculos,
                    manutencoes = manutencoes,
                    abastecimentos = abastecimentos,
                    initialTab = garagemInitialTab,
                    onCadastrarVeiculo = { viewModel.cadastrarVeiculo(it) },
                    onRemoverVeiculo = { viewModel.removerVeiculo(it) },
                    onRegistrarManutencao = { viewModel.registrarManutencao(it) },
                    onRemoverManutencao = { viewModel.removerManutencao(it) },
                    onRegistrarAbastecimento = { viewModel.registrarAbastecimento(it) },
                    onIrParaGuinchoProximo = {
                        estradaInitialTab = 1
                        selectedDestination = 5
                    }
                )
                2, 3, 4 -> MotoClubePillarScreen(
                    associado = associado,
                    todosAssociadosAdmin = todosAssociadosAdmin,
                    veiculos = veiculos,
                    manutencoes = manutencoes,
                    abastecimentos = abastecimentos,
                    eventos = eventos,
                    comunicados = comunicados,
                    mensalidades = mensalidades,
                    configIdentidade = configIdentidade,
                    initialTab = motoClubeInitialTab,
                    onSalvarAssociado = { viewModel.salvarPerfilAssociado(it) },
                    onAlterarStatusAssociado = { uid, st, pf -> viewModel.alterarStatusAssociado(uid, st, pf) },
                    onRegistrarDesligamento = { uid, dtDesl, motDesl ->
                        viewModel.registrarDesligamentoAssociado(uid, dtDesl, motDesl)
                    },
                    onCadastrarVeiculo = { viewModel.cadastrarVeiculo(it) },
                    onCriarEvento = { viewModel.criarEvento(it) },
                    onConfirmarPresenca = { viewModel.confirmarPresenca(it) },
                    onPublicarComunicado = { viewModel.publicarComunicado(it) },
                    onGerarMensalidade = { comp, vlr, venc ->
                        viewModel.gerarMensalidadeIndividualOuLote(comp, vlr, venc, associado)
                    },
                    onConfirmarPagamentoPix = { id, dt -> viewModel.confirmarPagamentoPix(id, dt) },
                    onSalvarConfiguracaoIdentidade = { cfg -> viewModel.salvarConfiguracaoIdentidadeClube(cfg) }
                )
                5 -> EstradaPillarScreen(
                    associado = associado,
                    parceiros = parceiros,
                    initialTab = estradaInitialTab,
                    onCadastrarParceiro = { viewModel.cadastrarParceiroServico(it) },
                    onAlternarStatusAtivoParceiro = { id, st -> viewModel.alternarStatusAtivoParceiro(id, st) },
                    onRemoverParceiro = { id -> viewModel.removerParceiroServico(id) }
                )
            }
        }
    }
}
