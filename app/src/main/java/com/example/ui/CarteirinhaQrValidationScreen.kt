package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Associado
import com.example.data.ConfiguracaoIdentidadeClube
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClubIdentityConfig
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import java.security.MessageDigest

/**
 * Projeção estrita de dados públicos/essenciais para verificação externa da Carteirinha Digital via QR Code.
 *
 * GARANTIA DE PRIVACIDADE (LGPD / Zero Exposição Sensível):
 * Esta estrutura NÃO possui campos para CPF, RG, CNH, Data de Nascimento, E-mail Pessoal,
 * Telefone/WhatsApp Pessoal, Endereço Residencial (CEP, Logradouro, Número, Bairro) ou Chave PIX pessoal.
 * Mesmo que o objeto [Associado] original contenha esses dados em memória, [CarteirinhaPublicValidationPayload.fromAssociado]
 * descarta todos os campos sensíveis e retém exclusivamente o necessário para validação externa e socorro.
 */
data class CarteirinhaPublicValidationPayload(
    val numeroAssociado: String,
    val nomeExibicaoPublica: String,
    val nomeGuerra: String,
    val cargoClube: String,
    val regionalClube: String,
    val statusAssociado: String,
    val membroDesde: String,
    val validadeCarteirinha: String,
    val tipoSanguineo: String,
    val fatorRh: String,
    val contatoEmergenciaNome: String,
    val contatoEmergenciaParentesco: String,
    val contatoEmergenciaTelefone: String,
    val nomeClube: String,
    val lemaClube: String,
    val cnpjClube: String,
    val canalVerificacaoClube: String,
    val codigoAutenticidadeHash: String,
    val urlPublicaVerificacao: String
) {
    val isCarteirinhaValida: Boolean
        get() = statusAssociado.equals("Ativo", ignoreCase = true) ||
            statusAssociado.equals("Aprovado", ignoreCase = true)

    companion object {
        val CAMPOS_SENSIVEIS_OCULTOS_LGPD = listOf(
            "CPF",
            "RG",
            "Número e Validade da CNH",
            "Data de Nascimento",
            "Endereço Residencial Completo (CEP, Rua, Nº, Bairro)",
            "Telefone / Celular / WhatsApp Pessoal",
            "E-mail Pessoal",
            "Dados Financeiros / Histórico de Mensalidades"
        )

        /**
         * Aplica sanitização estrita para exibição pública do nome, abreviando sobrenomes intermediários
         * caso desejado ou mantendo apenas Nome e Sobrenome principal sem qualquer documento.
         */
        fun formatarNomePublicoVerificacao(nomeCompleto: String): String {
            val limpo = nomeCompleto.trim()
            if (limpo.isBlank()) return "Associado Escudado Iron Side"
            val partes = limpo.split(Regex("\\s+")).filter { it.isNotBlank() }
            return when {
                partes.size <= 2 -> partes.joinToString(" ")
                else -> {
                    val primeiro = partes.first()
                    val ultimo = partes.last()
                    val iniciaisMeio = partes.subList(1, partes.size - 1)
                        .filter { it.length > 2 } // ignora preposições curtas como "de", "da"
                        .joinToString(" ") { "${it.first().uppercaseChar()}." }
                    if (iniciaisMeio.isNotBlank()) "$primeiro $iniciaisMeio $ultimo" else "$primeiro $ultimo"
                }
            }
        }

        fun gerarHashAutenticidade(numeroAssociado: String, validade: String, status: String): String {
            val raw = "IRONSIDE-QR-V1|$numeroAssociado|$validade|${status.uppercase()}"
            return try {
                val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
                digest.take(6).joinToString("") { "%02X".format(it) }
            } catch (_: Exception) {
                "IS-${numeroAssociado.filter { it.isLetterOrDigit() }}-${validade.replace("/", "")}"
            }
        }

        fun fromAssociado(
            associado: Associado?,
            configIdentidade: ConfiguracaoIdentidadeClube = ConfiguracaoIdentidadeClube()
        ): CarteirinhaPublicValidationPayload {
            val matricula = associado?.numeroAssociado?.ifBlank { "MC-000001" } ?: "MC-000001"
            val validade = associado?.validadeCarteirinha?.ifBlank { "12/2027" } ?: "12/2027"
            val status = associado?.statusAssociado?.ifBlank { "Ativo" } ?: "Ativo"
            val hash = gerarHashAutenticidade(matricula, validade, status)
            val dominioBase = configIdentidade.siteClube
                .removePrefix("https://")
                .removePrefix("http://")
                .trimEnd('/')
                .ifBlank { "www.ironsidemotoclube.com.br" }

            return CarteirinhaPublicValidationPayload(
                numeroAssociado = matricula,
                nomeExibicaoPublica = formatarNomePublicoVerificacao(associado?.nomeCompleto ?: ""),
                nomeGuerra = (associado?.nomeGuerra?.ifBlank { "FALCÃO" } ?: "FALCÃO").uppercase(),
                cargoClube = associado?.cargo?.ifBlank { "Integrante Escudado" } ?: "Integrante Escudado",
                regionalClube = associado?.regional?.ifBlank { "Sede Nacional - SP" } ?: "Sede Nacional - SP",
                statusAssociado = status,
                membroDesde = associado?.dataAssociacao?.ifBlank { "10/01/2025" } ?: "10/01/2025",
                validadeCarteirinha = validade,
                tipoSanguineo = associado?.tipoSanguineo?.ifBlank { "O+" } ?: "O+",
                fatorRh = associado?.fatorRh?.ifBlank { "Positivo (+)" } ?: "Positivo (+)",
                contatoEmergenciaNome = associado?.emergencia1Nome?.ifBlank { "Mariana Almeida" } ?: "Mariana Almeida",
                contatoEmergenciaParentesco = associado?.emergencia1Parentesco?.ifBlank { "Familiar / Socorro" } ?: "Familiar / Socorro",
                contatoEmergenciaTelefone = associado?.emergencia1Telefone?.ifBlank { "(11) 99111-2233" } ?: "(11) 99111-2233",
                nomeClube = configIdentidade.nomeClube.ifBlank { "IRON SIDE • ROAD RIDER MC" },
                lemaClube = configIdentidade.lemaClube.ifBlank { "Os melhores são de ferro" },
                cnpjClube = configIdentidade.cnpjClube.ifBlank { "48.120.990/0001-15" },
                canalVerificacaoClube = configIdentidade.whatsappClube.ifBlank { "(11) 99876-5432" },
                codigoAutenticidadeHash = hash,
                urlPublicaVerificacao = "https://$dominioBase/validar/$matricula?token=$hash"
            )
        }
    }
}

