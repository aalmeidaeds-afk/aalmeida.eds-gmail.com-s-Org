package com.example.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import android.widget.Toast
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.PictureAsPdf
import com.example.data.Abastecimento
import com.example.data.Associado
import com.example.data.AssociadosPdfExportService
import com.example.data.Evento
import com.example.data.Manutencao
import com.example.data.Mensalidade
import com.example.data.OpcoesExportacaoConsolidadaPdf
import com.example.data.Veiculo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClubIdentityConfig
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

val FLUXO_APROVACAO_ESTAGIOS = listOf(
    "Pendente",
    "Em analise",
    "Aprovado",
    "Ativo",
    "Suspenso",
    "Inativo",
    "Rejeitado",
    "Desligado"
)

val PERFIS_ACESSO_RBAC = listOf(
    "Associado",
    "Diretoria",
    "Financeiro",
    "Administrador",
    "SuperAdmin"
)

fun statusBadgeColor(status: String): Color = when (status) {
    "Ativo", "Aprovado" -> StatusGreen
    "Pendente", "Em analise" -> StatusYellow
    "Suspenso", "Rejeitado", "Inativo", "Desligado" -> StatusRed
    else -> StatusGreen
}

@Composable
fun PainelAdministrativoVisoesCard(
    associado: Associado?,
    todosAssociadosAdmin: List<Associado>,
    veiculos: List<Veiculo>,
    manutencoes: List<Manutencao>,
    abastecimentos: List<Abastecimento>,
    mensalidades: List<Mensalidade>,
    onAlterarStatusAssociado: (targetUserId: String, novoStatus: String, novoPerfil: String) -> Unit,
    onAlternarModoAcesso: () -> Unit,
    onAbrirPainelCompleto: () -> Unit
) {
    val isDiretoriaReal = associado?.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    // 0 = Visão Membro (CPF Individual), 1 = Visão Diretoria (Acesso Total ADMIN)
    var visaoSelecionada by rememberSaveable(isDiretoriaReal) {
        mutableIntStateOf(if (isDiretoriaReal) 1 else 0)
    }

    val listaCompleta = if (todosAssociadosAdmin.isNotEmpty()) {
        todosAssociadosAdmin
    } else {
        listOfNotNull(associado)
    }

    val qtdPendentes = listaCompleta.count { it.statusAssociado in listOf("Pendente", "Em analise") }
    val qtdAprovadosAtivos = listaCompleta.count { it.statusAssociado in listOf("Aprovado", "Ativo") }
    val qtdBloqueados = listaCompleta.count { it.statusAssociado in listOf("Suspenso", "Rejeitado", "Inativo") }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (visaoSelecionada == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("painel_administrativo_visoes_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IronSideLogoBadge(size = 44.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PAINEL ADMINISTRATIVO & CONTROLE DE ACESSO",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (visaoSelecionada == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Visões separadas: Membro (CPF) vs Diretoria (Acesso Total & Aprovação)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusBadgeColor(associado?.statusAssociado ?: "Ativo"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = (associado?.statusAssociado ?: "Ativo").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Seletor de Visões Separadas: 'Visão Membro' vs 'Visão Diretoria (Acesso Total)'
            TabRow(
                selectedTabIndex = visaoSelecionada,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("admin_panel_view_switcher")
            ) {
                Tab(
                    selected = visaoSelecionada == 0,
                    onClick = { visaoSelecionada = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Visão Membro (CPF)", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_visao_membro")
                )
                Tab(
                    selected = visaoSelecionada == 1,
                    onClick = { visaoSelecionada = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Visão Diretoria (Total)", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_visao_diretoria")
                )
            }

            if (visaoSelecionada == 0) {
                // VISÃO MEMBRO: Escopo estrito ao CPF do associado + Pipeline de Aprovação Pessoal
                VisaoMembroApprovalAndScopeContent(
                    associado = associado,
                    veiculosCount = veiculos.size,
                    manutencoesCount = manutencoes.size,
                    abastecimentosCount = abastecimentos.size,
                    mensalidadesCount = mensalidades.count {
                        it.userId == associado?.userId ||
                            (associado?.cpf?.isNotBlank() == true && it.cpf == associado.cpf)
                    },
                    onSolicitarRevisaoCadastro = {
                        if (associado != null) {
                            onAlterarStatusAssociado(associado.userId, "Em analise", associado.perfilAcesso)
                        }
                    },
                    onAbrirMeusDados = onAbrirPainelCompleto
                )
            } else {
                // VISÃO DIRETORIA (ACESSO TOTAL): Controle de Fluxo de Aprovação dos Usuários
                if (!isDiretoriaReal) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Modo Diretoria Requer Credencial de Comando",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Sua sessão atual está no modo MEMBRO (acesso restrito ao CPF ${associado?.cpf ?: "---"}). Para aprovar usuários, alterar cargos e acessar todos os registros do clube, habilite o Acesso Total da Diretoria.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onAlternarModoAcesso,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unlock_diretoria_view_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Autenticar Diretoria",
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    VisaoDiretoriaTotalSummaryContent(
                        associadoAtual = associado,
                        listaMembros = listaCompleta,
                        qtdPendentes = qtdPendentes,
                        qtdAprovadosAtivos = qtdAprovadosAtivos,
                        qtdBloqueados = qtdBloqueados,
                        onAlterarStatusAssociado = onAlterarStatusAssociado,
                        onAbrirPainelCompleto = onAbrirPainelCompleto
                    )
                }
            }
        }
    }
}

@Composable
private fun VisaoMembroApprovalAndScopeContent(
    associado: Associado?,
    veiculosCount: Int,
    manutencoesCount: Int,
    abastecimentosCount: Int,
    mensalidadesCount: Int,
    onSolicitarRevisaoCadastro: () -> Unit,
    onAbrirMeusDados: () -> Unit
) {
    val statusAtual = associado?.statusAssociado ?: "Ativo"
    val etapasFluxo = listOf("Pendente", "Em analise", "Aprovado", "Ativo")
    val indiceEtapa = etapasFluxo.indexOfFirst { it.equals(statusAtual, ignoreCase = true) }.coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FLUXO DE APROVAÇÃO DO SEU CADASTRO",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CPF Vinculado: ${associado?.cpf ?: "---"} • Matrícula: ${associado?.numeroAssociado ?: "MC-000001"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = statusBadgeColor(statusAtual).copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, statusBadgeColor(statusAtual), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = statusAtual.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Stepper visual de aprovação do membro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    etapasFluxo.forEachIndexed { idx, etapa ->
                        val concluidaOuAtual = statusAtual in listOf("Ativo", "Aprovado") && idx <= indiceEtapa || idx <= indiceEtapa
                        Surface(
                            color = if (concluidaOuAtual) StatusGreen.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = if (concluidaOuAtual) StatusGreenLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${idx + 1}. $etapa",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (concluidaOuAtual) StatusGreenLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (etapa.equals(statusAtual, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                val msgStatus = when (statusAtual) {
                    "Pendente" -> "⏳ Seu cadastro foi recebido e aguarda triagem inicial da Diretoria."
                    "Em analise" -> "🔍 Seus documentos (CPF, CNH e Contatos SOS) estão em análise pela Diretoria."
                    "Aprovado" -> "✅ Cadastro aprovado pela Diretoria! Sua Carteirinha Digital está pronta para ativação."
                    "Ativo" -> "🛡️ Membro Escudado Ativo! Carteirinha QR Code LGPD, Garagem e SOS liberados."
                    "Suspenso", "Rejeitado", "Inativo" -> "⚠️ Cadastro com restrição ($statusAtual). Solicite reavaliação à Diretoria."
                    else -> "✅ Acesso individual vinculado ao seu CPF."
                }

                Text(
                    text = msgStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )

                if (statusAtual !in listOf("Ativo", "Aprovado")) {
                    OutlinedButton(
                        onClick = onSolicitarRevisaoCadastro,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar / Solicitar Análise da Diretoria")
                    }
                }
            }
        }

        // Resumo de Isolamento de Dados por CPF (Visão Membro)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBox(
                modifier = Modifier.weight(1f),
                label = "MEUS VEÍCULOS",
                value = "$veiculosCount",
                sub = "CPF ${associado?.cpf?.take(7) ?: "---"}..."
            )
            MetricBox(
                modifier = Modifier.weight(1f),
                label = "MANUT. / ABAST.",
                value = "${manutencoesCount + abastecimentosCount}",
                sub = "$manutencoesCount Manut • $abastecimentosCount Abast"
            )
            MetricBox(
                modifier = Modifier.weight(1f),
                label = "MEU PIX",
                value = "$mensalidadesCount",
                sub = "Cobranças do CPF"
            )
        }

        OutlinedButton(
            onClick = onAbrirMeusDados,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("visao_membro_abrir_ficha_btn")
        ) {
            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Abrir Minha Ficha Cadastral & Carteirinha (Escopo CPF)")
        }
    }
}

