package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Associado
import com.example.data.MotoClubeRepository
import com.example.ui.PendingRegistrationData
import com.example.ui.PendingRegistrationStore
import com.example.ui.TipoAcessoClube
import com.example.ui.attemptAutoSignIn
import com.example.ui.onGoogleSignInClicked
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Fluxos de acesso autenticados no Moto Clube.
 */
enum class FluxoAutenticadoClube(
    val label: String,
    val badgeText: String,
    val isDiretoriaTotal: Boolean
) {
    MEMBRO(
        label = "Fluxo Membro (Isolamento por CPF / RLS)",
        badgeText = "🏍️ FLUXO MEMBRO • RLS PRÓPRIO",
        isDiretoriaTotal = false
    ),
    DIRETORIA(
        label = "Fluxo Diretoria (Acesso Total / Admin)",
        badgeText = "🛡️ FLUXO DIRETORIA • ACESSO TOTAL",
        isDiretoriaTotal = true
    )
}

/**
 * Estados de autenticação reativos para separar o estado deslogado,
 * a verificação inicial do Firebase Auth e os fluxos autenticados de Membro e Diretoria.
 */
sealed interface ClubAuthState {
    /** Inicializando o Firebase Auth e verificando credenciais de sessão existentes. */
    data object Initializing : ClubAuthState

    /** Usuário não autenticado (exibe tela de login com seletor Membro / Diretoria + Google Sign-In). */
    data class Unauthenticated(
        val selectedFlow: TipoAcessoClube = TipoAcessoClube.MEMBRO,
        val errorMessage: String? = null
    ) : ClubAuthState

    /** Autenticando via Google Sign-In (Credential Manager) ou resolvendo perfil no Firestore. */
    data class Authenticating(
        val targetFlow: TipoAcessoClube,
        val statusMessage: String = "Inicializando sessão segura no Firebase Auth..."
    ) : ClubAuthState

    /**
     * Fluxo autenticado de MEMBRO:
     * Acesso individual restrito aos próprios registros (Associado, Veículos, Manutenções, Abastecimentos e Mensalidades).
     */
    data class AuthenticatedMembro(
        val userId: String,
        val email: String,
        val displayName: String,
        val cpf: String,
        val statusAssociado: String,
        val cargo: String,
        val numeroAssociado: String,
        val associado: Associado? = null
    ) : ClubAuthState

    /**
     * Fluxo autenticado de DIRETORIA:
     * Acesso total administrativo (aprovação de membros, gestão de todos os registros RLS, Dashboard Financeiro e Identidade).
     */
    data class AuthenticatedDiretoria(
        val userId: String,
        val email: String,
        val displayName: String,
        val perfilAcesso: String,
        val cargo: String,
        val numeroAssociado: String,
        val associado: Associado? = null
    ) : ClubAuthState

    /** Estado de erro de autenticação com opção de retomada segura. */
    data class Error(
        val message: String,
        val lastSelectedFlow: TipoAcessoClube = TipoAcessoClube.MEMBRO
    ) : ClubAuthState
}

/**
 * Inicializador central do Firebase Auth e gerenciador reativo dos estados de autenticação
 * que separam os fluxos de Membro e Diretoria no aplicativo.
 */
object FirebaseAuthInitializer {
    private const val TAG = "FirebaseAuthInit"

    /**
     * Garante que o FirebaseApp padrão esteja inicializado e configura o idioma do FirebaseAuth
     * para português do Brasil ("pt-BR").
     */
    fun initialize(context: Context): FirebaseAuth {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context.applicationContext)
                Log.i(TAG, "FirebaseApp initialized successfully.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp initialization check warning: ${e.message}")
        }
        return FirebaseAuth.getInstance().apply {
            setLanguageCode("pt-BR")
        }
    }
}

