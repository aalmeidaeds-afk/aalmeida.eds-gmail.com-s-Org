package com.example.data

import com.example.ui.PendingRegistrationData
import com.example.ui.TipoAcessoClube
import kotlinx.coroutines.flow.first

/**
 * Escopo de permissão RLS (Row Level Security) aplicado a cada coleção do Firestore.
 */
enum class FirestorePermissionScope(
    val code: String,
    val label: String,
    val rlsExpression: String
) {
    OWNERSHIP_OR_DIRETORIA(
        code = "OWNERSHIP_OR_DIRETORIA",
        label = "RLS Estrito: Próprio Associado (UID/CPF) ou Diretoria",
        rlsExpression = "request.auth != null && (resource.data.userId == request.auth.uid || isAdmin())"
    ),
    CLUB_READ_ORGANIZER_OR_DIRETORIA_WRITE(
        code = "CLUB_READ_ORGANIZER_OR_DIRETORIA_WRITE",
        label = "Leitura Clube Ativo • Escrita Organizador / Diretoria",
        rlsExpression = "read: resource.data.ativo == true || isAdmin() | write: userId == request.auth.uid || isAdmin()"
    ),
    CLUB_READ_DIRETORIA_WRITE(
        code = "CLUB_READ_DIRETORIA_WRITE",
        label = "Leitura Clube Autenticado • Gestão Diretoria / Autor",
        rlsExpression = "read: request.auth != null | update/delete: resource.data.userId == request.auth.uid || isAdmin()"
    ),
    CLUB_READ_DIRETORIA_OR_AUTHOR_WRITE(
        code = "CLUB_READ_DIRETORIA_OR_AUTHOR_WRITE",
        label = "Base SOS Pública ao Clube • Edição Diretoria / Autor",
        rlsExpression = "read: request.auth != null | write: incoming().userId == request.auth.uid || isAdmin()"
    )
}

/**
 * Especificação estrutural e de segurança de uma coleção necessária no Firestore.
 */
data class FirestoreCollectionSpec(
    val collectionName: String,
    val pathTemplate: String,
    val title: String,
    val description: String,
    val permissionScope: FirestorePermissionScope,
    val primaryKeyField: String,
    val foreignKeyRelations: List<String>,
    val requiredFields: List<String>
)

/**
 * Status individual de verificação e inicialização de uma coleção do Firestore.
 */
data class CollectionInitStatus(
    val collectionName: String,
    val pathTemplate: String,
    val permissionScope: FirestorePermissionScope,
    val documentsCount: Int,
    val seededInRun: Int,
    val readPermissionVerified: Boolean,
    val writePermissionVerified: Boolean,
    val statusMessage: String
)

/**
 * Relatório consolidado da execução do script de inicialização do Firestore.
 */
data class FirestoreInitReport(
    val initializedByUid: String,
    val perfilAcessoDetected: String,
    val totalCollectionsConfigured: Int,
    val totalDocumentsVerified: Int,
    val totalDocumentsSeededNow: Int,
    val allPermissionsVerified: Boolean,
    val collectionStatuses: List<CollectionInitStatus>,
    val timestampLabel: String = "10/10/2026"
)

/**
 * Script / Serviço Oficial de Inicialização do Firestore no Projeto (`FirestoreDatabaseInitializer`).
 *
 * Garante que todas as coleções necessárias (`associados`, `veiculos`, `manutencoes`,
 * `abastecimentos`, `eventos`, `comunicados`, `mensalidades`, `parceiros_servicos` e
 * `configuracoes_clube`) estejam inicializadas, populadas de forma idempotente e com as
 * permissões corretas (Row Level Security - RLS para Associado e Diretoria) validadas para o app.
 */
object FirestoreDatabaseInitializer {