@Composable
private fun VisaoDiretoriaTotalSummaryContent(
    associadoAtual: Associado?,
    listaMembros: List<Associado>,
    qtdPendentes: Int,
    qtdAprovadosAtivos: Int,
    qtdBloqueados: Int,
    onAlterarStatusAssociado: (targetUserId: String, novoStatus: String, novoPerfil: String) -> Unit,
    onAbrirPainelCompleto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBox(
                modifier = Modifier.weight(1f),
                label = "FILA APROVAÇÃO",
                value = "$qtdPendentes",
                sub = "Pendentes / Análise"
            )
            MetricBox(
                modifier = Modifier.weight(1f),
                label = "APROVADOS / ATIVOS",
                value = "$qtdAprovadosAtivos",
                sub = "Acesso Liberado"
            )
            MetricBox(
                modifier = Modifier.weight(1f),
                label = "RETIDOS / SUSP.",
                value = "$qtdBloqueados",
                sub = "Suspenso / Rejeitado"
            )
        }

        // Ações rápidas de aprovação direta no resumo da Diretoria
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CONTROLE RÁPIDO DE FLUXO DE APROVAÇÃO (${listaMembros.size} Membro(s))",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                listaMembros.take(3).forEach { membro ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${membro.nomeGuerra} • ${membro.nomeCompleto}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "CPF: ${membro.cpf} • Status: ${membro.statusAssociado} (${membro.perfilAcesso})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (membro.statusAssociado != "Ativo") {
                                Button(
                                    onClick = {
                                        onAlterarStatusAssociado(membro.userId, "Ativo", membro.perfilAcesso)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("quick_approve_${membro.userId}")
                                ) {
                                    Text("Aprovar", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        onAlterarStatusAssociado(membro.userId, "Em analise", membro.perfilAcesso)
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Revisar", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAbrirPainelCompleto,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("abrir_central_aprovacao_diretoria_btn")
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Abrir Painel Completo da Diretoria & Fila de Aprovação",
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PainelAdministrativoCompletoSection(
    associado: Associado?,
    todosAssociadosAdmin: List<Associado>,
    veiculos: List<Veiculo>,
    manutencoes: List<Manutencao>,
    abastecimentos: List<Abastecimento>,
    mensalidades: List<Mensalidade>,
    eventos: List<Evento>,
    onAlterarStatusAssociado: (targetUserId: String, novoStatus: String, novoPerfil: String) -> Unit,
    onGerarMensalidade: (String, Double, String) -> Unit,
    onCadastrarNovoMembroDiretoria: (Associado) -> Unit = {}
) {
    val context = LocalContext.current
    val clubIdentity = LocalClubIdentityConfig.current
    val isDiretoriaReal = associado?.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    // Permite alternar entre 'Visão Diretoria (Acesso Total)' e 'Simulador Visão Membro (CPF)' dentro do Painel Administrativo
    var modoVisaoPainel by rememberSaveable { mutableIntStateOf(if (isDiretoriaReal) 1 else 0) }
    var filtroStatus by rememberSaveable { mutableStateOf("Todos") }
    var buscaTexto by rememberSaveable { mutableStateOf("") }

    // Campos para pré-cadastro / inclusão de membro na fila de aprovação
    var mostrarModalNovoMembro by remember { mutableStateOf(false) }
    var novoNomeCompleto by remember { mutableStateOf("") }
    var novoNomeGuerra by remember { mutableStateOf("") }
    var novoCpf by remember { mutableStateOf("") }
    var novoRegional by remember { mutableStateOf("Sede Nacional - SP") }
    var novoStatusInicial by remember { mutableStateOf("Pendente") }

    val listaBase = if (todosAssociadosAdmin.isNotEmpty()) {
        todosAssociadosAdmin
    } else {
        listOfNotNull(associado)
    }

    val listaFiltrada = remember(listaBase, filtroStatus, buscaTexto) {
        listaBase.filter { m ->
            val passaStatus = when (filtroStatus) {
                "Todos" -> true
                "Fila Pendente" -> m.statusAssociado in listOf("Pendente", "Em analise")
                "Aprovados/Ativos" -> m.statusAssociado in listOf("Aprovado", "Ativo")
                "Restritos" -> m.statusAssociado in listOf("Suspenso", "Rejeitado", "Inativo")
                else -> m.statusAssociado.equals(filtroStatus, ignoreCase = true)
            }
            val query = buscaTexto.trim().lowercase()
            val passaBusca = query.isEmpty() ||
                m.nomeCompleto.lowercase().contains(query) ||
                m.nomeGuerra.lowercase().contains(query) ||
                m.cpf.lowercase().contains(query) ||
                m.numeroAssociado.lowercase().contains(query)
            passaStatus && passaBusca
        }
    }

    val qtdPendentes = listaBase.count { it.statusAssociado in listOf("Pendente", "Em analise") }
    val qtdAtivos = listaBase.count { it.statusAssociado in listOf("Aprovado", "Ativo") }
    val qtdRestritos = listaBase.count { it.statusAssociado in listOf("Suspenso", "Rejeitado", "Inativo") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("painel_administrativo_completo_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cabeçalho do Painel Administrativo com Seletor de Visão ('Membro' vs 'Diretoria Acesso Total')
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (modoVisaoPainel == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IronSideFormHeader(
                        title = if (modoVisaoPainel == 1) {
                            "PAINEL ADMINISTRATIVO • VISÃO DIRETORIA (ACESSO TOTAL)"
                        } else {
                            "PAINEL DO ASSOCIADO • VISÃO MEMBRO (ISOLAMENTO CPF)"
                        },
                        subtitle = if (modoVisaoPainel == 1) {
                            "Comando da Diretoria • Controle de fluxo de aprovação, cargos RBAC e auditoria"
                        } else {
                            "Visão restrita aos registros vinculados ao CPF ${associado?.cpf ?: "---"} e status de aprovação"
                        }
                    )

                    TabRow(
                        selectedTabIndex = modoVisaoPainel,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = modoVisaoPainel == 0,
                            onClick = { modoVisaoPainel = 0 },
                            text = { Text("🏍️ Visão Membro (CPF)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("subtab_visao_membro")
                        )
                        Tab(
                            selected = modoVisaoPainel == 1,
                            onClick = { modoVisaoPainel = 1 },
                            text = { Text("🛡️ Visão Diretoria (Total)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("subtab_visao_diretoria")
                        )
                    }
                }
            }
        }

        if (modoVisaoPainel == 0) {
            // VISÃO MEMBRO DENTRO DO PAINEL
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        VisaoMembroApprovalAndScopeContent(
                            associado = associado,
                            veiculosCount = veiculos.size,
                            manutencoesCount = manutencoes.size,
                            abastecimentosCount = abastecimentos.size,
                            mensalidadesCount = mensalidades.count {
                                it.userId == associado?.userId ||
                                    (associado?.cpf?.isNotBlank() == true && it.cpf == associado.cpf)
                            },
                            onSolicitarRevisaoCadastro = {
                                if (associado != null) {
                                    onAlterarStatusAssociado(associado.userId, "Em analise", associado.perfilAcesso)
                                }
                            },
                            onAbrirMeusDados = {}
                        )
                    }
                }
            }
        } else {
            // VISÃO DIRETORIA (ACESSO TOTAL): KPIs, Fila de Aprovação, RBAC, Dashboard Financeiro e Operações em Lote
            item {
                FinancialDashboardDiretoriaCard(
                    mensalidades = mensalidades,
                    todosAssociados = listaBase,
                    onConfirmarPagamentoPix = { _, _ -> },
                    onGerarLoteMensalidade = onGerarMensalidade
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBox(
                                modifier = Modifier.weight(1f),
                                label = "TOTAL MEMBROS",
                                value = "${listaBase.size} / 15",
                                sub = "Fase 1 Custo R$ 0"
                            )
                            MetricBox(
                                modifier = Modifier.weight(1f),
                                label = "AGUARDANDO APROV.",
                                value = "$qtdPendentes",
                                sub = "Pendente / Análise"
                            )
                            MetricBox(
                                modifier = Modifier.weight(1f),
                                label = "ATIVOS / APROV.",
                                value = "$qtdAtivos",
                                sub = "$qtdRestritos Restrito(s)"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    listaBase
                                        .filter { it.statusAssociado in listOf("Pendente", "Em analise", "Aprovado") }
                                        .forEach { m ->
                                            onAlterarStatusAssociado(m.userId, "Ativo", m.perfilAcesso)
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("aprovar_todos_pendentes_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Aprovar Fila Pendente", color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { onGerarMensalidade("Novembro/2026", 50.0, "15/11/2026") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lote PIX R$ 50")
                            }
                        }

                        Button(
                            onClick = {
                                val res = AssociadosPdfExportService.exportarRelatorioConsolidadoAssociadosPdf(
                                    context = context,
                                    associados = listaFiltrada.ifEmpty { listaBase },
                                    veiculos = veiculos,
                                    manutencoes = manutencoes,
                                    mensalidades = mensalidades,
                                    configIdentidade = clubIdentity,
                                    opcoes = OpcoesExportacaoConsolidadaPdf()
                                )
                                res.fold(
                                    onSuccess = { resultado ->
                                        Toast.makeText(
                                            context,
                                            "Dossiê Consolidado PDF exportado (${resultado.associadosIncluidos} associados • ${resultado.arquivoInfo.codigoProtocoloArquivo})!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        AssociadosPdfExportService.compartilharOuAbrirPdfConsolidado(
                                            context = context,
                                            pdfFile = resultado.arquivoInfo.file,
                                            abrirDireto = false
                                        )
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(
                                            context,
                                            "Erro ao exportar PDF consolidado: ${err.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_panel_exportar_consolidado_pdf_btn")
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exportar Dossiê PDF Consolidado (${listaFiltrada.ifEmpty { listaBase }.size} Associados)",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Controle Rápido de Fluxo da Conta Atual (Simulação & Auditoria RBAC)
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
                            text = "FLUXO DE APROVAÇÃO & PERFIL RBAC (CONTA ATUAL)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Altere o estágio de aprovação ou o nível de acesso para testar as permissões em tempo real:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text("Estágio no Fluxo de Aprovação:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(FLUXO_APROVACAO_ESTAGIOS) { st ->
                                FilterChip(
                                    selected = associado?.statusAssociado == st,
                                    onClick = {
                                        if (associado != null) {
                                            onAlterarStatusAssociado(associado.userId, st, associado.perfilAcesso)
                                        }
                                    },
                                    label = { Text(st) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = statusBadgeColor(st),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text("Perfil de Acesso (RBAC):", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PERFIS_ACESSO_RBAC) { pf ->
                                FilterChip(
                                    selected = associado?.perfilAcesso == pf,
                                    onClick = {
                                        if (associado != null) {
                                            onAlterarStatusAssociado(associado.userId, associado.statusAssociado, pf)
                                        }
                                    },
                                    label = { Text(pf) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Filtros de Busca por CPF/Nome e Estágio de Aprovação
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
                            text = "FILA DE APROVAÇÃO & GESTÃO DE USUÁRIOS (${listaFiltrada.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        OutlinedTextField(
                            value = buscaTexto,
                            onValueChange = { buscaTexto = it },
                            label = { Text("Buscar por CPF, Nome de Guerra, Nome ou Matrícula") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_search_member_input"),
                            singleLine = true
                        )

                        val opcoesFiltro = listOf(
                            "Todos",
                            "Fila Pendente",
                            "Aprovados/Ativos",
                            "Restritos",
                            "Pendente",
                            "Em analise",
                            "Aprovado",
                            "Ativo",
                            "Suspenso",
                            "Rejeitado"
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(opcoesFiltro) { f ->
                                FilterChip(
                                    selected = filtroStatus == f,
                                    onClick = { filtroStatus = f },
                                    label = { Text(f) }
                                )
                            }
                        }
                    }
                }
            }

            // Lista de Membros com Controle Completo do Fluxo de Aprovação
            items(listaFiltrada, key = { it.userId.ifBlank { it.id } }) { membro ->
                MemberApprovalWorkflowCard(
                    membro = membro,
                    onAlterarStatusAssociado = onAlterarStatusAssociado
                )
            }
        }
    }
}

@Composable
private fun MemberApprovalWorkflowCard(
    membro: Associado,
    onAlterarStatusAssociado: (targetUserId: String, novoStatus: String, novoPerfil: String) -> Unit
) {
    val corStatus = statusBadgeColor(membro.statusAssociado)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, corStatus.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .testTag("member_approval_card_${membro.userId}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MemberPhotoAvatar(
                        fotoUri = membro.fotoUri,
                        nomeGuerra = membro.nomeGuerra,
                        size = 50.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${membro.nomeGuerra} (${membro.nomeCompleto})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CPF: ${membro.cpf} • ${membro.numeroAssociado} • Filiação: ${membro.dataAssociacao.ifBlank { "---" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (membro.dataDesligamento.isNotBlank()) {
                            Text(
                                text = "⚠️ DESLIGAMENTO: ${membro.dataDesligamento} (${membro.motivoDesligamento.ifBlank { "Registrado pela Diretoria" }})",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusRed,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Cargo: ${membro.cargo} • Sangue: ${membro.tipoSanguineo} • CNH: ${membro.cnhCategoria}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = corStatus,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = membro.statusAssociado.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Perfil: ${membro.perfilAcesso}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Pipeline de Decisão de Aprovação (Botões Diretos)
            Text(
                text = "Ações do Fluxo de Aprovação:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onAlterarStatusAssociado(membro.userId, "Em analise", membro.perfilAcesso)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusYellow),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("1. Em Análise", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onAlterarStatusAssociado(membro.userId, "Aprovado", membro.perfilAcesso)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("2. Aprovar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onAlterarStatusAssociado(membro.userId, "Ativo", membro.perfilAcesso)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("3. Ativar", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onAlterarStatusAssociado(membro.userId, "Pendente", membro.perfilAcesso)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pendente", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        onAlterarStatusAssociado(membro.userId, "Suspenso", membro.perfilAcesso)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Suspender", style = MaterialTheme.typography.labelSmall, color = StatusYellow)
                }

                OutlinedButton(
                    onClick = {
                        onAlterarStatusAssociado(membro.userId, "Rejeitado", membro.perfilAcesso)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rejeitar", style = MaterialTheme.typography.labelSmall, color = StatusRed)
                }
            }

            // Seletor rápido de Nível de Acesso (Membro Associado vs Diretoria)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Permissão de Visão:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = membro.perfilAcesso == "Associado",
                        onClick = {
                            onAlterarStatusAssociado(membro.userId, membro.statusAssociado, "Associado")
                        },
                        label = { Text("🏍️ Membro (CPF)", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = membro.perfilAcesso in listOf("Diretoria", "SuperAdmin", "Administrador"),
                        onClick = {
                            onAlterarStatusAssociado(membro.userId, membro.statusAssociado, "Diretoria")
                        },
                        label = { Text("🛡️ Diretoria (Total)", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
