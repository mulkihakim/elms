# Memory — ELMS

> Dokumen hidup: "ingatan" project lintas sesi. **Agent wajib mengupdate di akhir setiap sesi.**
> Jaga tetap ringkas — pangkas entri yang sudah tidak relevan. Detail fitur ada di dokumen lain, bukan di sini.

## 1. Status Saat Ini
 
| | |
|---|---|
| Fase aktif | MVP Selesai (Semua Fase 0 s/d 8 Tuntas) |
| Task terakhir selesai | Fase 8 — Hardening Selesai (P8-01 s/d P8-04) |
| Sedang dikerjakan | Proyek ELMS MVP siap didemokan dan digunakan untuk portfolio |
| Blocker | Tidak ada |
| Terakhir diupdate | 2026-09-21 |

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
| 2026-09-20 | Hari cuti dihitung hanya Senin–Jumat (skip weekend, tanpa tabel libur nasional di MVP) | MVP fokus alur kerja dasar |
| 2026-09-20 | Approval cuti: Manager oleh atasannya/HR; HR oleh HR lain; dilarang self-approval (403) | Menjaga integritas tata kelola organisasi |
| 2026-09-20 | Validasi saldo cuti: cek saat submit (pending belum memotong), cek ulang + lock pessimistic + potong saldo saat approve | Menghindari over-allocation tanpa mengorbankan kuota pending |
| 2026-09-20 | Validasi tanggal cuti: dilarang backdate (startDate >= today) & overlap dengan PENDING/APPROVED ditolak 409 LEAVE_OVERLAP | Mencegah anomali transaksi jadwal |
| 2026-09-20 | Review kinerja: hanya atasan langsung yang mengisi; HR baca-saja; pengisian/edit hanya selama rentang periode berjalan | Menjaga objektivitas hierarki dan integritas periode evaluasi |
| 2026-09-21 | Dashboard role-scoped (HR: organisasi & chart departemen; Manager: tim & chart tren; Employee: personal metrics) & Chart.js diinstall | Menjaga privasi organisasi, mencegah kebocoran data master ke non-HR, serta memvisualisasikan tren presensi |
| 2026-09-21 | Pasang `springdoc-openapi-starter-webmvc-ui:3.1.1` & `OpenApiConfig` (JWT Bearer support) | Menyediakan Swagger UI interaktif di `/swagger-ui.html` yang kompatibel dengan Spring Boot 4.x dan mengizinkan testing endpoint berotentikasi JWT |

## 3. Open Questions (butuh keputusan user — agent JANGAN asumsi)

Tiap item punya **rekomendasi** (⚠ PROPOSED). Setelah user memutuskan: pindahkan ke Decision Log, hapus `⚠ PROPOSED` di `DATA_MODEL.md`.

| # | Pertanyaan | Rekomendasi | Dibutuhkan sebelum |
|---|---|---|---|
| Q2 | Jam kerja mulai & toleransi telat? | 09:00 WIB, tanpa grace period, configurable di `elms.attendance.work-start` | P4-01 |
| Q3 | Saldo cuti awal employee baru? | 12 hari, configurable di `elms.leave.default-balance` | P3-01 |
| Q8 | `ON_LEAVE`: di-set otomatis (scheduler) atau dihitung dari data cuti? | Dihitung dari cuti `APPROVED` yang mencakup hari ini; `employment_status` tidak dimutasi otomatis | P7-01 |
| Q9 | `ABSENT`: disimpan atau dihitung saat laporan? | Dihitung saat laporan (hari kerja tanpa attendance & tanpa cuti approved) | P4-02 |
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
| 2026-09-21 | Fase 8 (Hardening): Penyempurnaan seeder komprehensif (10 karyawan, 5 divisi, presensi 7 hari, cuti multi-status, review matriks), pembaruan README.md (filosofi vibe coding & learning journey, akun demo, galeri screenshot), verifikasi manual tuntas | `backend/common/seeder/DataSeeder.java`, `README.md`, `docs/screenshots/*`, `docs/*` |
| 2026-09-21 | Implementasi modul Dashboard Fase 7 (P7-01 s/d P7-03): backend DashboardService & Controller (HR: organisasi, Manager: tim, Employee: personal), 5 unit test lolos (57 total), instalasi chart.js, BaseChart, dan DashboardView role-scoped | `backend/dashboard/*`, `frontend/src/components/common/BaseChart.vue`, `frontend/src/views/dashboard/*`, `frontend/src/api/*`, `frontend/src/stores/*`, `docs/*` |
| 2026-09-20 | Implementasi modul Performance Review Fase 6 (P6-01 s/d P6-06): entity ReviewPeriod & PerformanceReview, service (overallScore avg 2 decimal, team validation, active period window), controller, 11 unit test lolos (52 total), UI Vue & Pinia store | `backend/performance/*`, `frontend/src/views/performance/*`, `frontend/src/api/*`, `frontend/src/stores/*`, `docs/*` |
| 2026-09-20 | Penyusunan Implementation Plan Performance Review (Fase 6: P6-01 s/d P6-06) & perumusan diskusi Open Questions | `docs/TASKS.md`, `docs/MEMORY.md`, `implementation_plan.md` |
| 2026-09-20 | Implementasi lengkap modul Leave Management Fase 5 (P5-01 s/d P5-07): entity, DTO, repo, service dengan pessimistic lock, exception, controller, 14 unit test lolos, UI Vue & Pinia store | `backend/leave/*`, `backend/common/exception/*`, `frontend/src/*`, `docs/*` |