class ClubAuthViewModel(
    private val auth: FirebaseAuth,
    private val repository: MotoClubeRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<ClubAuthState>(ClubAuthState.Initializing)
    val authState: StateFlow<ClubAuthState> = _authState.asStateFlow()

    private var selectedPreAuthFlow: TipoAcessoClube = TipoAcessoClube.MEMBRO
    private var profileObserverJob: Job? = null
    private var observedUid: String? = null
    private var autoSignInAttempted: Boolean = false

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            stopObservingProfile()
            _authState.value = ClubAuthState.Unauthenticated(selectedFlow = selectedPreAuthFlow)
        } else {
            resolveAndObserveAuthenticatedFlow(user)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    /**
     * Tenta autenticação silenciosa via Credential Manager na inicialização do app
     * caso não haja sessão ativa no FirebaseAuth.
     */
    fun trySilentAutoSignInIfNeeded(
        context: Context,
        credentialManager: CredentialManager
    ) {
        if (autoSignInAttempted || auth.currentUser != null) return
        autoSignInAttempted = true
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                Log.i(TAG, "Silent Google Auto-Sign-In succeeded.")
            },
            onUnauthenticated = {
                if (auth.currentUser == null) {
                    _authState.value = ClubAuthState.Unauthenticated(selectedFlow = selectedPreAuthFlow)
                }
            },
            scope = viewModelScope
        )
    }

    /**
     * Seleciona previamente o fluxo desejado na tela de login (MEMBRO vs DIRETORIA).
     */
    fun selectPreAuthFlow(flow: TipoAcessoClube) {
        selectedPreAuthFlow = flow
        val current = _authState.value
        if (current is ClubAuthState.Unauthenticated) {
            _authState.value = current.copy(selectedFlow = flow, errorMessage = null)
        } else if (current is ClubAuthState.Error) {
            _authState.value = ClubAuthState.Unauthenticated(selectedFlow = flow, errorMessage = null)
        }
    }

    /**
     * Inicia o login interativo com Google via Credential Manager vinculando os dados pendentes
     * de Membro (CPF) ou Diretoria (ADMIN / ADMIN100).
     */
    fun startGoogleSignIn(
        context: Context,
        credentialManager: CredentialManager,
        pendingData: PendingRegistrationData
    ) {
        selectedPreAuthFlow = pendingData.tipoAcesso
        PendingRegistrationStore.pendingData = pendingData
        _authState.value = ClubAuthState.Authenticating(
            targetFlow = pendingData.tipoAcesso,
            statusMessage = if (pendingData.tipoAcesso == TipoAcessoClube.DIRETORIA) {
                "Autenticando credencial de Comando e liberando Fluxo Diretoria..."
            } else {
                "Autenticando conta Google e vinculando Fluxo Membro (CPF ${pendingData.cpf})..."
            }
        )

        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                val user = auth.currentUser
                if (user != null) {
                    resolveAndObserveAuthenticatedFlow(user, forceRefreshFromPending = true)
                }
            },
            onAuthError = { errorMsg ->
                _authState.value = ClubAuthState.Unauthenticated(
                    selectedFlow = pendingData.tipoAcesso,
                    errorMessage = errorMsg
                )
            },
            scope = viewModelScope,
            onAuthCancelled = {
                _authState.value = ClubAuthState.Unauthenticated(
                    selectedFlow = pendingData.tipoAcesso,
                    errorMessage = null
                )
            }
        )
    }

    /**
     * Resolve o perfil do usuário autenticado no Firestore e mantém um listener em tempo real
     * para separar dinamicamente os estados `AuthenticatedMembro` e `AuthenticatedDiretoria`.
     */
    private fun resolveAndObserveAuthenticatedFlow(
        user: FirebaseUser,
        forceRefreshFromPending: Boolean = false
    ) {
        if (!forceRefreshFromPending && observedUid == user.uid && profileObserverJob?.isActive == true) {
            return
        }
        stopObservingProfile()
        observedUid = user.uid

        val pendingSnapshot = PendingRegistrationStore.pendingData
        val targetFlowHint = pendingSnapshot?.tipoAcesso ?: selectedPreAuthFlow
        _authState.value = ClubAuthState.Authenticating(
            targetFlow = targetFlowHint,
            statusMessage = "Carregando permissões de acesso (${targetFlowHint.titulo})..."
        )

        profileObserverJob = viewModelScope.launch {
            repository.observeAssociado(user.uid)
                .catch { e ->
                    Log.w(TAG, "Failed to observe associado for auth flow separation, falling back to token/default role", e)
                    emit(null)
                }
                .collect { associado ->
                    val currentPending = PendingRegistrationStore.pendingData
                    val isBootstrappedEmailAdmin = user.email?.equals("aalmeida.eds@gmail.com", ignoreCase = true) == true
                    val perfilFirestore = when {
                        currentPending?.isDiretoriaMasterAuthenticated == true -> "SuperAdmin"
                        currentPending != null && currentPending.tipoAcesso == TipoAcessoClube.MEMBRO -> "Associado"
                        !associado?.perfilAcesso.isNullOrBlank() -> associado?.perfilAcesso ?: "Associado"
                        isBootstrappedEmailAdmin && selectedPreAuthFlow == TipoAcessoClube.DIRETORIA -> "Diretoria"
                        else -> "Associado"
                    }

                    val isDiretoriaRole = perfilFirestore in DIRETORIA_ROLES
                    val displayName = associado?.nomeGuerra?.takeIf { it.isNotBlank() }
                        ?: associado?.nomeCompleto?.takeIf { it.isNotBlank() }
                        ?: user.displayName?.takeIf { it.isNotBlank() }
                        ?: "Comandante Iron Side"

                    if (isDiretoriaRole) {
                        _authState.value = ClubAuthState.AuthenticatedDiretoria(
                            userId = user.uid,
                            email = user.email ?: "",
                            displayName = displayName,
                            perfilAcesso = perfilFirestore,
                            cargo = associado?.cargo?.ifBlank { "Diretoria / Acesso Total" } ?: "Diretoria / Acesso Total",
                            numeroAssociado = associado?.numeroAssociado ?: "MC-000001",
                            associado = associado
                        )
                    } else {
                        _authState.value = ClubAuthState.AuthenticatedMembro(
                            userId = user.uid,
                            email = user.email ?: "",
                            displayName = displayName,
                            cpf = associado?.cpf?.ifBlank { currentPending?.cpf ?: "123.456.789-00" } ?: "123.456.789-00",
                            statusAssociado = associado?.statusAssociado?.ifBlank { "Ativo" } ?: "Ativo",
                            cargo = associado?.cargo?.ifBlank { "Integrante Escudado" } ?: "Integrante Escudado",
                            numeroAssociado = associado?.numeroAssociado ?: "MC-000001",
                            associado = associado
                        )
                    }
                }
        }
    }

    /**
     * Alterna em tempo real entre o estado autenticado de Membro e o de Diretoria,
     * persistindo a mudança no Firestore e refletindo imediatamente na árvore de UI.
     */
    fun switchAuthenticatedFlow(targetFlow: FluxoAutenticadoClube) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val currentAssoc = when (val state = _authState.value) {
                is ClubAuthState.AuthenticatedDiretoria -> state.associado
                is ClubAuthState.AuthenticatedMembro -> state.associado
                else -> null
            }
            val currentStatus = currentAssoc?.statusAssociado?.ifBlank { "Ativo" } ?: "Ativo"
            val novoPerfil = if (targetFlow == FluxoAutenticadoClube.DIRETORIA) "Diretoria" else "Associado"
            repository.updateAssociadoStatusAndRole(
                targetUserId = user.uid,
                novoStatus = currentStatus,
                novoPerfil = novoPerfil
            )
        }
    }

    /**
     * Encerra a sessão no Firebase Auth e limpa o estado de credenciais no Credential Manager.
     */
    fun signOutSession(
        context: Context,
        credentialManager: CredentialManager
    ) {
        stopObservingProfile()
        PendingRegistrationStore.pendingData = null
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Clear credential state warning: ${e.message}")
            } finally {
                auth.signOut()
                _authState.value = ClubAuthState.Unauthenticated(selectedFlow = selectedPreAuthFlow)
            }
        }
    }

    private fun stopObservingProfile() {
        profileObserverJob?.cancel()
        profileObserverJob = null
        observedUid = null
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        stopObservingProfile()
    }

    companion object {
        private const val TAG = "ClubAuthVM"
        val DIRETORIA_ROLES = setOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    }
}
