# Tasks — ELMS

> Dokumen hidup. Agent mencentang task yang selesai dan menambah temuan baru di § Backlog.
> Legenda: `[ ]` belum · `[~]` sedang dikerjakan · `[x]` selesai · 🛑 = **gate** (jelaskan rancangan → tunggu konfirmasi user sebelum kode)
> Tag: `[BE]` backend · `[FE]` frontend · `[INFRA]` repo/tooling.
> Kerjakan berurutan per fase; jangan loncat fase kecuali user meminta.

**Fase aktif:** Fase 5 (Persiapan Gate P5-01)

---

## Fase 0 — Setup
- [x] **P0-01** `[INFRA]` Struktur monorepo (`backend/`, `frontend/`, `docs/`), `.gitignore`, `.env.example`
- [x] **P0-01b** `[INFRA]` Pastikan PostgreSQL lokal aktif & buat database `elms_db` (dilakukan user)
- [x] **P0-02** `[BE]` Scaffold Spring Boot (dependency di `README.md`), tambah jjwt & springdoc (sebutkan versi), `application.yml` + `application-dev.yml`, konfigurasi `elms.*`
- [x] **P0-03** `[FE]` Scaffold Vue (`npm create vue@latest`), install axios, Tailwind v4, PrimeVue + tema + primeicons, `.env` `VITE_API_BASE_URL`
- [x] **P0-04** `[FE]` `plugins/primevue.js` (preset indigo, ToastService) + verifikasi 1 komponen PrimeVue tampil
- [x] **P0-05** `[INFRA]` Isi `ARCHITECTURE.md` § 9 (versi terpasang)

**Selesai bila:** PostgreSQL lokal aktif → backend start & konek DB → frontend tampil di :5173.

## Fase 1 — Foundation & Auth
- [x] **P1-01** `[BE]` `ApiResponse`, format error, custom exception dasar, `GlobalExceptionHandler` (validation, 404, 409, 401, 403, 500)
- [x] **P1-02** `[BE]` Entity + repository: `Department`, `Position`, `Employee` + enum `Role`, `EmploymentStatus` (skeleton; CRUD menyusul di Fase 2–3)
- [x] **P1-03** 🛑 `[BE]` **Rancangan Auth/JWT** — jelaskan alur login, isi claim, filter, aturan otorisasi; tunggu konfirmasi
- [x] **P1-04** 🛑 `[BE]` `SecurityConfig`, `JwtTokenProvider`, `JwtAuthFilter`, `AuthController` (`/auth/login`, `/auth/me`), CORS, `@EnableMethodSecurity`
- [x] **P1-05** `[BE]` `DataSeeder` (profil `dev`): 1 akun HR agar bisa login pertama kali (password dari env/config, bukan hardcode di repo publik)
- [x] **P1-06** `[BE]` Test auth: login sukses/gagal, tanpa token = 401, role salah = 403, user non-`ACTIVE` ditolak
- [x] **P1-07** `[FE]` `api/axios.js` (interceptor JWT + 401), `authApi.js`, `authStore.js`, `utils/constants.js`, `LoginView.vue`, `router/guards.js`
- [x] **P1-08** `[FE]` `DefaultLayout`, `AppHeader`, `AppSidebar` (menu per role, lihat `DESIGN.md` §5)
- [x] **P1-09** `[FE]` Komponen dasar: `BaseButton`, `BaseInput`, `BaseSelect`, `BaseTable`, `BaseModal`, `StatusBadge`, `AppToast` + `useNotify`

**Selesai bila:** HR login → masuk layout dengan menu HR; token invalid → kembali ke login; akses route tanpa role → ditolak.

## Fase 2 — Organization
- [x] **P2-01** `[BE]` Department: CRUD + DTO + validasi + unique name; delete ditolak 409 `RESOURCE_IN_USE` bila dipakai
- [x] **P2-02** `[BE]` Position: CRUD + filter `departmentId`; aturan delete sama
- [x] **P2-03** `[FE]` `DepartmentListView` (form di modal) + `departmentApi/Store`
- [x] **P2-04** `[FE]` `PositionListView` (form di modal, filter department) + `positionApi/Store`

**Selesai bila:** HR bisa CRUD department & position; hapus data yang dipakai menampilkan pesan error jelas.

## Fase 3 — Employee Management
- [x] **P3-01** `[BE]` `EmployeeRequest/Response`, `EmployeeMapper`, `EmployeeService.create/update` (hash password, saldo default, validasi manager & position∈department, email lowercase unik)
- [x] **P3-02** `[BE]` List dengan `Specification` (search nama/email, department, status) + pagination; scope tim untuk Manager
- [x] **P3-03** `[BE]` `GET /employees/me`, `GET /employees/{id}` (HR/tim/pemilik), `PATCH /employees/{id}/status`
- [x] **P3-04** `[FE]` `EmployeeListView` (search, filter, pagination) + `employeeApi/Store`
- [x] **P3-05** `[FE]` `EmployeeFormView` halaman penuh (department → position dependent select, pilih manager)
- [x] **P3-06** `[FE]` `ProfileView` (profil sendiri + saldo cuti)

