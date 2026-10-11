package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import com.example.data.Abastecimento
import com.example.data.Associado
import com.example.data.Comunicado
import com.example.data.ConfiguracaoIdentidadeClube
import com.example.data.Evento
import com.example.data.FirestoreInitReport
import com.example.data.Manutencao
import com.example.data.Mensalidade
import com.example.data.ParceiroServico
import com.example.data.Veiculo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClubIdentityConfig
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

@Composable
fun InicioDashboardScreen(
    associado: Associado?,
    todosAssociadosAdmin: List<Associado> = emptyList(),
    veiculos: List<Veiculo>,
    manutencoes: List<Manutencao>,
    abastecimentos: List<Abastecimento>,
    eventos: List<Evento>,
    comunicados: List<Comunicado>,
    mensalidades: List<Mensalidade>,
    parceiros: List<ParceiroServico>,
    configIdentidade: ConfiguracaoIdentidadeClube = LocalClubIdentityConfig.current,
    firestoreInitReport: FirestoreInitReport? = null,
    onRunFirestoreInitScript: () -> Unit = {},
    onNavigateToPillar: (Int) -> Unit,
    onOpenAdminApprovalPanel: () -> Unit = {},
    onOpenClubIdentitySettings: () -> Unit = {},
    onAlternarModoAcesso: () -> Unit = {},
    onAlterarStatusAssociado: (String, String, String) -> Unit = { _, _, _ -> },
    onOpenEmergencyModal: () -> Unit,
    onConfirmarPresenca: (Evento) -> Unit
) {
    var totalAssociadosSimulador by remember { mutableIntStateOf(15) }
    var valorMensalidadeSimulador by remember(configIdentidade.valorMensalidadePadrao) {
        mutableIntStateOf(configIdentidade.valorMensalidadePadrao.toInt().coerceAtLeast(10))
    }
    val receitaMensal = totalAssociadosSimulador * valorMensalidadeSimulador
    val receitaAnual = receitaMensal * 12

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inicio_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner + Status do Associado
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_motoclube_1791663228603),
                        contentDescription = "Banner Moto Clube",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0x330E1013), Color(0xEE0E1013))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IronSideLogoBadge(size = 54.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = associado?.numeroAssociado ?: "MC-000001",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    color = StatusGreen,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "STATUS: ${associado?.statusAssociado?.uppercase() ?: "ATIVO"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "BEM-VINDO, ${associado?.nomeGuerra?.uppercase() ?: "COMANDANTE"}!",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "${configIdentidade.nomeClube} • ${configIdentidade.lemaClube}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                            Text(
                                text = "Sede: ${configIdentidade.sedeCidadeEstado} • PIX: ${configIdentidade.chavePix}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 1.5 Resumo Dinâmico de Identidade do Clube (Logo, Cores, Contato e PIX)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                    .clickable { onOpenClubIdentitySettings() }
                    .testTag("home_club_identity_banner_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IronSideLogoBadge(size = 46.dp, overrideConfig = configIdentidade)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "IDENTIDADE DO CLUBE • ${configIdentidade.presetPaleta.uppercase()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tel/Whats: ${configIdentidade.whatsappClube} • PIX (${configIdentidade.tipoChavePix}): ${configIdentidade.chavePix}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onOpenClubIdentitySettings,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("home_open_identity_settings_btn")
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Configurar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // 2. Atalhos Rápidos dos 3 Pilares + Botão SOS Emergência
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Carteirinha QR",
                    subtitle = "Frente & Verso",
                    icon = Icons.Default.Badge,
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigateToPillar(3) },
                    testTag = "quick_carteirinha_btn"
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Veículos",
                    subtitle = "${veiculos.size} Veículo(s)",
                    icon = Icons.Default.DirectionsBike,
                    accent = MaterialTheme.colorScheme.secondary,
                    onClick = { onNavigateToPillar(1) },
                    testTag = "quick_garagem_btn"
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "🚨 SOS 1-Toque",
                    subtitle = "Tipo: ${associado?.tipoSanguineo ?: "O+"}",
                    icon = Icons.Default.Emergency,
                    accent = MaterialTheme.colorScheme.tertiary,
                    onClick = onOpenEmergencyModal,
                    testTag = "quick_emergencia_btn"
                )
            }
        }

        // 2.5 Painel Administrativo com Visões Separadas ('Membro' vs 'Diretoria Acesso Total') e Fluxo de Aprovação
        item {
            PainelAdministrativoVisoesCard(
                associado = associado,
                todosAssociadosAdmin = todosAssociadosAdmin,
                veiculos = veiculos,
                manutencoes = manutencoes,
                abastecimentos = abastecimentos,
                mensalidades = mensalidades,
                onAlterarStatusAssociado = onAlterarStatusAssociado,
                onAlternarModoAcesso = onAlternarModoAcesso,
                onAbrirPainelCompleto = onOpenAdminApprovalPanel
            )
        }

        // 2.8 Dashboard Financeiro com Gráficos (Total Arrecadado no Mês vs Valores Pendentes de Mensalidades para a Diretoria)
        item {
            FinancialDashboardDiretoriaCard(
                mensalidades = mensalidades,
                todosAssociados = todosAssociadosAdmin,
                onConfirmarPagamentoPix = { _, _ -> onOpenAdminApprovalPanel() },
                onGerarLoteMensalidade = { _, _, _ -> onOpenAdminApprovalPanel() }
            )
        }

        // 3. Painel de Escalabilidade & Estratégia Custo R$ 0 (Fase 1 -> Fase 4)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ESTRATÉGIA & ESCALABILIDADE DO CLUBE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Simulador Financeiro (15 → 1.000 associados)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = StatusGreen.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, StatusGreenLight, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "INFRA: R$ 0",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusGreenLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Selecione a Fase de Crescimento para simular a arrecadação:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val escalas = listOf(
                            15 to "Fase 1: 15 Assoc. (Agora)",
                            30 to "Fase 2: 30 Assoc.",
                            50 to "Fase 2: 50 Assoc.",
                            100 to "Fase 3: 100 Assoc.",
                            250 to "250 Assoc.",
                            500 to "Fase 4: 500 Assoc.",
                            1000 to "1.000 Assoc."
                        )
                        items(escalas) { (qtd, label) ->
                            FilterChip(
                                selected = totalAssociadosSimulador == qtd,
                                onClick = { totalAssociadosSimulador = qtd },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Valor da Mensalidade:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(35, 50, 75, 100)) { valorOpcao ->
                                FilterChip(
                                    selected = valorMensalidadeSimulador == valorOpcao,
                                    onClick = { valorMensalidadeSimulador = valorOpcao },
                                    label = { Text("R$ $valorOpcao") }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "ARRECADAÇÃO MENSAL",
                            value = "R$ $receitaMensal,00",
                            sub = "$totalAssociadosSimulador × R$ $valorMensalidadeSimulador"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "CAIXA ANUAL DO CLUBE",
                            value = "R$ $receitaAnual,00",
                            sub = "R$ $receitaMensal × 12 meses"
                        )
                    }

                    val faseExplicacao = when {
                        totalAssociadosSimulador <= 15 -> "✅ FASE 1 (AGORA — 15 Associados): Custo R$ 0. Validação completa do app com Cadastro, Aprovação, Carteirinha QR, Garagem, Eventos, Mensalidades PIX manual e Busca Nacional SOS."
                        totalAssociadosSimulador <= 50 -> "🚀 FASE 2 (30 a 50 Associados): Custo R$ 0. Testes de campo em comboios reais com o grupo inteiro e refinamento operacional."
                        totalAssociadosSimulador <= 250 -> "📈 FASE 3 (100+ Associados): Com R$ $receitaMensal/mês em caixa, avalia-se plano Pro, PIX automático via Webhook e APIs de mapas dedicadas."
                        else -> "🏆 FASE 4 (500 a 1.000+ Associados): Plataforma nacional consolidada com regionais, clube de vantagens e telemetria de comboios."
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = faseExplicacao,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // 4. Resumo dos 3 Pilares em Tempo Real
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RESUMO OPERACIONAL EM TEMPO REAL",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    val pendentesPix = mensalidades.count { it.status == "Pendente" || it.status == "Vencido" }
                    val alertasManut = manutencoes.count { it.statusAlerta != "Em dia" }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "GARAGEM",
                            value = "${veiculos.size} Moto(s)",
                            sub = if (alertasManut > 0) "🟡 $alertasManut Alerta(s) Manut." else "🟢 Manutenção em dia"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "FINANCEIRO PIX",
                            value = if (pendentesPix == 0) "Em Dia" else "$pendentesPix Pendente",
                            sub = "Mensalidade R$ 50,00"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "SOS ESTRADA",
                            value = "${parceiros.size} Locais",
                            sub = "${parceiros.count { it.parceiroOficial }} Parceiros ⭐"
                        )
                    }
                }
            }
        }

        // 5. Comunicados Oficiais e Próximo Evento
        if (comunicados.isNotEmpty()) {
            item {
                val com = comunicados.first()
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMUNICADO OFICIAL • ${com.prioridade.uppercase()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = com.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = com.texto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Por ${com.autor} • ${com.data}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        if (eventos.isNotEmpty()) {
            item {
                val ev = eventos.first()
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PRÓXIMO COMBOIO / EVENTO",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = "${ev.confirmadosCount}/${ev.limiteParticipantes} Motos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ev.nome,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "📅 ${ev.data} às ${ev.horario} • 📍 ${ev.local} (${ev.cidade})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onConfirmarPresenca(ev) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar Presença no Comboio")
                        }
                    }
                }
            }
        }

        // Estrutura Oficial das Coleções do Firestore & Relacionamentos
        item {
            FirestoreCollectionsStatusCard(
                associado = associado,
                veiculosCount = veiculos.size,
                manutencoesCount = manutencoes.size,
                abastecimentosCount = abastecimentos.size,
                eventosCount = eventos.size,
                comunicadosCount = comunicados.size,
                mensalidadesCount = mensalidades.size,
                parceirosCount = parceiros.size,
                firestoreInitReport = firestoreInitReport,
                onRunFirestoreInitScript = onRunFirestoreInitScript
            )
        }
    }
}

