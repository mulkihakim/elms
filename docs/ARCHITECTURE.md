# Architecture — ELMS

## 1. Tech Stack

| Layer | Teknologi | Catatan |
|---|---|---|
| Backend | Java 21 (atau 17), Spring Boot (stable terbaru), Maven | Catat versi persis di § 9 setelah scaffold |
| Data | PostgreSQL 16, Spring Data JPA (Hibernate) | `ddl-auto: update` untuk MVP |
| Security | Spring Security + JWT (jjwt) + BCrypt | Stateless |
| API docs | springdoc-openapi | Versi harus cocok dengan major Spring Boot — cek tabel kompatibilitas springdoc |
| Frontend | Vue 3 (Composition API, `<script setup>`), Vite, Pinia, Vue Router, Axios | JavaScript (bukan TS) |
| UI | PrimeVue (tema Aura, primary `indigo`) + Tailwind CSS v4 | PrimeVue = tampilan & perilaku komponen; Tailwind = layout di sekitarnya |
| Infra lokal | PostgreSQL native (service lokal, tanpa Docker) | Database `elms_db` |

**Sengaja tidak dipakai (hindari over-engineering di awal):** MapStruct (mapping manual dulu), interface `XxxService`
+ `XxxServiceImpl` (class biasa saja), Flyway (nanti), Redis/cache, refresh token.

## 2. Overview

```
┌──────────────────────┐
│       Vue 3           │  Pinia + Router + PrimeVue
└──────────┬────────────┘
           │ REST API (JSON, JWT di header)
           ▼
┌──────────────────────┐
│    Spring Boot         │
│ Controller             │  ← terima request, @Valid DTO
│ Service                │  ← business logic, transaction
│ Repository             │  ← akses data (Spring Data JPA)
│ Security / JWT         │  ← autentikasi & otorisasi
└──────────┬────────────┘
           ▼
┌──────────────────────┐
│     PostgreSQL         │
└──────────────────────┘
```

Prinsip: **Controller tipis, Service berisi logic, Repository hanya akses data.**

## 3. Backend Layering

| Layer | Tanggung jawab | Tidak boleh |
|---|---|---|
| `controller` | Terima HTTP, validasi format (`@Valid`), panggil service, bungkus response | Business logic, akses repository langsung |
| `service` | Business logic, orkestrasi, `@Transactional` | Membangun HTTP response, akses `HttpServletRequest` |
| `repository` | Query data (`JpaRepository` / `JpaSpecificationExecutor`) | Business logic |
| `dto` | Bentuk request/response API | Logic apapun |
| `mapper` | Entity ↔ DTO (manual, class biasa) | Business logic |
| `entity` | Tabel DB (JPA) | Logic kompleks (method sederhana seperti `isActive()` boleh) |
| `exception` | Custom exception + `GlobalExceptionHandler` | — |
| `config` | Security, CORS, OpenAPI | — |

### Struktur folder backend (package-by-feature)

```
src/main/java/com/elms/backend/
├── BackendApplication.java
├── common/
│   ├── exception/   (GlobalExceptionHandler, ResourceNotFoundException,
│   │                 InvalidStatusTransitionException, InsufficientLeaveBalanceException, ...)
│   ├── response/    (ApiResponse)
│   └── config/      (SecurityConfig, JwtConfig, CorsConfig, AppProperties)
├── auth/            (AuthController, AuthService, dto/, jwt/{JwtTokenProvider, JwtAuthFilter})
├── employee/        (Employee, EmployeeController, EmployeeService, EmployeeRepository, dto/, mapper/)
├── organization/
│   ├── department/  (Department, DepartmentController, ...)
│   └── position/    (Position, PositionController, ...)
├── attendance/      (Attendance, AttendanceController, AttendanceService, AttendanceRepository, dto/)
├── leave/           (LeaveRequest, LeaveController, LeaveService ⭐, LeaveRepository, dto/)
├── performance/     (ReviewPeriod, PerformanceReview, PerformanceController, PerformanceService, ...)
└── dashboard/       (DashboardController, DashboardService)
```

## 4. Konvensi API

- Base path: `/api/v1`. Path kebab-case, resource jamak (`/employees`, `/leave-requests`); verb hanya untuk aksi non-CRUD (`/attendance/check-in`).
- **Sukses:** `{ "success": true, "data": {...}, "message": "opsional" }`
- **Error:** `{ "success": false, "error": { "code": "LEAVE_BALANCE_INSUFFICIENT", "message": "Sisa cuti tidak mencukupi" } }`
  - Error validasi field: tambahkan `error.fields: { "email": "Format email tidak valid" }` agar FE bisa menampilkan error per field.
