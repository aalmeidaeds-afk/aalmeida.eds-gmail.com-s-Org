package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MotoClubeRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Usuário precisa estar autenticado com Google antes de acessar o Firestore.")
    }

    // --- ASSOCIADO PROFILE ---
    fun observeAssociado(userId: String): Flow<Associado?> = flow {
        val path = "associados/$userId"
        emitAll(
            db.collection("associados").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(Associado::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(null)
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                        throw error
                    }
                }
        )
    }

    fun observeAllAssociadosForAdmin(): Flow<List<Associado>> = flow {
        val path = "associados"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Associado::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun getAssociadoOnce(userId: String): Associado? {
        val docRef = db.collection("associados").document(userId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(Associado::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveAssociado(associado: Associado): Result<Unit> {
        val uid = requireUserId()
        val targetUid = if (associado.userId.isNotBlank()) associado.userId else uid
        val docRef = db.collection("associados").document(targetUid)
        return try {
            val payload = associado.copy(
                id = targetUid,
                userId = targetUid,
                status = associado.statusAssociado.ifBlank { "Ativo" },
                createdAt = associado.createdAt,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateAssociadoStatusAndRole(
        targetUserId: String,
        novoStatus: String,
        novoPerfil: String
    ): Result<Unit> {
        requireUserId()
        val docRef = db.collection("associados").document(targetUserId)
        return try {
            docRef.update(
                mapOf(
                    "status" to novoStatus,
                    "statusAssociado" to novoStatus,
                    "perfilAcesso" to novoPerfil,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateAssociadoDesligamento(
        targetUserId: String,
        dataDesligamento: String,
        motivoDesligamento: String,
        novoStatus: String = if (dataDesligamento.isNotBlank()) "Desligado" else "Ativo"
    ): Result<Unit> {
        requireUserId()
        val docRef = db.collection("associados").document(targetUserId)
        return try {
            docRef.update(
                mapOf(
                    "dataDesligamento" to dataDesligamento.trim(),
                    "motivoDesligamento" to motivoDesligamento.trim(),
                    "status" to novoStatus,
                    "statusAssociado" to novoStatus,
                    "ativo" to dataDesligamento.isBlank(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- VEICULOS ---
    fun observeVeiculos(): Flow<List<Veiculo>> = flow {
        val uid = requireUserId()
        val path = "veiculos"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Veiculo::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun getUserVeiculos(): Result<List<Veiculo>> {
        val uid = requireUserId()
        val path = "veiculos"
        return try {
            val snap = db.collection(path)
                .whereEqualTo("userId", uid)
                .get()
                .await()
            Result.success(snap.toObjects(Veiculo::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun getVeiculoById(veiculoId: String): Result<Veiculo?> {
        requireUserId()
        val path = "veiculos/$veiculoId"
        return try {
            val snap = db.collection("veiculos").document(veiculoId).get().await()
            val veiculo = snap.toObject(Veiculo::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(veiculo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    fun observeAllVeiculosForAdmin(): Flow<List<Veiculo>> = flow {
        requireUserId()
        val path = "veiculos"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Veiculo::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun updateVeiculo(veiculo: Veiculo): Result<String> {
        val uid = requireUserId()
        require(veiculo.id.isNotBlank()) { "ID do veículo é obrigatório para edição" }
        val docRef = db.collection("veiculos").document(veiculo.id)
        return try {
            val payload = veiculo.copy(
                id = veiculo.id,
                userId = veiculo.userId.ifBlank { uid },
                associadoId = veiculo.associadoId.ifBlank { veiculo.userId.ifBlank { uid } },
                status = veiculo.status.ifBlank { if (veiculo.ativo) "Ativo" else "Inativo" },
                createdAt = veiculo.createdAt,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addVeiculo(veiculo: Veiculo): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("veiculos")
        val docRef = if (veiculo.id.isNotBlank()) colRef.document(veiculo.id) else colRef.document()
        return try {
            val payload = veiculo.copy(
                id = docRef.id,
                userId = uid,
                associadoId = veiculo.associadoId.ifBlank { uid },
                status = veiculo.status.ifBlank { if (veiculo.ativo) "Ativo" else "Inativo" },
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateVeiculoKm(veiculoId: String, novoKm: Long): Result<Unit> {
        requireUserId()
        val docRef = db.collection("veiculos").document(veiculoId)
        return try {
            docRef.update(
                mapOf(
                    "kmAtual" to novoKm,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteVeiculo(veiculoId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("veiculos").document(veiculoId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- MANUTENCOES ---
    fun observeManutencoes(): Flow<List<Manutencao>> = flow {
        val uid = requireUserId()
        val path = "manutencoes"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Manutencao::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.km }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    fun observeAllManutencoesForAdmin(): Flow<List<Manutencao>> = flow {
        requireUserId()
        val path = "manutencoes"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Manutencao::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.km }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun addManutencao(manutencao: Manutencao): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("manutencoes")
        val isExisting = manutencao.id.isNotBlank()
        val docRef = if (isExisting) colRef.document(manutencao.id) else colRef.document()
        return try {
            val payload = manutencao.copy(
                id = docRef.id,
                userId = manutencao.userId.ifBlank { uid },
                associadoId = manutencao.associadoId.ifBlank { uid },
                status = manutencao.status.ifBlank { manutencao.statusAlerta.ifBlank { "Em dia" } },
                createdAt = if (isExisting) manutencao.createdAt else null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isExisting) OperationType.UPDATE else OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateManutencao(manutencao: Manutencao): Result<String> {
        return addManutencao(manutencao)
    }

    suspend fun deleteManutencao(manutencaoId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("manutencoes").document(manutencaoId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- ABASTECIMENTOS ---
    fun observeAbastecimentos(): Flow<List<Abastecimento>> = flow {
        val uid = requireUserId()
        val path = "abastecimentos"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Abastecimento::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.km }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    fun observeAllAbastecimentosForAdmin(): Flow<List<Abastecimento>> = flow {
        requireUserId()
        val path = "abastecimentos"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Abastecimento::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.km }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun addAbastecimento(abastecimento: Abastecimento): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("abastecimentos")
        val docRef = colRef.document()
        return try {
            val payload = abastecimento.copy(
                id = docRef.id,
                userId = uid,
                associadoId = abastecimento.associadoId.ifBlank { uid },
                status = abastecimento.status.ifBlank { "Registrado" },
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- EVENTOS ---
    fun observeEventos(): Flow<List<Evento>> = flow {
        requireUserId()
        val path = "eventos"
        emitAll(
            db.collection(path)
                .whereEqualTo("ativo", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Evento::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun addEvento(evento: Evento): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("eventos").document()
        return try {
            val payload = evento.copy(
                id = docRef.id,
                userId = uid,
                associadoId = evento.associadoId.ifBlank { uid },
                status = evento.status.ifBlank { "Inscrições Abertas" },
                ativo = true,
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun confirmarPresencaEvento(eventoId: String, atualConfirmados: Long): Result<Unit> {
        requireUserId()
        val docRef = db.collection("eventos").document(eventoId)
        return try {
            docRef.update(
                mapOf(
                    "confirmadosCount" to (atualConfirmados + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- COMUNICADOS ---
    fun observeComunicados(): Flow<List<Comunicado>> = flow {
        requireUserId()
        val path = "comunicados"
        emitAll(
            db.collection(path)
                .whereEqualTo("ativo", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Comunicado::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun addComunicado(comunicado: Comunicado): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("comunicados").document()
        return try {
            val payload = comunicado.copy(
                id = docRef.id,
                userId = uid,
                ativo = true,
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- MENSALIDADES ---
    fun observeMensalidades(): Flow<List<Mensalidade>> = flow {
        val uid = requireUserId()
        val path = "mensalidades"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Mensalidade::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    fun observeAllMensalidadesForAdmin(): Flow<List<Mensalidade>> = flow {
        requireUserId()
        val path = "mensalidades"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Mensalidade::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun addMensalidade(mensalidade: Mensalidade): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("mensalidades").document()
        return try {
            val payload = mensalidade.copy(
                id = docRef.id,
                userId = if (mensalidade.userId.isNotBlank()) mensalidade.userId else uid,
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun registrarPagamentoPix(mensalidadeId: String, dataPagamento: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("mensalidades").document(mensalidadeId)
        return try {
            docRef.update(
                mapOf(
                    "status" to "Pago",
                    "dataPagamento" to dataPagamento,
                    "formaPagamento" to "PIX Manual Validado",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- PARCEIROS & SERVICOS SOS ESTRADA ---
    fun observeParceirosServicos(): Flow<List<ParceiroServico>> = flow {
        requireUserId()
        val path = "parceiros_servicos"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ParceiroServico::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(
                            compareByDescending<ParceiroServico> { it.ativo }
                                .thenByDescending { it.parceiroOficial }
                                .thenBy { it.distanciaKm }
                        )
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
                }
        )
    }

    suspend fun addParceiroServico(parceiro: ParceiroServico): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("parceiros_servicos")
        val isExisting = parceiro.id.isNotBlank()
        val docRef = if (isExisting) colRef.document(parceiro.id) else colRef.document()
        return try {
            val payload = parceiro.copy(
                id = docRef.id,
                userId = parceiro.userId.ifBlank { uid },
                createdAt = if (isExisting) parceiro.createdAt else null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isExisting) OperationType.UPDATE else OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateParceiroStatusAtivo(parceiroId: String, novoAtivo: Boolean): Result<Unit> {
        requireUserId()
        val docRef = db.collection("parceiros_servicos").document(parceiroId)
        return try {
            docRef.update(
                mapOf(
                    "ativo" to novoAtivo,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteParceiroServico(parceiroId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("parceiros_servicos").document(parceiroId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- CONFIGURACOES DE IDENTIDADE DO CLUBE (LOGO, CORES, CONTATO E PIX) ---
    fun observeConfiguracaoIdentidadeClube(configId: String = "identidade_oficial"): Flow<ConfiguracaoIdentidadeClube> = flow {
        requireUserId()
        val path = "configuracoes_clube/$configId"
        emitAll(
            db.collection("configuracoes_clube").document(configId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            ConfiguracaoIdentidadeClube::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ) ?: ConfiguracaoIdentidadeClube(id = configId)
                    } else {
                        ConfiguracaoIdentidadeClube(id = configId)
                    }
                }
                .catch { error ->
                    if (auth.currentUser == null) {
                        emit(ConfiguracaoIdentidadeClube(id = configId))
                    } else {
                        if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                        throw error
                    }
                }
        )
    }

    suspend fun getConfiguracaoIdentidadeClubeOnce(configId: String = "identidade_oficial"): ConfiguracaoIdentidadeClube? {
        val docRef = db.collection("configuracoes_clube").document(configId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(
                    ConfiguracaoIdentidadeClube::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveConfiguracaoIdentidadeClube(config: ConfiguracaoIdentidadeClube): Result<String> {
        val uid = requireUserId()
        val docId = config.id.ifBlank { "identidade_oficial" }
        val docRef = db.collection("configuracoes_clube").document(docId)
        return try {
            val payload = config.copy(
                id = docId,
                userId = config.userId.ifBlank { uid },
                ativo = true,
                createdAt = config.createdAt,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
