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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.Abastecimento
import com.example.data.Manutencao
import com.example.data.Veiculo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

@Composable
fun GaragemPillarScreen(
    veiculos: List<Veiculo>,
    manutencoes: List<Manutencao>,
    abastecimentos: List<Abastecimento>,
    initialTab: Int = 0,
    onCadastrarVeiculo: (Veiculo) -> Unit,
    onRemoverVeiculo: (String) -> Unit,
    onRegistrarManutencao: (Manutencao) -> Unit,
    onRemoverManutencao: (String) -> Unit = {},
    onRegistrarAbastecimento: (Abastecimento) -> Unit,
    onIrParaGuinchoProximo: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        "🏍️ Meus Veículos",
        "🔧 Manutenção",
        "⛽ Abastecimento",
        "🛡️ Seguros & Assistência",
        "📈 Relatório & Custos"
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
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("garagem_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> VeiculosSection(
                veiculos = veiculos,
                manutencoes = manutencoes,
                abastecimentos = abastecimentos,
                onCadastrarVeiculo = onCadastrarVeiculo,
                onRemoverVeiculo = onRemoverVeiculo
            )
            1 -> ManutencaoSection(
                veiculos = veiculos,
                manutencoes = manutencoes,
                onRegistrarManutencao = onRegistrarManutencao,
                onRemoverManutencao = onRemoverManutencao
            )
            2 -> AbastecimentoSection(
                veiculos = veiculos,
                abastecimentos = abastecimentos,
                onRegistrarAbastecimento = onRegistrarAbastecimento
            )
            3 -> SegurosSection(
                veiculos = veiculos,
                onIrParaGuinchoProximo = onIrParaGuinchoProximo
            )
            4 -> RelatorioVeiculoSection(
                veiculos = veiculos,
                manutencoes = manutencoes,
                abastecimentos = abastecimentos
            )
        }
    }
}