- **Pagination:** `?page=0&size=20&sort=createdAt,desc`; endpoint list mengembalikan struktur `Page<T>` Spring Data di dalam `data` (jangan bungkus manual).

### HTTP status

| Status | Dipakai untuk |
|---|---|
| 200 / 201 | OK / Created |
| 400 | Validasi input gagal |
| 401 | Belum login / token tidak valid |
| 403 | Role atau scope tidak sesuai |
| 404 | Resource tidak ada |
| 409 | Pelanggaran business rule (transisi status tidak valid, saldo kurang, overlap, double check-in, data masih dipakai) |

### Kode error

`VALIDATION_ERROR`, `UNAUTHENTICATED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `EMAIL_ALREADY_EXISTS`,
`RESOURCE_IN_USE`, `INVALID_STATUS_TRANSITION`, `LEAVE_BALANCE_INSUFFICIENT`, `LEAVE_OVERLAP`,
`ALREADY_CHECKED_IN`, `NOT_CHECKED_IN`, `ALREADY_CHECKED_OUT`, `REVIEW_ALREADY_EXISTS`, `INTERNAL_ERROR`.
Tambahkan kode baru di sini bersamaan dengan kode-nya.

### Endpoint per modul

| Method & Path | Akses | Catatan |
|---|---|---|
| `POST /auth/login` | Publik | Return JWT + info user |
| `GET /auth/me` | Login | Identitas dari token |
| `GET /employees` | HR (semua), Manager (tim) | Filter: `search`, `departmentId`, `status`; scope tim dipaksa di service |
| `GET /employees/me` | Login | Profil + saldo cuti |
| `GET /employees/{id}` | HR, Manager (tim), pemilik | |
| `POST /employees` | HR | Hash password, set saldo default |
| `PUT /employees/{id}` | HR | |
| `PATCH /employees/{id}/status` | HR | Pengganti delete |
| `GET /departments`, `GET /positions` | Login | `positions?departmentId=` |
| `POST/PUT/DELETE /departments`, `/positions` | HR | Delete ditolak 409 bila masih dipakai |
| `POST /attendance/check-in` | Login | |
| `POST /attendance/check-out` | Login | |
| `GET /attendance/me` | Login | `from`, `to` |
| `GET /attendance` | HR (semua), Manager (tim) | `employeeId`, `departmentId`, `from`, `to` |
| `POST /leave-requests` | Login | |
| `GET /leave-requests/me` | Login | |
| `GET /leave-requests/team` | Manager (tim), HR (semua) | `status` filter |
| `PATCH /leave-requests/{id}/approve` | Manager (tim), HR | 🛑 gate |
| `PATCH /leave-requests/{id}/reject` | Manager (tim), HR | 🛑 gate |
| `POST /review-periods`, `GET /review-periods` | HR buat; login baca | |
| `POST /reviews`, `PUT /reviews/{id}` | Manager (tim, reviewer sama) | |
| `GET /reviews/me` | Login | Read-only |
| `GET /reviews/team?periodId=` | Manager (tim), HR (read-only) | |
| `GET /dashboard/summary` | HR (semua), Manager (tim) | |
| `GET /dashboard/department-distribution` | HR, Manager (tim) | |
| `GET /dashboard/attendance-weekly` | HR, Manager (tim) | |

> Daftar ini adalah rencana; tambahkan/ubah sambil jalan **dan update tabel ini**.

## 5. Autentikasi & Otorisasi 🛑 (gate — jelaskan rancangan ke user sebelum kode)

- Login (email + password) → server mengeluarkan **access token JWT**, claim: `sub` (employeeId), `role`, `email`. Expiry 24 jam (`jwt.expiration-ms`).
- Request terproteksi membawa `Authorization: Bearer <token>`. `JwtAuthFilter` mengisi `SecurityContext` (authority `ROLE_HR`, `ROLE_MANAGER`, `ROLE_EMPLOYEE`).
- Session stateless; CSRF dinonaktifkan (API token-based); CORS mengizinkan origin FE (`http://localhost:5173` di dev).
- Employee dengan status `RESIGNED`/`TERMINATED` tidak boleh login.
- **Dua lapis otorisasi — jangan dicampur:**
  1. **Role kasar** → `@PreAuthorize("hasRole('HR')")` di controller (aktifkan `@EnableMethodSecurity`).
  2. **Scope data** (tim sendiri / milik sendiri) → di service layer atau security bean (`@leaveSecurity.isTeamMember(...)`), lewat query berdasarkan `managerId`. Bukan filter di frontend.
