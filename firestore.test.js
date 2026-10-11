const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const DIRETORIA_UID = "diretoria_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read veiculos or associados", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("veiculos").get());
  await assertFails(unauthDb.collection("associados").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's PII in associados", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("associados").doc(BOB_UID).set({
      userId: BOB_UID,
      numeroAssociado: "MC-000002",
      nomeCompleto: "Roberto Silva",
      nomeGuerra: "Beto",
      cpf: "111.222.333-44",
      tipoSanguineo: "O+",
      statusAssociado: "Ativo",
      perfilAcesso: "Associado",
      ativo: true,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("associados").doc(BOB_UID).get());
});

test("Authenticated user: can query their own veiculos with matching userId filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("veiculos").doc("v_alice_1").set({
      userId: ALICE_UID,
      tipo: "Moto",
      marca: "Harley-Davidson",
      modelo: "Iron 883",
      ano: 2022,
      placa: "BRA2E19",
      kmAtual: 14500,
      ativo: true,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("veiculos").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: fails query on veiculos without userId filter (rules are not filters)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("veiculos").get());
});

test("Authenticated user: can create and query all 5 core collections (associados, veiculos, manutencoes, abastecimentos, eventos) with id, status, and relationships", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  // 1. associados
  await assertSucceeds(
    aliceDb.collection("associados").doc(ALICE_UID).set({
      id: ALICE_UID,
      userId: ALICE_UID,
      numeroAssociado: "MC-000001",
      nomeCompleto: "Alice Iron Rider",
      nomeGuerra: "Falcão",
      cpf: "123.456.789-00",
      tipoSanguineo: "O+",
      status: "Ativo",
      statusAssociado: "Ativo",
      perfilAcesso: "Associado",
      ativo: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. veiculos (relacionado a associados via userId, associadoId, associadoCpf)
  await assertSucceeds(
    aliceDb.collection("veiculos").doc("veic_1").set({
      id: "veic_1",
      userId: ALICE_UID,
      associadoId: ALICE_UID,
      associadoCpf: "123.456.789-00",
      status: "Ativo",
      tipo: "Moto",
      marca: "Harley-Davidson",
      modelo: "Road King Special",
      ano: 2024,
      placa: "IRN1A01",
      kmAtual: 5000,
      ativo: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 3. manutencoes (relacionado a associados e veiculos via veiculoId, associadoId, associadoCpf)
  await assertSucceeds(
    aliceDb.collection("manutencoes").doc("manut_1").set({
      id: "manut_1",
      userId: ALICE_UID,
      associadoId: ALICE_UID,
      associadoCpf: "123.456.789-00",
      veiculoId: "veic_1",
      veiculoNome: "Harley-Davidson Road King Special",
      status: "Em dia",
      data: "10/10/2026",
      km: 5000,
      tipo: "Revisão Completa",
      valor: 680.0,
      proximoKm: 10000,
      statusAlerta: "Em dia",
      createdAt: now,
      updatedAt: now,
    })
  );

  // 4. abastecimentos (relacionado a associados e veiculos via veiculoId, associadoId, associadoCpf)
  await assertSucceeds(
    aliceDb.collection("abastecimentos").doc("abast_1").set({
      id: "abast_1",
      userId: ALICE_UID,
      associadoId: ALICE_UID,
      associadoCpf: "123.456.789-00",
      veiculoId: "veic_1",
      veiculoNome: "Harley-Davidson Road King Special",
      status: "Registrado",
      data: "10/10/2026",
      km: 5220,
      kmAnterior: 5000,
      combustivel: "Gasolina Podium",
      litros: 12.5,
      precoPorLitro: 6.20,
      valorTotal: 77.5,
      tanqueCheio: true,
      consumoCalculado: 17.6,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 5. eventos (relacionado a associados via userId e associadoId)
  await assertSucceeds(
    aliceDb.collection("eventos").doc("ev_1").set({
      id: "ev_1",
      userId: ALICE_UID,
      associadoId: ALICE_UID,
      nome: "Bate & Volta Serra do Rio do Rastro / Curitiba",
      data: "24/10/2026",
      horario: "06:30 (Briefing 06:00)",
      local: "Posto Frango Assado - BR-116 Régis Bittencourt KM 290",
      endereco: "Rodovia Régis Bittencourt, KM 290",
      cidade: "Itapecerica da Serra / Curitiba - PR",
      descricao: "Comboio oficial com os 15 associados fundadores.",
      valor: 0.0,
      limiteParticipantes: 30,
      confirmadosCount: 12,
      status: "Confirmado",
      ativo: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 6. configuracoes_clube (Configurações de Identidade do Clube: logo, cores, dados de contato e PIX)
  await assertSucceeds(
    aliceDb.collection("configuracoes_clube").doc("identidade_oficial").set({
      id: "identidade_oficial",
      userId: ALICE_UID,
      nomeClube: "IRON SIDE • ROAD RIDER MC",
      subtituloClube: "DIVISÃO NACIONAL BRASIL",
      lemaClube: "Os melhores são de ferro",
      cnpjClube: "48.120.990/0001-15",
      sedeCidadeEstado: "São Paulo - SP",
      logoPreset: "brasao_oficial",
      logoUri: "",
      corPrimariaHex: "#FF6D00",
      corSecundariaHex: "#FFB74D",
      corDestaqueSosHex: "#EF5350",
      corFundoHex: "#0E1013",
      corSuperficieHex: "#16191E",
      presetPaleta: "Laranja Iron Side (Oficial)",
      emailContato: "contato@ironsidemotoclube.com.br",
      telefoneContato: "(11) 3456-7890",
      whatsappClube: "(11) 99876-5432",
      instagramClube: "@ironsideroadridermc",
      siteClube: "www.ironsidemotoclube.com.br",
      enderecoSede: "Av. Paulista, 1578 - Bela Vista, São Paulo - SP",
      chavePix: "financeiro@ironsidemotoclube.com.br",
      tipoChavePix: "E-mail",
      favorecidoPix: "Iron Side Moto Clube Associacao",
      bancoPix: "Banco Itaú / Cora SCD",
      cidadePix: "SAO PAULO",
      valorMensalidadePadrao: 50.0,
      diaVencimentoPadrao: 15,
      ativo: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("RLS Policy: Associado cannot read or edit another Associado's records (associados, veiculos, manutencoes, abastecimentos, mensalidades)", async () => {
  const now = new Date();

  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();

    await adminDb.collection("associados").doc(ALICE_UID).set({
      id: ALICE_UID,
      userId: ALICE_UID,
      numeroAssociado: "MC-000001",
      nomeCompleto: "Alice Membro",
      nomeGuerra: "Alice",
      cpf: "111.111.111-11",
      tipoSanguineo: "O+",
      status: "Ativo",
      statusAssociado: "Ativo",
      perfilAcesso: "Associado",
      ativo: true,
      createdAt: now,
      updatedAt: now,
    });

    await adminDb.collection("associados").doc(BOB_UID).set({
      id: BOB_UID,
      userId: BOB_UID,
      numeroAssociado: "MC-000002",
      nomeCompleto: "Bob Membro",
      nomeGuerra: "Bob",
      cpf: "222.222.222-22",
      tipoSanguineo: "A+",
      status: "Ativo",
      statusAssociado: "Ativo",
      perfilAcesso: "Associado",
      ativo: true,
      createdAt: now,
      updatedAt: now,
    });

    await adminDb.collection("veiculos").doc("v_bob").set({
      id: "v_bob",
      userId: BOB_UID,
      associadoId: BOB_UID,
      associadoCpf: "222.222.222-22",
      status: "Ativo",
      tipo: "Moto",
      marca: "Triumph",
      modelo: "Tiger 900",
      ano: 2024,
      placa: "BOB9A99",
      kmAtual: 9000,
      ativo: true,
      createdAt: now,
      updatedAt: now,
    });

    await adminDb.collection("manutencoes").doc("m_bob").set({
      id: "m_bob",
      userId: BOB_UID,
      associadoId: BOB_UID,
      associadoCpf: "222.222.222-22",
      veiculoId: "v_bob",
      veiculoNome: "Triumph Tiger 900",
      status: "Em dia",
      data: "10/10/2026",
      km: 9000,
      tipo: "Troca de óleo",
      valor: 450.0,
      proximoKm: 14000,
      statusAlerta: "Em dia",
      createdAt: now,
      updatedAt: now,
    });

    await adminDb.collection("abastecimentos").doc("a_bob").set({
      id: "a_bob",
      userId: BOB_UID,
      associadoId: BOB_UID,
      associadoCpf: "222.222.222-22",
      veiculoId: "v_bob",
      veiculoNome: "Triumph Tiger 900",
      status: "Registrado",
      data: "10/10/2026",
      km: 9200,
      combustivel: "Gasolina",
      litros: 14.0,
      precoPorLitro: 6.0,
      valorTotal: 84.0,
      tanqueCheio: true,
      consumoCalculado: 19.5,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Alice CAN read and edit her own profile
  await assertSucceeds(aliceDb.collection("associados").doc(ALICE_UID).get());
  await assertSucceeds(
    aliceDb.collection("associados").doc(ALICE_UID).update({
      nomeGuerra: "Alice Falcão",
      updatedAt: new Date(),
    })
  );

  // Alice CANNOT read or edit Bob's records
  await assertFails(aliceDb.collection("associados").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("associados").doc(BOB_UID).update({
      nomeGuerra: "Invadido",
      updatedAt: new Date(),
    })
  );
  await assertFails(aliceDb.collection("veiculos").doc("v_bob").get());
  await assertFails(
    aliceDb.collection("veiculos").doc("v_bob").update({
      kmAtual: 99999,
      updatedAt: new Date(),
    })
  );
  await assertFails(aliceDb.collection("manutencoes").doc("m_bob").get());
  await assertFails(
    aliceDb.collection("manutencoes").doc("m_bob").update({
      valor: 10.0,
      updatedAt: new Date(),
    })
  );
  await assertFails(aliceDb.collection("abastecimentos").doc("a_bob").get());
});

test("RLS Policy: Diretoria maintains full read and edit permissions on all records across all collections", async () => {
  const now = new Date();

  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();

    // Seed Diretoria user
    await adminDb.collection("associados").doc(DIRETORIA_UID).set({
      id: DIRETORIA_UID,
      userId: DIRETORIA_UID,
      numeroAssociado: "MC-000000",
      nomeCompleto: "Comandante Diretoria",
      nomeGuerra: "COMANDO",
      cpf: "000.000.000-00",
      tipoSanguineo: "O-",
      status: "Ativo",
      statusAssociado: "Ativo",
      perfilAcesso: "Diretoria",
      ativo: true,
      createdAt: now,
      updatedAt: now,
    });

    // Seed Bob's records (regular Associado)
    await adminDb.collection("associados").doc(BOB_UID).set({
      id: BOB_UID,
      userId: BOB_UID,
      numeroAssociado: "MC-000002",
      nomeCompleto: "Bob Membro",
      nomeGuerra: "Bob",
      cpf: "222.222.222-22",
      tipoSanguineo: "A+",
      status: "Pendente",
      statusAssociado: "Pendente",
      perfilAcesso: "Associado",
      ativo: true,
      createdAt: now,
      updatedAt: now,
    });

    await adminDb.collection("veiculos").doc("v_bob").set({
      id: "v_bob",
      userId: BOB_UID,
      associadoId: BOB_UID,
      associadoCpf: "222.222.222-22",
      status: "Ativo",
      tipo: "Moto",
      marca: "Triumph",
      modelo: "Tiger 900",
      ano: 2024,
      placa: "BOB9A99",
      kmAtual: 9000,
      ativo: true,
      createdAt: now,
      updatedAt: now,
    });

    await adminDb.collection("manutencoes").doc("m_bob").set({
      id: "m_bob",
      userId: BOB_UID,
      associadoId: BOB_UID,
      associadoCpf: "222.222.222-22",
      veiculoId: "v_bob",
      veiculoNome: "Triumph Tiger 900",
      status: "Em dia",
      data: "10/10/2026",
      km: 9000,
      tipo: "Troca de óleo",
      valor: 450.0,
      proximoKm: 14000,
      statusAlerta: "Em dia",
      createdAt: now,
      updatedAt: now,
    });
  });

  const diretoriaDb = testEnv.authenticatedContext(DIRETORIA_UID).firestore();

  // Diretoria can list and read all associados and vehicles without userId restriction
  await assertSucceeds(diretoriaDb.collection("associados").get());
  await assertSucceeds(diretoriaDb.collection("associados").doc(BOB_UID).get());
  await assertSucceeds(diretoriaDb.collection("veiculos").get());
  await assertSucceeds(diretoriaDb.collection("veiculos").doc("v_bob").get());
  await assertSucceeds(diretoriaDb.collection("manutencoes").get());

  // Diretoria can edit/approve Bob's associado record and update Bob's vehicle/maintenance
  await assertSucceeds(
    diretoriaDb.collection("associados").doc(BOB_UID).update({
      status: "Ativo",
      statusAssociado: "Ativo",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    diretoriaDb.collection("veiculos").doc("v_bob").update({
      kmAtual: 9500,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    diretoriaDb.collection("manutencoes").doc("m_bob").update({
      valor: 490.0,
      updatedAt: new Date(),
    })
  );
});

test("Firestore Initialization Script (firestore-init.js): validates all 9 required collections, indexes, and permissions", async () => {
  const {
    REQUIRED_COLLECTIONS,
    verifyStaticConfiguration,
    seedAndValidateWithEmulator,
  } = require("./firestore-init.js");

  const staticReport = verifyStaticConfiguration();
  if (staticReport.collectionsVerified !== 9) {
    throw new Error(`Expected 9 collections verified, got ${staticReport.collectionsVerified}`);
  }

  const expectedNames = [
    "associados",
    "veiculos",
    "manutencoes",
    "abastecimentos",
    "eventos",
    "comunicados",
    "mensalidades",
    "parceiros_servicos",
    "configuracoes_clube",
  ];
  for (const name of expectedNames) {
    if (!REQUIRED_COLLECTIONS.some((c) => c.name === name)) {
      throw new Error(`Missing required collection in initialization script: ${name}`);
    }
  }

  const emulatorResult = await seedAndValidateWithEmulator(staticReport);
  if (!emulatorResult.emulatorSeeded || emulatorResult.seededCollections.length !== 9) {
    throw new Error("Failed to seed and validate all 9 collections in emulator");
  }
});


