package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Associado
import com.example.data.AssociadosPdfExportService
import com.example.data.ConfiguracaoIdentidadeClube
import com.example.data.FiltroConsolidacaoPdf
import com.example.data.Manutencao
import com.example.data.Mensalidade
import com.example.data.OpcoesExportacaoConsolidadaPdf
import com.example.data.Veiculo
import com.example.ui.CarteirinhaPublicValidationPayload
import com.example.ui.FichaCadastralPdfGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `qr code validation payload exposes only essential verification info and hides sensitive associate data`() {
        val associadoCompleto = Associado(
            id = "uid_secreto_123",
            userId = "uid_secreto_123",
            numeroAssociado = "MC-000777",
            nomeCompleto = "Roberto Carlos Silva Almeida",
            nomeGuerra = "TROVÃO",
            cpf = "987.654.321-11",
            rg = "44.555.666-7",
            dataNascimento = "15/04/1984",
            tipoSanguineo = "AB-",
            fatorRh = "Negativo (-)",
            email = "roberto.pessoal@gmail.com",
            telefone = "(11) 3222-1111",
            celular = "(11) 98888-7777",
            whatsapp = "(11) 98888-7777",
            cep = "01310-200",
            logradouro = "Rua Secreta dos Associados",
            numeroEndereco = "999",
            bairro = "Jardins",
            cnhNumero = "00998877665",
            cnhValidade = "10/10/2029",
            cargo = "Diretor de Estrada",
            regional = "Divisão Paulista",
            statusAssociado = "Ativo",
            validadeCarteirinha = "12/2028",
            emergencia1Nome = "Patrícia Almeida",
            emergencia1Parentesco = "Esposa",
            emergencia1Telefone = "(11) 97777-6666"
        )

        val payload = CarteirinhaPublicValidationPayload.fromAssociado(
            associado = associadoCompleto,
            configIdentidade = ConfiguracaoIdentidadeClube()
        )

        // Verifica que os dados essenciais de validação externa estão presentes
        assertEquals("MC-000777", payload.numeroAssociado)
        assertEquals("TROVÃO", payload.nomeGuerra)
        assertEquals("Roberto C. S. Almeida", payload.nomeExibicaoPublica)
        assertEquals("Diretor de Estrada", payload.cargoClube)
        assertEquals("Divisão Paulista", payload.regionalClube)
        assertEquals("Ativo", payload.statusAssociado)
        assertEquals("12/2028", payload.validadeCarteirinha)
        assertEquals("AB-", payload.tipoSanguineo)
        assertTrue(payload.isCarteirinhaValida)
        assertTrue(payload.codigoAutenticidadeHash.isNotBlank())

        // Garante que NENHUM dado sensível consta na representação do payload público do QR Code
        val dumpPayload = payload.toString()
        assertFalse(dumpPayload.contains("987.654.321-11")) // CPF oculto
        assertFalse(dumpPayload.contains("44.555.666-7"))   // RG oculto
        assertFalse(dumpPayload.contains("00998877665"))    // CNH oculta
        assertFalse(dumpPayload.contains("15/04/1984"))     // Data de nascimento oculta
        assertFalse(dumpPayload.contains("Rua Secreta"))    // Endereço residencial oculto
        assertFalse(dumpPayload.contains("01310-200"))      // CEP residencial oculto
        assertFalse(dumpPayload.contains("roberto.pessoal@gmail.com")) // E-mail pessoal oculto
        assertFalse(dumpPayload.contains("98888-7777"))     // Celular pessoal oculto
    }

    @Test
    fun `ficha cadastral pdf generator creates complete pdf with member photo affiliation date desligamento and motorcycle crlv document`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val associado = Associado(
            id = "uid_membro_01",
            userId = "uid_membro_01",
            numeroAssociado = "MC-000010",
            nomeCompleto = "Ricardo Mendes Ferro",
            nomeGuerra = "FERRO",
            fotoUri = "preset:comandante_estrada",
            cpf = "123.456.789-00",
            rg = "12.345.678-9",
            cnhNumero = "05498213490",
            cnhCategoria = "AB",
            cnhEmissao = "10/02/2021",
            cnhValidade = "10/02/2031",
            emergencia1Nome = "Juliana Ferro",
            emergencia1Parentesco = "Esposa",
            emergencia1Telefone = "(11) 99123-4567",
            emergencia2Nome = "Comandante Rocha",
            emergencia2Parentesco = "Diretor de Estrada",
            emergencia2Telefone = "(11) 99876-5432",
            dataAssociacao = "15/01/2024", // Obrigatório
            dataDesligamento = "10/10/2026", // Preenchido pela Diretoria
            motivoDesligamento = "Transferência de Regional solicitada à Diretoria",
            statusAssociado = "Desligado"
        )
        val moto = Veiculo(
            id = "v_01",
            userId = "uid_membro_01",
            associadoId = "uid_membro_01",
            associadoCpf = "123.456.789-00",
            fotoUri = "preset:moto_harley_ironside",
            marca = "Harley-Davidson",
            modelo = "Fat Boy 114",
            placa = "IRN2E26",
            renavam = "01283947561",
            chassi = "9321KBMJ884102931",
            numeroCrvCrlv = "CRLV-2026-99881122",
            numeroMotor = "HD114M8-882910",
            kmAtual = 18500L
        )
        val manutencao = Manutencao(
            id = "manut_01",
            userId = "uid_membro_01",
            associadoId = "uid_membro_01",
            associadoCpf = "123.456.789-00",
            veiculoId = "v_01",
            veiculoNome = "Harley-Davidson Fat Boy 114 • IRN2E26",
            tipo = "Revisão Completa 18.000 km",
            data = "05/10/2026",
            km = 18000L,
            valor = 920.0,
            oficina = "Iron Side Custom Garage",
            proximoKm = 23000L,
            statusAlerta = "Em dia"
        )
        val abastecimento = com.example.data.Abastecimento(
            id = "abast_01",
            userId = "uid_membro_01",
            associadoId = "uid_membro_01",
            associadoCpf = "123.456.789-00",
            veiculoId = "v_01",
            veiculoNome = "Harley-Davidson Fat Boy 114 • IRN2E26",
            data = "08/10/2026",
            km = 18500L,
            litros = 16.5,
            valorTotal = 99.0,
            consumoCalculado = 19.2
        )

        val pdfResult = FichaCadastralPdfGenerator.gerarPdfFichaCompletaAssociado(
            context = context,
            associado = associado,
            veiculosDoAssociado = listOf(moto),
            manutencoesDoAssociado = listOf(manutencao),
            abastecimentosDoAssociado = listOf(abastecimento),
            configIdentidade = ConfiguracaoIdentidadeClube()
        )

        assertTrue(pdfResult.isSuccess)
        val pdfFile = pdfResult.getOrNull()
        assertNotNull(pdfFile)
        assertTrue(pdfFile!!.exists())
        assertTrue(pdfFile.length() > 500L)
    }

    @Test
    fun `consolidated pdf export service consolidates multiple associate records and archives pdf document`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val membroAtivo = Associado(
            id = "uid_01",
            userId = "uid_01",
            numeroAssociado = "MC-000001",
            nomeCompleto = "Carlos Eduardo Rocha",
            nomeGuerra = "ROCHA",
            cpf = "111.222.333-44",
            rg = "11.222.333-4",
            dataAssociacao = "10/01/2024",
            statusAssociado = "Ativo"
        )
        val membroDesligado = Associado(
            id = "uid_02",
            userId = "uid_02",
            numeroAssociado = "MC-000002",
            nomeCompleto = "Marcos Vinicius Lima",
            nomeGuerra = "FALCAO",
            cpf = "555.666.777-88",
            rg = "55.666.777-8",
            dataAssociacao = "20/03/2024",
            dataDesligamento = "10/10/2026",
            motivoDesligamento = "Desligamento a pedido registrado pela Diretoria",
            statusAssociado = "Desligado"
        )
        val veiculos = listOf(
            Veiculo(
                id = "v_01",
                userId = "uid_01",
                associadoId = "uid_01",
                associadoCpf = "111.222.333-44",
                marca = "Harley-Davidson",
                modelo = "Road King Special",
                placa = "IRN1A11",
                renavam = "01122334455",
                chassi = "9321KBMJ111222"
            )
        )
        val manutencoes = listOf(
            Manutencao(
                id = "m_01",
                userId = "uid_01",
                associadoId = "uid_01",
                associadoCpf = "111.222.333-44",
                tipo = "Revisão Completa",
                data = "01/10/2026",
                km = 15000L,
                valor = 850.0,
                statusAlerta = "Em dia"
            )
        )
        val mensalidades = listOf(
            Mensalidade(
                id = "pix_01",
                userId = "uid_01",
                cpf = "111.222.333-44",
                competencia = "Outubro/2026",
                valor = 50.0,
                status = "Pago"
            )
        )

        val result = AssociadosPdfExportService.exportarRelatorioConsolidadoAssociadosPdf(
            context = context,
            associados = listOf(membroAtivo, membroDesligado),
            veiculos = veiculos,
            manutencoes = manutencoes,
            mensalidades = mensalidades,
            configIdentidade = ConfiguracaoIdentidadeClube(),
            opcoes = OpcoesExportacaoConsolidadaPdf(filtroSituacao = FiltroConsolidacaoPdf.TODOS)
        )

        assertTrue(result.isSuccess)
        val exportData = result.getOrNull()
        assertNotNull(exportData)
        assertEquals(2, exportData!!.associadosIncluidos)
        assertEquals(1, exportData.ativosCount)
        assertEquals(1, exportData.desligadosCount)
        assertTrue(exportData.arquivoInfo.file.exists())
        assertTrue(exportData.arquivoInfo.tamanhoBytes > 500L)
        assertTrue(exportData.arquivoInfo.codigoProtocoloArquivo.startsWith("ARQ-MC-"))

        val arquivados = AssociadosPdfExportService.listarArquivosPdfArquivados(context)
        assertTrue(arquivados.any { it.nomeArquivo == exportData.arquivoInfo.nomeArquivo })
    }
}

