package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ConfiguracaoIdentidadeClube
import com.example.ui.theme.IronBackground
import com.example.ui.theme.IronPrimary
import com.example.ui.theme.IronSecondary
import com.example.ui.theme.IronSurface
import com.example.ui.theme.IronTertiary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.parseHexColorSafe
import java.util.Locale

data class ClubColorPalettePreset(
    val nome: String,
    val descricao: String,
    val primariaHex: String,
    val secundariaHex: String,
    val sosHex: String,
    val fundoHex: String,
    val superficieHex: String
)

val PALETAS_IDENTIDADE_CLUBE = listOf(
    ClubColorPalettePreset(
        nome = "Laranja Iron Side (Oficial)",
        descricao = "Âmbar Racing & Asfalto Noturno",
        primariaHex = "#FF6D00",
        secundariaHex = "#FFB74D",
        sosHex = "#EF5350",
        fundoHex = "#0E1013",
        superficieHex = "#16191E"
    ),
    ClubColorPalettePreset(
        nome = "Ouro Imperial & Preto Fosco",
        descricao = "Dourado Clássico Custom & Grafite",
        primariaHex = "#D4AF37",
        secundariaHex = "#FFE082",
        sosHex = "#E53935",
        fundoHex = "#0B0C0E",
        superficieHex = "#15171B"
    ),
    ClubColorPalettePreset(
        nome = "Vermelho Rota 66",
        descricao = "Rubi Intenso & Prata Cromado",
        primariaHex = "#E53935",
        secundariaHex = "#FF8A80",
        sosHex = "#FF1744",
        fundoHex = "#110D0E",
        superficieHex = "#1B1416"
    ),
    ClubColorPalettePreset(
        nome = "Azul Titânio & Cromo",
        descricao = "Azul Metálico Estrada & Gelo",
        primariaHex = "#29B6F6",
        secundariaHex = "#81D4FA",
        sosHex = "#EF5350",
        fundoHex = "#0C1117",
        superficieHex = "#141C24"
    ),
    ClubColorPalettePreset(
        nome = "Verde Militar & Bronze",
        descricao = "Oliva Tático & Areia Expedição",
        primariaHex = "#66BB6A",
        secundariaHex = "#C5E1A5",
        sosHex = "#EF5350",
        fundoHex = "#0D120E",
        superficieHex = "#151D17"
    )
)

data class LogoPresetOption(
    val id: String,
    val label: String,
    val descricao: String
)

val LOGO_PRESET_OPTIONS = listOf(
    LogoPresetOption("brasao_oficial", "Brasão Oficial", "Escudo original Iron Side MC"),
    LogoPresetOption("escudo_cromado", "Escudo Cromado", "Insígnia metálica de proteção"),
    LogoPresetOption("caveira_custom", "Chama Custom", "Símbolo old-school de estrada"),
    LogoPresetOption("aguia_estrada", "Honra & Comando", "Medalhão de diretoria nacional"),
    LogoPresetOption("icone_moto_clube", "Motocicleta V-Twin", "Silhueta clássica de comboio")
)