@Composable
private fun VeiculosSection(
    veiculos: List<Veiculo>,
    manutencoes: List<Manutencao>,
    abastecimentos: List<Abastecimento>,
    onCadastrarVeiculo: (Veiculo) -> Unit,
    onRemoverVeiculo: (String) -> Unit
) {
    val context = LocalContext.current
    var mostrarModalNovoVeiculo by remember { mutableStateOf(false) }
    var veiculoEmEdicao by remember { mutableStateOf<Veiculo?>(null) }

    val kmAtualPrincipal = veiculos.firstOrNull()?.kmAtual ?: 0L
    val gastoCombustivel = abastecimentos.sumOf { it.valorTotal }
    val gastoManutencao = manutencoes.sumOf { it.valor }
    val gastoTotal = gastoCombustivel + gastoManutencao
    val tanquesCheios = abastecimentos.filter { it.tanqueCheio && it.consumoCalculado > 0.0 }
    val consumoMedio = if (tanquesCheios.isNotEmpty()) tanquesCheios.map { it.consumoCalculado }.average() else 18.5
    val custoPorKm = if (kmAtualPrincipal > 0) gastoTotal / kmAtualPrincipal.toDouble() else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Painel do Veículo consolidado (Item 16 da especificação)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IronSideLogoBadge(size = 48.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("GARAGEM IRON SIDE", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                Text("Foto da Moto + Documento Completo (CRLV-e)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = {
                                veiculoEmEdicao = null
                                mostrarModalNovoVeiculo = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("novo_veiculo_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Veículo")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "KM ATUAL",
                            value = "$kmAtualPrincipal km",
                            sub = "KM Mensal: ~650 km"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "CONSUMO MÉDIO",
                            value = String.format("%.2f km/L", consumoMedio),
                            sub = "Cálculo Tanque Cheio"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "COMBUSTÍVEL + MANUT.",
                            value = "R$ ${String.format("%.2f", gastoTotal)}",
                            sub = "Comb: R$ ${String.format("%.0f", gastoCombustivel)} | Manut: R$ ${String.format("%.0f", gastoManutencao)}"
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            label = "CUSTO POR KM",
                            value = "R$ ${String.format("%.2f", custoPorKm)}/km",
                            sub = "Próx. Revisão: ${manutencoes.firstOrNull()?.proximoKm ?: 18000} km"
                        )
                    }
                }
            }
        }

        items(veiculos) { v ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${v.marca} ${v.modelo}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "${v.tipo} • Fab/Mod: ${if (v.anoFabricacao > 0) "${v.anoFabricacao}/${v.ano}" else "${v.ano}"} • Cor: ${v.cor}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = v.placa,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Foto da Motocicleta
                    MotorcyclePhotoBanner(
                        fotoUri = v.fotoUri,
                        descricaoMoto = "${v.marca} ${v.modelo} (${v.placa})",
                        height = 148.dp
                    )

                    // Resumo Técnico + Dados do Documento do Veículo (CRLV / CRV)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📄 DADOS DO DOCUMENTO DO VEÍCULO (CRLV-e / CRV)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• RENAVAM: ${v.renavam.ifBlank { "---" }} • Chassi: ${v.chassi.ifBlank { "---" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                            Text(
                                text = "• Nº Motor: ${v.numeroMotor.ifBlank { "---" }} • Nº CRV/CRLV: ${v.numeroCrvCrlv.ifBlank { "---" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Exercício Licenc.: ${v.exercicioLicenciamento.ifBlank { "2026" }} (${v.status.ifBlank { "Ativo" }}) • UF/Município: ${v.municipioEmplacamento.ifBlank { "São Paulo" }}/${v.ufEmplacamento.ifBlank { "SP" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusGreenLight
                            )
                            Text(
                                text = "• Espécie/Categ.: ${v.especieTipoDoc.ifBlank { "Passageiro / Motocicleta" }} (${v.categoriaDoc.ifBlank { "Particular" }}) • Combustível: ${v.combustivelDoc.ifBlank { "Gasolina" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Proprietário no Doc: ${v.nomeProprietarioDoc.ifBlank { "Associado Titular" }} (CPF/CNPJ: ${v.cpfCnpjProprietarioDoc.ifBlank { "---" }})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "• Emissão CRLV: ${v.dataEmissaoCrv.ifBlank { "10/01/2026" }} • Obs/Restrições: ${v.observacoes.ifBlank { "SEM RESTRIÇÕES" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Hodômetro: ${v.kmAtual} km • ${v.cilindrada} cc • ${v.potenciaCilindradaDoc.ifBlank { "---" }} • ${v.cilindros} Cilindros • ${v.transmissao}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (v.seguradora.isNotBlank()) {
                        Text(
                            text = "🛡️ Seguro: ${v.seguradora} (Apólice ${v.apolice}) • Guincho ${v.seguroKmGuincho} km",
                            style = MaterialTheme.typography.labelLarge,
                            color = StatusGreenLight
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                veiculoEmEdicao = v
                                mostrarModalNovoVeiculo = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("editar_veiculo_doc_btn_${v.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Foto & Documento CRLV")
                        }
                        IconButton(onClick = { onRemoverVeiculo(v.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover Veículo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (mostrarModalNovoVeiculo) {
        val editando = veiculoEmEdicao
        val tipos = listOf("Moto", "Scooter", "Triciclo", "Custom", "Big Trail", "Sport", "Touring")
        var tipo by remember(editando) { mutableStateOf(editando?.tipo ?: "Moto") }
        var fotoUri by remember(editando) {
            mutableStateOf(editando?.fotoUri ?: "android.resource://com.example/drawable/hero_chopper_banner")
        }
        var marca by remember(editando) { mutableStateOf(editando?.marca ?: "Triumph") }
        var modelo by remember(editando) { mutableStateOf(editando?.modelo ?: "Tiger 900 Rally Pro") }
        var anoFabricacao by remember(editando) { mutableStateOf((editando?.anoFabricacao ?: 2024L).toString()) }
        var ano by remember(editando) { mutableStateOf((editando?.ano ?: 2025L).toString()) }
        var cor by remember(editando) { mutableStateOf(editando?.cor ?: "Preto Fosco") }
        var placa by remember(editando) { mutableStateOf(editando?.placa ?: "MTC9A25") }
        var kmAtual by remember(editando) { mutableStateOf((editando?.kmAtual ?: 4200L).toString()) }
        var cilindrada by remember(editando) { mutableStateOf((editando?.cilindrada ?: 888L).toString()) }
        var potenciaCv by remember(editando) { mutableStateOf(editando?.potenciaCilindradaDoc ?: "95 CV / 888 cc") }
        var combustivelDoc by remember(editando) { mutableStateOf(editando?.combustivelDoc ?: "Gasolina") }
        var renavam by remember(editando) { mutableStateOf(editando?.renavam ?: "01294857361") }
        var chassi by remember(editando) { mutableStateOf(editando?.chassi ?: "9C2KC1670PR004821") }
        var numeroMotor by remember(editando) { mutableStateOf(editando?.numeroMotor ?: "TR900E-7748291") }
        var numeroCrv by remember(editando) { mutableStateOf(editando?.numeroCrvCrlv ?: "CRLV-2026-99481203") }
        var exercicioLicenciamento by remember(editando) { mutableStateOf(editando?.exercicioLicenciamento ?: "2026") }
        var dataEmissaoCrv by remember(editando) { mutableStateOf(editando?.dataEmissaoCrv ?: "15/01/2026") }
        var categoriaDocumento by remember(editando) { mutableStateOf(editando?.categoriaDoc ?: "Particular") }
        var especieTipo by remember(editando) { mutableStateOf(editando?.especieTipoDoc ?: "Passageiro / Motocicleta") }
        var nomeProprietarioDoc by remember(editando) { mutableStateOf(editando?.nomeProprietarioDoc ?: "Carlos Eduardo Rocha") }
        var cpfCnpjProprietario by remember(editando) { mutableStateOf(editando?.cpfCnpjProprietarioDoc ?: "348.921.608-14") }
        var ufEmplacamento by remember(editando) { mutableStateOf(editando?.ufEmplacamento ?: "SP") }
        var municipioEmplacamento by remember(editando) { mutableStateOf(editando?.municipioEmplacamento ?: "São Paulo") }
        var statusLicenciamento by remember(editando) { mutableStateOf(editando?.status ?: "Ativo") }
        var restricoesDoc by remember(editando) { mutableStateOf(editando?.observacoes ?: "CRLV-e Digital regularizado • Sem restrições") }
        var seguradora by remember(editando) { mutableStateOf(editando?.seguradora ?: "Suhai Seguradora") }
        var apolice by remember(editando) { mutableStateOf(editando?.apolice ?: "AP-2026-9910") }
        var telefoneSeguro by remember(editando) { mutableStateOf(editando?.seguroTelefoneAssistencia ?: "0800 327 8424") }

        val motoPhotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
                fotoUri = uri.toString()
            }
        }

        AlertDialog(
            onDismissRequest = { mostrarModalNovoVeiculo = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (marca.isNotBlank() && modelo.isNotBlank() && placa.isNotBlank()) {
                            onCadastrarVeiculo(
                                Veiculo(
                                    id = editando?.id ?: "",
                                    userId = editando?.userId ?: "",
                                    associadoId = editando?.associadoId ?: "",
                                    associadoCpf = editando?.associadoCpf ?: cpfCnpjProprietario,
                                    status = statusLicenciamento,
                                    fotoUri = fotoUri,
                                    tipo = tipo,
                                    marca = marca,
                                    modelo = modelo,
                                    anoFabricacao = anoFabricacao.toLongOrNull() ?: 2024L,
                                    ano = ano.toLongOrNull() ?: 2025L,
                                    anoModelo = ano.toLongOrNull() ?: 2025L,
                                    cor = cor,
                                    placa = placa.uppercase(),
                                    renavam = renavam,
                                    chassi = chassi.uppercase(),
                                    numeroMotor = numeroMotor.uppercase(),
                                    numeroCrvCrlv = numeroCrv.uppercase(),
                                    exercicioLicenciamento = exercicioLicenciamento,
                                    dataEmissaoCrv = dataEmissaoCrv,
                                    categoriaDoc = categoriaDocumento,
                                    especieTipoDoc = especieTipo,
                                    nomeProprietarioDoc = nomeProprietarioDoc,
                                    cpfCnpjProprietarioDoc = cpfCnpjProprietario,
                                    ufEmplacamento = ufEmplacamento.uppercase(),
                                    municipioEmplacamento = municipioEmplacamento,
                                    observacoes = restricoesDoc,
                                    combustivelDoc = combustivelDoc,
                                    kmAtual = kmAtual.toLongOrNull() ?: 0L,
                                    cilindrada = cilindrada.toLongOrNull() ?: 888L,
                                    potenciaCilindradaDoc = potenciaCv,
                                    cilindros = editando?.cilindros ?: 3L,
                                    transmissao = editando?.transmissao ?: "6 Marchas / Corrente",
                                    seguradora = seguradora,
                                    apolice = apolice,
                                    seguroVigenciaFim = editando?.seguroVigenciaFim ?: "10/10/2027",
                                    seguroTelefoneAssistencia = telefoneSeguro,
                                    seguroKmGuincho = editando?.seguroKmGuincho ?: 500L,
                                    ativo = true
                                )
                            )
                            mostrarModalNovoVeiculo = false
                        }
                    }
                ) {
                    Text("Salvar Foto & Documento CRLV")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalNovoVeiculo = false }) { Text("Cancelar") }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IronSideLogoBadge(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (editando != null) "Editar Foto e Documento da Moto" else "Cadastrar Moto & Documento CRLV")
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(460.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        MotorcyclePhotoBanner(
                            fotoUri = fotoUri,
                            descricaoMoto = "$marca $modelo ($placa)",
                            height = 125.dp
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    motoPhotoLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("selecionar_foto_moto_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Foto da Moto")
                            }
                            OutlinedButton(
                                onClick = {
                                    fotoUri = "preset:moto_custom_ironside"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Foto Padrão")
                            }
                        }
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(tipos) { t ->
                                FilterChip(
                                    selected = tipo == t,
                                    onClick = { tipo = t },
                                    label = { Text(t) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(value = marca, onValueChange = { marca = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = modelo, onValueChange = { modelo = it }, label = { Text("Modelo / Versão") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = anoFabricacao, onValueChange = { anoFabricacao = it }, label = { Text("Ano Fab.") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = ano, onValueChange = { ano = it }, label = { Text("Ano Mod.") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = placa, onValueChange = { placa = it }, label = { Text("Placa") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = cor, onValueChange = { cor = it }, label = { Text("Cor Predominante") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = combustivelDoc, onValueChange = { combustivelDoc = it }, label = { Text("Combustível") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = kmAtual, onValueChange = { kmAtual = it }, label = { Text("KM Atual") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = cilindrada, onValueChange = { cilindrada = it }, label = { Text("Cilindrada (cc)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = potenciaCv, onValueChange = { potenciaCv = it }, label = { Text("Potência/Cil") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Text(
                            text = "DADOS DO DOCUMENTO DO VEÍCULO (CRLV-e / CRV)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = renavam, onValueChange = { renavam = it }, label = { Text("Código RENAVAM") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = chassi, onValueChange = { chassi = it }, label = { Text("Chassi (VIN)") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = numeroMotor, onValueChange = { numeroMotor = it }, label = { Text("Nº do Motor") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = numeroCrv, onValueChange = { numeroCrv = it }, label = { Text("Nº CRV / CRLV-e") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = exercicioLicenciamento, onValueChange = { exercicioLicenciamento = it }, label = { Text("Exercício Licenc.") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = dataEmissaoCrv, onValueChange = { dataEmissaoCrv = it }, label = { Text("Data Emissão CRLV") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = categoriaDocumento, onValueChange = { categoriaDocumento = it }, label = { Text("Categoria (Particular)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = especieTipo, onValueChange = { especieTipo = it }, label = { Text("Espécie / Tipo") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = nomeProprietarioDoc, onValueChange = { nomeProprietarioDoc = it }, label = { Text("Nome do Proprietário no Doc") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = cpfCnpjProprietario, onValueChange = { cpfCnpjProprietario = it }, label = { Text("CPF/CNPJ Proprietário") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = municipioEmplacamento, onValueChange = { municipioEmplacamento = it }, label = { Text("Município Emplacamento") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = ufEmplacamento, onValueChange = { ufEmplacamento = it }, label = { Text("UF") }, modifier = Modifier.width(90.dp))
                        }
                    }
                    item {
                        OutlinedTextField(value = restricoesDoc, onValueChange = { restricoesDoc = it }, label = { Text("Observações / Restrições / Alienação") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = seguradora, onValueChange = { seguradora = it }, label = { Text("Seguradora") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = apolice, onValueChange = { apolice = it }, label = { Text("Nº Apólice") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ManutencaoSection(
    veiculos: List<Veiculo>,
    manutencoes: List<Manutencao>,
    onRegistrarManutencao: (Manutencao) -> Unit,
    onRemoverManutencao: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tiposManutencao = listOf(
        "Troca de óleo",
        "Pneu",
        "Pastilha de freio",
        "Filtro de óleo",
        "Filtro de ar",
        "Fluido de freio",
        "Vela",
        "Bateria",
        "Corrente / Relação",
        "Correia",
        "Suspensão",
        "Preventiva",
        "Corretiva",
        "Elétrica"
    )

    // Estado de correção de registro existente (se não nulo, o formulário corrige este registro)
    var manutencaoEmCorrecao by remember { mutableStateOf<Manutencao?>(null) }
    var mostrarSeletorCorrecaoDialog by remember { mutableStateOf(false) }

    var veiculoSelecionadoIdx by remember { mutableIntStateOf(0) }
    val veiculoAtivo = veiculos.getOrNull(veiculoSelecionadoIdx) ?: veiculos.firstOrNull()

    var tipoSel by remember { mutableStateOf("Troca de óleo") }
    var dataOcorrencia by remember { mutableStateOf("10/10/2026") }
    var kmOcorrencia by remember(veiculoAtivo?.kmAtual) {
        mutableStateOf((veiculoAtivo?.kmAtual ?: 15000L).toString())
    }
    var proximoKm by remember(veiculoAtivo?.kmAtual) {
        mutableStateOf(((veiculoAtivo?.kmAtual ?: 15000L) + 5000L).toString())
    }
    var valorInput by remember { mutableStateOf("420.00") }
    var oficinaInput by remember { mutableStateOf("Iron Side Custom Garage") }
    var numeroNotaFiscal by remember { mutableStateOf("NF-5021") }
    var descricaoInput by remember { mutableStateOf("Revisão periódica e inspeção de segurança") }
    var statusAlerta by remember { mutableStateOf("Em dia") }

    // Lista de URIs das fotos da Nota Fiscal selecionadas via Android Photo Picker
    val fotosNotaFiscalUris = remember { mutableStateListOf<String>() }

    // Função auxiliar para carregar um registro de manutenção no formulário para correção
    val carregarParaCorrecao: (Manutencao) -> Unit = { reg ->
        manutencaoEmCorrecao = reg
        val vIdx = veiculos.indexOfFirst { it.id == reg.veiculoId }
        if (vIdx >= 0) veiculoSelecionadoIdx = vIdx
        tipoSel = reg.tipo.ifBlank { "Troca de óleo" }
        dataOcorrencia = reg.data.ifBlank { "10/10/2026" }
        kmOcorrencia = reg.km.toString()
        proximoKm = reg.proximoKm.toString()
        valorInput = String.format(java.util.Locale.US, "%.2f", reg.valor)
        oficinaInput = reg.oficina
        numeroNotaFiscal = reg.ordemServico.substringBefore(" (").trim()
        descricaoInput = reg.descricao
        statusAlerta = reg.statusAlerta.ifBlank { "Em dia" }
        fotosNotaFiscalUris.clear()
        if (reg.observacoes.contains("NF Anexada")) {
            reg.observacoes.substringAfter("NF Anexada", "")
                .substringAfter(":", "")
                .split(";")
                .map { it.trim() }
                .filter { it.startsWith("content://") || it.startsWith("file://") || it.startsWith("android.resource://") }
                .forEach { fotosNotaFiscalUris.add(it) }
        }
        Toast.makeText(
            context,
            "Modo Correção ativado para: ${reg.tipo} (${reg.data})",
            Toast.LENGTH_SHORT
        ).show()
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val uriStr = uri.toString()
                if (!fotosNotaFiscalUris.contains(uriStr)) {
                    fotosNotaFiscalUris.add(uriStr)
                }
            }
            Toast.makeText(
                context,
                "${uris.size} foto(s) da Nota Fiscal anexada(s)!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriStr = uri.toString()
            if (!fotosNotaFiscalUris.contains(uriStr)) {
                fotosNotaFiscalUris.add(uriStr)
            }
            Toast.makeText(
                context,
                "Foto da Nota Fiscal anexada!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("manutencao_section_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Formulário Completo de Registro de Manutenção
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("formulario_registro_manutencao_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IronSideFormHeader(
                        title = if (manutencaoEmCorrecao != null) {
                            "CORREÇÃO DE REGISTRO DE MANUTENÇÃO"
                        } else {
                            "FORMULÁRIO DE REGISTRO DE MANUTENÇÃO"
                        },
                        subtitle = if (manutencaoEmCorrecao != null) {
                            "Corrigindo registro: ${manutencaoEmCorrecao?.tipo} • ${manutencaoEmCorrecao?.data} (${manutencaoEmCorrecao?.km} km)"
                        } else {
                            "Seleção de tipo, KM da ocorrência, data, valor e fotos da Nota Fiscal"
                        }
                    )

                    // Barra de Ação Rápida: Botão de Correção de Registro
                    Surface(
                        color = if (manutencaoEmCorrecao != null) {
                            StatusYellow.copy(alpha = 0.18f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (manutencaoEmCorrecao != null) StatusYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
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
                                    text = if (manutencaoEmCorrecao != null) {
                                        "✏️ MODO CORREÇÃO DE REGISTRO ATIVO"
                                    } else {
                                        "Digitou KM, valor, data ou NF errada?"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (manutencaoEmCorrecao != null) StatusYellow else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (manutencaoEmCorrecao != null) {
                                        "Altere os campos abaixo e toque em Salvar Correção"
                                    } else {
                                        "Use o botão ao lado para corrigir um registro já salvo"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (manutencaoEmCorrecao != null) {
                                OutlinedButton(
                                    onClick = {
                                        manutencaoEmCorrecao = null
                                        fotosNotaFiscalUris.clear()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("cancelar_correcao_manutencao_btn")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cancelar Correção")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (manutencoes.isNotEmpty()) {
                                            mostrarSeletorCorrecaoDialog = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Nenhum registro anterior para corrigir.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatusYellow,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("botao_correcao_registro_manutencao")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Correção de Registro", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Seleção do Veículo
                    if (veiculos.isNotEmpty()) {
                        Text(
                            text = "Veículo da Garagem:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(veiculos.indices.toList()) { idx ->
                                val v = veiculos[idx]
                                FilterChip(
                                    selected = veiculoSelecionadoIdx == idx,
                                    onClick = { veiculoSelecionadoIdx = idx },
                                    label = { Text("${v.marca} ${v.modelo} (${v.placa})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    modifier = Modifier.testTag("manut_veiculo_chip_$idx")
                                )
                            }
                        }
                    }

                    // 1. Seleção de Tipo de Manutenção (Troca de óleo, Pneu, Pastilha, etc.)
                    Text(
                        text = "1. Tipo de Manutenção (Toque para selecionar):",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("manutencao_tipo_selector_row")
                    ) {
                        items(tiposManutencao) { t ->
                            FilterChip(
                                selected = tipoSel == t,
                                onClick = { tipoSel = t },
                                label = { Text(t) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("manut_tipo_${t.lowercase().replace(" ", "_")}")
                            )
                        }
                    }

                    // 2. KM da Ocorrência, Data e Valor
                    Text(
                        text = "2. KM da Ocorrência, Data e Valor:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = kmOcorrencia,
                            onValueChange = { kmOcorrencia = it },
                            label = { Text("KM da Ocorrência *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manutencao_km_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dataOcorrencia,
                            onValueChange = { dataOcorrencia = it },
                            label = { Text("Data (DD/MM/AAAA) *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manutencao_data_input"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = valorInput,
                            onValueChange = { valorInput = it },
                            label = { Text("Valor Total (R$) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manutencao_valor_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = proximoKm,
                            onValueChange = { proximoKm = it },
                            label = { Text("Próxima Revisão (KM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manutencao_proximo_km_input"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = oficinaInput,
                            onValueChange = { oficinaInput = it },
                            label = { Text("Oficina / Mecânico") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manutencao_oficina_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = numeroNotaFiscal,
                            onValueChange = { numeroNotaFiscal = it },
                            label = { Text("Nº Nota Fiscal / OS") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manutencao_nf_numero_input"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = descricaoInput,
                        onValueChange = { descricaoInput = it },
                        label = { Text("Descrição das Peças / Serviços Realizados") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manutencao_descricao_input")
                    )

                    // 3. Upload / Seleção de Fotos da Nota Fiscal (Zero-Permission Photo Picker)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    Text(
                        text = "3. Upload / Seleção de Fotos da Nota Fiscal:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (fotosNotaFiscalUris.isNotEmpty()) StatusGreenLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (fotosNotaFiscalUris.isEmpty()) {
                                                "Nenhuma foto da Nota Fiscal anexada"
                                            } else {
                                                "${fotosNotaFiscalUris.size} foto(s) da Nota Fiscal selecionada(s)"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Selecione imagens do comprovante, cupom ou Nota Fiscal de peças/serviço",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        singlePhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("selecionar_foto_nota_fiscal_btn")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Selecionar Foto NF")
                                }

                                OutlinedButton(
                                    onClick = {
                                        multiplePhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("upload_multiplas_fotos_nf_btn")
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Múltiplas Fotos NF")
                                }
                            }

                            // Pré-visualização das Fotos da Nota Fiscal selecionadas
                            if (fotosNotaFiscalUris.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("nota_fiscal_photos_preview_row")
                                ) {
                                    items(fotosNotaFiscalUris.toList()) { uriStr ->
                                        Box(
                                            modifier = Modifier
                                                .size(92.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.5.dp, StatusGreenLight, RoundedCornerShape(12.dp))
                                        ) {
                                            AsyncImage(
                                                model = Uri.parse(uriStr),
                                                contentDescription = "Foto da Nota Fiscal",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Surface(
                                                color = Color(0xCC000000),
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(24.dp)
                                                    .clickable { fotosNotaFiscalUris.remove(uriStr) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remover foto da nota fiscal",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Status do Alerta da Manutenção
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Status do Alerta:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        listOf("Em dia", "Proxima", "Vencida").forEach { st ->
                            FilterChip(
                                selected = statusAlerta == st,
                                onClick = { statusAlerta = st },
                                label = { Text(st) }
                            )
                        }
                    }

                    // Botão Principal para Salvar ou Corrigir o Registro de Manutenção
                    val registroSendoCorrigido = manutencaoEmCorrecao
                    Button(
                        onClick = {
                            val kmLong = kmOcorrencia.filter { it.isDigit() }.toLongOrNull() ?: 15000L
                            val proxKmLong = proximoKm.filter { it.isDigit() }.toLongOrNull() ?: (kmLong + 5000L)
                            val vlrDouble = valorInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val resumoFotosNf = if (fotosNotaFiscalUris.isNotEmpty()) {
                                " | NF Anexada (${fotosNotaFiscalUris.size} foto(s)): ${fotosNotaFiscalUris.joinToString("; ")}"
                            } else {
                                ""
                            }
                            val obsCompleta = (descricaoInput.trim() + resumoFotosNf).take(950)

                            onRegistrarManutencao(
                                Manutencao(
                                    id = registroSendoCorrigido?.id ?: "",
                                    userId = registroSendoCorrigido?.userId ?: "",
                                    associadoId = registroSendoCorrigido?.associadoId ?: "",
                                    associadoCpf = registroSendoCorrigido?.associadoCpf ?: "",
                                    veiculoId = veiculoAtivo?.id ?: registroSendoCorrigido?.veiculoId ?: "v_default",
                                    veiculoNome = if (veiculoAtivo != null) "${veiculoAtivo.marca} ${veiculoAtivo.modelo}" else (registroSendoCorrigido?.veiculoNome ?: "Motocicleta Principal"),
                                    data = dataOcorrencia.ifBlank { "10/10/2026" },
                                    km = kmLong,
                                    tipo = tipoSel,
                                    descricao = descricaoInput.ifBlank { "Manutenção $tipoSel" },
                                    valor = vlrDouble,
                                    oficina = oficinaInput.ifBlank { "Iron Side Custom Garage" },
                                    ordemServico = if (fotosNotaFiscalUris.isNotEmpty()) {
                                        "${numeroNotaFiscal.ifBlank { "NF" }} (${fotosNotaFiscalUris.size} Foto(s) NF)"
                                    } else {
                                        numeroNotaFiscal.ifBlank { "OS-5021" }
                                    },
                                    proximaManutencaoData = registroSendoCorrigido?.proximaManutencaoData ?: "10/04/2027",
                                    proximoKm = proxKmLong,
                                    statusAlerta = statusAlerta,
                                    observacoes = obsCompleta,
                                    createdAt = registroSendoCorrigido?.createdAt
                                )
                            )
                            manutencaoEmCorrecao = null
                            fotosNotaFiscalUris.clear()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (registroSendoCorrigido != null) StatusYellow else MaterialTheme.colorScheme.primary,
                            contentColor = if (registroSendoCorrigido != null) Color.Black else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("salvar_manutencao_btn")
                    ) {
                        Icon(
                            imageVector = if (registroSendoCorrigido != null) Icons.Default.Edit else Icons.Default.Build,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                registroSendoCorrigido != null -> "Salvar Correção de Registro (${tipoSel})"
                                fotosNotaFiscalUris.isNotEmpty() -> "Registrar Manutenção + ${fotosNotaFiscalUris.size} Foto(s) da Nota Fiscal"
                                else -> "Registrar Manutenção & Atualizar KM"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Cabeçalho do Histórico de Manutenções Registradas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HISTÓRICO DE MANUTENÇÕES (${manutencoes.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "🟢 Em dia • 🟡 Próxima • 🔴 Vencida",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(manutencoes) { m ->
            val corAlerta = when (m.statusAlerta) {
                "Em dia" -> StatusGreen
                "Proxima" -> StatusYellow
                else -> StatusRed
            }
            val iconeTexto = when (m.statusAlerta) {
                "Em dia" -> "🟢 EM DIA"
                "Proxima" -> "🟡 PRÓXIMA"
                else -> "🔴 VENCIDA"
            }
            val urisExtraidas = remember(m.observacoes) {
                if (m.observacoes.contains("NF Anexada")) {
                    m.observacoes.substringAfter("NF Anexada", "")
                        .substringAfter(":", "")
                        .split(";")
                        .map { it.trim() }
                        .filter { it.startsWith("content://") || it.startsWith("file://") || it.startsWith("android.resource://") }
                } else {
                    emptyList()
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${m.tipo} • ${m.veiculoNome}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Surface(color = corAlerta, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = iconeTexto,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "📅 Data: ${m.data} • 🛣️ KM da Ocorrência: ${m.km} km • Próx.: ${m.proximoKm} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (m.descricao.isNotBlank()) {
                        Text(m.descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Exibe miniatura das fotos da Nota Fiscal caso anexadas
                    if (urisExtraidas.isNotEmpty() || m.ordemServico.contains("Foto(s) NF")) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = StatusGreenLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Comprovante / Nota Fiscal Anexada (${m.ordemServico})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusGreenLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (urisExtraidas.isNotEmpty()) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(urisExtraidas) { u ->
                                            AsyncImage(
                                                model = Uri.parse(u),
                                                contentDescription = "Foto da Nota Fiscal",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, StatusGreenLight, RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Oficina: ${m.oficina.ifBlank { "Própria" }} (NF/OS: ${m.ordemServico.ifBlank { "-" }})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "R$ ${String.format("%.2f", m.valor)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ações do Registro: Botão de Correção de Registro e Exclusão
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { carregarParaCorrecao(m) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("corrigir_manutencao_item_btn_${m.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Correção de Registro",
                                tint = StatusYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Correção de Registro",
                                color = StatusYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (m.id.isNotBlank()) {
                            IconButton(
                                onClick = { onRemoverManutencao(m.id) },
                                modifier = Modifier.testTag("remover_manutencao_item_btn_${m.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover Registro de Manutenção",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para escolher qual registro de manutenção corrigir ao clicar no botão "Correção de Registro" do formulário
    if (mostrarSeletorCorrecaoDialog) {
        AlertDialog(
            onDismissRequest = { mostrarSeletorCorrecaoDialog = false },
            confirmButton = {
                TextButton(onClick = { mostrarSeletorCorrecaoDialog = false }) {
                    Text("Fechar")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IronSideLogoBadge(size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Selecionar Registro para Correção")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Toque no registro de manutenção que deseja corrigir:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    manutencoes.take(8).forEach { itemManut ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    carregarParaCorrecao(itemManut)
                                    mostrarSeletorCorrecaoDialog = false
                                }
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
                                        text = "${itemManut.tipo} • ${itemManut.veiculoNome}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Data: ${itemManut.data} • ${itemManut.km} km • R$ ${String.format("%.2f", itemManut.valor)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Corrigir",
                                    tint = StatusYellow
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun AbastecimentoSection(
    veiculos: List<Veiculo>,
    abastecimentos: List<Abastecimento>,
    onRegistrarAbastecimento: (Abastecimento) -> Unit
) {
    val ultimoKm = abastecimentos.firstOrNull()?.km ?: veiculos.firstOrNull()?.kmAtual ?: 45000L
    var kmAnteriorInput by remember(ultimoKm) { mutableStateOf(ultimoKm.toString()) }
    var kmAtualInput by remember(ultimoKm) { mutableStateOf((ultimoKm + 500L).toString()) }
    var litrosInput by remember { mutableStateOf("40.0") }
    var precoLitroInput by remember { mutableStateOf("6.15") }
    var combustivelSel by remember { mutableStateOf("Gasolina aditivada") }
    var tanqueCheio by remember { mutableStateOf(true) }
    var postoInput by remember { mutableStateOf("Posto Graal BR-116") }

    val kmAnt = kmAnteriorInput.toLongOrNull() ?: 0L
    val kmAtu = kmAtualInput.toLongOrNull() ?: 0L
    val dist = (kmAtu - kmAnt).coerceAtLeast(0L)
    val litros = litrosInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val preco = precoLitroInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val valorTotal = litros * preco
    val consumoCalc = if (tanqueCheio && litros > 0.0 && dist > 0L) dist.toDouble() / litros else 0.0

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
                    IronSideFormHeader(
                        title = "FORMULÁRIO DE ABASTECIMENTO",
                        subtitle = "Cálculo automático de consumo km/L (Tanque Cheio vs Parcial)"
                    )

                    val combustiveis = listOf("Gasolina comum", "Gasolina aditivada", "Etanol", "Diesel S10", "Flex")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(combustiveis) { c ->
                            FilterChip(selected = combustivelSel == c, onClick = { combustivelSel = c }, label = { Text(c) })
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = kmAnteriorInput,
                            onValueChange = { kmAnteriorInput = it },
                            label = { Text("KM Anterior") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = kmAtualInput,
                            onValueChange = { kmAtualInput = it },
                            label = { Text("KM Atual") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = litrosInput,
                            onValueChange = { litrosInput = it },
                            label = { Text("Litros (L)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = precoLitroInput,
                            onValueChange = { precoLitroInput = it },
                            label = { Text("Preço/L (R$)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = postoInput,
                        onValueChange = { postoInput = it },
                        label = { Text("Posto / Cidade") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = tanqueCheio, onCheckedChange = { tanqueCheio = it })
                        Text(
                            text = if (tanqueCheio) "Tanque Cheio (Calcula consumo exato km/L)" else "Abastecimento Parcial (Não distorce média)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("DISTÂNCIA RODADA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$dist km", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            }
                            Column {
                                Text("VALOR TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("R$ ${String.format("%.2f", valorTotal)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CONSUMO CALCULADO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (tanqueCheio) String.format("%.2f km/L", consumoCalc) else "Parcial",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (litros > 0.0 && kmAtu > 0L) {
                                val vPrincipal = veiculos.firstOrNull()
                                onRegistrarAbastecimento(
                                    Abastecimento(
                                        veiculoId = vPrincipal?.id ?: "v_default",
                                        veiculoNome = if (vPrincipal != null) "${vPrincipal.marca} ${vPrincipal.modelo}" else "Motocicleta Principal",
                                        data = "10/10/2026",
                                        hora = "14:20",
                                        km = kmAtu,
                                        kmAnterior = kmAnt,
                                        combustivel = combustivelSel,
                                        litros = litros,
                                        precoPorLitro = preco,
                                        valorTotal = valorTotal,
                                        tanqueCheio = tanqueCheio,
                                        posto = postoInput,
                                        cidade = "São Paulo - SP",
                                        consumoCalculado = consumoCalc,
                                        observacao = if (tanqueCheio) "Tanque cheio ($dist km rodados)" else "Abastecimento parcial"
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("registrar_abastecimento_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Abastecimento & Atualizar KM")
                    }
                }
            }
        }

        items(abastecimentos) { ab ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${ab.combustivel} • ${ab.posto}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("KM: ${ab.km} (${ab.km - ab.kmAnterior} km) • ${String.format("%.1f", ab.litros)} L × R$ ${String.format("%.2f", ab.precoPorLitro)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (ab.tanqueCheio) String.format("%.2f km/L", ab.consumoCalculado) else "Parcial",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("R$ ${String.format("%.2f", ab.valorTotal)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SegurosSection(
    veiculos: List<Veiculo>,
    onIrParaGuinchoProximo: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "APÓLICES DE SEGURO & ASSISTÊNCIA 24H",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(veiculos) { v ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = StatusGreenLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${v.marca} ${v.modelo} (${v.placa})", style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Text("Seguradora: ${v.seguradora.ifBlank { "Porto Seguro Moto" }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Text("• Apólice: ${v.apolice.ifBlank { "PS-2026-889120" }} • Vigência até: ${v.seguroVigenciaFim.ifBlank { "15/03/2027" }}", style = MaterialTheme.typography.bodyMedium)
                    Text("• Guincho Contratado: ${v.seguroKmGuincho} KM Livre • Cobertura Completa + Pane Seca/Elétrica", style = MaterialTheme.typography.bodyMedium, color = StatusGreenLight)
                    Text("• Central 24h: ${v.seguroTelefoneAssistencia.ifBlank { "0800 727 0800" }}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val fone = v.seguroTelefoneAssistencia.ifBlank { "08007270800" }
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$fone"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📞 ACIONAR ASSISTÊNCIA", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onIrParaGuinchoProximo,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🚚 GUINCHO PRÓXIMO")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatorioVeiculoSection(
    veiculos: List<Veiculo>,
    manutencoes: List<Manutencao>,
    abastecimentos: List<Abastecimento>
) {
    val context = LocalContext.current
    var periodoFiltro by remember { mutableStateOf("Ano") }
    val periodos = listOf("Mês", "Trimestre", "Semestre", "Ano", "Personalizado")

    val kmFinal = veiculos.firstOrNull()?.kmAtual ?: 14850L
    val kmInicial = (kmFinal - 2500L).coerceAtLeast(0L)
    val kmRodados = kmFinal - kmInicial
    val litrosTotais = abastecimentos.sumOf { it.litros }
    val gastoComb = abastecimentos.sumOf { it.valorTotal }
    val gastoManut = manutencoes.sumOf { it.valor }
    val gastoTotal = gastoComb + gastoManut
    val custoKm = if (kmRodados > 0) gastoTotal / kmRodados.toDouble() else 0.0

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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IronSideFormHeader(
                        title = "RELATÓRIO CONSOLIDADO DO VEÍCULO",
                        subtitle = "Iron Side • Road Rider MC • Os melhores são de ferro"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(periodos) { p ->
                            FilterChip(selected = periodoFiltro == p, onClick = { periodoFiltro = p }, label = { Text(p) })
                        }
                    }

                    HorizontalDivider()

                    Text("Período selecionado: $periodoFiltro", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    Text("• KM Inicial: $kmInicial km  →  KM Final: $kmFinal km")
                    Text("• Total KM Rodados no Período: $kmRodados km", fontWeight = FontWeight.Bold)
                    Text("• Combustível Utilizado: ${String.format("%.1f", litrosTotais)} Litros")
                    Text("• Gasto com Combustível: R$ ${String.format("%.2f", gastoComb)}")
                    Text("• Gasto com Manutenção: R$ ${String.format("%.2f", gastoManut)}")
                    Text("• Gasto Total Consolidado: R$ ${String.format("%.2f", gastoTotal)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("• Custo Real por KM: R$ ${String.format("%.2f", custoKm)} / km", style = MaterialTheme.typography.titleMedium, color = StatusGreenLight)

                    Button(
                        onClick = {
                            val resumo = """
                                RELATÓRIO GARAGEM - IRON SIDE MOTO CLUBE
                                Período: $periodoFiltro
                                KM Rodados: $kmRodados km ($kmInicial -> $kmFinal km)
                                Litros: ${String.format("%.1f", litrosTotais)} L
                                Combustível: R$ ${String.format("%.2f", gastoComb)}
                                Manutenção: R$ ${String.format("%.2f", gastoManut)}
                                Total: R$ ${String.format("%.2f", gastoTotal)} (R$ ${String.format("%.2f", custoKm)}/km)
                            """.trimIndent()
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, resumo)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Exportar Relatório do Veículo"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar / Compartilhar Relatório")
                    }
                }
            }
        }
    }
}
