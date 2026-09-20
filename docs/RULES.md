# Coding Rules, Git, Testing & Batasan Agent — ELMS

> File ini memuat aturan teknis, konvensi git, standar testing, dan batasan kerja agent.
> Agent wajib membaca dokumen ini sebelum menulis kode.

---

## 1. Prinsip Umum & Arsitektur

1. **Layering Backend:**
   - `Controller` tipis: hanya menerima HTTP request, validasi `@Valid`, memanggil `Service`, dan membungkus hasil ke `ApiResponse`. Dilarang ada business logic atau akses `Repository` langsung.
   - `Service`: tempat seluruh business logic, orkestrasi, dan transaksi `@Transactional`. Dilarang membangun HTTP response (`ResponseEntity`) atau mengakses `HttpServletRequest`.
   - `Repository`: interface Spring Data JPA (`JpaRepository`, `JpaSpecificationExecutor`). Hanya untuk query data. Dilarang memuat logic.
   - `DTO`: bentuk data request dan response API. Tidak boleh mengekspos Entity JPA langsung ke client.
   - `Mapper`: pemetaan Entity ↔ DTO secara manual di kelas terpisah (hindari magic library jika belum diperlukan).

2. **Enum:**
   - Selalu gunakan `@Enumerated(EnumType.STRING)` di JPA. Dilarang `ORDINAL`.
   - String enum di Frontend (`src/utils/constants.js`) harus **identik persis** dengan Java enum di Backend (misalnya `"PENDING"`, `"ACTIVE"`).

3. **Database & Transaksi:**
   - Operasi multi-tabel atau mutasi saldo/status wajib berada dalam satu blok `@Transactional`.
   - Dilarang hard delete pada data yang memiliki relasi transaksional (seperti `Employee`). Gunakan soft delete lewat mutasi status (`employment_status`).
   - Unique constraints di database harus didampingi validasi ramah di service layer (cek keberadaan data sebelum save).

4. **Error Handling & Response:**
   - Semua custom exception ditangani di `GlobalExceptionHandler` (`@RestControllerAdvice`).
   - Format response sukses: `{ "success": true, "data": {...}, "message": "opsional" }`.
   - Format response error: `{ "success": false, "error": { "code": "...", "message": "...", "fields": { "field": "pesan" } } }`.
   - Pelanggaran business rule (saldo cuti kurang, double check-in, status invalid, resource in use) harus mengembalikan **HTTP 409 Conflict**.
   - Stack trace database / internal server tidak boleh bocor ke client (log di server via `log.error`).

---

## 2. Frontend Conventions

1. **PrimeVue Encapsulation:**
   - PrimeVue hanya boleh diimpor di `src/components/common/Base*.vue` dan `src/plugins/primevue.js`.
   - Komponen view/fitur dilarang mengimpor library PrimeVue secara langsung (`import ... from 'primevue/...'`). Gunakan wrapper `BaseButton`, `BaseInput`, `BaseSelect`, `BaseTable`, `BaseModal`, `StatusBadge`, `AppToast`, dll.
   - Gunakan composable `useNotify()` untuk menampilkan feedback notifikasi toast.

2. **Panggilan API:**
   - Semua panggilan HTTP wajib melalui file API di `src/api/*` (menggunakan Axios instance terpusat di `src/api/axios.js`).
   - Komponen `.vue` dilarang memanggil `axios` langsung.

3. **State Management & Navigasi:**
   - State lintas komponen dikelola dengan Pinia (`src/stores/*`). Satu store per domain.
   - Route guard: route login-only memiliki `meta: { requiresAuth: true }`, route dengan batasan role memiliki `meta: { roles: [...] }`.
   - Menu navigasi disesuaikan dengan role sesuai matriks izin di `docs/DESIGN.md` § 5.

4. **Tampilan & Styling:**
   - Gunakan Tailwind CSS untuk layout, spacing, dan container.
   - Tidak ada fitur dark mode di MVP; jangan menambahkan class `dark:`.
   - Label UI dan pesan error berbahasa Indonesia; identifier kode berbahasa Inggris.

---

## 3. Git & Commit Conventions

1. **Commit Message Format (Conventional Commits):**
   ```
   feat(leave): add balance validation before approval
   fix(attendance): correct late calculation timezone
   refactor(employee): replace hard delete with status update
   docs: update TASKS and MEMORY for phase 4
   test(leave): add unit test for double approval
   ```

2. **Batasan File:**
   - Dilarang meng-commit secret, credentials, file `.env`, atau private key ke git.
   - Pastikan `.env.example` diperbarui jika ada env variable baru yang dibutuhkan.

---

## 4. Batasan Kerja Agent (Critical Gates)

| Situasi | Tindakan Wajib Agent |
|---|---|
| Modul CRUD sederhana (Department, Position) | Boleh langsung generate kode end-to-end |
| **🛑 GATE: Leave Management & Auth/Security** | **Wajib jelaskan rancangan singkat (langkah/pseudocode), tunggu persetujuan user, baru generate kode** |
| Business rule ambigu | Cek `docs/MEMORY.md` § Open Questions; jika belum ada, **tanyakan ke user**, jangan berasumsi diam-diam |
| Menambah dependency baru (`pom.xml` / `package.json`) | **Sebutkan nama library dan alasan ke user SEBELUM menambahkan** |
| Mengubah struktur folder yang sudah ada | Jelaskan alasan dan minta persetujuan sebelum restrukturisasi besar |
| Setelah menyelesaikan task | Berikan ringkasan file yang diubah + bagian yang perlu di-review manual oleh user |

---

## 5. Testing Requirements

1. **Modul Kritis (Wajib Unit Test):**
   - `LeaveService`: Wajib memiliki unit test menyeluruh (`LeaveServiceTest`) mencakup skenario:
     - Saldo cuti mencukupi (berhasil submit/approve)
     - Saldo cuti tidak cukup (ditolak 409)
     - Approval dua kali pada pengajuan yang sama (ditolak 409)
     - Penolakan pengajuan (saldo tidak berkurang)
     - Non-manager / bukan atasan langsung mencoba approve (ditolak 403)
     - Pemohon mencoba approve cuti miliknya sendiri (ditolak 403)
     - Saldo berkurang tepat sebesar `requestedDays`
   - `AttendanceService`: Test tepat waktu, terlambat, double check-in, check-out tanpa check-in.
   - `Auth`: Test login sukses/gagal, token invalid = 401, role salah = 403, status non-ACTIVE ditolak.

2. **Modul CRUD Standar:**
   - Diperbolehkan tanpa test lengkap saat fase MVP cepat, namun jika dilewati harus dicatat sebagai technical debt di `docs/MEMORY.md` § 4.
