# Security Specification - Moto Clube (Iron Side)

## 1. Data Invariants
1. **Authenticated Identity**: Every Firestore operation requires an authenticated user (`request.auth != null`).
2. **PII Isolation & Ownership (`associados`)**: Personal and sensitive data (CPF, RG, CNH, Endereço, Contatos de Emergência) in `/associados/{userId}` can only be read or modified by the document owner (`request.auth.uid == userId`) or an authorized Admin (`isAdmin()`).
3. **Role & Privilege Escalation Prevention**: Only an Admin (`isAdmin()`) can change `perfilAcesso` or approve `statusAssociado` to `Ativo` for other users, or the bootstrapped admin (`aalmeida.eds@gmail.com` with `email_verified == true`). Regular users creating their profile must start with `perfilAcesso == 'Associado'` or if bootstrapped admin, `'SuperAdmin'`.
4. **Vehicle & Garage Ownership (`veiculos`, `manutencoes`, `abastecimentos`, `seguros_veiculos`)**: Every garage document must have `userId == request.auth.uid` on creation, `userId` is immutable on update, and list/get queries require `resource.data.userId == request.auth.uid` (or `isAdmin()`).
5. **Financial & Mensalidades (`mensalidades`)**: Regular users can only read/query their own mensalidades (`resource.data.userId == request.auth.uid`) and can only update payment status/proof fields or create their initial dues, while Admins can manage all mensalidades.
6. **Club Wide Entities (`eventos`, `comunicados`, `parceiros_clube`, `contatos_emergencia_publicos`, `configuracao_clube`)**: Readable by authenticated members where `resource.data.ativo == true` (for list queries, clients filter `.whereEqualTo("ativo", true)`), writable by Admins or authenticated members registering event attendance/SOS partners.
7. **Temporal Integrity & Immutability**: `createdAt` and `updatedAt` must be valid server timestamps (`<= request.time`), and `createdAt` plus `userId` are strictly immutable during updates.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read/Write**: Caller with `request.auth == null` attempts to list `/associados` or `/veiculos`. -> REJECTED.
2. **Cross-User PII Read**: User Bob (`uid: bob_456`) attempts `get` on `/associados/alice_123`. -> REJECTED.
3. **Unfiltered List Query Scraping**: User Alice attempts `db.collection("veiculos").get()` without `.where("userId", "==", "alice_123")`. -> REJECTED.
4. **Identity Spoofing on Create**: User Alice creates a `/veiculos/{id}` document with `userId: "bob_456"`. -> REJECTED.
5. **Owner Mutation on Update**: User Alice updates `/veiculos/{id}` changing `userId` from `"alice_123"` to `"bob_456"`. -> REJECTED.
6. **Privilege Escalation (Self-Admin)**: Unverified or non-admin user attempts to set `perfilAcesso: "SuperAdmin"` on `/associados/{uid}` update. -> REJECTED.
7. **Email Spoofing Attack**: Attacker with `email: "aalmeida.eds@gmail.com"` but `email_verified: false` attempts admin write. -> REJECTED.
8. **Shadow Update (Ghost Field Injection)**: User sends update to `/veiculos/{id}` including an undeclared field `isVerified: true`. -> REJECTED by `hasOnly()`.
9. **Value Poisoning (1MB String Bomb)**: User sends a 10,000-character string in `modelo` or `placa`. -> REJECTED by `.size()` boundary checks.
10. **Path ID Poisoning**: Caller attempts to create a document with malicious ID containing spaces or special characters. -> REJECTED by `isValidId()`.
11. **Timestamp Manipulation (Future Timestamp)**: Client sends a future timestamp `> request.time` or mutates `createdAt` on update. -> REJECTED.
12. **Required Field Deletion**: Client sends `FieldValue.delete()` on required field `placa` or `marca` during update. -> REJECTED by `isValidVeiculo(incoming())`.
