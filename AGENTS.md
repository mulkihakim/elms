# AGENTS.md — ELMS (Employee Lifecycle Management System)

> File ini dibaca agent PERTAMA di setiap sesi. Sengaja singkat — detail ada di `docs/`.
> Kalau tool-mu membaca `CLAUDE.md` / `.cursorrules` / dll, buat file itu berisi 1 baris: `Baca AGENTS.md`.

## Project

HR system MVP (Spring Boot + Vue 3 + PostgreSQL) untuk belajar: layering backend yang rapi,
business logic non-trivial (leave approval), dan role-based access control.
**Bukan** HRIS komersial. Monorepo: `backend/`, `frontend/`, `docs/`.

Pemilik project belum pernah pakai Vue → jelaskan pola Vue yang dipakai secara singkat
saat pertama kali muncul, jangan asumsi dia sudah paham.

## Peta Dokumen

| File | Isi | Baca saat |
|---|---|---|
| `docs/MEMORY.md` | Status terakhir, keputusan, open questions, tech debt | **Selalu, awal sesi** |
| `docs/TASKS.md` | Checklist per fase + definition of done | **Selalu, awal sesi** |
| `docs/RULES.md` | Aturan kode, git, testing, batasan kerja agent | Sebelum menulis kode |
| `docs/PRD.md` | Fitur, user flow, permission, acceptance criteria | Saat mengerjakan fitur baru |
| `docs/ARCHITECTURE.md` | Stack, layering, folder, API, auth | Saat menyentuh struktur/endpoint/security |
| `docs/DATA_MODEL.md` | Entity, constraint, business rules (cuti, absensi, review) | Saat menyentuh entity/service/query |
| `docs/DESIGN.md` | Warna, komponen, pola halaman | Saat menulis kode frontend |
| `README.md` | Cara setup & run (untuk manusia) | Jarang |

## Alur Kerja Setiap Sesi

1. Baca `docs/MEMORY.md` (§ Status Saat Ini, § Open Questions) dan `docs/TASKS.md`.
2. Ambil task yang ditunjuk user. Kalau tidak ditunjuk, ambil task pertama yang belum `[x]`
   dan **konfirmasi dulu** ke user.
3. Baca dokumen relevan dari peta di atas (jangan baca semuanya kalau tidak perlu).
4. Kerjakan **satu task** sekaligus. Hal di luar scope → catat di `TASKS.md`/`MEMORY.md`, jangan dikerjakan.
5. Selesai → jalankan checklist "Definition of Done" di bawah.

## Aturan Kritis (versi ringkas — detail di `docs/RULES.md`)

1. **Layering:** Controller tipis → Service (logic + `@Transactional`) → Repository. Jangan expose Entity, selalu DTO.
2. **Enum** selalu `EnumType.STRING`; string enum FE = BE persis (`"PENDING"`, bukan `"pending"`).
3. **Frontend:** panggil API hanya lewat `src/api/`; komponen PrimeVue hanya lewat `components/common/Base*.vue`; state lintas komponen lewat Pinia.
4. **🛑 GATE — Leave Management & Auth/Security:** jelaskan rancangan singkat (langkah/pseudocode), **tunggu konfirmasi user**, baru generate kode lengkap.
5. **Business rule ambigu → tanya user.** Cek dulu `docs/MEMORY.md` § Open Questions; jangan asumsi diam-diam.
6. **Dependency baru** (pom.xml / package.json) → sebutkan nama + alasan ke user SEBELUM menambahkan.
7. **Jangan ubah struktur folder** yang sudah ada tanpa alasan yang dijelaskan dan disetujui.
8. **Jangan commit secret** (`.env`, `application-local.yml`, credential, JWT secret).
9. **Ikuti pola yang sudah ada** di codebase; jangan campur dua pendekatan untuk hal yang sama.
10. **Verifikasi API library sesuai versi yang terpasang** (Spring Boot/Security, PrimeVue, Vue Router) — jangan mengandalkan memori; banyak contoh lama sudah usang.

## Perintah Umum

PostgreSQL berjalan sebagai service lokal (native, **tanpa Docker**); database `elms_db`.
Jangan membuat `docker-compose.yml` atau menyarankan Docker.

```bash
cd backend  && ./mvnw spring-boot:run      # API di :8080  (Swagger: /swagger-ui.html)
cd backend  && ./mvnw test                 # unit test
cd frontend && npm run dev                 # UI di :5173
cd frontend && npm run lint                # ESLint
cd frontend && npm run format              # Prettier
```

## Definition of Done (setiap task)

- [ ] Kode jalan (backend start / frontend render) dan sesuai acceptance criteria di `TASKS.md`
- [ ] Test wajib ada untuk `LeaveService` (lihat `docs/RULES.md` §6); lint/format lolos
- [ ] Centang task di `docs/TASKS.md`
- [ ] Update `docs/MEMORY.md` (status, keputusan baru, debt, gotcha) bila ada
- [ ] Ringkasan ke user: **file yang dibuat/diubah** + **bagian yang sebaiknya di-review manual**
