# Coding Guidelines & Batasan Kerja Agent — ELMS

## 1. Prinsip Umum

- Kode harus bisa dibaca tanpa perlu tanya "kenapa ini gini" — kalau logic-nya
  tidak jelas, tambahkan komentar singkat, jangan biarkan magic number/logic
  tersembunyi.
- Konsisten dengan pola yang sudah ada di codebase. Kalau modul sebelumnya
  pakai pola tertentu (misal MapStruct untuk mapping), modul baru ikuti pola
  yang sama — jangan campur beberapa pendekatan untuk hal yang sama.
- Jangan tambah dependency/library baru tanpa menyebutkan alasannya ke user
  terlebih dahulu (supaya user tahu apa yang baru masuk ke `pom.xml`/`package.json`).

## 2. Git & Commit

- Branch naming: `feature/leave-approval`, `fix/attendance-late-calc`,
  `chore/setup-security-config`
- Commit message (Conventional Commits):
  ```
  feat(leave): add balance validation before approval
  fix(attendance): correct late calculation timezone
  refactor(employee): extract mapper to separate class
  docs: update PRD for performance review module
  ```
- Jangan commit file `.env`, `application-local.yml`, atau credential apapun.

## 3. Backend — Aturan Wajib

- Semua endpoint yang mengubah data (`POST`/`PUT`/`PATCH`/`DELETE`) di service
  layer yang menyentuh lebih dari 1 tabel **harus** `@Transactional`.
- Validasi input pakai Bean Validation (`@NotNull`, `@Email`, dst) di DTO,
  jangan validasi manual di controller kalau bisa pakai annotation.
- Jangan expose Entity JPA langsung sebagai response API — selalu lewat DTO.
- Query dengan filter dinamis (banyak parameter opsional) gunakan
  `Specification`, jangan bikin banyak method `findByXAndY...` yang bercabang.
- Setiap custom exception harus ditangani di `GlobalExceptionHandler`, jangan
  biarkan stack trace mentah terkirim ke client.
- Password wajib di-hash (`BCryptPasswordEncoder`), jangan pernah simpan plain text.

## 4. Frontend — Aturan Wajib

- Semua panggilan API lewat file di `api/`, jangan panggil `axios` langsung
  dari dalam komponen `.vue`.
- State yang dipakai lebih dari 1 komponen wajib lewat Pinia store, jangan
  prop-drilling berlapis-lapis.
- Route yang butuh login wajib punya `meta: { requiresAuth: true }` dan route
  yang butuh role tertentu wajib punya `meta: { roles: [...] }`, dicek di
  `router/guards.js`.
- Tangani state loading & error di setiap pemanggilan API (jangan biarkan UI
  diam tanpa feedback saat request gagal).

## 5. Batasan Kerja Agent (Penting — baca sebelum generate kode)

| Situasi | Yang harus dilakukan agent |
|---|---|
| Diminta buat modul CRUD sederhana (Department, Position) | Boleh generate langsung end-to-end |
| Diminta buat/ubah **Leave Management** atau **Auth/Security** | Jelaskan dulu rancangan singkat (langkah/pseudocode), tunggu konfirmasi, baru generate kode lengkap |
| Menemukan business rule yang ambigu (mis. apakah weekend dihitung sebagai leave day) | Tanyakan ke user, jangan asumsi diam-diam |
| Perlu menambah library/dependency baru | Sebutkan nama library & alasannya sebelum menambahkan |
| Selesai mengerjakan sesuatu | Rangkum: file yang dibuat/diubah + bagian mana yang sebaiknya di-review manual oleh user |
| Mengubah struktur folder yang sudah ada | Jelaskan alasan perubahan, jangan restrukturisasi besar tanpa diminta |

## 6. Testing (minimal, jangan diskip untuk modul kunci)

- `LeaveService` (validasi balance, transisi status) — wajib ada unit test
  dasar (mis. JUnit + Mockito): kasus balance cukup, balance tidak cukup,
  approve dua kali.
- Modul CRUD sederhana boleh tanpa test dulu di tahap MVP, tapi tandai sebagai
  technical debt di `AGENTS.md` § Status Project kalau memang dilewati.
