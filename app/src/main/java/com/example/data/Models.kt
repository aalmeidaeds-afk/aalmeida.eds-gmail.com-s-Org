package com.example.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@IgnoreExtraProperties
data class Associado(
    @DocumentId val id: String = "",
    val userId: String = "",
    val numeroAssociado: String = "MC-000001",
    val nomeCompleto: String = "",
    val nomeGuerra: String = "",
    val fotoUri: String = "", // Foto do membro no cadastro (URI ou preset)
    val cpf: String = "",
    val rg: String = "",
    val dataNascimento: String = "",
    val tipoSanguineo: String = "O+",
    val fatorRh: String = "Positivo (+)",
    val email: String = "",
    val telefone: String = "",
    val celular: String = "",
    val whatsapp: String = "",
    val dataAssociacao: String = "10/10/2026", // DATA DA FILIAÇÃO DO MEMBRO (obrigatório)
    val dataDesligamento: String = "", // DATA DO DESLIGAMENTO (preenchido pela diretoria quando aplicável)
    val motivoDesligamento: String = "", // Motivo/Observação de desligamento pela diretoria
    val cargo: String = "Integrante Escudado",
    val regional: String = "Sede Nacional - SP",
    val observacoes: String = "",
    // Endereco
    val cep: String = "",
    val logradouro: String = "",
    val numeroEndereco: String = "",
    val complemento: String = "",
    val bairro: String = "",
    val cidade: String = "São Paulo",
    val estado: String = "SP",
    // Habilitacao
    val cnhNumero: String = "",
    val cnhCategoria: String = "AB",
    val cnhEmissao: String = "",
    val cnhValidade: String = "10/10/2030",
    val cnhPrimeiraHab: String = "",
    // Contatos de emergencia
    val emergencia1Nome: String = "",
    val emergencia1Parentesco: String = "",
    val emergencia1Telefone: String = "",
    val emergencia1Whatsapp: String = "",
    val emergencia2Nome: String = "",
    val emergencia2Parentesco: String = "",
    val emergencia2Telefone: String = "",
    val emergencia2Whatsapp: String = "",
    // Status e Perfil
    val status: String = "Ativo", // Espelho padronizado de status do registro
    val statusAssociado: String = "Ativo", // Pendente, Em analise, Aprovado, Ativo, Inativo, Suspenso, Rejeitado, Desligado
    val perfilAcesso: String = "Associado", // Associado, Diretoria, Financeiro, Administrador, SuperAdmin
    val validadeCarteirinha: String = "12/2027",
    val ativo: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Veiculo(
    @DocumentId val id: String = "",
    val userId: String = "",
    val associadoId: String = "", // Relacionamento FK -> associados/{associadoId}
    val associadoCpf: String = "", // Relacionamento por CPF do membro
    val status: String = "Ativo", // Status padronizado (Ativo, Em manutenção, Inativo)
    val tipo: String = "Moto", // Moto, Scooter, Carro, Caminhão, Utilitário, Quadriciclo, Outros
    val fotoUri: String = "", // Foto da Motocicleta / Veículo
    val marca: String = "",
    val modelo: String = "",
    val versao: String = "",
    val ano: Long = 2024L,
    val anoFabricacao: Long = 2024L, // Ano de Fabricação (Documento CRLV)
    val anoModelo: Long = 2024L, // Ano Modelo (Documento CRLV)
    val cor: String = "Preto Fosco",
    val placa: String = "",
    val chassi: String = "",
    val renavam: String = "",
    // Dados completos do DOCUMENTO do veículo (CRLV / CRV)
    val numeroCrvCrlv: String = "", // Nº do Documento CRLV-e / CRV
    val exercicioLicenciamento: String = "2026", // Exercício do Licenciamento
    val combustivelDoc: String = "Gasolina", // Combustivel no Documento
    val especieTipoDoc: String = "Passageiro / Motocicleta", // Espécie / Tipo no CRLV
    val categoriaDoc: String = "Particular", // Categoria (Particular, Oficial, etc.)
    val numeroMotor: String = "", // Número do Motor no Documento
    val potenciaCilindradaDoc: String = "85 CV / 883 cc", // Potência / Cilindrada no CRLV
    val ufEmplacamento: String = "SP", // UF de Emplacamento
    val municipioEmplacamento: String = "São Paulo", // Município de Emplacamento
    val nomeProprietarioDoc: String = "", // Nome do Proprietário no Documento
    val cpfCnpjProprietarioDoc: String = "", // CPF/CNPJ do Proprietário no Documento
    val dataEmissaoCrv: String = "10/01/2026", // Data de Emissão do CRLV
    val kmAtual: Long = 0L,
    val dataCompra: String = "",
    val cilindrada: Long = 883L,
    val cilindros: Long = 2L,
    val transmissao: String = "6 Marchas / Correia",
    // Seguro integrado do veiculo
    val seguradora: String = "",
    val apolice: String = "",
    val seguroVigenciaFim: String = "",
    val seguroTelefoneAssistencia: String = "",
    val seguroKmGuincho: Long = 500L,
    val observacoes: String = "",
    val ativo: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Manutencao(
    @DocumentId val id: String = "",
    val userId: String = "",
    val associadoId: String = "", // Relacionamento FK -> associados/{associadoId}
    val associadoCpf: String = "", // Relacionamento por CPF do membro
    val veiculoId: String = "", // Relacionamento FK -> veiculos/{veiculoId}
    val veiculoNome: String = "",
    val status: String = "Em dia", // Status padronizado (Em dia, Proxima, Vencida, Concluida)
    val data: String = "",
    val km: Long = 0L,
    val tipo: String = "Troca de óleo",
    val descricao: String = "",
    val valor: Double = 0.0,
    val oficina: String = "",
    val ordemServico: String = "",
    val proximaManutencaoData: String = "",
    val proximoKm: Long = 0L,
    val statusAlerta: String = "Em dia", // Em dia, Proxima, Vencida
    val observacoes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Abastecimento(
    @DocumentId val id: String = "",
    val userId: String = "",
    val associadoId: String = "", // Relacionamento FK -> associados/{associadoId}
    val associadoCpf: String = "", // Relacionamento por CPF do membro
    val veiculoId: String = "", // Relacionamento FK -> veiculos/{veiculoId}
    val veiculoNome: String = "",
    val status: String = "Registrado", // Status padronizado (Registrado, Validado)
    val data: String = "",
    val hora: String = "",
    val km: Long = 0L,
    val kmAnterior: Long = 0L,
    val combustivel: String = "Gasolina aditivada",
    val litros: Double = 0.0,
    val precoPorLitro: Double = 0.0,
    val valorTotal: Double = 0.0,
    val tanqueCheio: Boolean = true,
    val posto: String = "",
    val cidade: String = "",
    val consumoCalculado: Double = 0.0,
    val observacao: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Evento(
    @DocumentId val id: String = "",
    val userId: String = "",
    val associadoId: String = "", // Relacionamento FK -> associados/{associadoId} (Organizador)
    val nome: String = "",
    val data: String = "",
    val horario: String = "",
    val local: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val descricao: String = "",
    val valor: Double = 0.0,
    val limiteParticipantes: Long = 50L,
    val confirmadosCount: Long = 1L,
    val status: String = "Inscrições Abertas",
    val ativo: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Comunicado(
    @DocumentId val id: String = "",
    val userId: String = "",
    val titulo: String = "",
    val texto: String = "",
    val tipo: String = "Comunicado", // Notícia, Aviso, Campanha, Comunicado
    val data: String = "",
    val autor: String = "Diretoria Iron Side",
    val prioridade: String = "Alta", // Normal, Alta, Urgente
    val ativo: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Mensalidade(
    @DocumentId val id: String = "",
    val userId: String = "",
    val associadoNome: String = "",
    val numeroAssociado: String = "MC-000001",
    val competencia: String = "",
    val ano: Long = 2026L,
    val valor: Double = 50.0,
    val cpf: String = "",
    val vencimento: String = "",
    val status: String = "Pendente", // Pendente, Pago, Vencido, Cancelado, Isento
    val dataPagamento: String = "",
    val formaPagamento: String = "PIX Manual",
    val chavePix: String = "financeiro@ironsidemotoclube.com.br",
    val observacoes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ParceiroServico(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nome: String = "",
    val categoria: String = "Mecânicos", // Emergências, Mecânicos, Oficinas para motos, Guinchos, Borracharias, Seguros/Assistência, Postos, Hospitais, Farmácias, Lojas de peças
    val telefone: String = "",
    val whatsapp: String = "",
    val endereco: String = "",
    val cep: String = "",
    val cidade: String = "",
    val estado: String = "",
    val rodovia: String = "",
    val horario: String = "08:00 às 18:00",
    val atendimento24h: Boolean = false,
    val atendeMotos: Boolean = true,
    val atendeCarros: Boolean = true,
    val atendeCaminhoes: Boolean = false,
    val parceiroOficial: Boolean = true,
    val fonteExterna: Boolean = false,
    val distanciaKm: Double = 2.5,
    val observacoes: String = "",
    val ativo: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ConfiguracaoIdentidadeClube(
    @DocumentId val id: String = "identidade_oficial",
    val userId: String = "",
    // Identidade & Marca (Logo / Nome / Lema / CNPJ)
    val nomeClube: String = "IRON SIDE • ROAD RIDER MC",
    val subtituloClube: String = "DIVISÃO NACIONAL BRASIL",
    val lemaClube: String = "Os melhores são de ferro",
    val cnpjClube: String = "48.120.990/0001-15",
    val sedeCidadeEstado: String = "São Paulo - SP",
    val logoPreset: String = "brasao_oficial", // brasao_oficial, escudo_cromado, caveira_custom, aguia_estrada
    val logoUri: String = "",
    // Paleta de Cores Dinâmicas (Hexadecimal)
    val corPrimariaHex: String = "#FF6D00",
    val corSecundariaHex: String = "#FFB74D",
    val corDestaqueSosHex: String = "#EF5350",
    val corFundoHex: String = "#0E1013",
    val corSuperficieHex: String = "#16191E",
    val presetPaleta: String = "Laranja Iron Side (Oficial)",
    // Dados de Contato Oficiais
    val emailContato: String = "contato@ironsidemotoclube.com.br",
    val telefoneContato: String = "(11) 3456-7890",
    val whatsappClube: String = "(11) 99876-5432",
    val instagramClube: String = "@ironsideroadridermc",
    val siteClube: String = "www.ironsidemotoclube.com.br",
    val enderecoSede: String = "Av. Paulista, 1578 - Bela Vista, São Paulo - SP",
    // Parâmetros Financeiros & PIX Dinâmicos
    val chavePix: String = "financeiro@ironsidemotoclube.com.br",
    val tipoChavePix: String = "E-mail", // E-mail, CNPJ, Telefone, Chave Aleatória
    val favorecidoPix: String = "Iron Side Moto Clube Associacao",
    val bancoPix: String = "Banco Itaú / Cora SCD",
    val cidadePix: String = "SAO PAULO",
    val valorMensalidadePadrao: Double = 50.0,
    val diaVencimentoPadrao: Long = 15L,
    val ativo: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class TelefoneEmergenciaPublico(
    val nome: String,
    val numero: String,
    val descricao: String,
    val categoria: String
)

val TELEFONES_UTEIS_NACIONAIS = listOf(
    TelefoneEmergenciaPublico("SAMU — Socorro Médico", "192", "Atendimento pré-hospitalar de urgência 24h", "Saúde"),
    TelefoneEmergenciaPublico("Corpo de Bombeiros", "193", "Resgate em acidentes, salvamento e incêndios", "Resgate"),
    TelefoneEmergenciaPublico("Polícia Rodoviária Federal (PRF)", "191", "Emergências e ocorrências em rodovias federais (BR)", "Rodovias"),
    TelefoneEmergenciaPublico("Polícia Militar", "190", "Segurança pública e emergências policiais imediatas", "Polícia"),
    TelefoneEmergenciaPublico("Polícia Rodoviária Estadual", "198", "Atendimento em rodovias estaduais (SP/PR/MG/RJ e outros)", "Rodovias"),
    TelefoneEmergenciaPublico("Defesa Civil", "199", "Deslizamentos, alagamentos e alertas severos na rota", "Defesa"),
    TelefoneEmergenciaPublico("Polícia Civil", "197", "Ocorrências, furtos, roubos e apoio investigativo", "Polícia"),
    TelefoneEmergenciaPublico("Polícia Federal", "194", "Fronteiras, imigração em viagens Mercosul e crimes federais", "Polícia"),
    TelefoneEmergenciaPublico("Disque-Denúncia", "181", " Informações anônimas sobre segurança nas estradas", "Apoio"),
    TelefoneEmergenciaPublico("Central de Atendimento à Mulher", "180", "Canal nacional de orientação e proteção 24h", "Apoio")
)
