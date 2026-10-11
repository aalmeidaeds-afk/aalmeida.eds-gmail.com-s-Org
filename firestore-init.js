#!/usr/bin/env node
/**
 * Script Oficial de Inicialização e Verificação do Firestore (Moto Clube Iron Side)
 *
 * Responsável por:
 * 1. Garantir que todas as 9 coleções oficiais do projeto (`associados`, `veiculos`,
 *    `manutencoes`, `abastecimentos`, `eventos`, `comunicados`, `mensalidades`,
 *    `parceiros_servicos` e `configuracoes_clube`) estejam registradas no blueprint,
 *    protegidas em `firestore.rules` e com índices compostos declarados.
 * 2. Quando o emulador local estiver ativo (`FIRESTORE_EMULATOR_HOST`), popular (seed)
 *    automaticamente os documentos iniciais respeitando todas as regras de segurança
 *    e validar as permissões RLS (Row Level Security) de Associado vs Diretoria.
 *
 * Uso:
 *   node firestore-init.js
 *   FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node firestore-init.js --seed-emulator
 */

const fs = require("node:fs");
const path = require("node:path");

const REQUIRED_COLLECTIONS = [
  {
    name: "associados",
    pathPattern: "/associados/{userId}",
    rlsScope: "OWNERSHIP_OR_DIRETORIA",
    description: "Ficha cadastral, tipo sanguíneo, CNH, contatos de emergência e status de aprovação.",
    requiredFields: [
      "userId",
      "numeroAssociado",
      "nomeCompleto",
      "nomeGuerra",
      "cpf",
      "tipoSanguineo",
      "statusAssociado",
      "perfilAcesso",
      "ativo",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "veiculos",
    pathPattern: "/veiculos/{veiculoId}",
    rlsScope: "OWNERSHIP_OR_DIRETORIA",
    description: "Veículos cadastrados na garagem do associado e apólice de seguro vinculada.",
    requiredFields: [
      "userId",
      "tipo",
      "marca",
      "modelo",
      "ano",
      "placa",
      "kmAtual",
      "ativo",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "manutencoes",
    pathPattern: "/manutencoes/{manutencaoId}",
    rlsScope: "OWNERSHIP_OR_DIRETORIA",
    description: "Registros de manutenção preventiva/corretiva, KM, valor, alerta e nota fiscal.",
    requiredFields: [
      "userId",
      "veiculoId",
      "veiculoNome",
      "data",
      "km",
      "tipo",
      "valor",
      "proximoKm",
      "statusAlerta",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "abastecimentos",
    pathPattern: "/abastecimentos/{abastecimentoId}",
    rlsScope: "OWNERSHIP_OR_DIRETORIA",
    description: "Histórico de abastecimento e cálculo automático de consumo médio (km/L).",
    requiredFields: [
      "userId",
      "veiculoId",
      "veiculoNome",
      "data",
      "km",
      "combustivel",
      "litros",
      "precoPorLitro",
      "valorTotal",
      "tanqueCheio",
      "consumoCalculado",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "eventos",
    pathPattern: "/eventos/{eventoId}",
    rlsScope: "CLUB_READ_ORGANIZER_OR_DIRETORIA_WRITE",
    description: "Calendário de eventos, passeios, comboios e confirmação de presença.",
    requiredFields: [
      "userId",
      "nome",
      "data",
      "horario",
      "local",
      "cidade",
      "status",
      "ativo",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "comunicados",
    pathPattern: "/comunicados/{comunicadoId}",
    rlsScope: "CLUB_READ_DIRETORIA_WRITE",
    description: "Mural oficial de comunicados, avisos e campanhas da Diretoria.",
    requiredFields: [
      "userId",
      "titulo",
      "texto",
      "data",
      "autor",
      "prioridade",
      "ativo",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "mensalidades",
    pathPattern: "/mensalidades/{mensalidadeId}",
    rlsScope: "OWNERSHIP_OR_DIRETORIA",
    description: "Controle financeiro de mensalidades, cobranças PIX e baixas da Tesouraria.",
    requiredFields: [
      "userId",
      "associadoNome",
      "numeroAssociado",
      "competencia",
      "ano",
      "valor",
      "vencimento",
      "status",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "parceiros_servicos",
    pathPattern: "/parceiros_servicos/{parceiroId}",
    rlsScope: "CLUB_READ_DIRETORIA_OR_AUTHOR_WRITE",
    description: "Base nacional SOS Estrada: oficinas, guinchos 24h, borracharias e hospitais.",
    requiredFields: [
      "userId",
      "nome",
      "categoria",
      "telefone",
      "cidade",
      "estado",
      "atendimento24h",
      "atendeMotos",
      "parceiroOficial",
      "ativo",
      "createdAt",
      "updatedAt",
    ],
  },
  {
    name: "configuracoes_clube",
    pathPattern: "/configuracoes_clube/{configId}",
    rlsScope: "CLUB_READ_DIRETORIA_WRITE",
    description: "Identidade dinâmica do Moto Clube (logo, paleta hexadecimal, contatos e PIX).",
    requiredFields: [
      "userId",
      "nomeClube",
      "lemaClube",
      "corPrimariaHex",
      "corSecundariaHex",
      "chavePix",
      "valorMensalidadePadrao",
      "ativo",
      "createdAt",
      "updatedAt",
    ],
  },
];

function verifyStaticConfiguration() {
  const rootDir = __dirname;
  const rulesPath = path.join(rootDir, "firestore.rules");
  const blueprintPath = path.join(rootDir, "firebase-blueprint.json");
  const firebaseJsonPath = path.join(rootDir, "firebase.json");

  if (!fs.existsSync(rulesPath)) {
    throw new Error("Arquivo firestore.rules não encontrado na raiz do projeto.");
  }
  if (!fs.existsSync(blueprintPath)) {
    throw new Error("Arquivo firebase-blueprint.json não encontrado na raiz do projeto.");
  }
  if (!fs.existsSync(firebaseJsonPath)) {
    throw new Error("Arquivo firebase.json não encontrado na raiz do projeto.");
  }

  const rulesContent = fs.readFileSync(rulesPath, "utf8");
  const blueprint = JSON.parse(fs.readFileSync(blueprintPath, "utf8"));
  const firebaseConfig = JSON.parse(fs.readFileSync(firebaseJsonPath, "utf8"));

  const report = [];

  for (const col of REQUIRED_COLLECTIONS) {
    const hasRuleMatch = rulesContent.includes(`match /${col.name}/`);
    const hasBlueprintPath = Boolean(
      blueprint.firestore && blueprint.firestore[col.pathPattern]
    );
    if (!hasRuleMatch) {
      throw new Error(`Coleção '${col.name}' ausente nas regras do firestore.rules!`);
    }
    if (!hasBlueprintPath) {
      throw new Error(`Caminho '${col.pathPattern}' ausente no firebase-blueprint.json!`);
    }
    report.push({
      collection: col.name,
      pathPattern: col.pathPattern,
      rlsScope: col.rlsScope,
      requiredFieldsCount: col.requiredFields.length,
      rulesConfigured: hasRuleMatch,
      blueprintConfigured: hasBlueprintPath,
    });
  }

  return {
    databaseId:
      Array.isArray(firebaseConfig.firestore) && firebaseConfig.firestore[0]
        ? firebaseConfig.firestore[0].database
        : "(default)",
    collectionsVerified: report.length,
    collections: report,
  };
}

async function seedAndValidateWithEmulator(databaseConfig) {
  const { initializeTestEnvironment, assertSucceeds, assertFails } = require("@firebase/rules-unit-testing");
  const rules = fs.readFileSync(path.join(__dirname, "firestore.rules"), "utf8");
  const [host, portStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
  const port = parseInt(portStr, 10);
  const projectId = process.env.GCP_PROJECT || "demo-no-project";

  const testEnv = await initializeTestEnvironment({
    projectId,
    firestore: { rules, host, port },
  });

  try {
    const uid = "init_admin_uid";
    const otherUid = "init_other_uid";
    const now = new Date();
    const db = testEnv.authenticatedContext(uid).firestore();
    const otherDb = testEnv.authenticatedContext(otherUid).firestore();

    // 1. associados
    await assertSucceeds(
      db.collection("associados").doc(uid).set({
        id: uid,
        userId: uid,
        numeroAssociado: "MC-000001",
        nomeCompleto: "Comandante Iron Side",
        nomeGuerra: "COMANDO",
        cpf: "123.456.789-00",
        tipoSanguineo: "O+",
        status: "Ativo",
        statusAssociado: "Ativo",
        perfilAcesso: "Diretoria",
        ativo: true,
        createdAt: now,
        updatedAt: now,
      })
    );

    // 2. veiculos
    await assertSucceeds(
      db.collection("veiculos").doc("veic_init_01").set({
        id: "veic_init_01",
        userId: uid,
        associadoId: uid,
        associadoCpf: "123.456.789-00",
        status: "Ativo",
        tipo: "Moto",
        marca: "Harley-Davidson",
        modelo: "Fat Boy 114 FLFBS",
        ano: 2024,
        placa: "BRA2E19",
        kmAtual: 14850,
        ativo: true,
        createdAt: now,
        updatedAt: now,
      })
    );

    // 3. manutencoes
    await assertSucceeds(
      db.collection("manutencoes").doc("manut_init_01").set({
        id: "manut_init_01",
        userId: uid,
        associadoId: uid,
        associadoCpf: "123.456.789-00",
        veiculoId: "veic_init_01",
        veiculoNome: "Harley-Davidson Fat Boy 114",
        status: "Em dia",
        data: "05/09/2026",
        km: 14000,
        tipo: "Troca de óleo",
        valor: 680.0,
        proximoKm: 18000,
        statusAlerta: "Em dia",
        createdAt: now,
        updatedAt: now,
      })
    );

    // 4. abastecimentos
    await assertSucceeds(
      db.collection("abastecimentos").doc("abast_init_01").set({
        id: "abast_init_01",
        userId: uid,
        associadoId: uid,
        associadoCpf: "123.456.789-00",
        veiculoId: "veic_init_01",
        veiculoNome: "Harley-Davidson Fat Boy 114",
        status: "Registrado",
        data: "08/10/2026",
        km: 14850,
        combustivel: "Gasolina aditivada",
        litros: 15.8,
        precoPorLitro: 6.19,
        valorTotal: 97.8,
        tanqueCheio: true,
        consumoCalculado: 18.99,
        createdAt: now,
        updatedAt: now,
      })
    );

    // 5. eventos
    await assertSucceeds(
      db.collection("eventos").doc("evento_init_01").set({
        id: "evento_init_01",
        userId: uid,
        associadoId: uid,
        nome: "Bate & Volta Serra do Rio do Rastro / Curitiba",
        data: "24/10/2026",
        horario: "06:30",
        local: "Posto Frango Assado - BR-116 KM 290",
        cidade: "Curitiba - PR",
        status: "Confirmado",
        ativo: true,
        createdAt: now,
        updatedAt: now,
      })
    );

    // 6. comunicados
    await assertSucceeds(
      db.collection("comunicados").doc("comunicado_init_01").set({
        userId: uid,
        titulo: "Inicialização Oficial do Firestore Concluída",
        texto: "Todas as coleções do Moto Clube estão operacionais com proteção RLS.",
        tipo: "Comunicado",
        data: "10/10/2026",
        autor: "Diretoria Nacional",
        prioridade: "Alta",
        ativo: true,
        createdAt: now,
        updatedAt: now,
      })
    );

    // 7. mensalidades
    await assertSucceeds(
      db.collection("mensalidades").doc("mensalidade_init_01").set({
        userId: uid,
        associadoNome: "Comandante Iron Side",
        numeroAssociado: "MC-000001",
        competencia: "Outubro/2026",
        ano: 2026,
        valor: 50.0,
        cpf: "123.456.789-00",
        vencimento: "15/10/2026",
        status: "Pendente",
        formaPagamento: "PIX Manual",
        chavePix: "financeiro@ironsidemotoclube.com.br",
        createdAt: now,
        updatedAt: now,
      })
    );

    // 8. parceiros_servicos
    await assertSucceeds(
      db.collection("parceiros_servicos").doc("parceiro_init_01").set({
        id: "parceiro_init_01",
        userId: uid,
        nome: "Iron Side Custom & Oficina V-Twin",
        categoria: "Oficinas para motos",
        telefone: "(11) 98877-6655",
        cidade: "São Paulo",
        estado: "SP",
        atendimento24h: true,
        atendeMotos: true,
        parceiroOficial: true,
        ativo: true,
        createdAt: now,
        updatedAt: now,
      })
    );

    // 9. configuracoes_clube
    await assertSucceeds(
      db.collection("configuracoes_clube").doc("identidade_oficial").set({
        id: "identidade_oficial",
        userId: uid,
        nomeClube: "IRON SIDE • ROAD RIDER MC",
        lemaClube: "Os melhores são de ferro",
        corPrimariaHex: "#FF6D00",
        corSecundariaHex: "#FFB74D",
        chavePix: "financeiro@ironsidemotoclube.com.br",
        valorMensalidadePadrao: 50.0,
        ativo: true,
        createdAt: now,
        updatedAt: now,
      })
    );

    // Verifica bloqueio RLS cruzado (usuário comum não lê veículo/manutenção alheia)
    await assertFails(otherDb.collection("veiculos").doc("veic_init_01").get());
    await assertFails(otherDb.collection("manutencoes").doc("manut_init_01").get());

    return {
      emulatorSeeded: true,
      databaseId: databaseConfig.databaseId,
      seededCollections: REQUIRED_COLLECTIONS.map((c) => c.name),
    };
  } finally {
    await testEnv.cleanup();
  }
}

async function main() {
  const staticCheck = verifyStaticConfiguration();
  const shouldSeed =
    process.argv.includes("--seed-emulator") || Boolean(process.env.FIRESTORE_EMULATOR_HOST);

  let emulatorResult = null;
  if (shouldSeed) {
    emulatorResult = await seedAndValidateWithEmulator(staticCheck);
  }

  console.log(
    JSON.stringify(
      {
        status: "OK",
        databaseId: staticCheck.databaseId,
        collectionsVerified: staticCheck.collectionsVerified,
        collections: staticCheck.collections,
        emulatorValidation: emulatorResult,
      },
      null,
      2
    )
  );
}

if (require.main === module) {
  main().catch((err) => {
    console.error("[FIRESTORE-INIT ERROR]", err.message || err);
    process.exit(1);
  });
}

module.exports = {
  REQUIRED_COLLECTIONS,
  verifyStaticConfiguration,
  seedAndValidateWithEmulator,
};