- Jangan sebar `if (user.getRole() == ...)` di banyak service; sentralisasi lewat annotation/security bean.
- **JWT secret:** wajib minimal 32 byte (256-bit) untuk HS256, kalau tidak jjwt melempar `WeakKeyException`. Default placeholder di `application.yml` harus memenuhi ini atau aplikasi gagal start.

## 6. Error Handling

- Exception spesifik per kasus (bukan `RuntimeException` generik): `ResourceNotFoundException`, `InvalidStatusTransitionException`, `InsufficientLeaveBalanceException`, dst.
- Semua ditangkap di satu `GlobalExceptionHandler` (`@RestControllerAdvice`) dan dipetakan ke format error § 4. Stack trace tidak pernah dikirim ke client.
- Harus ter-cover: validation (`MethodArgumentNotValidException`), `AccessDeniedException` → 403, auth gagal → 401, `DataIntegrityViolationException` (mis. unique) → 409, fallback → 500.

## 7. Frontend Architecture

```
src/
├── main.js, App.vue
├── api/          axios.js (instance + interceptor JWT & 401), employeeApi.js, leaveApi.js, ...
├── stores/       authStore.js, employeeStore.js, leaveStore.js, ... (satu store per domain)
├── router/       index.js, guards.js (role-based)
├── plugins/      primevue.js (config + tema Aura preset indigo + ToastService)
├── views/        auth/, employee/, organization/, attendance/, leave/, performance/, dashboard/
├── components/
│   ├── common/   Base*.vue, StatusBadge.vue, ...  (lihat DESIGN.md §4)
│   └── layout/   AppHeader.vue, AppSidebar.vue, DefaultLayout.vue
├── composables/  useAuth.js, usePagination.js, useNotify.js
└── utils/        formatDate.js, constants.js (enum mirror backend)
```

- **Axios terpusat** (`api/axios.js`): interceptor request menyisipkan JWT; interceptor response menangani 401 (logout + redirect ke login).
- **Pinia:** satu store per domain, tidak ada store raksasa. Komponen tidak memanggil axios langsung.
- **Router guard:** route login-only `meta: { requiresAuth: true }`; route per role `meta: { roles: [...] }`, dicek di `guards.js`.
- **`views/`** = halaman yang 1:1 dengan route; **`components/`** = reusable, sebisa mungkin tidak tahu soal router/store.
- **PrimeVue** hanya boleh diimport di `components/common/Base*.vue` dan `plugins/primevue.js`.
- Enum FE (`utils/constants.js`) harus **string-identik** dengan Java enum di backend.

## 8. Environment & Config

- Backend: `application.yml` + `application-dev.yml` / `application-prod.yml`. Kredensial via environment variable.
- **Spring Boot tidak membaca `.env` secara otomatis.** Pilihan tanpa dependency baru: set env var di run config IDE / shell, atau
  tambahkan `spring.config.import: optional:file:.env[.properties]` dan tulis `.env` dalam format `KEY=value`.
- Commit `.env.example` (tanpa nilai rahasia), jangan commit `.env`.
- Config aplikasi custom (dibaca lewat `@ConfigurationProperties`, prefix `elms`):

```yaml
elms:
  timezone: Asia/Jakarta
  attendance:
    work-start: "09:00"        # check-in setelah jam ini = LATE  (⚠ menunggu konfirmasi, lihat MEMORY)
  leave:
    default-balance: 12        # hari, saldo awal employee baru  (⚠ menunggu konfirmasi)
```

- Frontend: `.env` → `VITE_API_BASE_URL=http://localhost:8080/api/v1`.

## 9. Versi Terpasang (isi setelah scaffold — Fase 0)

| Komponen | Versi |
|---|---|
| Java | 21 (OpenJDK 21.0.12.1) |
| Spring Boot | 4.1.1 |
| jjwt | 0.12.6 |
| springdoc-openapi | 2.8.5 |
| Vue / Vite / Pinia / Vue Router | Vue 3.5.42 / Vite 8.2.2 / Pinia 4.0.3 / Vue Router 5.3.1 |
| PrimeVue + paket tema | PrimeVue 4.2.1 (`@primevue/themes` 4.2.1) |
| Tailwind | 4.3.3 |

> Agent: verifikasi API library terhadap versi ini. Tutorial & contoh lama sering mengasumsikan major version yang berbeda
> (mis. Spring Security config style, package Jackson, nama paket tema PrimeVue).
