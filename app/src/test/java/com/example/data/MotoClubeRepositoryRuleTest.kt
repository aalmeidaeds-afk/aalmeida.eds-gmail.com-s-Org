package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class MotoClubeRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addAndObserveVeiculo_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = MotoClubeRepository(firestore)
        val uniqueId = "veic_${UUID.randomUUID().toString().replace("-", "")}"

        val veiculo = Veiculo(
            id = uniqueId,
            status = "Ativo",
            tipo = "Moto",
            marca = "Harley-Davidson",
            modelo = "Fat Boy 114",
            ano = 2023L,
            placa = "BRA2E19",
            kmAtual = 8500L,
            ativo = true
        )

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.addVeiculo(veiculo) }
        assertTrue("Expected addVeiculo to succeed: ${addResult.exceptionOrNull()}", addResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeVeiculos().first { list -> list.any { it.id == uniqueId } }
        }
        assertTrue(emitted.any { it.id == uniqueId && it.modelo == "Fat Boy 114" })
    }

    @Test
    fun getVeiculoById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = MotoClubeRepository(firestore)
        val uniqueId = "veic_${UUID.randomUUID().toString().replace("-", "")}"
        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addVeiculo(
                Veiculo(
                    id = uniqueId,
                    status = "Ativo",
                    tipo = "Moto",
                    marca = "Triumph",
                    modelo = "Bonneville T120",
                    ano = 2024L,
                    placa = "MTC9A88",
                    kmAtual = 3200L,
                    ativo = true
                )
            )
        }
        assertTrue("Expected createRes to succeed: ${createRes.exceptionOrNull()}", createRes.isSuccess)

        signInTestUser(BOB_EMAIL)
        val bobUid = checkNotNull(auth.currentUser?.uid)
        val bobRepo = MotoClubeRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.saveAssociado(
                Associado(
                    id = bobUid,
                    userId = bobUid,
                    numeroAssociado = "MC-000002",
                    nomeCompleto = "Bob Associado",
                    nomeGuerra = "BOB",
                    cpf = "555.666.777-88",
                    tipoSanguineo = "A+",
                    status = "Ativo",
                    statusAssociado = "Ativo",
                    perfilAcesso = "Associado",
                    ativo = true,
                    createdAt = bobRepo.getAssociadoOnce(bobUid)?.createdAt
                )
            )
        }
        val bobResult = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getVeiculoById(uniqueId) }
        assertTrue("Expected bobResult to fail, but got: $bobResult", bobResult.isFailure)
        val ex = bobResult.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeVeiculos_unauthenticatedUser_fails() = runBlocking {
        auth.signOut()
        val repository = MotoClubeRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeVeiculos().first()
            }
            fail("Should have thrown IllegalStateException when unauthenticated")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("autenticado") == true)
        }
    }

    @Test
    fun saveAndObserveConfiguracaoIdentidadeClube_authenticatedUser_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = MotoClubeRepository(firestore)
        val configId = "identidade_${UUID.randomUUID().toString().replace("-", "")}"

        val config = ConfiguracaoIdentidadeClube(
            id = configId,
            nomeClube = "IRON SIDE • CUSTOM MC",
            subtituloClube = "COMANDO NACIONAL",
            lemaClube = "Os melhores são de ferro",
            corPrimariaHex = "#D4AF37",
            corSecundariaHex = "#FFD700",
            chavePix = "tesouraria@ironsidemc.com.br",
            valorMensalidadePadrao = 60.0,
            ativo = true
        )

        val saveRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveConfiguracaoIdentidadeClube(config)
        }
        assertTrue("Expected saveConfiguracaoIdentidadeClube to succeed: ${saveRes.exceptionOrNull()}", saveRes.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeConfiguracaoIdentidadeClube(configId).first { it.nomeClube == "IRON SIDE • CUSTOM MC" }
        }
        assertEquals("#D4AF37", emitted.corPrimariaHex)
        assertEquals("tesouraria@ironsidemc.com.br", emitted.chavePix)
    }

    @Test
    fun rlsPolicy_associadoOnlyReadsAndEditsOwnData_diretoriaReadsAndEditsAllRecords() = runBlocking {
        // 1. Alice (Associado comum) cria perfil e veículo próprio
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = MotoClubeRepository(firestore)
        val existingAlice = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.getAssociadoOnce(aliceUid) }
        val aliceAssociado = Associado(
            id = aliceUid,
            userId = aliceUid,
            numeroAssociado = "MC-000101",
            nomeCompleto = "Alice Associada",
            nomeGuerra = "ALICE",
            cpf = "111.222.333-44",
            tipoSanguineo = "O+",
            status = "Ativo",
            statusAssociado = "Ativo",
            perfilAcesso = "Associado",
            ativo = true,
            createdAt = existingAlice?.createdAt
        )
        val saveAliceRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.saveAssociado(aliceAssociado) }
        assertTrue("Alice should save own profile: ${saveAliceRes.exceptionOrNull()}", saveAliceRes.isSuccess)

        val aliceVeiculoId = "veic_${UUID.randomUUID().toString().replace("-", "")}"
        val saveVeicRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addVeiculo(
                Veiculo(
                    id = aliceVeiculoId,
                    userId = aliceUid,
                    associadoId = aliceUid,
                    associadoCpf = "111.222.333-44",
                    status = "Ativo",
                    tipo = "Moto",
                    marca = "Harley-Davidson",
                    modelo = "Street Glide",
                    ano = 2024L,
                    placa = "ALC1A11",
                    kmAtual = 4200L,
                    ativo = true
                )
            )
        }
        assertTrue("Alice should create own vehicle: ${saveVeicRes.exceptionOrNull()}", saveVeicRes.isSuccess)

        // 2. Bob (Associado comum) tenta editar o veículo de Alice -> DEVE FALHAR com PERMISSION_DENIED
        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = MotoClubeRepository(firestore)
        val existingBobBefore = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getAssociadoOnce(bobUid) }
        val bobAssociado = Associado(
            id = bobUid,
            userId = bobUid,
            numeroAssociado = "MC-000102",
            nomeCompleto = "Bob Associado",
            nomeGuerra = "BOB",
            cpf = "555.666.777-88",
            tipoSanguineo = "A+",
            status = "Ativo",
            statusAssociado = "Ativo",
            perfilAcesso = "Associado",
            ativo = true,
            createdAt = existingBobBefore?.createdAt
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.saveAssociado(bobAssociado) }

        val bobCrossEditResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.updateVeiculoKm(aliceVeiculoId, 99999L)
        }
        assertTrue("Associado Bob must NOT edit Alice's vehicle", bobCrossEditResult.isFailure)
        val bobEx = bobCrossEditResult.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, bobEx?.code)

    // 3. Bob é promovido a Diretoria e passa a ter permissão de leitura e edição em todos os registros
        val existingBob = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getAssociadoOnce(bobUid) }
        assertNotNull("Bob profile should exist", existingBob)
        val bobDiretoria = existingBob!!.copy(perfilAcesso = "Diretoria")
        val promoteRes = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.saveAssociado(bobDiretoria) }
        assertTrue("Bob promoted to Diretoria: ${promoteRes.exceptionOrNull()}", promoteRes.isSuccess)

        // Diretoria lê o veículo de Alice
        val diretoriaReadAliceVeiculo = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getVeiculoById(aliceVeiculoId)
        }
        assertTrue("Diretoria must read Alice's vehicle", diretoriaReadAliceVeiculo.isSuccess)
        assertEquals("Street Glide", diretoriaReadAliceVeiculo.getOrNull()?.modelo)

        // Diretoria edita o veículo de Alice e aprova/edita o registro de Alice
        val diretoriaUpdateAliceVeiculo = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.updateVeiculoKm(aliceVeiculoId, 5500L)
        }
        assertTrue("Diretoria must edit Alice's vehicle: ${diretoriaUpdateAliceVeiculo.exceptionOrNull()}", diretoriaUpdateAliceVeiculo.isSuccess)

        val diretoriaUpdateAliceStatus = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.updateAssociadoStatusAndRole(aliceUid, "Aprovado", "Associado")
        }
        assertTrue("Diretoria must edit Alice's status: ${diretoriaUpdateAliceStatus.exceptionOrNull()}", diretoriaUpdateAliceStatus.isSuccess)

        // Reset Bob back to Associado
        withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.updateAssociadoStatusAndRole(bobUid, "Ativo", "Associado")
        }
        Unit
    }

    @Test
    fun clubAuthViewModel_separatesMembroAndDiretoriaFlows() {
        runBlocking {
        auth.signOut()
        val repo = MotoClubeRepository(firestore)
        val authVm = com.example.auth.ClubAuthViewModel(auth, repo)

        // 1. Sem usuário logado -> Estado deve transicionar para Unauthenticated
        val unauthState = withTimeout(DEFAULT_TIMEOUT_MS) {
            authVm.authState.first { it is com.example.auth.ClubAuthState.Unauthenticated }
        }
        assertTrue(unauthState is com.example.auth.ClubAuthState.Unauthenticated)

        // 2. Autentica usuário com perfil Associado -> Deve emitir AuthenticatedMembro
        val aliceUid = signInTestUser(ALICE_EMAIL)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveAssociado(
                Associado(
                    id = aliceUid,
                    userId = aliceUid,
                    numeroAssociado = "MC-000101",
                    nomeCompleto = "Alice Associada",
                    nomeGuerra = "ALICE",
                    cpf = "111.222.333-44",
                    tipoSanguineo = "O+",
                    status = "Ativo",
                    statusAssociado = "Ativo",
                    perfilAcesso = "Associado",
                    ativo = true,
                    createdAt = repo.getAssociadoOnce(aliceUid)?.createdAt
                )
            )
        }

        val membroState = withTimeout(DEFAULT_TIMEOUT_MS) {
            authVm.authState.first { it is com.example.auth.ClubAuthState.AuthenticatedMembro }
        } as com.example.auth.ClubAuthState.AuthenticatedMembro
        assertEquals(aliceUid, membroState.userId)
        assertEquals("111.222.333-44", membroState.cpf)

        // 3. Alterna o fluxo autenticado para DIRETORIA -> Deve emitir AuthenticatedDiretoria
        authVm.switchAuthenticatedFlow(com.example.auth.FluxoAutenticadoClube.DIRETORIA)
        val diretoriaState = withTimeout(DEFAULT_TIMEOUT_MS) {
            authVm.authState.first { it is com.example.auth.ClubAuthState.AuthenticatedDiretoria }
        } as com.example.auth.ClubAuthState.AuthenticatedDiretoria
        assertEquals(aliceUid, diretoriaState.userId)
        assertEquals("Diretoria", diretoriaState.perfilAcesso)

        // Restaura para Membro
        authVm.switchAuthenticatedFlow(com.example.auth.FluxoAutenticadoClube.MEMBRO)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            authVm.authState.first { it is com.example.auth.ClubAuthState.AuthenticatedMembro }
        }
        Unit
        }
    }

    @Test
    fun firestoreDatabaseInitializer_initializesAllNineCollectionsWithValidPermissions() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = MotoClubeRepository(firestore)

        val initResult = withTimeout(10_000L) {
            FirestoreDatabaseInitializer.ensureInitializedAndVerifyPermissions(
                repository = repo,
                currentUserId = uid,
                currentUserEmail = ALICE_EMAIL,
                currentUserDisplayName = "Alice Comandante"
            )
        }

        assertTrue(
            "Expected FirestoreDatabaseInitializer to succeed: ${initResult.exceptionOrNull()}",
            initResult.isSuccess
        )

        val report = checkNotNull(initResult.getOrNull())
        val failedStatuses = report.collectionStatuses.filter { !it.readPermissionVerified || !it.writePermissionVerified }
        assertEquals(9, FirestoreDatabaseInitializer.REQUIRED_COLLECTIONS.size)
        assertEquals(9, report.totalCollectionsConfigured)
        assertTrue(
            "All 9 collections must have verified read/write permissions. Failed: $failedStatuses",
            report.allPermissionsVerified
        )
        assertTrue("Total verified documents should be at least 9, got ${report.totalDocumentsVerified}", report.totalDocumentsVerified >= 9)

        val expectedCollections = setOf(
            "associados",
            "veiculos",
            "manutencoes",
            "abastecimentos",
            "eventos",
            "comunicados",
            "mensalidades",
            "parceiros_servicos",
            "configuracoes_clube"
        )
        val initializedNames = report.collectionStatuses.map { it.collectionName }.toSet()
        assertEquals(expectedCollections, initializedNames)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@motoclube.com"
        const val BOB_EMAIL = "bob@motoclube.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