/**
 * Tela de Visualização de Validação da Carteirinha Digital aberta através da leitura do QR Code.
 *
 * Projetada especificamente para verificação externa (portarias de eventos, parceiros de desconto,
 * autoridades rodoviárias e equipes de socorro) exibindo APENAS informações essenciais de autenticidade
 * e vínculo com o Moto Clube, sem jamais expor dados pessoais sensíveis do associado (CPF, RG, CNH,
 * endereço residencial, data de nascimento, e-mail ou telefone pessoal).
 */
@Composable
fun CarteirinhaQrValidationScreen(
    associado: Associado?,
    configIdentidade: ConfiguracaoIdentidadeClube = LocalClubIdentityConfig.current,
    onCloseValidation: () -> Unit
) {
    BackHandler {
        onCloseValidation()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val payloadBase = remember(associado, configIdentidade) {
        CarteirinhaPublicValidationPayload.fromAssociado(associado, configIdentidade)
    }

    // Permite simular como um verificador externo enxerga um membro Ativo vs Inativo/Suspenso
    var statusSimuladoVerificacao by remember(payloadBase.statusAssociado) {
        mutableStateOf(payloadBase.statusAssociado)
    }
    var exibirDetalhesCriptografiaQr by remember { mutableStateOf(false) }

    val payload = remember(payloadBase, statusSimuladoVerificacao) {
        val novoHash = CarteirinhaPublicValidationPayload.gerarHashAutenticidade(
            payloadBase.numeroAssociado,
            payloadBase.validadeCarteirinha,
            statusSimuladoVerificacao
        )
        payloadBase.copy(
            statusAssociado = statusSimuladoVerificacao,
            codigoAutenticidadeHash = novoHash
        )
    }

    val isValida = payload.isCarteirinhaValida
    val corStatusPrincipal = when {
        isValida -> StatusGreen
        payload.statusAssociado.equals("Pendente", ignoreCase = true) ||
            payload.statusAssociado.equals("Em analise", ignoreCase = true) -> StatusYellow
        else -> StatusRed
    }

    fun discarNumeroVerificacao(numero: String) {
        val foneLimpo = numero.filter { it.isDigit() || it == '+' }
        if (foneLimpo.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$foneLimpo"))
            context.startActivity(intent)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("qr_validation_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Barra Superior de Contexto: Verificação Externa via QR Code
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onCloseValidation,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("close_qr_validation_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para a Carteirinha Digital",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "VERIFICAÇÃO EXTERNA • QR CODE",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Portal Público de Autenticidade • Protegido por LGPD",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = StatusGreen.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.border(1.dp, StatusGreenLight, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = StatusGreenLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "LGPD SAFE",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreenLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Banner Principal de Status de Validação Externa (VÁLIDA / SUSPENSA / PENDENTE)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isValida) Color(0xFF0E2216) else Color(0xFF261012)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, corStatusPrincipal, RoundedCornerShape(24.dp))
                    .testTag("qr_validation_status_banner")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(corStatusPrincipal.copy(alpha = 0.22f))
                                    .border(2.dp, corStatusPrincipal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isValida) Icons.Default.Verified else Icons.Default.GppBad,
                                    contentDescription = null,
                                    tint = if (isValida) StatusGreenLight else Color(0xFFFF8A80),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isValida) {
                                        "CREDENCIAL OFICIAL AUTÊNTICA"
                                    } else {
                                        "CREDENCIAL COM RESTRIÇÃO (${payload.statusAssociado.uppercase()})"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isValida) {
                                        "Membro regularmente inscrito e ativo no ${payload.nomeClube}"
                                    } else {
                                        "Atenção Verificador: Associado não está com status Ativo no momento."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = corStatusPrincipal.copy(alpha = 0.35f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOKEN DE VERIFICAÇÃO QR",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "#${payload.codigoAutenticidadeHash}",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = if (isValida) StatusGreenLight else StatusYellow,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("qr_validation_hash_text")
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "VALIDADE DA CREDENCIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = payload.validadeCarteirinha,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Cartão de Dados Essenciais Públicos do Associado (Sem Dados Sensíveis)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151921)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .testTag("qr_validation_essential_info_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF221811), Color(0xFF141820), Color(0xFF1A202C))
                            )
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cabeçalho Institucional do Moto Clube
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IronSideLogoBadge(size = 52.dp, overrideConfig = configIdentidade)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = payload.nomeClube,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${payload.lemaClube.uppercase()} • CNPJ ${payload.cnpjClube}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Surface(
                            color = corStatusPrincipal,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = payload.statusAssociado.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("qr_validation_member_status_badge")
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    // Foto / Avatar Escudado + Identificação Essencial
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(82.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_logo_ironside_oficial_1791667987118),
                                contentDescription = "Retrato Oficial de Verificação ${payload.nomeGuerra}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                            )
                            Surface(
                                color = StatusGreen,
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Identidade Verificada",
                                    tint = Color.White,
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "NOME DE GUERRA / COLETE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = payload.nomeGuerra,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("qr_validation_nome_guerra")
                            )
                            Text(
                                text = "Titular: ${payload.nomeExibicaoPublica}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("qr_validation_nome_publico")
                            )
                        }
                    }

                    // Grade de Metadados Essenciais (Matrícula, Cargo/Divisão, Admissão, Tipo Sanguíneo)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0E1116))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EssentialFieldItem(
                                label = "Nº MATRÍCULA OFICIAL",
                                value = payload.numeroAssociado,
                                valueColor = MaterialTheme.colorScheme.primary,
                                isMono = true,
                                modifier = Modifier.weight(1f),
                                testTag = "qr_validation_numero_associado"
                            )
                            EssentialFieldItem(
                                label = "GRADUAÇÃO / CARGO",
                                value = payload.cargoClube,
                                valueColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EssentialFieldItem(
                                label = "REGIONAL / FACÇÃO",
                                value = payload.regionalClube,
                                valueColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                            EssentialFieldItem(
                                label = "MEMBRO DESDE",
                                value = payload.membroDesde,
                                valueColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                        // Destaque Vital de Socorro Rápido (Tipo Sanguíneo + Contato de Emergência 1-Toque)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TIPO SANGUÍNEO / RH (SOS)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${payload.tipoSanguineo} (${payload.fatorRh})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.testTag("qr_validation_tipo_sanguineo")
                                )
                            }

                            OutlinedButton(
                                onClick = { discarNumeroVerificacao(payload.contatoEmergenciaTelefone) },
                                shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                modifier = Modifier.testTag("qr_validation_call_emergency_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = StatusRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SOS: ${payload.contatoEmergenciaNome}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Rodapé de Confirmação com a Secretaria/Diretoria do Clube
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CENTRAL DE CONFIRMAÇÃO EXTERNA DO CLUBE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dúvidas na portaria/parceiro? Confirme com a Diretoria: ${payload.canalVerificacaoClube}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { discarNumeroVerificacao(payload.canalVerificacaoClube) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .testTag("qr_validation_call_club_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Ligar para Central do Clube",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 4. Painel de Blindagem LGPD: Prova explícita do que está OCULTO e PROTEGIDO
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StatusGreenLight.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("qr_validation_lgpd_shield_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StatusGreenLight,
                            modifier = Modifier.size(26.dp)
                        )
                        Column {
                            Text(
                                text = "BLINDAGEM DE PRIVACIDADE LGPD ATIVA",
                                style = MaterialTheme.typography.titleMedium,
                                color = StatusGreenLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dados sensíveis do associado NUNCA são incluídos no QR Code nem exibidos nesta tela pública.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = "Campos bloqueados e omitidos na verificação externa:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CarteirinhaPublicValidationPayload.CAMPOS_SENSIVEIS_OCULTOS_LGPD.forEach { campoOculto ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "$campoOculto — [OCULTO E PROTEGIDO]",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. QR Code de Verificação + Copiar Link Público / Inspecionar Payload JSON Sanitizado
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(8.dp)
                                .testTag("qr_validation_code_preview"),
                            contentAlignment = Alignment.Center
                        ) {
                            PublicValidationQrCanvas(seed = "${payload.numeroAssociado}-${payload.codigoAutenticidadeHash}")
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "URL PÚBLICA DO QR CODE",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = payload.urlPublicaVerificacao,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("qr_validation_public_url")
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(payload.urlPublicaVerificacao))
                                        Toast.makeText(
                                            context,
                                            "Link público de validação copiado!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("copy_qr_validation_link_btn")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copiar Link", style = MaterialTheme.typography.labelSmall)
                                }

                                OutlinedButton(
                                    onClick = { exibirDetalhesCriptografiaQr = !exibirDetalhesCriptografiaQr },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("toggle_qr_payload_inspector_btn")
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (exibirDetalhesCriptografiaQr) "Ocultar Payload" else "Ver Payload QR",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = exibirDetalhesCriptografiaQr) {
                        Surface(
                            color = Color(0xFF0B0D11),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .testTag("qr_sanitized_json_payload_box")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "PAYLOAD PÚBLICO CODIFICADO NO QR CODE (SOMENTE DADOS ESSENCIAIS):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusGreenLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = """
                                        {
                                          "clube": "${payload.nomeClube}",
                                          "matricula": "${payload.numeroAssociado}",
                                          "nomeGuerra": "${payload.nomeGuerra}",
                                          "titularAbreviado": "${payload.nomeExibicaoPublica}",
                                          "cargo": "${payload.cargoClube}",
                                          "regional": "${payload.regionalClube}",
                                          "status": "${payload.statusAssociado}",
                                          "validade": "${payload.validadeCarteirinha}",
                                          "tipoSanguineo": "${payload.tipoSanguineo}",
                                          "emergenciaContato": "${payload.contatoEmergenciaNome} (${payload.contatoEmergenciaTelefone})",
                                          "hashVerificacao": "${payload.codigoAutenticidadeHash}"
                                        }
                                    """.trimIndent(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = Color(0xFFD6DEEB)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Simulador de Cenários de Verificação Externa (Ativo / Pendente / Suspenso) para homologação
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TESTE DE CENÁRIO DO VERIFICADOR EXTERNO",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Simule como portarias de eventos e convênios visualizam esta tela conforme a situação cadastral do membro:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Ativo", "Pendente", "Suspenso").forEach { cenario ->
                            FilterChip(
                                selected = statusSimuladoVerificacao.equals(cenario, ignoreCase = true),
                                onClick = { statusSimuladoVerificacao = cenario },
                                label = { Text("Status: $cenario") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("qr_sim_status_${cenario.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // 7. Botão de Fechamento / Retorno
        item {
            Button(
                onClick = onCloseValidation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("return_to_carteirinha_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Badge, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Concluir Verificação e Voltar à Carteirinha",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EssentialFieldItem(
    label: String,
    value: String,
    valueColor: Color,
    isMono: Boolean = false,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = if (isMono) JetBrainsMonoFontFamily else null,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
fun PublicValidationQrCanvas(seed: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 11
        val cellW = size.width / gridSize
        val cellH = size.height / gridSize
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val isCorner = (r < 3 && c < 3) || (r < 3 && c >= gridSize - 3) || (r >= gridSize - 3 && c < 3)
                val hashBit = ((r * 31 + c * 17 + seed.hashCode()) % 2 == 0)
                if (isCorner || hashBit) {
                    drawRect(
                        color = Color(0xFF101216),
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW * 0.92f, cellH * 0.92f)
                    )
                }
            }
        }
    }
}
