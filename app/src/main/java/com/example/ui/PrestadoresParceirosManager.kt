package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ParceiroServico
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

val CATEGORIAS_PRESTADORES_SOS = listOf(
    "Guinchos",
    "Mecânicos",
    "Oficinas para motos",
    "Borracharias",
    "Seguros/Assistência",
    "Postos",
    "Hospitais",
    "Farmácias",
    "Lojas de peças",
    "Emergências"
)

/**
 * Tela completa de Gerenciamento de Prestadores e Parceiros do Moto Clube para compor a Base de Dados SOS.
 * Inclui campos para:
 * - Categoria (Guinchos, Mecânicos, Oficinas para motos, Borracharias, Seguros/Assistência, Postos, Hospitais, Farmácias, Lojas de peças)
 * - Endereço completo (Logradouro/Rodovia, CEP, Cidade, Estado/UF)
 * - Atendimento 24 Horas (Switch + horário detalhado)
 * - Status de Atividade (Ativo / Inativo na Base de Dados SOS)
 * - Edição/Correção de Registro e Alternância rápida de Status
 */
@Composable
fun GerenciamentoPrestadoresParceirosSosSection(
    parceiros: List<ParceiroServico>,
    onSalvarParceiro: (ParceiroServico) -> Unit,
    onAlternarStatusAtivo: (parceiroId: String, novoAtivo: Boolean) -> Unit,
    onRemoverParceiro: (parceiroId: String) -> Unit
) {
    val context = LocalContext.current

    // Estado de edição (quando não nulo, carrega os dados do prestador no formulário)
    var prestadorEmEdicao by remember { mutableStateOf<ParceiroServico?>(null) }

    // Campos do Formulário de Gerenciamento da Base SOS
    var nomeInput by remember { mutableStateOf("SOS Moto & Guincho Plataforma 24h") }
    var categoriaSelecionada by remember { mutableStateOf("Guinchos") }
    var telefoneInput by remember { mutableStateOf("(11) 98811-2233") }
    var whatsappInput by remember { mutableStateOf("(11) 98811-2233") }
    var enderecoInput by remember { mutableStateOf("Rodovia Régis Bittencourt BR-116, KM 312 - Pista Sul") }
    var cepInput by remember { mutableStateOf("06850-000") }
    var cidadeInput by remember { mutableStateOf("Itapecerica da Serra") }
    var estadoInput by remember { mutableStateOf("SP") }
    var rodoviaInput by remember { mutableStateOf("BR-116 (São Paulo ↔ Curitiba)") }
    var atendimento24h by remember { mutableStateOf(true) }
    var horarioFuncionamento by remember { mutableStateOf("Plantão 24 horas") }
    var statusAtividadeAtivo by remember { mutableStateOf(true) }
    var parceiroOficialClube by remember { mutableStateOf(true) }
    var atendeMotos by remember { mutableStateOf(true) }
    var atendeCarros by remember { mutableStateOf(true) }
    var atendeCaminhoes by remember { mutableStateOf(false) }
    var observacoesInput by remember { mutableStateOf("Parceiro oficial homologado pela Diretoria com rampa hidráulica e cinta de guidão.") }

    // Filtros da listagem da Base de Dados SOS
    var buscaQuery by remember { mutableStateOf("") }
    var filtroCategoria by remember { mutableStateOf("Todas") }
    var filtroStatusAtividade by remember { mutableStateOf("Todos") } // Todos, Ativos, Inativos, 24 Horas

    val carregarPrestadorParaEdicao: (ParceiroServico) -> Unit = { p ->
        prestadorEmEdicao = p
        nomeInput = p.nome
        categoriaSelecionada = p.categoria.ifBlank { "Guinchos" }
        telefoneInput = p.telefone
        whatsappInput = p.whatsapp.ifBlank { p.telefone }
        enderecoInput = p.endereco
        cepInput = p.cep
        cidadeInput = p.cidade
        estadoInput = p.estado
        rodoviaInput = p.rodovia
        atendimento24h = p.atendimento24h
        horarioFuncionamento = p.horario
        statusAtividadeAtivo = p.ativo
        parceiroOficialClube = p.parceiroOficial
        atendeMotos = p.atendeMotos
        atendeCarros = p.atendeCarros
        atendeCaminhoes = p.atendeCaminhoes
        observacoesInput = p.observacoes
        Toast.makeText(
            context,
            "Editando prestador SOS: ${p.nome}",
            Toast.LENGTH_SHORT
        ).show()
    }

    val totalAtivos = parceiros.count { it.ativo }
    val totalInativos = parceiros.count { !it.ativo }
    val total24h = parceiros.count { it.atendimento24h && it.ativo }

    val listaFiltrada = remember(parceiros, buscaQuery, filtroCategoria, filtroStatusAtividade) {
        parceiros.filter { p ->
            val matchCat = filtroCategoria == "Todas" || p.categoria.equals(filtroCategoria, ignoreCase = true)
            val matchStatus = when (filtroStatusAtividade) {
                "Ativos" -> p.ativo
                "Inativos" -> !p.ativo
                "24 Horas" -> p.atendimento24h
                else -> true
            }
            val q = buscaQuery.trim().lowercase()
            val matchQuery = q.isEmpty() ||
                p.nome.lowercase().contains(q) ||
                p.categoria.lowercase().contains(q) ||
                p.endereco.lowercase().contains(q) ||
                p.cidade.lowercase().contains(q) ||
                p.estado.lowercase().contains(q) ||
                p.rodovia.lowercase().contains(q)
            matchCat && matchStatus && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gerenciamento_prestadores_sos_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Painel de Indicadores da Base de Dados SOS
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IronSideFormHeader(
                        title = "GERENCIAMENTO DE PRESTADORES & PARCEIROS SOS",
                        subtitle = "Cadastro, Categorias, Endereço, Plantão 24h e Status de Atividade na Base SOS"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "ATIVOS NO SOS",
                            value = "$totalAtivos",
                            sub = "Visíveis na Busca"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "PLANTÃO 24 HORAS",
                            value = "$total24h",
                            sub = "Socorro Imediato"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "INATIVOS / PAUSA",
                            value = "$totalInativos",
                            sub = "Ocultos do SOS"
                        )
                    }
                }
            }
        }

        // 2. Formulário Completo de Cadastro / Edição de Prestador e Parceiro SOS
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (prestadorEmEdicao != null) StatusYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("form_prestador_parceiro_sos_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (prestadorEmEdicao != null) {
                                    "✏️ EDITANDO PRESTADOR / PARCEIRO DA BASE SOS"
                                } else {
                                    "➕ CADASTRAR NOVO PRESTADOR / PARCEIRO NA BASE SOS"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (prestadorEmEdicao != null) StatusYellow else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Preencha categoria, endereço completo, regime 24h e status de atividade",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (prestadorEmEdicao != null) {
                            OutlinedButton(
                                onClick = { prestadorEmEdicao = null },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("cancelar_edicao_prestador_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancelar")
                            }
                        }
                    }

                    // Campo 1: Categoria do Prestador / Parceiro SOS
                    Text(
                        text = "1. Categoria na Base de Dados SOS *:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prestador_categoria_selector_row")
                    ) {
                        items(CATEGORIAS_PRESTADORES_SOS) { cat ->
                            FilterChip(
                                selected = categoriaSelecionada == cat,
                                onClick = { categoriaSelecionada = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("chip_cat_sos_${cat.lowercase().replace(" ", "_")}")
                            )
                        }
                    }

                    // Nome e Telefones de Contato
                    OutlinedTextField(
                        value = nomeInput,
                        onValueChange = { nomeInput = it },
                        label = { Text("Nome do Prestador / Oficina / Guincho *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prestador_nome_input"),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = telefoneInput,
                            onValueChange = { telefoneInput = it },
                            label = { Text("Telefone SOS *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prestador_telefone_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsappInput,
                            onValueChange = { whatsappInput = it },
                            label = { Text("WhatsApp Plantão") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prestador_whatsapp_input"),
                            singleLine = true
                        )
                    }

                    // Campo 2: Endereço Completo e Localização Rodoviária
                    Text(
                        text = "2. Endereço Completo & Trecho Rodoviário *:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = enderecoInput,
                        onValueChange = { enderecoInput = it },
                        label = { Text("Endereço Completo (Rua, Número, KM da Rodovia) *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prestador_endereco_input"),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cidadeInput,
                            onValueChange = { cidadeInput = it },
                            label = { Text("Cidade *") },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("prestador_cidade_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = estadoInput,
                            onValueChange = { estadoInput = it },
                            label = { Text("UF *") },
                            modifier = Modifier
                                .weight(0.6f)
                                .testTag("prestador_uf_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cepInput,
                            onValueChange = { cepInput = it },
                            label = { Text("CEP") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prestador_cep_input"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = rodoviaInput,
                        onValueChange = { rodoviaInput = it },
                        label = { Text("Rodovia / Corredor de Apoio (Ex: BR-116, BR-277)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prestador_rodovia_input"),
                        singleLine = true
                    )

                    // Campo 3 e 4: Atendimento 24 Horas & Status de Atividade na Base SOS
                    Text(
                        text = "3. Atendimento 24 Horas & Status de Atividade SOS:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Switch 24 Horas
                        Surface(
                            color = if (atendimento24h) StatusGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = if (atendimento24h) StatusGreenLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🕒 24 HORAS",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (atendimento24h) StatusGreenLight else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (atendimento24h) "Plantão 24h ativo" else "Horário comercial",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = atendimento24h,
                                    onCheckedChange = { is24h ->
                                        atendimento24h = is24h
                                        horarioFuncionamento = if (is24h) "Plantão 24 horas" else "08:00 às 18:00"
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StatusGreenLight),
                                    modifier = Modifier.testTag("switch_prestador_24h")
                                )
                            }
                        }

                        // Switch Status de Atividade (Ativo vs Inativo na Base SOS)
                        Surface(
                            color = if (statusAtividadeAtivo) StatusGreen.copy(alpha = 0.16f) else StatusRed.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = if (statusAtividadeAtivo) StatusGreenLight else StatusRed,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (statusAtividadeAtivo) "🟢 STATUS: ATIVO" else "🔴 STATUS: INATIVO",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (statusAtividadeAtivo) StatusGreenLight else StatusRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (statusAtividadeAtivo) "Disponível no SOS" else "Pausado no SOS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = statusAtividadeAtivo,
                                    onCheckedChange = { statusAtividadeAtivo = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StatusGreenLight),
                                    modifier = Modifier.testTag("switch_prestador_status_ativo")
                                )
                            }
                        }
                    }

                    // Classificação Parceiro Oficial vs Base Externa e Tipos de Veículos Atendidos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = parceiroOficialClube,
                                onCheckedChange = { parceiroOficialClube = it },
                                modifier = Modifier.testTag("checkbox_parceiro_oficial")
                            )
                            Text("⭐ Parceiro Oficial do Moto Clube", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = atendeMotos,
                                onCheckedChange = { atendeMotos = it }
                            )
                            Text("🏍️ Motos", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = observacoesInput,
                        onValueChange = { observacoesInput = it },
                        label = { Text("Observações / Benefícios / Convênio para Associados") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prestador_observacoes_input")
                    )

                    val editandoAtual = prestadorEmEdicao
                    Button(
                        onClick = {
                            if (nomeInput.isNotBlank() && enderecoInput.isNotBlank() && cidadeInput.isNotBlank()) {
                                onSalvarParceiro(
                                    ParceiroServico(
                                        id = editandoAtual?.id ?: "",
                                        userId = editandoAtual?.userId ?: "",
                                        nome = nomeInput.trim(),
                                        categoria = categoriaSelecionada,
                                        telefone = telefoneInput.ifBlank { "(11) 99999-0000" },
                                        whatsapp = whatsappInput.ifBlank { telefoneInput },
                                        endereco = enderecoInput.trim(),
                                        cep = cepInput.trim(),
                                        cidade = cidadeInput.trim(),
                                        estado = estadoInput.trim().uppercase().ifBlank { "SP" },
                                        rodovia = rodoviaInput.trim().ifBlank { "Rodovia Nacional" },
                                        horario = if (atendimento24h) "Plantão 24 horas" else horarioFuncionamento.ifBlank { "08:00 às 18:00" },
                                        atendimento24h = atendimento24h,
                                        atendeMotos = atendeMotos,
                                        atendeCarros = atendeCarros,
                                        atendeCaminhoes = atendeCaminhoes,
                                        parceiroOficial = parceiroOficialClube,
                                        fonteExterna = !parceiroOficialClube,
                                        distanciaKm = editandoAtual?.distanciaKm ?: 3.5,
                                        observacoes = observacoesInput.trim(),
                                        ativo = statusAtividadeAtivo,
                                        createdAt = editandoAtual?.createdAt
                                    )
                                )
                                prestadorEmEdicao = null
                            } else {
                                Toast.makeText(
                                    context,
                                    "Preencha nome, endereço e cidade do prestador.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (editandoAtual != null) StatusYellow else MaterialTheme.colorScheme.primary,
                            contentColor = if (editandoAtual != null) Color.Black else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("salvar_prestador_sos_btn")
                    ) {
                        Icon(
                            imageVector = if (editandoAtual != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (editandoAtual != null) {
                                "Salvar Alterações do Prestador na Base SOS"
                            } else {
                                "Adicionar Prestador / Parceiro à Base SOS"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Filtros de Pesquisa e Auditoria da Base de Dados SOS
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "BASE DE DADOS SOS CADASTRADA (${listaFiltrada.size} de ${parceiros.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = buscaQuery,
                        onValueChange = { buscaQuery = it },
                        label = { Text("Buscar por nome, endereço, cidade, UF ou rodovia") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("busca_gerenciamento_sos_input"),
                        singleLine = true
                    )

                    // Filtro de Status de Atividade e 24h
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Todos", "Ativos", "Inativos", "24 Horas")) { st ->
                            FilterChip(
                                selected = filtroStatusAtividade == st,
                                onClick = { filtroStatusAtividade = st },
                                label = { Text(st) }
                            )
                        }
                    }

                    // Filtro de Categoria
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Todas") + CATEGORIAS_PRESTADORES_SOS) { cat ->
                            FilterChip(
                                selected = filtroCategoria == cat,
                                onClick = { filtroCategoria = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Lista de Prestadores e Parceiros com Controle de Status de Atividade e Edição
        items(listaFiltrada, key = { it.id.ifBlank { it.nome } }) { item ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (item.ativo) StatusGreen.copy(alpha = 0.55f) else StatusRed.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .testTag("prestador_sos_manage_card_${item.id}")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (item.ativo) StatusGreen else StatusRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (item.ativo) "🟢 ATIVO NO SOS" else "🔴 INATIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            if (item.atendimento24h) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "🕒 24 HORAS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (item.parceiroOficial) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "⭐ PARCEIRO CLUBE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Switch rápido para ativar/desativar na Base SOS
                        Switch(
                            checked = item.ativo,
                            onCheckedChange = { novoSt ->
                                if (item.id.isNotBlank()) {
                                    onAlternarStatusAtivo(item.id, novoSt)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StatusGreenLight),
                            modifier = Modifier.testTag("toggle_ativo_prestador_${item.id}")
                        )
                    }

                    Text(
                        text = item.nome,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Categoria: ${item.categoria} • Horário: ${if (item.atendimento24h) "Atendimento 24 Horas" else item.horario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "📍 Endereço: ${item.endereco} — ${item.cidade}/${item.estado} ${if (item.cep.isNotBlank()) "• CEP ${item.cep}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (item.rodovia.isNotBlank()) {
                        Text(
                            text = "🛣️ Corredor / Rodovia: ${item.rodovia}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreenLight
                        )
                    }

                    if (item.observacoes.isNotBlank()) {
                        Text(
                            text = item.observacoes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { carregarPrestadorParaEdicao(item) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("editar_prestador_sos_${item.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = StatusYellow)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar / Corrigir", color = StatusYellow)
                            }

                            Button(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.telefone}")))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ligar", color = Color.White)
                            }
                        }

                        if (item.id.isNotBlank()) {
                            IconButton(
                                onClick = { onRemoverParceiro(item.id) },
                                modifier = Modifier.testTag("remover_prestador_sos_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover da Base SOS",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
