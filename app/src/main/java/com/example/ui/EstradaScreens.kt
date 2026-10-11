package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Associado
import com.example.data.ParceiroServico
import com.example.data.TELEFONES_UTEIS_NACIONAIS
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

@Composable
fun EstradaPillarScreen(
    associado: Associado?,
    parceiros: List<ParceiroServico>,
    initialTab: Int = 0,
    onCadastrarParceiro: (ParceiroServico) -> Unit,
    onAlternarStatusAtivoParceiro: (String, Boolean) -> Unit = { _, _ -> },
    onRemoverParceiro: (String) -> Unit = {}
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        "🚨 Emergência",
        "🔍 Busca Nacional & SOS",
        "🛠️ Gerenciar Parceiros SOS",
        "🛣️ Estou na Estrada",
        "📞 Telefones Úteis"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs.indices.toList()) { idx ->
                FilterChip(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    label = { Text(tabs[idx]) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (idx == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        selectedLabelColor = if (idx == 0) Color.White else MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("estrada_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> ModoEmergenciaSection(associado = associado)
            1 -> BuscaNacionalSosSection(
                parceiros = parceiros.filter { it.ativo },
                onCadastrarParceiro = onCadastrarParceiro,
                onAbrirGerenciadorSos = { selectedTab = 2 }
            )
            2 -> GerenciamentoPrestadoresParceirosSosSection(
                parceiros = parceiros,
                onSalvarParceiro = onCadastrarParceiro,
                onAlternarStatusAtivo = onAlternarStatusAtivoParceiro,
                onRemoverParceiro = onRemoverParceiro
            )
            3 -> EstouNaEstradaViagemSection(parceiros = parceiros.filter { it.ativo })
            4 -> TelefonesUteisNacionaisSection()
        }
    }
}

@Composable
fun ModoEmergenciaSection(associado: Associado?) {
    val context = LocalContext.current

    val nomeCompletoVital = associado?.nomeCompleto?.ifBlank { "Associado Iron Side" } ?: "Associado Iron Side"
    val nomeGuerraVital = associado?.nomeGuerra?.ifBlank { "FALCÃO" } ?: "FALCÃO"
    val tipoSanguineoVital = associado?.tipoSanguineo?.ifBlank { "O+" } ?: "O+"
    val fatorRhVital = associado?.fatorRh?.ifBlank { "Positivo (+)" } ?: "Positivo (+)"

    val contato1Nome = associado?.emergencia1Nome?.ifBlank { "Mariana Almeida" } ?: "Mariana Almeida"
    val contato1Parentesco = associado?.emergencia1Parentesco?.ifBlank { "Esposa" } ?: "Esposa"
    val contato1Telefone = associado?.emergencia1Telefone?.ifBlank { "(11) 99111-2233" } ?: "(11) 99111-2233"

    val contato2Nome = associado?.emergencia2Nome?.ifBlank { "Carlos Eduardo" } ?: "Carlos Eduardo"
    val contato2Parentesco = associado?.emergencia2Parentesco?.ifBlank { "Irmão de Colete / Diretor" } ?: "Irmão de Colete / Diretor"
    val contato2Telefone = associado?.emergencia2Telefone?.ifBlank { "(11) 99444-5566" } ?: "(11) 99444-5566"

    fun iniciarChamadaTelefonica(numero: String) {
        val foneLimpo = numero.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$foneLimpo"))
        context.startActivity(intent)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("modo_emergencia_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F0909)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, StatusRed, RoundedCornerShape(24.dp))
                    .testTag("vital_emergency_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cabeçalho de Emergência Vital
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IronSideLogoBadge(size = 38.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🚨 TELA DE EMERGÊNCIA",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Apenas Informações Vitais do Associado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = StatusRed,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SOS VITAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = StatusRed.copy(alpha = 0.35f))

                    // Bloco Principal: FOTO DO ASSOCIADO + NOME + TIPO SANGUÍNEO EM DESTAQUE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF2B0E0E))
                            .border(1.dp, StatusRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Foto / Retrato Oficial do Associado com selo de identificação
                            Box(
                                modifier = Modifier
                                    .size(78.dp)
                                    .testTag("emergencia_foto_associado"),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(
                                        id = com.example.R.drawable.img_logo_ironside_oficial_1791667987118
                                    ),
                                    contentDescription = "Foto de Identificação do Associado $nomeCompletoVital",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Surface(
                                    color = StatusRed,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(1.5.dp, Color.White, CircleShape)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = nomeGuerraVital.take(2).uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Nome Vital do Associado
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "ASSOCIADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = nomeCompletoVital,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("emergencia_nome_associado")
                                )
                                Text(
                                    text = "Nome de Guerra: ${nomeGuerraVital.uppercase()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Tipo Sanguíneo & Fator RH em Alto Contraste
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .border(2.dp, StatusRed, RoundedCornerShape(16.dp))
                                .testTag("emergencia_tipo_sanguineo_badge")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TIPO SANGUÍNEO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1A1A1A),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tipoSanguineoVital,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = StatusRed,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = fatorRhVital,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    }

                    // BOTÃO DE AÇÃO RÁPIDA PRINCIPAL PARA CHAMADA TELEFÔNICA IMEDIATA (1-TOQUE)
                    Button(
                        onClick = { iniciarChamadaTelefonica(contato1Telefone) },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("emergencia_chamada_rapida_principal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Chamada Telefônica de Emergência",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "CHAMADA RÁPIDA DE EMERGÊNCIA (1-TOQUE)",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Ligar agora para $contato1Nome ($contato1Parentesco) • $contato1Telefone",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }

                    // Seção: Contatos de Emergência do Associado
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CONTATOS DE EMERGÊNCIA DO ASSOCIADO",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )

                        // Contato de Emergência 1
                        EmergencyContactCallCard(
                            prioridade = "CONTATO DE EMERGÊNCIA 1 (PRIORITÁRIO)",
                            nome = contato1Nome,
                            parentesco = contato1Parentesco,
                            telefone = contato1Telefone,
                            testTag = "emergencia_ligar_contato_1_btn",
                            onCall = { fone -> iniciarChamadaTelefonica(fone) }
                        )

                        // Contato de Emergência 2
                        EmergencyContactCallCard(
                            prioridade = "CONTATO DE EMERGÊNCIA 2 (SECUNDÁRIO)",
                            nome = contato2Nome,
                            parentesco = contato2Parentesco,
                            telefone = contato2Telefone,
                            testTag = "emergencia_ligar_contato_2_btn",
                            onCall = { fone -> iniciarChamadaTelefonica(fone) }
                        )
                    }

                    // Aviso de Proteção LGPD
                    Surface(
                        color = Color(0xFF14171C),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔒 Modo Vital LGPD: Exibe apenas Nome, Foto, Tipo Sanguíneo e Contatos de Emergência. Dados sensíveis (CPF, RG, CNH e Endereço) permanecem ocultos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusGreenLight,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Botões de Chamada Telefônica Rápida para Socorro Público (SAMU 192 / Bombeiros 193 / PRF 191)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { iniciarChamadaTelefonica("192") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("emergencia_ligar_samu_192_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🚑 SAMU 192",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = { iniciarChamadaTelefonica("193") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("emergencia_ligar_bombeiros_193_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🚒 BOMBEIROS 193",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = { iniciarChamadaTelefonica("191") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("emergencia_ligar_prf_191_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🚓 PRF 191",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactCallCard(
    prioridade: String,
    nome: String,
    parentesco: String,
    telefone: String,
    testTag: String = "emergencia_call_contact_btn",
    onCall: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(prioridade, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                Text("$nome ($parentesco)", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(telefone, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
            }
            Button(
                onClick = { onCall(telefone) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(50.dp)
                    .testTag(testTag)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Ligar para $nome", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("LIGAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BuscaNacionalSosSection(
    parceiros: List<ParceiroServico>,
    onCadastrarParceiro: (ParceiroServico) -> Unit,
    onAbrirGerenciadorSos: () -> Unit = {}
) {
    val context = LocalContext.current
    var cidadeFiltro by remember { mutableStateOf("Todas as Cidades") }
    var categoriaFiltro by remember { mutableStateOf("Todas") }
    var mostrarModalNovoParceiro by remember { mutableStateOf(false) }

    val cidadesDisponiveis = listOf("Todas as Cidades", "Foz do Iguaçu/PR", "São Paulo/SP", "Curitiba/PR", "Registro/SP")
    val categoriasSos = listOf(
        "Todas", "Guinchos", "Mecânicos", "Oficinas para motos",
        "Borracharias", "Seguros/Assistência", "Postos", "Hospitais", "Farmácias", "Lojas de peças"
    )

    val filtrados = parceiros.filter { p ->
        val matchCidade = when (cidadeFiltro) {
            "Todas as Cidades" -> true
            "Foz do Iguaçu/PR" -> p.cidade.contains("Foz", ignoreCase = true) || p.estado.equals("PR", ignoreCase = true)
            "São Paulo/SP" -> p.cidade.contains("Paulo", ignoreCase = true)
            "Curitiba/PR" -> p.cidade.contains("Curitiba", ignoreCase = true)
            "Registro/SP" -> p.cidade.contains("Registro", ignoreCase = true)
            else -> true
        }
        val matchCat = categoriaFiltro == "Todas" || p.categoria.equals(categoriaFiltro, ignoreCase = true)
        matchCidade && matchCat
    }.sortedWith(compareByDescending<ParceiroServico> { it.parceiroOficial }.thenBy { it.distanciaKm })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IronSideLogoBadge(size = 40.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BUSCA NACIONAL • SOS & SERVIÇOS", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("⭐ Parceiros primeiro • Base Externa depois", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onAbrirGerenciadorSos,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("novo_parceiro_btn")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Base SOS", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }

                    Text("Localização / Cidade da Viagem:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cidadesDisponiveis) { cid ->
                            FilterChip(
                                selected = cidadeFiltro == cid,
                                onClick = { cidadeFiltro = cid },
                                label = { Text(cid) }
                            )
                        }
                    }

                    Text("Categoria de Serviço:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categoriasSos) { cat ->
                            FilterChip(
                                selected = categoriaFiltro == cat,
                                onClick = { categoriaFiltro = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }
        }

        items(filtrados) { p ->
            ParceiroServicoCard(
                parceiro = p,
                onLigar = { fone ->
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$fone")))
                },
                onAbrirRota = { end ->
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode("${p.nome} $end ${p.cidade} ${p.estado}")}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            )
        }
    }

    if (mostrarModalNovoParceiro) {
        var nome by remember { mutableStateOf("") }
        var categoria by remember { mutableStateOf("Guinchos") }
        var telefone by remember { mutableStateOf("(45) 99911-2233") }
        var cidade by remember { mutableStateOf("Foz do Iguaçu") }
        var estado by remember { mutableStateOf("PR") }
        var endereco by remember { mutableStateOf("BR-277 KM 720") }
        var atendimento24h by remember { mutableStateOf(true) }
        var parceiroOficial by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { mostrarModalNovoParceiro = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (nome.isNotBlank()) {
                            onCadastrarParceiro(
                                ParceiroServico(
                                    nome = nome,
                                    categoria = categoria,
                                    telefone = telefone,
                                    whatsapp = telefone,
                                    endereco = endereco,
                                    cidade = cidade,
                                    estado = estado,
                                    rodovia = "BR-116 / BR-277",
                                    horario = if (atendimento24h) "Atendimento 24 horas" else "08:00 às 18:00",
                                    atendimento24h = atendimento24h,
                                    atendeMotos = true,
                                    parceiroOficial = parceiroOficial,
                                    fonteExterna = !parceiroOficial,
                                    distanciaKm = 3.8,
                                    observacoes = if (parceiroOficial) "Parceiro Oficial cadastrado pela Diretoria" else "Informação externa — confirme disponibilidade",
                                    ativo = true
                                )
                            )
                            mostrarModalNovoParceiro = false
                        }
                    }
                ) {
                    Text("Cadastrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalNovoParceiro = false }) { Text("Cancelar") }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IronSideLogoBadge(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Novo Parceiro / Serviço SOS")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Prestador") })
                    OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoria (Guinchos, Mecânicos, Borracharias...)") })
                    OutlinedTextField(value = telefone, onValueChange = { telefone = it }, label = { Text("Telefone / WhatsApp") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cidade, onValueChange = { cidade = it }, label = { Text("Cidade") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = estado, onValueChange = { estado = it }, label = { Text("UF") }, modifier = Modifier.width(72.dp))
                    }
                    OutlinedTextField(value = endereco, onValueChange = { endereco = it }, label = { Text("Endereço / Rodovia") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = parceiroOficial, onCheckedChange = { parceiroOficial = it })
                        Text("⭐ Parceiro Oficial do Moto Clube")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = atendimento24h, onCheckedChange = { atendimento24h = it })
                        Text("Atendimento 24 Horas")
                    }
                }
            }
        )
    }
}

@Composable
fun ParceiroServicoCard(
    parceiro: ParceiroServico,
    onLigar: (String) -> Unit,
    onAbrirRota: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (parceiro.parceiroOficial) 1.5.dp else 1.dp,
                color = if (parceiro.parceiroOficial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (parceiro.parceiroOficial) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⭐ PARCEIRO DO MOTO CLUBE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🌐 INFORMAÇÃO EXTERNA (OpenStreetMap / Diretório)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "${String.format("%.1f", parceiro.distanciaKm)} km",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(parceiro.nome, style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(
                text = "Categoria: ${parceiro.categoria} • ${if (parceiro.atendimento24h) "🕒 24 HORAS" else parceiro.horario}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (parceiro.atendimento24h) StatusGreenLight else MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "📍 ${parceiro.endereco} — ${parceiro.cidade}/${parceiro.estado}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (parceiro.fonteExterna) {
                Surface(
                    color = Color(0xFF332616),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Orientação: Resultado de base externa. Confirme telefone, endereço e disponibilidade antes de contratar.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (parceiro.observacoes.isNotBlank()) {
                Text(parceiro.observacoes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onLigar(parceiro.telefone) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ligar (${parceiro.telefone})", color = Color.White)
                }
                OutlinedButton(
                    onClick = { onAbrirRota(parceiro.endereco) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rota")
                }
            }
        }
    }
}

@Composable
private fun EstouNaEstradaViagemSection(parceiros: List<ParceiroServico>) {
    val context = LocalContext.current
    var origem by remember { mutableStateOf("São Paulo - SP") }
    var destino by remember { mutableStateOf("Curitiba - PR") }
    var categoriaRota by remember { mutableStateOf("Todos no Trajeto") }

    val categoriasTrajeto = listOf("Todos no Trajeto", "Mecânicos", "Guinchos", "Borracharias", "Postos", "Hospitais")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IronSideFormHeader(
                        title = "FUNÇÃO 'ESTOU NA ESTRADA'",
                        subtitle = "Mapeia serviços de apoio ao longo do corredor rodoviário"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = origem,
                            onValueChange = { origem = it },
                            label = { Text("Origem") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = destino,
                            onValueChange = { destino = it },
                            label = { Text("Destino da Viagem") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏍️ TRAJETO ATIVO MONITORADO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$origem  ➔  Registro/SP (Serra)  ➔  $destino", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Corredor: BR-116 Régis Bittencourt • 408 km • 5 Pontos de Apoio", style = MaterialTheme.typography.labelSmall, color = StatusGreenLight)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categoriasTrajeto) { cat ->
                            FilterChip(
                                selected = categoriaRota == cat,
                                onClick = { categoriaRota = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }
        }

        val servicosNoCaminho = parceiros.filter { p ->
            categoriaRota == "Todos no Trajeto" || p.categoria.contains(categoriaRota.take(5), ignoreCase = true)
        }

        items(servicosNoCaminho) { servico ->
            ParceiroServicoCard(
                parceiro = servico,
                onLigar = { fone ->
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$fone")))
                },
                onAbrirRota = { end ->
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode("${servico.nome} $end ${servico.cidade}")}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            )
        }
    }
}

@Composable
private fun TelefonesUteisNacionaisSection() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "BASE NACIONAL DE TELEFONES DE EMERGÊNCIA",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Acionamento rápido com 1 toque em qualquer estado do Brasil",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(TELEFONES_UTEIS_NACIONAIS) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${item.nome} — ${item.numero}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(item.descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.numero}")))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item.numero, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
