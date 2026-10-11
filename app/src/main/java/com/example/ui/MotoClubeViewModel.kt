package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Abastecimento
import com.example.data.Associado
import com.example.data.Comunicado
import com.example.data.ConfiguracaoIdentidadeClube
import com.example.data.Evento
import com.example.data.FirestoreDatabaseInitializer
import com.example.data.FirestoreInitReport
import com.example.data.Manutencao
import com.example.data.Mensalidade
import com.example.data.MotoClubeRepository
import com.example.data.ParceiroServico
import com.example.data.UiState
import com.example.data.Veiculo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MotoClubeViewModel(
    private val repository: MotoClubeRepository,
    val currentUserId: String,
    val currentUserEmail: String,
    val currentUserDisplayName: String
) : ViewModel() {

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val associadoState: StateFlow<UiState<Associado?>> = repository.observeAssociado(currentUserId)
        .map<Associado?, UiState<Associado?>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing associado", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar perfil do associado"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val isDiretoriaFlow = associadoState
        .map { state ->
            val perfil = (state as? UiState.Success)?.data?.perfilAcesso
            perfil in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
        }
        .distinctUntilChanged()

    // RLS: Diretoria lê todos os registros de associados; Associado lê apenas o próprio registro
    val todosAssociadosAdminState: StateFlow<UiState<List<Associado>>> = isDiretoriaFlow
        .flatMapLatest { isDiretoria ->
            if (isDiretoria) {
                repository.observeAllAssociadosForAdmin()
            } else {
                repository.observeAssociado(currentUserId).map { listOfNotNull(it) }
            }
        }
        .map<List<Associado>, UiState<List<Associado>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing associados with RLS", e)
            emit(UiState.Success(emptyList()))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    // RLS: Associado lê apenas os próprios veículos; Diretoria lê todos os veículos do clube
    val veiculosState: StateFlow<UiState<List<Veiculo>>> = isDiretoriaFlow
        .flatMapLatest { isDiretoria ->
            if (isDiretoria) {
                repository.observeAllVeiculosForAdmin()
            } else {
                repository.observeVeiculos()
            }
        }
        .map<List<Veiculo>, UiState<List<Veiculo>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing veiculos", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar veículos"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    // RLS: Associado lê apenas as próprias manutenções; Diretoria lê todas as manutenções
    val manutencoesState: StateFlow<UiState<List<Manutencao>>> = isDiretoriaFlow
        .flatMapLatest { isDiretoria ->
            if (isDiretoria) {
                repository.observeAllManutencoesForAdmin()
            } else {
                repository.observeManutencoes()
            }
        }
        .map<List<Manutencao>, UiState<List<Manutencao>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing manutencoes", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar manutenções"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    // RLS: Associado lê apenas os próprios abastecimentos; Diretoria lê todos os abastecimentos
    val abastecimentosState: StateFlow<UiState<List<Abastecimento>>> = isDiretoriaFlow
        .flatMapLatest { isDiretoria ->
            if (isDiretoria) {
                repository.observeAllAbastecimentosForAdmin()
            } else {
                repository.observeAbastecimentos()
            }
        }
        .map<List<Abastecimento>, UiState<List<Abastecimento>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing abastecimentos", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar abastecimentos"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val eventosState: StateFlow<UiState<List<Evento>>> = repository.observeEventos()
        .map<List<Evento>, UiState<List<Evento>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing eventos", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar eventos"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val comunicadosState: StateFlow<UiState<List<Comunicado>>> = repository.observeComunicados()
        .map<List<Comunicado>, UiState<List<Comunicado>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing comunicados", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar comunicados"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    // RLS: Associado lê apenas as próprias mensalidades; Diretoria lê todas as mensalidades
    val mensalidadesState: StateFlow<UiState<List<Mensalidade>>> = isDiretoriaFlow
        .flatMapLatest { isDiretoria ->
            if (isDiretoria) {
                repository.observeAllMensalidadesForAdmin()
            } else {
                repository.observeMensalidades()
            }
        }
        .map<List<Mensalidade>, UiState<List<Mensalidade>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing mensalidades", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar mensalidades"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val parceirosState: StateFlow<UiState<List<ParceiroServico>>> = repository.observeParceirosServicos()
        .map<List<ParceiroServico>, UiState<List<ParceiroServico>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing parceiros", e)
            emit(UiState.Error(e.localizedMessage ?: "Erro ao carregar SOS & Serviços"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val configuracaoIdentidadeState: StateFlow<UiState<ConfiguracaoIdentidadeClube>> = repository.observeConfiguracaoIdentidadeClube()
        .map<ConfiguracaoIdentidadeClube, UiState<ConfiguracaoIdentidadeClube>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing configuracao identidade", e)
            emit(UiState.Success(ConfiguracaoIdentidadeClube()))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _firestoreInitReportState = MutableStateFlow<FirestoreInitReport?>(null)
    val firestoreInitReportState: StateFlow<FirestoreInitReport?> = _firestoreInitReportState.asStateFlow()

    init {
        bootstrapInitialClubDataIfNeeded()
    }

    private fun bootstrapInitialClubDataIfNeeded() {
        viewModelScope.launch {
            try {
                val pendingReg = PendingRegistrationStore.pendingData
                PendingRegistrationStore.pendingData = null
                val initResult = FirestoreDatabaseInitializer.ensureInitializedAndVerifyPermissions(
                    repository = repository,
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail,
                    currentUserDisplayName = currentUserDisplayName,
                    pendingRegistration = pendingReg
                )
                initResult.onSuccess { report ->
                    _firestoreInitReportState.value = report
                    if (pendingReg != null) {
                        val isAdminMaster = pendingReg.isDiretoriaMasterAuthenticated
                        val cpfFinal = pendingReg.cpf.ifBlank { "123.456.789-00" }
                        _feedbackMessage.value = when {
                            isAdminMaster -> "DIRETORIA autenticada • Acesso Total liberado!"
                            pendingReg.isNewRegistration -> "Registro criado para o CPF $cpfFinal (Status: Pendente • Aguardando Aprovação da Diretoria)!"
                            else -> "Acesso de Membro liberado para os dados do CPF $cpfFinal!"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Bootstrap skipped or partial: ${e.message}")
            }
        }
    }

    fun executarInicializacaoFirestore() {
        viewModelScope.launch {
            val initResult = FirestoreDatabaseInitializer.ensureInitializedAndVerifyPermissions(
                repository = repository,
                currentUserId = currentUserId,
                currentUserEmail = currentUserEmail,
                currentUserDisplayName = currentUserDisplayName,
                pendingRegistration = null
            )
            initResult.fold(
                onSuccess = { report ->
                    _firestoreInitReportState.value = report
                    _feedbackMessage.value =
                        "Firestore inicializado e verificado: ${report.totalCollectionsConfigured}/9 coleções com permissões RLS ativas (${report.totalDocumentsVerified} documentos)!"
                },
                onFailure = { err ->
                    _feedbackMessage.value =
                        "Falha ao verificar inicialização do Firestore: ${err.localizedMessage}"
                }
            )
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun salvarPerfilAssociado(associado: Associado) {
        viewModelScope.launch {
            val res = repository.saveAssociado(associado)
            _feedbackMessage.value = if (res.isSuccess) {
                "Dados do associado e prontuário de emergência salvos com sucesso!"
            } else {
                "Erro ao salvar perfil: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun alterarStatusAssociado(targetUserId: String, novoStatus: String, novoPerfil: String) {
        viewModelScope.launch {
            val res = repository.updateAssociadoStatusAndRole(targetUserId, novoStatus, novoPerfil)
            _feedbackMessage.value = if (res.isSuccess) {
                "Status atualizado para $novoStatus ($novoPerfil)!"
            } else {
                "Erro ao atualizar status: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun registrarDesligamentoAssociado(
        targetUserId: String,
        dataDesligamento: String,
        motivoDesligamento: String
    ) {
        viewModelScope.launch {
            val novoStatus = if (dataDesligamento.isNotBlank()) "Desligado" else "Ativo"
            val res = repository.updateAssociadoDesligamento(
                targetUserId = targetUserId,
                dataDesligamento = dataDesligamento,
                motivoDesligamento = motivoDesligamento,
                novoStatus = novoStatus
            )
            _feedbackMessage.value = if (res.isSuccess) {
                if (dataDesligamento.isNotBlank()) {
                    "Desligamento registrado pela Diretoria em $dataDesligamento!"
                } else {
                    "Registro de desligamento removido • Associado reativado!"
                }
            } else {
                "Erro ao atualizar desligamento: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun cadastrarVeiculo(veiculo: Veiculo) {
        viewModelScope.launch {
            val assoc = (associadoState.value as? UiState.Success)?.data
            val isEdicao = veiculo.id.isNotBlank()
            val payload = veiculo.copy(
                associadoId = veiculo.associadoId.ifBlank { currentUserId },
                associadoCpf = veiculo.associadoCpf.ifBlank { assoc?.cpf ?: "" },
                nomeProprietarioDoc = veiculo.nomeProprietarioDoc.ifBlank { assoc?.nomeCompleto ?: "" },
                cpfCnpjProprietarioDoc = veiculo.cpfCnpjProprietarioDoc.ifBlank { assoc?.cpf ?: "" }
            )
            val res = if (isEdicao) {
                repository.updateVeiculo(payload)
            } else {
                repository.addVeiculo(payload)
            }
            _feedbackMessage.value = if (res.isSuccess) {
                if (isEdicao) {
                    "Dados e documento do veículo ${veiculo.marca} ${veiculo.modelo} atualizados!"
                } else {
                    "Veículo ${veiculo.marca} ${veiculo.modelo} e Documento CRLV adicionados à Garagem!"
                }
            } else {
                "Erro ao salvar veículo: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun removerVeiculo(veiculoId: String) {
        viewModelScope.launch {
            repository.deleteVeiculo(veiculoId)
            _feedbackMessage.value = "Veículo removido da garagem."
        }
    }

    fun registrarManutencao(manutencao: Manutencao, atualizarKmVeiculo: Boolean = true) {
        viewModelScope.launch {
            val isCorrecao = manutencao.id.isNotBlank()
            val assoc = (associadoState.value as? UiState.Success)?.data
            val res = repository.addManutencao(
                manutencao.copy(
                    associadoId = manutencao.associadoId.ifBlank { currentUserId },
                    associadoCpf = manutencao.associadoCpf.ifBlank { assoc?.cpf ?: "" },
                    status = manutencao.statusAlerta.ifBlank { "Em dia" }
                )
            )
            if (res.isSuccess && atualizarKmVeiculo && manutencao.veiculoId.isNotBlank()) {
                repository.updateVeiculoKm(manutencao.veiculoId, manutencao.km)
            }
            _feedbackMessage.value = if (res.isSuccess) {
                if (isCorrecao) {
                    "Correção do registro de manutenção (${manutencao.tipo}) salva com sucesso!"
                } else {
                    "Manutenção (${manutencao.tipo}) registrada com sucesso!"
                }
            } else {
                "Erro ao salvar manutenção: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun removerManutencao(manutencaoId: String) {
        viewModelScope.launch {
            val res = repository.deleteManutencao(manutencaoId)
            _feedbackMessage.value = if (res.isSuccess) {
                "Registro de manutenção removido."
            } else {
                "Erro ao remover registro de manutenção."
            }
        }
    }

    fun registrarAbastecimento(abastecimento: Abastecimento) {
        viewModelScope.launch {
            val assoc = (associadoState.value as? UiState.Success)?.data
            val res = repository.addAbastecimento(
                abastecimento.copy(
                    associadoId = currentUserId,
                    associadoCpf = assoc?.cpf ?: "",
                    status = "Registrado"
                )
            )
            if (res.isSuccess && abastecimento.veiculoId.isNotBlank()) {
                repository.updateVeiculoKm(abastecimento.veiculoId, abastecimento.km)
            }
            _feedbackMessage.value = if (res.isSuccess) {
                val consumoTxt = if (abastecimento.tanqueCheio && abastecimento.consumoCalculado > 0) {
                    String.format(" • Consumo: %.2f km/L", abastecimento.consumoCalculado)
                } else {
                    " (Abastecimento parcial)"
                }
                "Abastecimento registrado$consumoTxt!"
            } else {
                "Erro ao registrar abastecimento: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun criarEvento(evento: Evento) {
        viewModelScope.launch {
            val res = repository.addEvento(evento)
            _feedbackMessage.value = if (res.isSuccess) {
                "Evento '${evento.nome}' publicado para o Moto Clube!"
            } else {
                "Erro ao criar evento: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun confirmarPresenca(evento: Evento) {
        viewModelScope.launch {
            val res = repository.confirmarPresencaEvento(evento.id, evento.confirmadosCount)
            _feedbackMessage.value = if (res.isSuccess) {
                "Presença confirmada no comboio '${evento.nome}'!"
            } else {
                "Erro ao confirmar presença."
            }
        }
    }

    fun publicarComunicado(comunicado: Comunicado) {
        viewModelScope.launch {
            val res = repository.addComunicado(comunicado)
            _feedbackMessage.value = if (res.isSuccess) {
                "Comunicado publicado no mural oficial!"
            } else {
                "Erro ao publicar comunicado."
            }
        }
    }

    fun gerarMensalidadeIndividualOuLote(competencia: String, valor: Double, vencimento: String, associado: Associado?) {
        viewModelScope.launch {
            val configClube = (configuracaoIdentidadeState.value as? UiState.Success)?.data ?: ConfiguracaoIdentidadeClube()
            val valorFinal = if (valor > 0) valor else configClube.valorMensalidadePadrao
            val res = repository.addMensalidade(
                Mensalidade(
                    userId = currentUserId,
                    associadoNome = associado?.nomeCompleto?.ifBlank { "Associado ${configClube.nomeClube}" } ?: "Associado ${configClube.nomeClube}",
                    numeroAssociado = associado?.numeroAssociado ?: "MC-000001",
                    competencia = competencia,
                    ano = 2026L,
                    valor = valorFinal,
                    cpf = associado?.cpf ?: "123.456.789-00",
                    vencimento = vencimento,
                    status = "Pendente",
                    formaPagamento = "PIX Manual (${configClube.tipoChavePix})",
                    chavePix = configClube.chavePix.ifBlank { "financeiro@ironsidemotoclube.com.br" },
                    observacoes = "Lote mensal gerado pela Tesouraria • Favorecido: ${configClube.favorecidoPix}"
                )
            )
            _feedbackMessage.value = if (res.isSuccess) {
                "Cobrança PIX ($competencia • R$ ${String.format("%.2f", valorFinal)}) gerada com a Chave PIX oficial!"
            } else {
                "Erro ao gerar mensalidade."
            }
        }
    }

    fun salvarConfiguracaoIdentidadeClube(config: ConfiguracaoIdentidadeClube) {
        viewModelScope.launch {
            val res = repository.saveConfiguracaoIdentidadeClube(config)
            _feedbackMessage.value = if (res.isSuccess) {
                "Identidade do Clube ('${config.nomeClube}') salva e aplicada dinamicamente!"
            } else {
                "Erro ao salvar configurações de identidade: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun confirmarPagamentoPix(mensalidadeId: String, dataHoje: String) {
        viewModelScope.launch {
            val res = repository.registrarPagamentoPix(mensalidadeId, dataHoje)
            _feedbackMessage.value = if (res.isSuccess) {
                "Pagamento PIX confirmado! Mensalidade marcada como PAGA."
            } else {
                "Erro ao confirmar pagamento PIX."
            }
        }
    }

    fun cadastrarParceiroServico(parceiro: ParceiroServico) {
        viewModelScope.launch {
            val isEdicao = parceiro.id.isNotBlank()
            val res = repository.addParceiroServico(parceiro)
            _feedbackMessage.value = if (res.isSuccess) {
                if (isEdicao) {
                    "Prestador/Parceiro '${parceiro.nome}' atualizado na Base de Dados SOS!"
                } else {
                    "Prestador/Parceiro '${parceiro.nome}' cadastrado na Base de Dados SOS!"
                }
            } else {
                "Erro ao salvar parceiro: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun alternarStatusAtivoParceiro(parceiroId: String, novoAtivo: Boolean) {
        viewModelScope.launch {
            val res = repository.updateParceiroStatusAtivo(parceiroId, novoAtivo)
            _feedbackMessage.value = if (res.isSuccess) {
                val stTxt = if (novoAtivo) "ATIVO na Base SOS" else "INATIVO na Base SOS"
                "Status do prestador alterado para $stTxt!"
            } else {
                "Erro ao alterar status de atividade do prestador."
            }
        }
    }

    fun removerParceiroServico(parceiroId: String) {
        viewModelScope.launch {
            val res = repository.deleteParceiroServico(parceiroId)
            _feedbackMessage.value = if (res.isSuccess) {
                "Prestador/Parceiro removido da Base de Dados SOS."
            } else {
                "Erro ao remover prestador."
            }
        }
    }

    private companion object {
        const val TAG = "MotoClubeVM"
    }
}