@Composable
fun ConfiguracaoIdentidadeClubeSection(
    configAtual: ConfiguracaoIdentidadeClube,
    isDiretoria: Boolean,
    onSalvarConfiguracao: (ConfiguracaoIdentidadeClube) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // Estado editável sincronizado com o documento do Firestore
    var nomeClube by remember(configAtual.nomeClube) { mutableStateOf(configAtual.nomeClube) }
    var subtituloClube by remember(configAtual.subtituloClube) { mutableStateOf(configAtual.subtituloClube) }
    var lemaClube by remember(configAtual.lemaClube) { mutableStateOf(configAtual.lemaClube) }
    var cnpjClube by remember(configAtual.cnpjClube) { mutableStateOf(configAtual.cnpjClube) }
    var sedeCidadeEstado by remember(configAtual.sedeCidadeEstado) { mutableStateOf(configAtual.sedeCidadeEstado) }

    // Logo
    var logoPreset by remember(configAtual.logoPreset) { mutableStateOf(configAtual.logoPreset) }
    var logoUri by remember(configAtual.logoUri) { mutableStateOf(configAtual.logoUri) }

    // Cores Dinâmicas
    var presetPaleta by remember(configAtual.presetPaleta) { mutableStateOf(configAtual.presetPaleta) }
    var corPrimariaHex by remember(configAtual.corPrimariaHex) { mutableStateOf(configAtual.corPrimariaHex) }
    var corSecundariaHex by remember(configAtual.corSecundariaHex) { mutableStateOf(configAtual.corSecundariaHex) }
    var corDestaqueSosHex by remember(configAtual.corDestaqueSosHex) { mutableStateOf(configAtual.corDestaqueSosHex) }
    var corFundoHex by remember(configAtual.corFundoHex) { mutableStateOf(configAtual.corFundoHex) }
    var corSuperficieHex by remember(configAtual.corSuperficieHex) { mutableStateOf(configAtual.corSuperficieHex) }

    // Dados de Contato
    var emailContato by remember(configAtual.emailContato) { mutableStateOf(configAtual.emailContato) }
    var telefoneContato by remember(configAtual.telefoneContato) { mutableStateOf(configAtual.telefoneContato) }
    var whatsappClube by remember(configAtual.whatsappClube) { mutableStateOf(configAtual.whatsappClube) }
    var instagramClube by remember(configAtual.instagramClube) { mutableStateOf(configAtual.instagramClube) }
    var siteClube by remember(configAtual.siteClube) { mutableStateOf(configAtual.siteClube) }
    var enderecoSede by remember(configAtual.enderecoSede) { mutableStateOf(configAtual.enderecoSede) }

    // Dados PIX & Mensalidade
    var chavePix by remember(configAtual.chavePix) { mutableStateOf(configAtual.chavePix) }
    var tipoChavePix by remember(configAtual.tipoChavePix) { mutableStateOf(configAtual.tipoChavePix) }
    var favorecidoPix by remember(configAtual.favorecidoPix) { mutableStateOf(configAtual.favorecidoPix) }
    var bancoPix by remember(configAtual.bancoPix) { mutableStateOf(configAtual.bancoPix) }
    var cidadePix by remember(configAtual.cidadePix) { mutableStateOf(configAtual.cidadePix) }
    var valorMensalidadeTexto by remember(configAtual.valorMensalidadePadrao) {
        mutableStateOf(String.format(Locale.US, "%.2f", configAtual.valorMensalidadePadrao))
    }
    var diaVencimentoTexto by remember(configAtual.diaVencimentoPadrao) {
        mutableStateOf(configAtual.diaVencimentoPadrao.toString())
    }

    // Photo Picker nativo (zero permissões de armazenamento) para Logo Customizado do Clube
    val pickLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Algumas fontes de URI temporárias não exigem persistência explícita
            }
            logoUri = uri.toString()
            Toast.makeText(context, "Logo da galeria selecionado! Toque em Salvar para aplicar no app.", Toast.LENGTH_SHORT).show()
        }
    }

    val previewConfig = ConfiguracaoIdentidadeClube(
        id = configAtual.id.ifBlank { "identidade_oficial" },
        userId = configAtual.userId,
        nomeClube = nomeClube.ifBlank { "IRON SIDE • ROAD RIDER MC" },
        subtituloClube = subtituloClube.ifBlank { "DIVISÃO NACIONAL BRASIL" },
        lemaClube = lemaClube.ifBlank { "Os melhores são de ferro" },
        cnpjClube = cnpjClube.ifBlank { "48.120.990/0001-15" },
        sedeCidadeEstado = sedeCidadeEstado.ifBlank { "São Paulo - SP" },
        logoPreset = logoPreset,
        logoUri = logoUri,
        corPrimariaHex = corPrimariaHex.ifBlank { "#FF6D00" },
        corSecundariaHex = corSecundariaHex.ifBlank { "#FFB74D" },
        corDestaqueSosHex = corDestaqueSosHex.ifBlank { "#EF5350" },
        corFundoHex = corFundoHex.ifBlank { "#0E1013" },
        corSuperficieHex = corSuperficieHex.ifBlank { "#16191E" },
        presetPaleta = presetPaleta,
        emailContato = emailContato,
        telefoneContato = telefoneContato,
        whatsappClube = whatsappClube,
        instagramClube = instagramClube,
        siteClube = siteClube,
        enderecoSede = enderecoSede,
        chavePix = chavePix.ifBlank { "financeiro@ironsidemotoclube.com.br" },
        tipoChavePix = tipoChavePix,
        favorecidoPix = favorecidoPix,
        bancoPix = bancoPix,
        cidadePix = cidadePix,
        valorMensalidadePadrao = valorMensalidadeTexto.replace(",", ".").toDoubleOrNull() ?: 50.0,
        diaVencimentoPadrao = diaVencimentoTexto.filter { it.isDigit() }.toLongOrNull()?.coerceIn(1L, 31L) ?: 15L,
        ativo = true,
        createdAt = configAtual.createdAt
    )

    val previewPrimaryColor = parseHexColorSafe(corPrimariaHex, IronPrimary)
    val previewSecondaryColor = parseHexColorSafe(corSecundariaHex, IronSecondary)
    val previewSosColor = parseHexColorSafe(corDestaqueSosHex, IronTertiary)
    val previewSurfaceColor = parseHexColorSafe(corSuperficieHex, IronSurface)
    val previewBgColor = parseHexColorSafe(corFundoHex, IronBackground)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("configuracao_identidade_clube_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cabeçalho + Pré-Visualização Dinâmica em Tempo Real
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = previewSurfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, previewPrimaryColor, RoundedCornerShape(22.dp))
                    .testTag("identidade_live_preview_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    previewBgColor,
                                    previewSurfaceColor,
                                    previewPrimaryColor.copy(alpha = 0.18f)
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = previewPrimaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "CONFIGURAÇÕES DE IDENTIDADE DO CLUBE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = previewPrimaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Logo, Cores, Dados de Contato e PIX aplicados dinamicamente em todo o app",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = StatusGreen.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, StatusGreenLight, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "DINÂMICO • FIRESTORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusGreenLight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Simulação em tempo real do Cabeçalho / Carteirinha / PIX com as cores e logo escolhidos
                    Surface(
                        color = previewBgColor.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, previewSecondaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IronSideLogoBadge(
                                    size = 64.dp,
                                    overrideConfig = previewConfig
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = previewConfig.nomeClube,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = previewPrimaryColor,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${previewConfig.lemaClube} • ${previewConfig.subtituloClube}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = previewSecondaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "CNPJ: ${previewConfig.cnpjClube} • Sede: ${previewConfig.sedeCidadeEstado}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            HorizontalDivider(color = previewPrimaryColor.copy(alpha = 0.25f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PIX (${previewConfig.tipoChavePix}): ${previewConfig.chavePix}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Mensalidade Padrão: R$ ${String.format(Locale.getDefault(), "%.2f", previewConfig.valorMensalidadePadrao)} (Venc. dia ${previewConfig.diaVencimentoPadrao})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = previewSecondaryColor
                                    )
                                }
                                Surface(
                                    color = previewSosColor,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "SOS • ${previewConfig.whatsappClube}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Botões de Ação Principal no Topo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onSalvarConfiguracao(previewConfig) },
                            colors = ButtonDefaults.buttonColors(containerColor = previewPrimaryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("salvar_identidade_clube_top_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar e Aplicar Identidade", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val padrao = ConfiguracaoIdentidadeClube()
                                nomeClube = padrao.nomeClube
                                subtituloClube = padrao.subtituloClube
                                lemaClube = padrao.lemaClube
                                cnpjClube = padrao.cnpjClube
                                sedeCidadeEstado = padrao.sedeCidadeEstado
                                logoPreset = padrao.logoPreset
                                logoUri = ""
                                presetPaleta = padrao.presetPaleta
                                corPrimariaHex = padrao.corPrimariaHex
                                corSecundariaHex = padrao.corSecundariaHex
                                corDestaqueSosHex = padrao.corDestaqueSosHex
                                corFundoHex = padrao.corFundoHex
                                corSuperficieHex = padrao.corSuperficieHex
                                emailContato = padrao.emailContato
                                telefoneContato = padrao.telefoneContato
                                whatsappClube = padrao.whatsappClube
                                instagramClube = padrao.instagramClube
                                siteClube = padrao.siteClube
                                enderecoSede = padrao.enderecoSede
                                chavePix = padrao.chavePix
                                tipoChavePix = padrao.tipoChavePix
                                favorecidoPix = padrao.favorecidoPix
                                bancoPix = padrao.bancoPix
                                cidadePix = padrao.cidadePix
                                valorMensalidadeTexto = String.format(Locale.US, "%.2f", padrao.valorMensalidadePadrao)
                                diaVencimentoTexto = padrao.diaVencimentoPadrao.toString()
                                onSalvarConfiguracao(padrao)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("restaurar_padrao_identidade_btn")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = "Restaurar Padrão")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Padrão")
                        }
                    }
                }
            }
        }

        // 2. SEÇÃO 1: LOGO, BRASÃO & NOME DO MOTO CLUBE
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "1. LOGO, BRASÃO & IDENTIFICAÇÃO OFICIAL",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Escolha um estilo de brasão ou envie o logo próprio da galeria",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                pickLogoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("selecionar_logo_galeria_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecionar Logo da Galeria")
                        }

                        if (logoUri.isNotBlank()) {
                            OutlinedButton(
                                onClick = { logoUri = "" },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Usar Brasão")
                            }
                        }
                    }

                    Text(
                        text = "Estilos de Brasão Pré-Definidos:",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(LOGO_PRESET_OPTIONS) { preset ->
                            FilterChip(
                                selected = logoUri.isBlank() && logoPreset == preset.id,
                                onClick = {
                                    logoUri = ""
                                    logoPreset = preset.id
                                },
                                label = { Text(preset.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = nomeClube,
                        onValueChange = { nomeClube = it },
                        label = { Text("Nome Oficial do Moto Clube (Exibido no Topo e Carteirinha)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_nome_clube"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = subtituloClube,
                            onValueChange = { subtituloClube = it },
                            label = { Text("Subtítulo / Divisão") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = lemaClube,
                            onValueChange = { lemaClube = it },
                            label = { Text("Lema Oficial do Clube") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_lema_clube"),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cnpjClube,
                            onValueChange = { cnpjClube = it },
                            label = { Text("CNPJ Oficial do Clube") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sedeCidadeEstado,
                            onValueChange = { sedeCidadeEstado = it },
                            label = { Text("Cidade / UF da Sede") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // 3. SEÇÃO 2: PALETA DE CORES DINÂMICA DO CLUBE
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "2. CORES DO TEMA DINÂMICO DO CLUBE",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Altera instantaneamente botões, destaques, abas, gráficos e cartões do app",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Temas Rápidos de Moto Clube (1-Toque):",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(PALETAS_IDENTIDADE_CLUBE) { paleta ->
                            val isSelected = presetPaleta == paleta.nome
                            val primarySwatch = parseHexColorSafe(paleta.primariaHex, IronPrimary)
                            val secondarySwatch = parseHexColorSafe(paleta.secundariaHex, IronSecondary)

                            Surface(
                                color = if (isSelected) primarySwatch.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .width(210.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) primarySwatch else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        presetPaleta = paleta.nome
                                        corPrimariaHex = paleta.primariaHex
                                        corSecundariaHex = paleta.secundariaHex
                                        corDestaqueSosHex = paleta.sosHex
                                        corFundoHex = paleta.fundoHex
                                        corSuperficieHex = paleta.superficieHex
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(primarySwatch)
                                                .border(1.dp, Color.White, CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(secondarySwatch)
                                                .border(1.dp, Color.White, CircleShape)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = primarySwatch,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = paleta.nome,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = paleta.descricao,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ColorHexInputBox(
                            modifier = Modifier.weight(1f),
                            label = "Cor Primária (#HEX)",
                            hexValue = corPrimariaHex,
                            previewColor = previewPrimaryColor,
                            onHexChange = {
                                corPrimariaHex = it
                                presetPaleta = "Personalizada"
                            },
                            testTag = "input_cor_primaria_hex"
                        )
                        ColorHexInputBox(
                            modifier = Modifier.weight(1f),
                            label = "Cor Secundária (#HEX)",
                            hexValue = corSecundariaHex,
                            previewColor = previewSecondaryColor,
                            onHexChange = {
                                corSecundariaHex = it
                                presetPaleta = "Personalizada"
                            },
                            testTag = "input_cor_secundaria_hex"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ColorHexInputBox(
                            modifier = Modifier.weight(1f),
                            label = "Cor SOS Emergência",
                            hexValue = corDestaqueSosHex,
                            previewColor = previewSosColor,
                            onHexChange = { corDestaqueSosHex = it },
                            testTag = "input_cor_sos_hex"
                        )
                        ColorHexInputBox(
                            modifier = Modifier.weight(1f),
                            label = "Cor Fundo (#HEX)",
                            hexValue = corFundoHex,
                            previewColor = previewBgColor,
                            onHexChange = { corFundoHex = it },
                            testTag = "input_cor_fundo_hex"
                        )
                        ColorHexInputBox(
                            modifier = Modifier.weight(1f),
                            label = "Cor Cards (#HEX)",
                            hexValue = corSuperficieHex,
                            previewColor = previewSurfaceColor,
                            onHexChange = { corSuperficieHex = it },
                            testTag = "input_cor_superficie_hex"
                        )
                    }
                }
            }
        }

        // 4. SEÇÃO 3: DADOS DE CONTATO E SEDE DO CLUBE
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "3. DADOS DE CONTATO OFICIAIS & REDES SOCIAIS",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Exibidos no rodapé da Carteirinha Digital, central SOS e canais de atendimento",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = telefoneContato,
                            onValueChange = { telefoneContato = it },
                            label = { Text("Telefone Oficial Sede") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_telefone_clube"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsappClube,
                            onValueChange = { whatsappClube = it },
                            label = { Text("WhatsApp Diretoria / SOS") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_whatsapp_clube"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = emailContato,
                        onValueChange = { emailContato = it },
                        label = { Text("E-mail Oficial da Secretaria / Diretoria") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_email_clube"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = instagramClube,
                            onValueChange = { instagramClube = it },
                            label = { Text("Instagram Oficial") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = siteClube,
                            onValueChange = { siteClube = it },
                            label = { Text("Site / Portal do Clube") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = enderecoSede,
                        onValueChange = { enderecoSede = it },
                        label = { Text("Endereço Completo da Sede / Ponto de Encontro Oficial") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botões rápidos para testar os canais de contato configurados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val num = whatsappClube.filter { it.isDigit() }
                                if (num.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                                    context.startActivity(intent)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testar Ligação Sede")
                        }

                        OutlinedButton(
                            onClick = {
                                clipboard.setText(
                                    AnnotatedString(
                                        "${previewConfig.nomeClube}\nTel/WhatsApp: $whatsappClube\nE-mail: $emailContato\nSede: $enderecoSede"
                                    )
                                )
                                Toast.makeText(context, "Dados de contato do clube copiados!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Contatos")
                        }
                    }
                }
            }
        }

        // 5. SEÇÃO 4: PARÂMETROS PIX & TESOURARIA DINÂMICA
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = StatusGreenLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "4. CONFIGURAÇÃO DE CHAVE PIX & MENSALIDADE PADRÃO",
                                style = MaterialTheme.typography.titleMedium,
                                color = StatusGreenLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Utilizado automaticamente na geração de cobranças, QR Code PIX e Dashboard Financeiro",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Tipo da Chave PIX:",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val tiposPix = listOf("E-mail", "CNPJ", "Celular", "Chave Aleatória")
                        items(tiposPix) { tipo ->
                            FilterChip(
                                selected = tipoChavePix == tipo,
                                onClick = { tipoChavePix = tipo },
                                label = { Text(tipo) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = chavePix,
                        onValueChange = { chavePix = it },
                        label = { Text("Chave PIX Oficial do Moto Clube") },
                        leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(chavePix))
                                    Toast.makeText(context, "Chave PIX copiada!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar PIX")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_chave_pix_clube"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = favorecidoPix,
                            onValueChange = { favorecidoPix = it },
                            label = { Text("Favorecido / Recebedor PIX") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bancoPix,
                            onValueChange = { bancoPix = it },
                            label = { Text("Banco / Instituição") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = valorMensalidadeTexto,
                            onValueChange = { valorMensalidadeTexto = it },
                            label = { Text("Mensalidade Padrão (R$)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_valor_mensalidade_padrao"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = diaVencimentoTexto,
                            onValueChange = { diaVencimentoTexto = it },
                            label = { Text("Dia Vencimento (1-31)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cidadePix,
                            onValueChange = { cidadePix = it },
                            label = { Text("Cidade PIX") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = { onSalvarConfiguracao(previewConfig) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("salvar_identidade_clube_bottom_btn")
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SALVAR CONFIGURAÇÕES DE IDENTIDADE NO FIRESTORE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorHexInputBox(
    modifier: Modifier = Modifier,
    label: String,
    hexValue: String,
    previewColor: Color,
    onHexChange: (String) -> Unit,
    testTag: String
) {
    OutlinedTextField(
        value = hexValue,
        onValueChange = onHexChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        },
        modifier = modifier.testTag(testTag),
        singleLine = true
    )
}
