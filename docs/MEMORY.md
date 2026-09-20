# Memory — ELMS

> Dokumen hidup: "ingatan" project lintas sesi. **Agent wajib mengupdate di akhir setiap sesi.**
> Jaga tetap ringkas — pangkas entri yang sudah tidak relevan. Detail fitur ada di dokumen lain, bukan di sini.

## 1. Status Saat Ini

| | |
|---|---|
| Fase aktif | Fase 5 — Leave Management (Persiapan Gate P5-01) |
| Task terakhir selesai | P4-05 & Sinkronisasi Fase 0–4 |
| Sedang dikerjakan | Penyelarasan arsitektur/konvensi Fase 0–4 sebelum masuk Fase 5 |
| Blocker | Open Questions § 3 yang menyangkut Fase 5 (Leave) harus dijawab sebelum P5-01 |
| Terakhir diupdate | 2026-09-20 |

## 2. Decision Log

| Tanggal | Keputusan | Alasan |
|---|---|---|
| 2026-09-20 | PostgreSQL native di mesin lokal, **tanpa Docker** | RAM laptop 8 GB tidak cukup untuk Docker saat development |
| 2026-09-20 | Monorepo (`backend/`, `frontend/`, `docs/`) | Satu link portfolio; docs relevan untuk FE & BE |
| 2026-09-20 | Package-by-feature di backend | Modul mandiri, mudah dicari |
| 2026-09-20 | Tanpa MapStruct di MVP (mapping manual) | Lebih mudah dipahami saat belajar; tambah bila repetitif |
| 2026-09-20 | Service = class biasa (tanpa interface + Impl) | Hindari over-engineering |
| 2026-09-20 | JWT manual (jjwt), access token saja, tanpa refresh token | Cukup untuk MVP; fokus belajar Spring Security |
| 2026-09-20 | `ddl-auto: update` di MVP, Flyway menyusul | Cepat iterasi; jadi debt (§ 4) |
| 2026-09-20 | Enum disimpan `STRING` | Aman bila urutan enum berubah |
| 2026-09-20 | PrimeVue (Aura, primary indigo) + Tailwind untuk layout; komponen dibungkus `Base*.vue` | Ganti library cukup ubah wrapper |
| 2026-09-20 | Registrasi PrimeVue di `plugins/primevue.js` (bukan inline di `main.js`) | Sesuai struktur folder; sentral |
| 2026-09-20 | Endpoint cuti: `/leave-requests` (sebelumnya campur `/leaves` & `/leave-requests`) | Ikuti aturan penamaan resource jamak kebab-case |
| 2026-09-20 | Employee tanpa hard delete (ganti `employment_status`) | FK ke banyak tabel; jejak data terjaga |
| 2026-09-20 | Urutan fase: Foundation/Auth → Organization → Employee → ... | Employee butuh Department/Position (FK) dan login butuh entity Employee |
| 2026-09-20 | Pelanggaran business rule → HTTP 409 | Konsisten dengan kasus status transition |
| 2026-09-20 | Hybrid ID: `Long` untuk Master Data (`Department`, `Position`), `UUID` untuk Entity Personal & Transaksional (`Employee`, `Attendance`, `LeaveRequest`, `PerformanceReview`) | Master data internal sederhana menggunakan auto-increment, data personal & transaksi terlindungi dari ID enumeration |

## 3. Open Questions (butuh keputusan user — agent JANGAN asumsi)

Tiap item punya **rekomendasi** (⚠ PROPOSED). Setelah user memutuskan: pindahkan ke Decision Log, hapus `⚠ PROPOSED` di `DATA_MODEL.md`.

| # | Pertanyaan | Rekomendasi | Dibutuhkan sebelum |
|---|---|---|---|
| Q1 | Apakah weekend dihitung sebagai hari cuti? Perlu tabel libur nasional? | Hanya hitung Senin–Jumat; tanpa tabel libur di MVP | P5-01 |
| Q2 | Jam kerja mulai & toleransi telat? | 09:00 WIB, tanpa grace period, configurable di `elms.attendance.work-start` | P4-01 |
| Q3 | Saldo cuti awal employee baru? | 12 hari, configurable di `elms.leave.default-balance` | P3-01 |
| Q4 | Siapa yang memutuskan cuti Manager dan cuti HR? Boleh approve cuti sendiri? | Manager → atasannya (atau HR bila tidak ada); HR → HR lain; **tidak boleh** approve milik sendiri | P5-01 |
| Q5 | Kapan saldo divalidasi? | Saat submit **dan** dicek ulang saat approve (pending tidak menahan saldo) | P5-01 |
| Q6 | Cuti overlap dengan request lain (PENDING/APPROVED) ditolak? Boleh backdate? | Overlap ditolak; backdate tidak boleh (`startDate ≥ hari ini`) | P5-01 |
| Q7 | Tipe ID: `bigint` auto-increment atau UUID? | **Selesai (Hybrid)**: Long untuk master data, UUID untuk employee/attendance/transaksi | Selesai |
| Q8 | `ON_LEAVE`: di-set otomatis (scheduler) atau dihitung dari data cuti? | Dihitung dari cuti `APPROVED` yang mencakup hari ini; `employment_status` tidak dimutasi otomatis | P7-01 |
| Q9 | `ABSENT`: disimpan atau dihitung saat laporan? | Dihitung saat laporan (hari kerja tanpa attendance & tanpa cuti approved) | P4-02 |
| Q10 | Boleh HR melihat semua review (read-only)? Review boleh diedit sampai kapan? | HR baca-saja; edit hanya selama periode berjalan | P6-02 |
| Q11 | Bahasa UI? | Indonesia (label & pesan error); kode berbahasa Inggris | P1-07 |

## 4. Technical Debt

| Item | Alasan ditunda | Ditangani di |
|---|---|---|
| `ddl-auto: update` (belum Flyway) | Iterasi cepat di MVP | Setelah MVP |
| _(tambahkan modul CRUD yang dilewati tanpa test di sini)_ | | |

## 5. Gotchas & Learnings

- Spring Boot tidak membaca `.env` otomatis — lihat `ARCHITECTURE.md` § 8.
- JWT secret < 32 byte membuat jjwt (HS256) gagal (`WeakKeyException`) — pastikan placeholder dev cukup panjang.
- Nama paket tema PrimeVue berubah antar versi (`@primevue/themes` lama vs `@primeuix/themes` baru) — verifikasi di docs saat Fase 0.
- Contoh/tutorial Spring lama sering mengasumsikan Spring Boot 3 / Spring Security 6 (`WebSecurityConfigurerAdapter` sudah tidak ada; pakai `SecurityFilterChain` bean). Cek migration guide bila versi terpasang lebih baru.
- PrimeVue `Dropdown` sudah diganti `Select`; `Chart` PrimeVue memerlukan `chart.js` terpasang.
- Aura mengikuti dark mode OS secara default — nonaktifkan sesuai `DESIGN.md` § 2 bila tampilan aneh.

## 6. Session Log (5 terakhir; entri terbaru di atas)

| Tanggal | Ringkasan | File utama yang berubah |
|---|---|---|
| 2026-09-20 | Audit & sinkronisasi docs ↔ codebase (Fase 0–4 dicatat selesai, gap diatasi, RULES.md dibuat) | `AGENTS.md`, `docs/*`, `backend/*`, `frontend/*` |
| 2026-09-20 | Docs dirombak menjadi AGENTS + PRD/ARCHITECTURE/DATA_MODEL/RULES/DESIGN/TASKS/MEMORY | `AGENTS.md`, `docs/*` |