@Composable
fun FirestoreCollectionsStatusCard(
    associado: Associado?,
    veiculosCount: Int,
    manutencoesCount: Int,
    abastecimentosCount: Int,
    eventosCount: Int,
    comunicadosCount: Int = 0,
    mensalidadesCount: Int = 0,
    parceirosCount: Int = 0,
    firestoreInitReport: FirestoreInitReport? = null,
    onRunFirestoreInitScript: () -> Unit = {}
) {
    val isDiretoria = associado?.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .testTag("firestore_collections_schema_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IronSideFormHeader(
                title = "SCRIPT DE INICIALIZAÇÃO FIRESTORE • 9 COLEÇÕES & RLS",
                subtitle = if (isDiretoria) {
                    "Política RLS Ativa: Modo DIRETORIA com permissão total de leitura e edição em todos os registros"
                } else {
                    "Política RLS Ativa: Modo ASSOCIADO restrito à leitura e edição exclusiva dos dados próprios (UID / CPF)"
                }
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDiretoria) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                } else {
                    StatusGreen.copy(alpha = 0.16f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isDiretoria) MaterialTheme.colorScheme.secondary else StatusGreenLight,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("rls_security_policy_banner")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isDiretoria) {
                            "🛡️ RLS DIRETORIA (`isAdmin() == true`): Leitura e Edição Liberadas em Todos os Registros"
                        } else {
                            "🔒 RLS ASSOCIADO (`request.auth.uid == resource.data.userId`): Isolamento Estrito de Dados Próprios"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Associado: Só lê e edita documentos onde `userId == request.auth.uid`.\n• Diretoria: Mantém permissão total (`get`, `list`, `create`, `update`, `delete`) em todas as 9 coleções.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (firestoreInitReport != null) {
                        Text(
                            text = "✅ Inicialização Verificada: ${firestoreInitReport.totalCollectionsConfigured}/9 coleções • ${firestoreInitReport.totalDocumentsVerified} documentos validados",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreenLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = onRunFirestoreInitScript,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("run_firestore_init_script_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Executar / Verificar Inicialização (9 Coleções)",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }

            val collectionsInfo = listOf(
                Triple(
                    "associados/{id}",
                    "RLS: Próprio UID ou Diretoria • Status: ${associado?.status ?: "Ativo"}",
                    "Regra: isOwner(userId) || isAdmin() | Chave Membro: cpf (${associado?.cpf?.ifBlank { "CPF" } ?: "CPF"}) | Matrícula: ${associado?.numeroAssociado ?: "MC-000001"}"
                ),
                Triple(
                    "veiculos/{id}",
                    "$veiculosCount registro(s) • RLS: Owner / Diretoria",
                    "Regra: existing().userId == request.auth.uid || isAdmin() | FK: userId & associadoId -> associados/{id}"
                ),
                Triple(
                    "manutencoes/{id}",
                    "$manutencoesCount registro(s) • RLS: Owner / Diretoria",
                    "Regra: existing().userId == request.auth.uid || isAdmin() | FK: veiculoId -> veiculos/{id}"
                ),
                Triple(
                    "abastecimentos/{id}",
                    "$abastecimentosCount registro(s) • RLS: Owner / Diretoria",
                    "Regra: existing().userId == request.auth.uid || isAdmin() | FK: veiculoId -> veiculos/{id}"
                ),
                Triple(
                    "mensalidades/{id}",
                    "$mensalidadesCount registro(s) • RLS: Owner / Diretoria",
                    "Regra: existing().userId == request.auth.uid || isAdmin() | FK: userId & cpf -> associados/{id}"
                ),
                Triple(
                    "eventos/{id}",
                    "$eventosCount evento(s) • RLS: Leitura Clube / Edição Diretoria",
                    "Regra: Criação/Edição por Organizador (userId) ou Diretoria (isAdmin()) | Presença liberada"
                ),
                Triple(
                    "comunicados/{id}",
                    "$comunicadosCount aviso(s) • RLS: Leitura Clube / Edição Diretoria",
                    "Regra: Leitura para membros autenticados (ativo == true) | Escrita por Diretoria / Autor"
                ),
                Triple(
                    "parceiros_servicos/{id}",
                    "$parceirosCount prestador(es) • RLS: Base SOS / Diretoria",
                    "Regra: Base SOS Nacional 24h acessível ao clube | Edição/Status por Diretoria ou Autor"
                ),
                Triple(
                    "configuracoes_clube/{id}",
                    "RLS: Leitura Clube / Edição Diretoria",
                    "PK: id (identidade_oficial) | Logo, Cores (${LocalClubIdentityConfig.current.corPrimariaHex}), Contato e PIX (${LocalClubIdentityConfig.current.chavePix})"
                )
            )

            collectionsInfo.forEach { (colName, statusBadge, relDesc) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = colName,
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = statusBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusGreenLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = relDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun MotoClubePillarScreen(
    associado: Associado?,
    todosAssociadosAdmin: List<Associado> = emptyList(),
    veiculos: List<Veiculo> = emptyList(),
    manutencoes: List<Manutencao> = emptyList(),
    abastecimentos: List<Abastecimento> = emptyList(),
    eventos: List<Evento>,
    comunicados: List<Comunicado>,
    mensalidades: List<Mensalidade>,
    configIdentidade: ConfiguracaoIdentidadeClube = LocalClubIdentityConfig.current,
    initialTab: Int = 0,
    onSalvarAssociado: (Associado) -> Unit,
    onAlterarStatusAssociado: (String, String, String) -> Unit,
    onRegistrarDesligamento: (String, String, String) -> Unit = { _, _, _ -> },
    onCadastrarVeiculo: (Veiculo) -> Unit = {},
    onCriarEvento: (Evento) -> Unit,
    onConfirmarPresenca: (Evento) -> Unit,
    onPublicarComunicado: (Comunicado) -> Unit,
    onGerarMensalidade: (String, Double, String) -> Unit,
    onConfirmarPagamentoPix: (String, String) -> Unit,
    onSalvarConfiguracaoIdentidade: (ConfiguracaoIdentidadeClube) -> Unit = {}
) {
    val isDiretoria = associado?.perfilAcesso in listOf("Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    var selectedTab by remember(initialTab, isDiretoria) { mutableIntStateOf(initialTab.coerceIn(0, 6)) }
    val tabs = if (isDiretoria) {
        listOf(
            "🎫 Carteirinha",
            "👤 Meu Cadastro & CPF",
            "📅 Eventos & Avisos",
            "💰 PIX & Mensalidades",
            "🛡️ Painel Admin & Aprovação (Total)",
            "🎨 Identidade do Clube",
            "📄 RELATÓRIOS (PDF & Documentos)"
        )
    } else {
        listOf(
            "🎫 Minha Carteirinha",
            "👤 Meus Dados (CPF)",
            "📅 Eventos & Avisos",
            "💰 Minhas Mensalidades (CPF)",
            "🛡️ Painel Membro & Aprovação",
            "🎨 Identidade do Clube",
            "📄 Ficha PDF & Relatórios"
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner de escopo de acesso (DIRETORIA ACESSO TOTAL vs MEMBRO ISOLAMENTO POR CPF)
        Surface(
            color = if (isDiretoria) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDiretoria) {
                        "🛡️ MODO DIRETORIA • Acesso Total a todos os dados e aprovações"
                    } else {
                        "🔒 MODO MEMBRO • Acesso exclusivo aos dados registrados no seu CPF: ${associado?.cpf ?: "---"}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("clube_tab_$idx")
                )
            }
        }

        val mensalidadesFiltradasPorEscopo = if (isDiretoria) {
            mensalidades
        } else {
            val cpfDigits = associado?.cpf?.filter { it.isDigit() }.orEmpty()
            mensalidades.filter { m ->
                m.userId == associado?.userId ||
                    (cpfDigits.isNotEmpty() && m.cpf.filter { it.isDigit() } == cpfDigits)
            }
        }

        when (selectedTab) {
            0 -> CarteirinhaDigitalSection(
                associado = associado,
                veiculos = veiculos,
                manutencoes = manutencoes,
                abastecimentos = abastecimentos,
                configIdentidade = configIdentidade,
                onOpenIdentitySettings = { selectedTab = 5 }
            )
            1 -> CadastroAssociadoSection(
                associado = associado,
                veiculos = veiculos,
                manutencoes = manutencoes,
                abastecimentos = abastecimentos,
                configIdentidade = configIdentidade,
                isDiretoria = isDiretoria,
                onSalvar = onSalvarAssociado,
                onAbrirAbaRelatoriosPdf = { selectedTab = 6 }
            )
            2 -> EventosComunicadosSection(
                isDiretoria = isDiretoria,
                eventos = eventos,
                comunicados = comunicados,
                onCriarEvento = onCriarEvento,
                onConfirmarPresenca = onConfirmarPresenca,
                onPublicarComunicado = onPublicarComunicado
            )
            3 -> MensalidadesPixSection(
                isDiretoria = isDiretoria,
                associado = associado,
                mensalidades = mensalidadesFiltradasPorEscopo,
                configIdentidade = configIdentidade,
                onGerarMensalidade = onGerarMensalidade,
                onConfirmarPagamentoPix = onConfirmarPagamentoPix,
                onOpenIdentitySettings = { selectedTab = 5 }
            )
            4 -> PainelAdministrativoCompletoSection(
                associado = associado,
                todosAssociadosAdmin = todosAssociadosAdmin,
                veiculos = veiculos,
                manutencoes = manutencoes,
                abastecimentos = abastecimentos,
                mensalidades = mensalidades,
                eventos = eventos,
                onAlterarStatusAssociado = onAlterarStatusAssociado,
                onGerarMensalidade = onGerarMensalidade
            )
            5 -> ConfiguracaoIdentidadeClubeSection(
                configAtual = configIdentidade,
                isDiretoria = isDiretoria,
                onSalvarConfiguracao = onSalvarConfiguracaoIdentidade
            )
            6 -> RelatoriosDiretoriaScreen(
                associadoAtual = associado,
                todosAssociadosAdmin = todosAssociadosAdmin,
                veiculos = veiculos,
                manutencoes = manutencoes,
                abastecimentos = abastecimentos,
                mensalidades = mensalidades,
                configIdentidade = configIdentidade,
                onRegistrarDesligamento = onRegistrarDesligamento,
                onSalvarAssociado = onSalvarAssociado,
                onSalvarVeiculo = onCadastrarVeiculo
            )
        }
    }
}

@Composable
private fun CarteirinhaDigitalSection(
    associado: Associado?,
    veiculos: List<Veiculo> = emptyList(),
    manutencoes: List<Manutencao> = emptyList(),
    abastecimentos: List<Abastecimento> = emptyList(),
    configIdentidade: ConfiguracaoIdentidadeClube = LocalClubIdentityConfig.current,
    onOpenIdentitySettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var mostrarVerso by remember { mutableStateOf(false) }
    var mostrarTelaValidacaoQr by remember { mutableStateOf(false) }

    if (mostrarTelaValidacaoQr) {
        CarteirinhaQrValidationScreen(
            associado = associado,
            configIdentidade = configIdentidade,
            onCloseValidation = { mostrarTelaValidacaoQr = false }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text(
                        text = "CARTEIRINHA DIGITAL OFICIAL",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (!mostrarVerso) "FRENTE • Identificação Escudada" else "VERSO • QR Code LGPD & Emergência",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { mostrarTelaValidacaoQr = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_qr_validation_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Abrir Tela de Validação QR",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validar QR", maxLines = 1)
                    }
                    Button(
                        onClick = { mostrarVerso = !mostrarVerso },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flip_carteirinha_button")
                    ) {
                        Icon(
                            imageVector = if (!mostrarVerso) Icons.Default.FlipToBack else Icons.Default.FlipToFront,
                            contentDescription = "Virar Carteirinha",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (!mostrarVerso) "Ver Verso (QR)" else "Ver Frente", maxLines = 1)
                    }
                }
            }
        }

        item {
            AnimatedContent(targetState = mostrarVerso, label = "carteirinha_flip") { isBack ->
                if (!isBack) {
                    // FRENTE DA CARTEIRINHA
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B20))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF231912), Color(0xFF14171C), Color(0xFF1E232B))
                                    )
                                )
                                .padding(20.dp)
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
                                    IronSideLogoBadge(
                                        size = 46.dp,
                                        overrideConfig = configIdentidade
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = configIdentidade.nomeClube,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${configIdentidade.lemaClube.uppercase()} • CARTEIRINHA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (associado?.statusAssociado == "Ativo" || associado?.statusAssociado == "Aprovado") StatusGreen else StatusYellow,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = (associado?.statusAssociado ?: "ATIVO").uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MemberPhotoAvatar(
                                    fotoUri = associado?.fotoUri.orEmpty(),
                                    nomeGuerra = associado?.nomeGuerra ?: "FALCÃO",
                                    size = 84.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "NOME DE GUERRA: ${associado?.nomeGuerra?.uppercase() ?: "FALCÃO"}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = associado?.nomeCompleto ?: "Associado Fundador Iron Side",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${associado?.cargo ?: "Integrante Escudado"} • ${associado?.regional ?: "Sede SP"}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Nº ASSOCIADO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = associado?.numeroAssociado ?: "MC-000001",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Text("TIPO SANGUÍNEO / RH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${associado?.tipoSanguineo ?: "O+"} (${associado?.fatorRh ?: "Positivo"})",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DESDE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = associado?.dataAssociacao ?: "10/01/2025",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { mostrarTelaValidacaoQr = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_qr_validation_front_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGreenLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abrir Tela de Validação Pública do QR Code (LGPD)")
                            }
                        }
                    }
                } else {
                    // VERSO DA CARTEIRINHA (QR CODE LGPD + EMERGENCIA)
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14171C))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
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
                                    IronSideLogoBadge(size = 40.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "VERSO • VALIDAÇÃO PÚBLICA LGPD",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Sem exposição de CPF, RG, CNH ou Endereço",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusGreenLight,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VAL: ${associado?.validadeCarteirinha ?: "12/2027"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // QR Code Visual Canvas
                                Box(
                                    modifier = Modifier
                                        .size(116.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .padding(8.dp)
                                        .clickable { mostrarTelaValidacaoQr = true }
                                        .testTag("carteirinha_qr_code_box"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SimulatedQrCodeCanvas(seed = associado?.numeroAssociado ?: "MC-000001")
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "CONTATOS DE EMERGÊNCIA",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = "1. ${associado?.emergencia1Nome ?: "Mariana"} (${associado?.emergencia1Parentesco ?: "Esposa"}): ${associado?.emergencia1Telefone ?: "(11) 99111-2233"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "2. ${associado?.emergencia2Nome ?: "Carlos"} (${associado?.emergencia2Parentesco ?: "Diretor"}): ${associado?.emergencia2Telefone ?: "(11) 99444-5566"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${configIdentidade.nomeClube} • CNPJ ${configIdentidade.cnpjClube}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Contato Clube: ${configIdentidade.whatsappClube} • ${configIdentidade.emailContato}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { mostrarTelaValidacaoQr = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_qr_validation_back_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abrir Tela de Validação do QR Code (Verificação Externa)")
                            }
                        }
                    }
                }
            }
        }

        // Card de Exportação da Ficha do Associado para PDF (Formatado para Impressão A4)
        item {
            val associadoExport = associado ?: Associado()
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                    .testTag("carteirinha_exportar_ficha_pdf_card")
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📄 FICHA DO ASSOCIADO EM PDF (LAYOUT PARA IMPRESSÃO A4)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Consolida Dados Pessoais, CNH, Contatos de Emergência SOS, Documento CRLV e Histórico do Veículo (${manutencoes.size} manutenções) pronto para impressão.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val res = FichaCadastralPdfGenerator.gerarPdfFichaCompletaAssociado(
                                    context = context,
                                    associado = associadoExport,
                                    veiculosDoAssociado = veiculos,
                                    manutencoesDoAssociado = manutencoes,
                                    abastecimentosDoAssociado = abastecimentos,
                                    configIdentidade = configIdentidade
                                )
                                res.onSuccess { pdfFile ->
                                    Toast.makeText(
                                        context,
                                        "Ficha do Associado exportada em PDF: ${pdfFile.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    FichaCadastralPdfGenerator.compartilharOuAbrirPdf(context, pdfFile, abrirDireto = false)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(42.dp)
                                .testTag("carteirinha_exportar_ficha_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar Ficha PDF", style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                val res = FichaCadastralPdfGenerator.gerarPdfFichaCompletaAssociado(
                                    context = context,
                                    associado = associadoExport,
                                    veiculosDoAssociado = veiculos,
                                    manutencoesDoAssociado = manutencoes,
                                    abastecimentosDoAssociado = abastecimentos,
                                    configIdentidade = configIdentidade
                                )
                                res.onSuccess { pdfFile ->
                                    FichaCadastralPdfGenerator.imprimirFichaAssociadoPdf(
                                        context = context,
                                        pdfFile = pdfFile,
                                        jobName = "Ficha_${associadoExport.numeroAssociado}_${associadoExport.nomeGuerra}"
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(0.95f)
                                .height(42.dp)
                                .testTag("carteirinha_imprimir_ficha_a4_btn")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Imprimir A4", style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedQrCodeCanvas(seed: String) {
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

@Composable
private fun CadastroAssociadoSection(
    associado: Associado?,
    veiculos: List<Veiculo> = emptyList(),
    manutencoes: List<Manutencao> = emptyList(),
    abastecimentos: List<Abastecimento> = emptyList(),
    configIdentidade: ConfiguracaoIdentidadeClube = LocalClubIdentityConfig.current,
    isDiretoria: Boolean = false,
    onSalvar: (Associado) -> Unit,
    onAbrirAbaRelatoriosPdf: () -> Unit = {}
) {
    val context = LocalContext.current
    var fotoUri by remember(associado) {
        mutableStateOf(associado?.fotoUri?.ifBlank { "preset:piloto_escudado" } ?: "preset:piloto_escudado")
    }
    var nomeCompleto by remember(associado) { mutableStateOf(associado?.nomeCompleto ?: "") }
    var nomeGuerra by remember(associado) { mutableStateOf(associado?.nomeGuerra ?: "") }
    var cpf by remember(associado) { mutableStateOf(associado?.cpf ?: "") }
    var rg by remember(associado) { mutableStateOf(associado?.rg ?: "") }
    var dataNascimento by remember(associado) { mutableStateOf(associado?.dataNascimento ?: "14/05/1985") }
    var tipoSanguineo by remember(associado) { mutableStateOf(associado?.tipoSanguineo ?: "O+") }
    var fatorRh by remember(associado) { mutableStateOf(associado?.fatorRh ?: "Positivo (+)") }
    var email by remember(associado) { mutableStateOf(associado?.email ?: "") }
    var telefone by remember(associado) { mutableStateOf(associado?.telefone ?: "") }
    var celular by remember(associado) { mutableStateOf(associado?.celular ?: "") }
    var whatsapp by remember(associado) { mutableStateOf(associado?.whatsapp ?: "") }
    // DATA DA FILIAÇÃO DO MEMBRO (OBRIGATÓRIO)
    var dataAssociacao by remember(associado) {
        mutableStateOf(associado?.dataAssociacao?.ifBlank { "10/01/2025" } ?: "10/01/2025")
    }
    var erroDataFiliacao by remember { mutableStateOf<String?>(null) }

    // DESLIGAMENTO (Caso esteja preenchido pela Diretoria)
    var dataDesligamento by remember(associado) { mutableStateOf(associado?.dataDesligamento ?: "") }
    var motivoDesligamento by remember(associado) { mutableStateOf(associado?.motivoDesligamento ?: "") }

    var cargo by remember(associado) { mutableStateOf(associado?.cargo ?: "Integrante Escudado") }
    var regional by remember(associado) { mutableStateOf(associado?.regional ?: "Sede Nacional - SP") }
    var observacoes by remember(associado) { mutableStateOf(associado?.observacoes ?: "") }

    // Endereço
    var cep by remember(associado) { mutableStateOf(associado?.cep ?: "") }
    var logradouro by remember(associado) { mutableStateOf(associado?.logradouro ?: "") }
    var numeroEndereco by remember(associado) { mutableStateOf(associado?.numeroEndereco ?: "") }
    var complemento by remember(associado) { mutableStateOf(associado?.complemento ?: "") }
    var bairro by remember(associado) { mutableStateOf(associado?.bairro ?: "") }
    var cidade by remember(associado) { mutableStateOf(associado?.cidade ?: "São Paulo") }
    var estado by remember(associado) { mutableStateOf(associado?.estado ?: "SP") }

    // CNH
    var cnhNumero by remember(associado) { mutableStateOf(associado?.cnhNumero ?: "") }
    var cnhCategoria by remember(associado) { mutableStateOf(associado?.cnhCategoria ?: "AB") }
    var cnhEmissao by remember(associado) { mutableStateOf(associado?.cnhEmissao ?: "10/02/2021") }
    var cnhValidade by remember(associado) { mutableStateOf(associado?.cnhValidade ?: "") }
    var cnhPrimeiraHab by remember(associado) { mutableStateOf(associado?.cnhPrimeiraHab ?: "15/06/2004") }

    // Contatos de Emergência (2 obrigatórios conforme especificação)
    var em1Nome by remember(associado) { mutableStateOf(associado?.emergencia1Nome ?: "") }
    var em1Parentesco by remember(associado) { mutableStateOf(associado?.emergencia1Parentesco ?: "") }
    var em1Telefone by remember(associado) { mutableStateOf(associado?.emergencia1Telefone ?: "") }
    var em2Nome by remember(associado) { mutableStateOf(associado?.emergencia2Nome ?: "") }
    var em2Parentesco by remember(associado) { mutableStateOf(associado?.emergencia2Parentesco ?: "") }
    var em2Telefone by remember(associado) { mutableStateOf(associado?.emergencia2Telefone ?: "") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri.toString()
        }
    }

    fun buildAssociadoAtualizado(): Associado {
        val base = associado ?: Associado()
        return base.copy(
            fotoUri = fotoUri.ifBlank { "preset:piloto_escudado" },
            nomeCompleto = nomeCompleto.ifBlank { "Associado Iron Side" },
            nomeGuerra = nomeGuerra.ifBlank { "FALCAO" },
            cpf = cpf.ifBlank { "123.456.789-00" },
            rg = rg,
            dataNascimento = dataNascimento,
            tipoSanguineo = tipoSanguineo.ifBlank { "O+" },
            fatorRh = fatorRh,
            email = email,
            telefone = telefone,
            celular = celular,
            whatsapp = whatsapp,
            dataAssociacao = dataAssociacao.trim(),
            dataDesligamento = dataDesligamento.trim(),
            motivoDesligamento = motivoDesligamento.trim(),
            cargo = cargo,
            regional = regional,
            observacoes = observacoes,
            cep = cep,
            logradouro = logradouro,
            numeroEndereco = numeroEndereco,
            complemento = complemento,
            bairro = bairro,
            cidade = cidade,
            estado = estado,
            cnhNumero = cnhNumero,
            cnhCategoria = cnhCategoria,
            cnhEmissao = cnhEmissao,
            cnhValidade = cnhValidade,
            cnhPrimeiraHab = cnhPrimeiraHab,
            emergencia1Nome = em1Nome,
            emergencia1Parentesco = em1Parentesco,
            emergencia1Telefone = em1Telefone,
            emergencia1Whatsapp = em1Telefone,
            emergencia2Nome = em2Nome,
            emergencia2Parentesco = em2Parentesco,
            emergencia2Telefone = em2Telefone,
            emergencia2Whatsapp = em2Telefone,
            statusAssociado = if (dataDesligamento.isNotBlank()) "Desligado" else base.statusAssociado
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IronSideFormHeader(
                        title = "FICHA CADASTRAL COMPLETA DO ASSOCIADO",
                        subtitle = "Nº Oficial: ${associado?.numeroAssociado ?: "MC-000001"} • Status: ${associado?.statusAssociado ?: "Ativo"} • Filiação Obrigatória & PDF"
                    )

                    // 1. FOTO DO MEMBRO NO CADASTRO + SELETOR DE FOTO (GALERIA OU PRESET OFICIAL)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MemberPhotoAvatar(
                                    fotoUri = fotoUri,
                                    nomeGuerra = nomeGuerra.ifBlank { "FALCAO" },
                                    size = 86.dp,
                                    modifier = Modifier.testTag("cadastro_membro_foto_preview")
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "FOTO DO MEMBRO NO CADASTRO (OBRIGATÓRIA NA FICHA PDF)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Selecione uma foto da galeria (Zero-Permission Photo Picker) ou escolha um avatar oficial para constar na Carteirinha e no PDF da Ficha Cadastral.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("selecionar_foto_membro_btn")
                                    ) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Selecionar Foto do Membro (Galeria)")
                                    }
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val presetsFoto = listOf(
                                    "preset:piloto_escudado" to "Piloto Escudado",
                                    "preset:comandante_estrada" to "Comandante Estrada",
                                    "preset:capitao_comboio" to "Capitão Comboio"
                                )
                                items(presetsFoto) { (presetKey, label) ->
                                    FilterChip(
                                        selected = fotoUri == presetKey,
                                        onClick = { fotoUri = presetKey },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }

                    // 2. DATA DA FILIAÇÃO DO MEMBRO (OBRIGATÓRIA) + DESLIGAMENTO (CASO PREENCHIDO PELA DIRETORIA)
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📅 DATA DA FILIAÇÃO DO MEMBRO (OBRIGATÓRIO) & CONTROLE DE DESLIGAMENTO",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = dataAssociacao,
                                    onValueChange = {
                                        dataAssociacao = it
                                        if (it.isNotBlank()) erroDataFiliacao = null
                                    },
                                    label = { Text("Data da Filiação * (Obrigatório)") },
                                    isError = erroDataFiliacao != null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_data_filiacao_membro"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = dataNascimento,
                                    onValueChange = { dataNascimento = it },
                                    label = { Text("Data de Nascimento") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            if (erroDataFiliacao != null) {
                                Text(
                                    text = erroDataFiliacao!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Exibição / Edição de Desligamento (Preenchido pela Diretoria ou exibido na Ficha)
                            if (isDiretoria || dataDesligamento.isNotBlank()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                                Text(
                                    text = if (isDiretoria) {
                                        "🛡️ REGISTRO DE DESLIGAMENTO (PREENCHIDO PELA DIRETORIA — EXIBIDO NO PDF):"
                                    } else {
                                        "⚠️ REGISTRO DE DESLIGAMENTO PREENCHIDO PELA DIRETORIA:"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (dataDesligamento.isNotBlank()) StatusRed else MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = dataDesligamento,
                                        onValueChange = { if (isDiretoria) dataDesligamento = it },
                                        readOnly = !isDiretoria,
                                        label = { Text("Data do Desligamento (Diretoria)") },
                                        placeholder = { Text("Em branco = Ativo") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_data_desligamento_ficha"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = motivoDesligamento,
                                        onValueChange = { if (isDiretoria) motivoDesligamento = it },
                                        readOnly = !isDiretoria,
                                        label = { Text("Motivo do Desligamento") },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .testTag("input_motivo_desligamento_ficha"),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nomeCompleto,
                        onValueChange = { nomeCompleto = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nomeGuerra,
                            onValueChange = { nomeGuerra = it },
                            label = { Text("Nome de Guerra / Apelido") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tipoSanguineo,
                            onValueChange = { tipoSanguineo = it },
                            label = { Text("Tipo Sanguíneo") },
                            modifier = Modifier.weight(0.6f)
                        )
                        OutlinedTextField(
                            value = fatorRh,
                            onValueChange = { fatorRh = it },
                            label = { Text("Fator RH") },
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cpf,
                            onValueChange = { cpf = it },
                            label = { Text("CPF (Protegido LGPD)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rg,
                            onValueChange = { rg = it },
                            label = { Text("RG") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail Oficial") },
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = telefone,
                            onValueChange = { telefone = it },
                            label = { Text("Telefone Fixo") },
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = celular,
                            onValueChange = { celular = it },
                            label = { Text("Celular") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cargo,
                            onValueChange = { cargo = it },
                            label = { Text("Cargo / Função") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = regional,
                            onValueChange = { regional = it },
                            label = { Text("Regional / Subgrupo") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Observações / Prontuário Médico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. ENDEREÇO RESIDENCIAL & HABILITAÇÃO (CNH)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cep,
                            onValueChange = { cep = it },
                            label = { Text("CEP") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = numeroEndereco,
                            onValueChange = { numeroEndereco = it },
                            label = { Text("Número") },
                            modifier = Modifier.weight(0.7f)
                        )
                        OutlinedTextField(
                            value = complemento,
                            onValueChange = { complemento = it },
                            label = { Text("Complemento") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = logradouro,
                        onValueChange = { logradouro = it },
                        label = { Text("Logradouro / Rua / Avenida") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = bairro,
                            onValueChange = { bairro = it },
                            label = { Text("Bairro") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cidade,
                            onValueChange = { cidade = it },
                            label = { Text("Cidade") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = estado,
                            onValueChange = { estado = it },
                            label = { Text("UF") },
                            modifier = Modifier.width(76.dp)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cnhNumero,
                            onValueChange = { cnhNumero = it },
                            label = { Text("Número CNH") },
                            modifier = Modifier.weight(1.4f)
                        )
                        OutlinedTextField(
                            value = cnhCategoria,
                            onValueChange = { cnhCategoria = it },
                            label = { Text("Cat. (A/AB)") },
                            modifier = Modifier.weight(0.8f)
                        )
                        OutlinedTextField(
                            value = cnhValidade,
                            onValueChange = { cnhValidade = it },
                            label = { Text("Validade") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cnhEmissao,
                            onValueChange = { cnhEmissao = it },
                            label = { Text("Data Emissão CNH") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cnhPrimeiraHab,
                            onValueChange = { cnhPrimeiraHab = it },
                            label = { Text("Data 1ª Habilitação") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. CONTATOS DE EMERGÊNCIA (MODO SOS)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                    Text("Exibidos no Modo Emergência com botão LIGAR imediato:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = em1Nome,
                        onValueChange = { em1Nome = it },
                        label = { Text("Contato 1 — Nome (Prioridade 1)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = em1Parentesco,
                            onValueChange = { em1Parentesco = it },
                            label = { Text("Parentesco") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = em1Telefone,
                            onValueChange = { em1Telefone = it },
                            label = { Text("Telefone / WhatsApp") },
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedTextField(
                        value = em2Nome,
                        onValueChange = { em2Nome = it },
                        label = { Text("Contato 2 — Nome (Prioridade 2)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = em2Parentesco,
                            onValueChange = { em2Parentesco = it },
                            label = { Text("Parentesco / Função") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = em2Telefone,
                            onValueChange = { em2Telefone = it },
                            label = { Text("Telefone / WhatsApp") },
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (dataAssociacao.isBlank()) {
                                erroDataFiliacao = "A DATA DA FILIAÇÃO DO MEMBRO é obrigatória para salvar a ficha cadastral!"
                                Toast.makeText(context, erroDataFiliacao, Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            onSalvar(buildAssociadoAtualizado())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("salvar_cadastro_associado_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Salvar Ficha Completa (Foto & Filiação)",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (dataAssociacao.isBlank()) {
                                    erroDataFiliacao = "Preencha a Data da Filiação (obrigatória) antes de gerar o PDF!"
                                    Toast.makeText(context, erroDataFiliacao, Toast.LENGTH_LONG).show()
                                    return@OutlinedButton
                                }
                                val assocCompleto = buildAssociadoAtualizado()
                                onSalvar(assocCompleto)
                                val res = FichaCadastralPdfGenerator.gerarPdfFichaCompletaAssociado(
                                    context = context,
                                    associado = assocCompleto,
                                    veiculosDoAssociado = veiculos,
                                    manutencoesDoAssociado = manutencoes,
                                    abastecimentosDoAssociado = abastecimentos,
                                    configIdentidade = configIdentidade
                                )
                                res.onSuccess { pdfFile ->
                                    Toast.makeText(
                                        context,
                                        "PDF da Ficha Cadastral + Histórico do Veículo gerado com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    FichaCadastralPdfGenerator.compartilharOuAbrirPdf(context, pdfFile, abrirDireto = false)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("gerar_pdf_ficha_cadastral_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gerar Ficha PDF", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                if (dataAssociacao.isBlank()) {
                                    erroDataFiliacao = "Preencha a Data da Filiação (obrigatória) antes de imprimir!"
                                    Toast.makeText(context, erroDataFiliacao, Toast.LENGTH_LONG).show()
                                    return@OutlinedButton
                                }
                                val assocCompleto = buildAssociadoAtualizado()
                                onSalvar(assocCompleto)
                                val res = FichaCadastralPdfGenerator.gerarPdfFichaCompletaAssociado(
                                    context = context,
                                    associado = assocCompleto,
                                    veiculosDoAssociado = veiculos,
                                    manutencoesDoAssociado = manutencoes,
                                    abastecimentosDoAssociado = abastecimentos,
                                    configIdentidade = configIdentidade
                                )
                                res.onSuccess { pdfFile ->
                                    FichaCadastralPdfGenerator.imprimirFichaAssociadoPdf(
                                        context = context,
                                        pdfFile = pdfFile,
                                        jobName = "Ficha_${assocCompleto.numeroAssociado}_${assocCompleto.nomeGuerra}"
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(0.95f)
                                .height(44.dp)
                                .testTag("imprimir_pdf_ficha_cadastral_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Imprimir A4", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onAbrirAbaRelatoriosPdf,
                            modifier = Modifier
                                .weight(0.95f)
                                .height(44.dp)
                                .testTag("ir_para_aba_relatorios_diretoria_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Relatórios", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventosComunicadosSection(
    isDiretoria: Boolean,
    eventos: List<Evento>,
    comunicados: List<Comunicado>,
    onCriarEvento: (Evento) -> Unit,
    onConfirmarPresenca: (Evento) -> Unit,
    onPublicarComunicado: (Comunicado) -> Unit
) {
    var mostrarDialogEvento by remember { mutableStateOf(false) }
    var mostrarDialogComunicado by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isDiretoria) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { mostrarDialogEvento = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo Evento (Admin)")
                    }
                    OutlinedButton(
                        onClick = { mostrarDialogComunicado = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo Comunicado")
                    }
                }
            }
        }

        item {
            Text(
                text = "AGENDA DE EVENTOS & COMBOIOS (${eventos.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(eventos) { ev ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ev.nome, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = ev.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text("📅 ${ev.data} às ${ev.horario}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    Text("📍 ${ev.local} — ${ev.cidade}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (ev.descricao.isNotBlank()) {
                        Text(ev.descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Confirmados: ${ev.confirmadosCount}/${ev.limiteParticipantes} • Taxa: R$ ${String.format("%.2f", ev.valor)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { onConfirmarPresenca(ev) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Confirmar Presença")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MURAL DE COMUNICADOS & AVISOS (${comunicados.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(comunicados) { com ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(com.titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(com.prioridade.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Text(com.texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("${com.autor} • ${com.data}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (mostrarDialogEvento) {
        var nome by remember { mutableStateOf("") }
        var data by remember { mutableStateOf("15/11/2026") }
        var horario by remember { mutableStateOf("07:00") }
        var local by remember { mutableStateOf("Posto Lago Azul Rod. Anhanguera") }
        var cidade by remember { mutableStateOf("Campinas - SP") }
        var descricao by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogEvento = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (nome.isNotBlank()) {
                            onCriarEvento(
                                Evento(
                                    nome = nome,
                                    data = data,
                                    horario = horario,
                                    local = local,
                                    cidade = cidade,
                                    descricao = descricao,
                                    status = "Inscrições Abertas",
                                    ativo = true
                                )
                            )
                            mostrarDialogEvento = false
                        }
                    }
                ) {
                    Text("Publicar Evento")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogEvento = false }) { Text("Cancelar") }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IronSideLogoBadge(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Novo Evento / Comboio")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Evento") })
                    OutlinedTextField(value = data, onValueChange = { data = it }, label = { Text("Data (DD/MM/AAAA)") })
                    OutlinedTextField(value = horario, onValueChange = { horario = it }, label = { Text("Horário") })
                    OutlinedTextField(value = local, onValueChange = { local = it }, label = { Text("Ponto de Encontro") })
                    OutlinedTextField(value = cidade, onValueChange = { cidade = it }, label = { Text("Cidade / Destino") })
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Observações do Comboio") })
                }
            }
        )
    }

    if (mostrarDialogComunicado) {
        var titulo by remember { mutableStateOf("") }
        var texto by remember { mutableStateOf("") }
        var prioridade by remember { mutableStateOf("Alta") }

        AlertDialog(
            onDismissRequest = { mostrarDialogComunicado = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (titulo.isNotBlank() && texto.isNotBlank()) {
                            onPublicarComunicado(
                                Comunicado(
                                    titulo = titulo,
                                    texto = texto,
                                    data = "10/10/2026",
                                    autor = "Diretoria Iron Side",
                                    prioridade = prioridade,
                                    ativo = true
                                )
                            )
                            mostrarDialogComunicado = false
                        }
                    }
                ) {
                    Text("Publicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogComunicado = false }) { Text("Cancelar") }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IronSideLogoBadge(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Publicar Comunicado Oficial")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") })
                    OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Mensagem aos Associados") })
                    OutlinedTextField(value = prioridade, onValueChange = { prioridade = it }, label = { Text("Prioridade (Normal / Alta / Urgente)") })
                }
            }
        )
    }
}

@Composable
private fun MensalidadesPixSection(
    isDiretoria: Boolean,
    associado: Associado?,
    mensalidades: List<Mensalidade>,
    configIdentidade: ConfiguracaoIdentidadeClube = LocalClubIdentityConfig.current,
    onGerarMensalidade: (String, Double, String) -> Unit,
    onConfirmarPagamentoPix: (String, String) -> Unit,
    onOpenIdentitySettings: () -> Unit = {}
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val chavePixClube = configIdentidade.chavePix.ifBlank { "financeiro@ironsidemotoclube.com.br" }
    val valorPadrao = configIdentidade.valorMensalidadePadrao
    val diaVencPadrao = configIdentidade.diaVencimentoPadrao.toString().padStart(2, '0')

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isDiretoria) {
            item {
                FinancialDashboardDiretoriaCard(
                    mensalidades = mensalidades,
                    todosAssociados = listOfNotNull(associado),
                    onConfirmarPagamentoPix = onConfirmarPagamentoPix,
                    onGerarLoteMensalidade = onGerarMensalidade
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IronSideFormHeader(
                        title = if (isDiretoria) "CENTRAL PIX • GESTÃO DIRETORIA" else "MINHAS MENSALIDADES PIX (POR CPF)",
                        subtitle = if (isDiretoria) {
                            "Favorecido: ${configIdentidade.favorecidoPix} (${configIdentidade.bancoPix})"
                        } else {
                            "Exibindo exclusivamente cobranças do seu CPF: ${associado?.cpf ?: "---"}"
                        }
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CHAVE PIX OFICIAL (${configIdentidade.tipoChavePix.uppercase()}) • ${configIdentidade.favorecidoPix}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = chavePixClube,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = Color.White
                                )
                                Text(
                                    text = "Banco: ${configIdentidade.bancoPix} • Contato Tesouraria: ${configIdentidade.whatsappClube}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(chavePixClube))
                                    Toast.makeText(context, "Chave PIX copiada!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Chave PIX", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isDiretoria) {
                            Button(
                                onClick = {
                                    onGerarMensalidade(
                                        "Novembro/2026",
                                        valorPadrao,
                                        "$diaVencPadrao/11/2026"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gerar Lote PIX (R$ ${String.format("%.2f", valorPadrao)})")
                            }
                        }
                        OutlinedButton(
                            onClick = onOpenIdentitySettings,
                            shape = RoundedCornerShape(12.dp),
                            modifier = if (isDiretoria) Modifier else Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Editar PIX & Identidade")
                        }
                    }
                }
            }
        }

        items(mensalidades) { m ->
            val isPago = m.status == "Pago"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${m.competencia} • ${m.numeroAssociado}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Vencimento: ${m.vencimento} • CPF: ${m.cpf}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = if (isPago) StatusGreen else StatusYellow,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = m.status.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "R$ ${String.format("%.2f", m.valor)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!isPago) {
                            Button(
                                onClick = { onConfirmarPagamentoPix(m.id, "10/10/2026") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirmar PIX Pago", color = Color.White)
                            }
                        } else {
                            Text(
                                text = "✅ Pago em ${m.dataPagamento}",
                                style = MaterialTheme.typography.labelLarge,
                                color = StatusGreenLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AprovacaoDiretoriaSection(
    associado: Associado?,
    todosAssociadosAdmin: List<Associado>,
    mensalidades: List<Mensalidade>,
    eventos: List<Evento>,
    onAlterarStatusAssociado: (String, String, String) -> Unit,
    onGerarMensalidade: (String, Double, String) -> Unit
) {
    val statusFluxo = listOf("Pendente", "Em analise", "Aprovado", "Ativo", "Inativo", "Suspenso", "Rejeitado")
    val perfisAcesso = listOf("Associado", "Diretoria", "Financeiro", "Administrador", "SuperAdmin")
    val listaMembros = if (todosAssociadosAdmin.isNotEmpty()) {
        todosAssociadosAdmin
    } else {
        listOfNotNull(associado)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IronSideFormHeader(
                        title = "DIRETORIA • ACESSO TOTAL",
                        subtitle = "Controle Geral de Todos os Membros, CPFs e Fluxo de Aprovação"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "ASSOCIADOS ATIVOS",
                            value = "${listaMembros.size} / 15",
                            sub = "Fase 1 Validada"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "RECEITA MENSAL",
                            value = "R$ 750,00",
                            sub = "15 × R$ 50,00"
                        )
                    }

                    Text("Alterar Estágio de Aprovação da Conta Atual:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(statusFluxo) { st ->
                            FilterChip(
                                selected = associado?.statusAssociado == st,
                                onClick = {
                                    if (associado != null) {
                                        onAlterarStatusAssociado(associado.userId, st, associado.perfilAcesso)
                                    }
                                },
                                label = { Text(st) }
                            )
                        }
                    }

                    Text("Alterar Perfil de Acesso (RBAC):", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(perfisAcesso) { pf ->
                            FilterChip(
                                selected = associado?.perfilAcesso == pf,
                                onClick = {
                                    if (associado != null) {
                                        onAlterarStatusAssociado(associado.userId, associado.statusAssociado, pf)
                                    }
                                },
                                label = { Text(pf) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "MEMBROS REGISTRADOS NO CLUBE (${listaMembros.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(listaMembros) { membro ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${membro.nomeGuerra} (${membro.nomeCompleto})",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "CPF: ${membro.cpf} • ${membro.numeroAssociado} • ${membro.regional}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = if (membro.statusAssociado == "Ativo" || membro.statusAssociado == "Aprovado") StatusGreen else StatusYellow,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${membro.statusAssociado.uppercase()} • ${membro.perfilAcesso}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onAlterarStatusAssociado(membro.userId, "Ativo", membro.perfilAcesso) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                        ) {
                            Text("Aprovar / Ativar", color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { onAlterarStatusAssociado(membro.userId, "Suspenso", membro.perfilAcesso) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Suspender")
                        }
                    }
                }
            }
        }
    }
}