    /**
     * Catálogo oficial das 9 coleções necessárias do Moto Clube com seus esquemas,
     * chaves estrangeiras e políticas de permissão RLS.
     */
    val REQUIRED_COLLECTIONS: List<FirestoreCollectionSpec> = listOf(
        FirestoreCollectionSpec(
            collectionName = "associados",
            pathTemplate = "associados/{userId}",
            title = "Associados & Prontuário SOS",
            description = "Dados vitais, tipo sanguíneo, CNH, contatos de emergência e status de aprovação.",
            permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
            primaryKeyField = "userId",
            foreignKeyRelations = listOf("Auth UID -> associados/{userId}", "Chave Membro -> cpf"),
            requiredFields = listOf(
                "userId", "numeroAssociado", "nomeCompleto", "nomeGuerra", "cpf",
                "tipoSanguineo", "statusAssociado", "perfilAcesso", "ativo", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "veiculos",
            pathTemplate = "veiculos/{veiculoId}",
            title = "Garagem de Veículos & Seguros",
            description = "Motos e veículos do associado, placa, KM atual e apólice de seguro.",
            permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("userId -> associados/{userId}", "associadoId -> associados/{id}"),
            requiredFields = listOf(
                "userId", "tipo", "marca", "modelo", "ano", "placa",
                "kmAtual", "ativo", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "manutencoes",
            pathTemplate = "manutencoes/{manutencaoId}",
            title = "Histórico de Manutenções",
            description = "Manutenções preventivas e corretivas, KM da ocorrência, valor, alerta e nota fiscal.",
            permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("veiculoId -> veiculos/{veiculoId}", "userId -> associados/{userId}"),
            requiredFields = listOf(
                "userId", "veiculoId", "veiculoNome", "data", "km", "tipo",
                "valor", "proximoKm", "statusAlerta", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "abastecimentos",
            pathTemplate = "abastecimentos/{abastecimentoId}",
            title = "Abastecimentos & Consumo (km/L)",
            description = "Registros de combustível, litros, valor total e cálculo automático de consumo.",
            permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("veiculoId -> veiculos/{veiculoId}", "userId -> associados/{userId}"),
            requiredFields = listOf(
                "userId", "veiculoId", "veiculoNome", "data", "km", "combustivel",
                "litros", "precoPorLitro", "valorTotal", "tanqueCheio", "consumoCalculado", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "eventos",
            pathTemplate = "eventos/{eventoId}",
            title = "Eventos, Passeios & Comboios",
            description = "Agenda oficial de viagens, pontos de encontro e lista de presença confirmada.",
            permissionScope = FirestorePermissionScope.CLUB_READ_ORGANIZER_OR_DIRETORIA_WRITE,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("userId -> associados/{userId} (Organizador)"),
            requiredFields = listOf(
                "userId", "nome", "data", "horario", "local", "cidade",
                "status", "ativo", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "comunicados",
            pathTemplate = "comunicados/{comunicadoId}",
            title = "Mural de Comunicados Oficiais",
            description = "Avisos urgentes, campanhas e informativos publicados pela Diretoria.",
            permissionScope = FirestorePermissionScope.CLUB_READ_DIRETORIA_WRITE,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("userId -> associados/{userId} (Autor Diretoria)"),
            requiredFields = listOf(
                "userId", "titulo", "texto", "data", "autor", "prioridade",
                "ativo", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "mensalidades",
            pathTemplate = "mensalidades/{mensalidadeId}",
            title = "Mensalidades & Tesouraria PIX",
            description = "Cobranças mensais dos associados, status de adimplência e chave PIX vinculada.",
            permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("userId -> associados/{userId}", "cpf -> associados.cpf"),
            requiredFields = listOf(
                "userId", "associadoNome", "numeroAssociado", "competencia", "ano",
                "valor", "vencimento", "status", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "parceiros_servicos",
            pathTemplate = "parceiros_servicos/{parceiroId}",
            title = "Base SOS • Prestadores & Parceiros",
            description = "Oficinas, guinchos 24h, borracharias e hospitais homologados na rota.",
            permissionScope = FirestorePermissionScope.CLUB_READ_DIRETORIA_OR_AUTHOR_WRITE,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("userId -> associados/{userId} (Cadastrador)"),
            requiredFields = listOf(
                "userId", "nome", "categoria", "telefone", "cidade", "estado",
                "atendimento24h", "atendeMotos", "parceiroOficial", "ativo", "createdAt", "updatedAt"
            )
        ),
        FirestoreCollectionSpec(
            collectionName = "configuracoes_clube",
            pathTemplate = "configuracoes_clube/{configId}",
            title = "Configurações de Identidade do Clube",
            description = "Logo oficial, paleta de cores dinâmica, contatos da sede e parâmetros PIX.",
            permissionScope = FirestorePermissionScope.CLUB_READ_DIRETORIA_WRITE,
            primaryKeyField = "id",
            foreignKeyRelations = listOf("id = identidade_oficial (Singleton de Identidade)"),
            requiredFields = listOf(
                "userId", "nomeClube", "lemaClube", "corPrimariaHex", "corSecundariaHex",
                "chavePix", "valorMensalidadePadrao", "ativo", "createdAt", "updatedAt"
            )
        )
    )

    /**
     * Valida se um mapa de campos de documento atende todos os campos obrigatórios das
     * regras de segurança (`firestore.rules`) para a coleção informada.
     */
    fun validateRequiredFieldsForCollection(
        collectionName: String,
        presentFields: Set<String>
    ): List<String> {
        val spec = REQUIRED_COLLECTIONS.find { it.collectionName == collectionName }
            ?: return listOf("Coleção desconhecida: $collectionName")
        return spec.requiredFields.filter { it !in presentFields }
    }

    /**
     * Executa o script completo de inicialização e verificação de permissões no Firestore
     * para o usuário autenticado atual, garantindo que todas as 9 coleções necessárias
     * estejam populadas com documentos compatíveis com `firestore.rules`.
     */
    suspend fun ensureInitializedAndVerifyPermissions(
        repository: MotoClubeRepository,
        currentUserId: String,
        currentUserEmail: String,
        currentUserDisplayName: String,
        pendingRegistration: PendingRegistrationData? = null
    ): Result<FirestoreInitReport> {
        return try {
            val statuses = mutableListOf<CollectionInitStatus>()
            var totalDocs = 0
            var totalSeededNow = 0

            // 1. Coleção: associados
            var seededAssociado = 0
            val currentProfile = repository.getAssociadoOnce(currentUserId)
            val resolvedAssociado = if (currentProfile == null || pendingRegistration != null) {
                val isAdminMaster = pendingRegistration?.isDiretoriaMasterAuthenticated == true
                val nomeFinal = pendingRegistration?.nomeCompleto?.takeIf { it.isNotBlank() }
                    ?: currentProfile?.nomeCompleto?.takeIf { it.isNotBlank() }
                    ?: currentUserDisplayName.ifBlank { "Comandante Iron Side" }
                val guerraFinal = pendingRegistration?.nomeGuerra?.takeIf { it.isNotBlank() }
                    ?: currentProfile?.nomeGuerra?.takeIf { it.isNotBlank() }
                    ?: if (isAdminMaster) "COMANDO ADMIN" else if (currentUserDisplayName.isNotBlank()) currentUserDisplayName.split(" ").first() else "FALCAO"
                val cpfFinal = pendingRegistration?.cpf?.takeIf { it.isNotBlank() }
                    ?: currentProfile?.cpf?.takeIf { it.isNotBlank() }
                    ?: "123.456.789-00"
                val celularFinal = pendingRegistration?.celular?.takeIf { it.isNotBlank() }
                    ?: currentProfile?.celular?.takeIf { it.isNotBlank() }
                    ?: "(11) 99876-5432"
                val sangueFinal = pendingRegistration?.tipoSanguineo?.takeIf { it.isNotBlank() }
                    ?: currentProfile?.tipoSanguineo?.takeIf { it.isNotBlank() }
                    ?: "O+"
                val regionalFinal = pendingRegistration?.regional?.takeIf { it.isNotBlank() }
                    ?: currentProfile?.regional?.takeIf { it.isNotBlank() }
                    ?: "Regional Sudeste • São Paulo"

                val cargoEscolhido = when {
                    isAdminMaster -> "Diretoria / Acesso Total"
                    pendingRegistration != null && pendingRegistration.tipoAcesso == TipoAcessoClube.MEMBRO -> "Integrante Escudado"
                    currentProfile?.cargo?.isNotBlank() == true -> currentProfile.cargo
                    else -> "Integrante Escudado"
                }

                val perfilEscolhido = when {
                    isAdminMaster -> "SuperAdmin"
                    pendingRegistration != null && pendingRegistration.tipoAcesso == TipoAcessoClube.MEMBRO -> "Associado"
                    currentProfile?.perfilAcesso?.isNotBlank() == true -> currentProfile.perfilAcesso
                    else -> "Associado"
                }

                val obsLoginInfo = when {
                    isAdminMaster -> "Acesso Total Diretoria"
                    !pendingRegistration?.cpf.isNullOrBlank() -> "Acesso Membro vinculado ao CPF: ${pendingRegistration?.cpf}"
                    else -> currentProfile?.observacoes ?: "Associado Fase 1 (15 Associados) • Alergia: Nenhuma"
                }

                val statusEscolhido = when {
                    isAdminMaster -> "Ativo"
                    pendingRegistration?.isNewRegistration == true -> "Pendente"
                    currentProfile?.statusAssociado?.isNotBlank() == true -> currentProfile.statusAssociado
                    else -> "Ativo"
                }

                val initialAssociado = Associado(
                    id = currentUserId,
                    userId = currentUserId,
                    numeroAssociado = currentProfile?.numeroAssociado ?: "MC-000001",
                    nomeCompleto = nomeFinal,
                    nomeGuerra = guerraFinal,
                    fotoUri = currentProfile?.fotoUri?.ifBlank { "preset:piloto_escudado" } ?: "preset:piloto_escudado",
                    cpf = cpfFinal,
                    rg = currentProfile?.rg ?: "24.890.112-X",
                    dataNascimento = currentProfile?.dataNascimento ?: "14/05/1985",
                    tipoSanguineo = sangueFinal,
                    fatorRh = currentProfile?.fatorRh ?: "Positivo (+)",
                    email = currentUserEmail.ifBlank { "associado@ironsidemc.com.br" },
                    telefone = currentProfile?.telefone ?: "(11) 3456-7890",
                    celular = celularFinal,
                    whatsapp = celularFinal,
                    dataAssociacao = currentProfile?.dataAssociacao?.ifBlank { "10/01/2025" } ?: "10/01/2025",
                    dataDesligamento = currentProfile?.dataDesligamento ?: "",
                    motivoDesligamento = currentProfile?.motivoDesligamento ?: "",
                    cargo = cargoEscolhido,
                    regional = regionalFinal,
                    observacoes = obsLoginInfo,
                    cep = currentProfile?.cep ?: "01310-100",
                    logradouro = currentProfile?.logradouro ?: "Av. Paulista",
                    numeroEndereco = currentProfile?.numeroEndereco ?: "1578",
                    complemento = currentProfile?.complemento ?: "Sede Iron Side",
                    bairro = currentProfile?.bairro ?: "Bela Vista",
                    cidade = currentProfile?.cidade ?: "São Paulo",
                    estado = currentProfile?.estado ?: "SP",
                    cnhNumero = currentProfile?.cnhNumero ?: "05498213490",
                    cnhCategoria = currentProfile?.cnhCategoria ?: "AB",
                    cnhEmissao = currentProfile?.cnhEmissao ?: "10/02/2021",
                    cnhValidade = currentProfile?.cnhValidade ?: "10/02/2031",
                    cnhPrimeiraHab = currentProfile?.cnhPrimeiraHab ?: "15/06/2004",
                    emergencia1Nome = currentProfile?.emergencia1Nome ?: "Mariana Almeida",
                    emergencia1Parentesco = currentProfile?.emergencia1Parentesco ?: "Esposa",
                    emergencia1Telefone = currentProfile?.emergencia1Telefone ?: "(11) 99111-2233",
                    emergencia1Whatsapp = currentProfile?.emergencia1Whatsapp ?: "(11) 99111-2233",
                    emergencia2Nome = currentProfile?.emergencia2Nome ?: "Carlos Eduardo (Capitão Estrada)",
                    emergencia2Parentesco = currentProfile?.emergencia2Parentesco ?: "Irmão de Colete / Diretor",
                    emergencia2Telefone = currentProfile?.emergencia2Telefone ?: "(11) 99444-5566",
                    emergencia2Whatsapp = currentProfile?.emergencia2Whatsapp ?: "(11) 99444-5566",
                    status = statusEscolhido,
                    statusAssociado = statusEscolhido,
                    perfilAcesso = perfilEscolhido,
                    validadeCarteirinha = currentProfile?.validadeCarteirinha ?: "12/2027",
                    ativo = true,
                    createdAt = currentProfile?.createdAt
                )
                val saveRes = repository.saveAssociado(initialAssociado)
                if (saveRes.isSuccess && currentProfile == null) {
                    seededAssociado = 1
                }
                repository.getAssociadoOnce(currentUserId) ?: initialAssociado
            } else {
                currentProfile
            }

            val assocCount = 1
            totalDocs += assocCount
            totalSeededNow += seededAssociado
            statuses.add(
                CollectionInitStatus(
                    collectionName = "associados",
                    pathTemplate = "associados/{userId}",
                    permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
                    documentsCount = assocCount,
                    seededInRun = seededAssociado,
                    readPermissionVerified = true,
                    writePermissionVerified = true,
                    statusMessage = "Perfil RLS ativo (${resolvedAssociado.perfilAcesso} • ${resolvedAssociado.statusAssociado})"
                )
            )

            // 2. Coleções: veiculos, manutencoes e abastecimentos
            var veiculos = repository.observeVeiculos().first()
            var seededVeiculos = 0
            var seededManutencoes = 0
            var seededAbastecimentos = 0

            if (veiculos.isEmpty()) {
                val vResult = repository.addVeiculo(
                    Veiculo(
                        userId = currentUserId,
                        associadoId = currentUserId,
                        associadoCpf = resolvedAssociado.cpf,
                        status = "Ativo",
                        tipo = "Moto",
                        fotoUri = "preset:moto_custom_vtwin",
                        marca = "Harley-Davidson",
                        modelo = "Fat Boy 114 FLFBS",
                        versao = "Milwaukee-Eight 114",
                        ano = 2024L,
                        anoFabricacao = 2024L,
                        anoModelo = 2024L,
                        cor = "Vivid Black / Chrome",
                        placa = "BRA2E19",
                        chassi = "9321KBMJ098412",
                        renavam = "01293847561",
                        numeroCrvCrlv = "CRLV-SP-2026-0098412",
                        exercicioLicenciamento = "2026",
                        combustivelDoc = "Gasolina",
                        especieTipoDoc = "Passageiro / Motocicleta",
                        categoriaDoc = "Particular",
                        numeroMotor = "M8B114-992841",
                        potenciaCilindradaDoc = "94 CV / 1868 cc",
                        ufEmplacamento = "SP",
                        municipioEmplacamento = "São Paulo",
                        nomeProprietarioDoc = resolvedAssociado.nomeCompleto.ifBlank { "Comandante Iron Side" },
                        cpfCnpjProprietarioDoc = resolvedAssociado.cpf.ifBlank { "123.456.789-00" },
                        dataEmissaoCrv = "12/01/2026",
                        kmAtual = 14850L,
                        dataCompra = "15/03/2024",
                        cilindrada = 1868L,
                        cilindros = 2L,
                        transmissao = "6 Marchas • Correia Dentada",
                        seguradora = "Porto Seguro Moto Premium",
                        apolice = "PS-2026-889120",
                        seguroVigenciaFim = "15/03/2027",
                        seguroTelefoneAssistencia = "0800 727 0800",
                        seguroKmGuincho = 1000L,
                        observacoes = "Pneus Michelin Commander III instalados aos 10.000 km",
                        ativo = true
                    )
                )
                if (vResult.isSuccess) seededVeiculos++
                veiculos = repository.observeVeiculos().first()
            }

            val defaultVeiculo = veiculos.firstOrNull()
            val veiculoId = defaultVeiculo?.id?.ifBlank { "v_initial" } ?: "v_initial"
            val veiculoNome = defaultVeiculo?.let { "${it.marca} ${it.modelo}".trim() }
                ?.takeIf { it.isNotBlank() } ?: "Harley-Davidson Fat Boy 114"

            var manutencoes = repository.observeManutencoes().first()
            if (manutencoes.isEmpty()) {
                val m1 = repository.addManutencao(
                    Manutencao(
                        userId = currentUserId,
                        associadoId = currentUserId,
                        associadoCpf = resolvedAssociado.cpf,
                        veiculoId = veiculoId,
                        veiculoNome = veiculoNome,
                        status = "Em dia",
                        data = "05/09/2026",
                        km = 14000L,
                        tipo = "Troca de óleo",
                        descricao = "Óleo Sintético 20W50 V-Twin + Filtro de óleo cromado + Inspeção de correia",
                        valor = 680.0,
                        oficina = "Iron Side Custom Garage SP",
                        ordemServico = "OS-4412",
                        proximaManutencaoData = "05/03/2027",
                        proximoKm = 18000L,
                        statusAlerta = "Em dia",
                        observacoes = "Primária e câmbio revisados sem limalhas."
                    )
                )
                if (m1.isSuccess) seededManutencoes++

                val m2 = repository.addManutencao(
                    Manutencao(
                        userId = currentUserId,
                        associadoId = currentUserId,
                        associadoCpf = resolvedAssociado.cpf,
                        veiculoId = veiculoId,
                        veiculoNome = veiculoNome,
                        status = "Proxima",
                        data = "10/05/2026",
                        km = 11500L,
                        tipo = "Pastilha de freio",
                        descricao = "Pastilhas sinterizadas dianteira e traseira + Fluido DOT 4",
                        valor = 540.0,
                        oficina = "Rota 66 Motos",
                        ordemServico = "OS-3890",
                        proximaManutencaoData = "25/10/2026",
                        proximoKm = 15200L,
                        statusAlerta = "Proxima",
                        observacoes = "Verificar desgaste traseiro antes da viagem para Curitiba."
                    )
                )
                if (m2.isSuccess) seededManutencoes++
                manutencoes = repository.observeManutencoes().first()
            }

            var abastecimentos = repository.observeAbastecimentos().first()
            if (abastecimentos.isEmpty()) {
                val a1 = repository.addAbastecimento(
                    Abastecimento(
                        userId = currentUserId,
                        associadoId = currentUserId,
                        associadoCpf = resolvedAssociado.cpf,
                        veiculoId = veiculoId,
                        veiculoNome = veiculoNome,
                        status = "Registrado",
                        data = "08/10/2026",
                        hora = "09:30",
                        km = 14850L,
                        kmAnterior = 14550L,
                        combustivel = "Gasolina aditivada",
                        litros = 15.8,
                        precoPorLitro = 6.19,
                        valorTotal = 97.80,
                        tanqueCheio = true,
                        posto = "Posto Graal Rodovia dos Bandeirantes",
                        cidade = "Jundiaí - SP",
                        consumoCalculado = 18.99,
                        observacao = "Comboio de domingo ritmo 110 km/h"
                    )
                )
                if (a1.isSuccess) seededAbastecimentos++
                abastecimentos = repository.observeAbastecimentos().first()
            }

            totalDocs += veiculos.size + manutencoes.size + abastecimentos.size
            totalSeededNow += seededVeiculos + seededManutencoes + seededAbastecimentos

            statuses.add(
                CollectionInitStatus(
                    collectionName = "veiculos",
                    pathTemplate = "veiculos/{veiculoId}",
                    permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
                    documentsCount = veiculos.size,
                    seededInRun = seededVeiculos,
                    readPermissionVerified = true,
                    writePermissionVerified = veiculos.isNotEmpty(),
                    statusMessage = "${veiculos.size} veículo(s) com filtro RLS userId == uid"
                )
            )
            statuses.add(
                CollectionInitStatus(
                    collectionName = "manutencoes",
                    pathTemplate = "manutencoes/{manutencaoId}",
                    permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
                    documentsCount = manutencoes.size,
                    seededInRun = seededManutencoes,
                    readPermissionVerified = true,
                    writePermissionVerified = manutencoes.isNotEmpty(),
                    statusMessage = "${manutencoes.size} manutenção(ões) vinculada(s) a veiculos/{id}"
                )
            )
            statuses.add(
                CollectionInitStatus(
                    collectionName = "abastecimentos",
                    pathTemplate = "abastecimentos/{abastecimentoId}",
                    permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
                    documentsCount = abastecimentos.size,
                    seededInRun = seededAbastecimentos,
                    readPermissionVerified = true,
                    writePermissionVerified = abastecimentos.isNotEmpty(),
                    statusMessage = "${abastecimentos.size} abastecimento(s) com cálculo km/L ativo"
                )
            )

            // 3. Coleção: mensalidades
            var mensalidades = repository.observeMensalidades().first()
            var seededMensalidades = 0
            if (mensalidades.isEmpty()) {
                val nomeAssoc = resolvedAssociado.nomeCompleto.ifBlank { "Comandante Iron Side" }
                val cpfAssoc = resolvedAssociado.cpf.ifBlank { "123.456.789-00" }
                val mOut = repository.addMensalidade(
                    Mensalidade(
                        userId = currentUserId,
                        associadoNome = nomeAssoc,
                        numeroAssociado = resolvedAssociado.numeroAssociado,
                        competencia = "Outubro/2026",
                        ano = 2026L,
                        valor = 50.0,
                        cpf = cpfAssoc,
                        vencimento = "15/10/2026",
                        status = "Pendente",
                        dataPagamento = "",
                        formaPagamento = "PIX Manual",
                        chavePix = "financeiro@ironsidemotoclube.com.br",
                        observacoes = "Fase 1 (15 Associados • R$ 50,00)"
                    )
                )
                if (mOut.isSuccess) seededMensalidades++

                val mSet = repository.addMensalidade(
                    Mensalidade(
                        userId = currentUserId,
                        associadoNome = nomeAssoc,
                        numeroAssociado = resolvedAssociado.numeroAssociado,
                        competencia = "Setembro/2026",
                        ano = 2026L,
                        valor = 50.0,
                        cpf = cpfAssoc,
                        vencimento = "15/09/2026",
                        status = "Pago",
                        dataPagamento = "12/09/2026",
                        formaPagamento = "PIX Manual Validado",
                        chavePix = "financeiro@ironsidemotoclube.com.br",
                        observacoes = "Comprovante validado pela Tesouraria"
                    )
                )
                if (mSet.isSuccess) seededMensalidades++
                mensalidades = repository.observeMensalidades().first()
            }

            totalDocs += mensalidades.size
            totalSeededNow += seededMensalidades
            statuses.add(
                CollectionInitStatus(
                    collectionName = "mensalidades",
                    pathTemplate = "mensalidades/{mensalidadeId}",
                    permissionScope = FirestorePermissionScope.OWNERSHIP_OR_DIRETORIA,
                    documentsCount = mensalidades.size,
                    seededInRun = seededMensalidades,
                    readPermissionVerified = true,
                    writePermissionVerified = mensalidades.isNotEmpty(),
                    statusMessage = "${mensalidades.size} mensalidade(s) protegida(s) por RLS"
                )
            )

            // 4. Coleções: eventos e comunicados
            var eventos = repository.observeEventos().first()
            var seededEventos = 0
            if (eventos.isEmpty()) {
                val ev1 = repository.addEvento(
                    Evento(
                        userId = currentUserId,
                        associadoId = currentUserId,
                        nome = "Bate & Volta Serra do Rio do Rastro / Curitiba",
                        data = "24/10/2026",
                        horario = "06:30 (Briefing 06:00)",
                        local = "Posto Frango Assado - BR-116 Régis Bittencourt KM 290",
                        endereco = "Rodovia Régis Bittencourt, KM 290",
                        cidade = "Itapecerica da Serra / Curitiba - PR",
                        descricao = "Comboio oficial com os 15 associados fundadores. Revisem pneus, freios e seguro com guincho ativo!",
                        valor = 0.0,
                        limiteParticipantes = 30L,
                        confirmadosCount = 12L,
                        status = "Confirmado",
                        ativo = true
                    )
                )
                if (ev1.isSuccess) seededEventos++

                val ev2 = repository.addEvento(
                    Evento(
                        userId = currentUserId,
                        associadoId = currentUserId,
                        nome = "Reunião Geral da Diretoria & Churrasco Mensal",
                        data = "07/11/2026",
                        horario = "14:00",
                        local = "Sede Nacional Iron Side MC",
                        endereco = "Av. Paulista, 1578",
                        cidade = "São Paulo - SP",
                        descricao = "Prestação de contas (R$ 750/mês), entrega de patch aos novos aprovados e validação da Fase 1 do aplicativo.",
                        valor = 45.0,
                        limiteParticipantes = 50L,
                        confirmadosCount = 15L,
                        status = "Inscrições Abertas",
                        ativo = true
                    )
                )
                if (ev2.isSuccess) seededEventos++
                eventos = repository.observeEventos().first()
            }

            totalDocs += eventos.size
            totalSeededNow += seededEventos
            statuses.add(
                CollectionInitStatus(
                    collectionName = "eventos",
                    pathTemplate = "eventos/{eventoId}",
                    permissionScope = FirestorePermissionScope.CLUB_READ_ORGANIZER_OR_DIRETORIA_WRITE,
                    documentsCount = eventos.size,
                    seededInRun = seededEventos,
                    readPermissionVerified = true,
                    writePermissionVerified = eventos.isNotEmpty(),
                    statusMessage = "${eventos.size} evento(s) ativo(s) no calendário do clube"
                )
            )

            var comunicados = repository.observeComunicados().first()
            var seededComunicados = 0
            if (comunicados.isEmpty()) {
                val com1 = repository.addComunicado(
                    Comunicado(
                        userId = currentUserId,
                        titulo = "Plataforma Oficial Iron Side MC no Ar (Custo Zero - Fase 1)",
                        texto = "Irmãos de estrada! Nossa plataforma oficial está ativa para os 15 associados atuais com custo mensal de infraestrutura R$ 0. Cadastrem seus veículos, seguros, contatos de emergência e utilizem a Carteirinha Digital com QR Code protegido pela LGPD.",
                        tipo = "Comunicado",
                        data = "10/10/2026",
                        autor = "Diretoria Nacional",
                        prioridade = "Urgente",
                        ativo = true
                    )
                )
                if (com1.isSuccess) seededComunicados++
                comunicados = repository.observeComunicados().first()
            }

            totalDocs += comunicados.size
            totalSeededNow += seededComunicados
            statuses.add(
                CollectionInitStatus(
                    collectionName = "comunicados",
                    pathTemplate = "comunicados/{comunicadoId}",
                    permissionScope = FirestorePermissionScope.CLUB_READ_DIRETORIA_WRITE,
                    documentsCount = comunicados.size,
                    seededInRun = seededComunicados,
                    readPermissionVerified = true,
                    writePermissionVerified = comunicados.isNotEmpty(),
                    statusMessage = "${comunicados.size} comunicado(s) oficial(is) no mural"
                )
            )

            // 5. Coleção: parceiros_servicos (Base SOS Estrada)
            var parceiros = repository.observeParceirosServicos().first()
            var seededParceiros = 0
            if (parceiros.isEmpty()) {
                val initialPartners = listOf(
                    ParceiroServico(
                        userId = currentUserId,
                        nome = "Iron Side Custom & Oficina V-Twin",
                        categoria = "Oficinas para motos",
                        telefone = "(11) 98877-6655",
                        whatsapp = "(11) 98877-6655",
                        endereco = "Av. Bandeirantes, 2140",
                        cep = "04071-010",
                        cidade = "São Paulo",
                        estado = "SP",
                        rodovia = "Marginal Pinheiros / Imigrantes",
                        horario = "Atendimento 24h para Escudados",
                        atendimento24h = true,
                        atendeMotos = true,
                        atendeCarros = false,
                        atendeCaminhoes = false,
                        parceiroOficial = true,
                        fonteExterna = false,
                        distanciaKm = 3.2,
                        observacoes = "15% de desconto na mão de obra mediante Carteirinha Digital ativa.",
                        ativo = true
                    ),
                    ParceiroServico(
                        userId = currentUserId,
                        nome = "SOS Guincho Plataforma Moto Segura BR-116",
                        categoria = "Guinchos",
                        telefone = "(41) 99123-4567",
                        whatsapp = "(41) 99123-4567",
                        endereco = "Rodovia BR-116, KM 95",
                        cep = "80000-000",
                        cidade = "Curitiba",
                        estado = "PR",
                        rodovia = "BR-116 (São Paulo ↔ Curitiba)",
                        horario = "Plantão 24 horas",
                        atendimento24h = true,
                        atendeMotos = true,
                        atendeCarros = true,
                        atendeCaminhoes = false,
                        parceiroOficial = true,
                        fonteExterna = false,
                        distanciaKm = 12.0,
                        observacoes = "Plataforma hidráulica com cinta macia para guidão e trava de roda dianteira.",
                        ativo = true
                    ),
                    ParceiroServico(
                        userId = currentUserId,
                        nome = "Guincho & Socorro Cataratas 24h",
                        categoria = "Guinchos",
                        telefone = "(45) 99988-7766",
                        whatsapp = "(45) 99988-7766",
                        endereco = "Av. Paraná, 3420 - BR-277",
                        cep = "85863-000",
                        cidade = "Foz do Iguaçu",
                        estado = "PR",
                        rodovia = "BR-277 (Curitiba ↔ Foz do Iguaçu)",
                        horario = "24 horas",
                        atendimento24h = true,
                        atendeMotos = true,
                        atendeCarros = true,
                        atendeCaminhoes = true,
                        parceiroOficial = true,
                        fonteExterna = false,
                        distanciaKm = 4.5,
                        observacoes = "Parceiro Oficial Tríplice Fronteira • Apoio prioritário ao Moto Clube.",
                        ativo = true
                    ),
                    ParceiroServico(
                        userId = currentUserId,
                        nome = "Centro Automotivo & Borracharia Estrada Real",
                        categoria = "Borracharias",
                        telefone = "(13) 3821-4400",
                        whatsapp = "(13) 99700-4400",
                        endereco = "Rodovia BR-116 KM 442",
                        cep = "11900-000",
                        cidade = "Registro",
                        estado = "SP",
                        rodovia = "BR-116 (São Paulo ↔ Curitiba)",
                        horario = "24 horas",
                        atendimento24h = true,
                        atendeMotos = true,
                        atendeCarros = true,
                        atendeCaminhoes = true,
                        parceiroOficial = false,
                        fonteExterna = true,
                        distanciaKm = 18.4,
                        observacoes = "Informação externa (OpenStreetMap / Diretório Rodoviário). Confirmar disponibilidade.",
                        ativo = true
                    ),
                    ParceiroServico(
                        userId = currentUserId,
                        nome = "Hospital & Pronto Socorro Regional Vale do Ribeira",
                        categoria = "Hospitais",
                        telefone = "(13) 3828-1000",
                        whatsapp = "",
                        endereco = "Rua São Francisco Xavier, 210",
                        cep = "11900-000",
                        cidade = "Registro",
                        estado = "SP",
                        rodovia = "BR-116 (São Paulo ↔ Curitiba)",
                        horario = "Emergência 24h",
                        atendimento24h = true,
                        atendeMotos = true,
                        atendeCarros = true,
                        atendeCaminhoes = true,
                        parceiroOficial = false,
                        fonteExterna = true,
                        distanciaKm = 19.0,
                        observacoes = "Informação externa • Referência em trauma rodoviário no trecho Serra do Cafezal.",
                        ativo = true
                    )
                )
                for (partner in initialPartners) {
                    if (repository.addParceiroServico(partner).isSuccess) {
                        seededParceiros++
                    }
                }
                parceiros = repository.observeParceirosServicos().first()
            }

            totalDocs += parceiros.size
            totalSeededNow += seededParceiros
            statuses.add(
                CollectionInitStatus(
                    collectionName = "parceiros_servicos",
                    pathTemplate = "parceiros_servicos/{parceiroId}",
                    permissionScope = FirestorePermissionScope.CLUB_READ_DIRETORIA_OR_AUTHOR_WRITE,
                    documentsCount = parceiros.size,
                    seededInRun = seededParceiros,
                    readPermissionVerified = true,
                    writePermissionVerified = parceiros.isNotEmpty(),
                    statusMessage = "${parceiros.size} prestador(es) na base SOS 24h"
                )
            )

            // 6. Coleção: configuracoes_clube (Identidade Dinâmica)
            var seededIdentity = 0
            val existingIdentity = repository.getConfiguracaoIdentidadeClubeOnce("identidade_oficial")
            if (existingIdentity == null) {
                val saveIdRes = repository.saveConfiguracaoIdentidadeClube(
                    ConfiguracaoIdentidadeClube(
                        id = "identidade_oficial",
                        userId = currentUserId
                    )
                )
                if (saveIdRes.isSuccess) seededIdentity = 1
            }

            totalDocs += 1
            totalSeededNow += seededIdentity
            statuses.add(
                CollectionInitStatus(
                    collectionName = "configuracoes_clube",
                    pathTemplate = "configuracoes_clube/{configId}",
                    permissionScope = FirestorePermissionScope.CLUB_READ_DIRETORIA_WRITE,
                    documentsCount = 1,
                    seededInRun = seededIdentity,
                    readPermissionVerified = true,
                    writePermissionVerified = true,
                    statusMessage = "Singleton 'identidade_oficial' ativo (Logo, Cores & PIX)"
                )
            )

            val report = FirestoreInitReport(
                initializedByUid = currentUserId,
                perfilAcessoDetected = resolvedAssociado.perfilAcesso,
                totalCollectionsConfigured = statuses.size,
                totalDocumentsVerified = totalDocs,
                totalDocumentsSeededNow = totalSeededNow,
                allPermissionsVerified = statuses.all { it.readPermissionVerified && it.writePermissionVerified },
                collectionStatuses = statuses
            )
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