**Selesai bila:** HR membuat 1 manager + 3–4 employee dengan `manager_id` benar; employee baru bisa login.

## Fase 4 — Attendance
- [x] **P4-01** `[BE]` `Attendance` entity + unique `(employee_id, date)`; `AttendanceService.checkIn/checkOut` (zona waktu, late calc dari config)
- [x] **P4-02** `[BE]` Query riwayat dengan `Specification` (employee, department, date range) + scope tim; work duration di DTO
- [x] **P4-03** `[BE]` Test `AttendanceService` (tepat waktu, telat, double check-in, check-out tanpa check-in)
- [x] **P4-04** `[FE]` Halaman Absensi: kartu status hari ini + tombol check-in/out + riwayat sendiri
- [x] **P4-05** `[FE]` `AttendanceListView` untuk HR/Manager (filter department, employee, tanggal)

**Selesai bila:** double check-in ditolak; status LATE/ON_TIME sesuai config; manager hanya melihat timnya.

## Fase 5 — Leave Management ⭐
- [ ] **P5-01** 🛑 `[BE]` **Rancangan Leave** — walk-through `DATA_MODEL.md` § 3 (submit, decide, locking, siapa pemutus); **selesaikan Open Questions terkait di `MEMORY.md` dulu**; tunggu konfirmasi
- [ ] **P5-02** `[BE]` `LeaveRequest` entity + repository + DTO + `InsufficientLeaveBalanceException`, `LEAVE_OVERLAP`
- [ ] **P5-03** 🛑 `[BE]` `LeaveService.submit` (validasi § 3.1) + `POST /leave-requests`, `GET /leave-requests/me`
- [ ] **P5-04** 🛑 `[BE]` `LeaveService.approve/reject` (`@Transactional`, otorisasi, locking, cek ulang saldo) + endpoint + `GET /leave-requests/team`
- [ ] **P5-05** `[BE]` **`LeaveServiceTest`** — semua kasus di `DATA_MODEL.md` § 3.4 (wajib)
- [ ] **P5-06** `[FE]` `LeaveRequestView`: form ajukan (date range, alasan; tampilkan saldo & estimasi hari) + riwayat pribadi dengan `StatusBadge`
- [ ] **P5-07** `[FE]` `LeaveApprovalView` (Manager/HR): daftar tim, tombol approve/reject dengan konfirmasi, refresh saldo

**Selesai bila:** alur PRD § 6.1 langkah 2–4 jalan: request → approve → saldo berkurang; request berikutnya ditolak bila saldo kurang; approve dua kali → 409.

## Fase 6 — Performance Review
- [ ] **P6-01** `[BE]` `ReviewPeriod` CRUD-minimal (HR) + `PerformanceReview` entity, unique `(period, employee)`
- [ ] **P6-02** `[BE]` `PerformanceService.create/update` (hanya manager langsung, skor 1–5, overall dihitung service) + `GET /reviews/me`, `/reviews/team`
- [ ] **P6-03** `[BE]` Test `PerformanceService` (overall calc, bukan tim → 403, duplikat → 409)
- [ ] **P6-04** `[FE]` `ReviewPeriodView` (HR, modal form)
- [ ] **P6-05** `[FE]` `ReviewFormView` (Manager: pilih periode & anggota tim, 4 aspek + komentar)
- [ ] **P6-06** `[FE]` `MyReviewView` (Employee, read-only)

**Selesai bila:** PRD § 6.1 langkah 5–6 jalan.

## Fase 7 — Dashboard
- [ ] **P7-01** `[BE]` `DashboardService`: summary (total employee, hadir hari ini, sedang cuti), distribusi department, attendance mingguan; scope tim untuk Manager
- [ ] **P7-02** `[FE]` ⚠ Konfirmasi install `chart.js` ke user → `BaseChart.vue`
- [ ] **P7-03** `[FE]` `DashboardView`: kartu ringkasan + grafik distribusi + grafik mingguan

**Selesai bila:** angka dashboard cocok dengan data hasil alur E2E.

## Fase 8 — Hardening
- [ ] **P8-01** Jalankan alur E2E PRD § 6.1 dari database kosong; catat & perbaiki bug
- [ ] **P8-02** Seed data demo (opsional, profil `dev`)
- [ ] **P8-03** Review debt di `MEMORY.md`; tulis test untuk modul yang dilewati bila perlu
- [ ] **P8-04** Lengkapi `README.md` (setup, akun demo, screenshot)

---

## Backlog / Temuan (agent menambah di sini)
_Kosong._
