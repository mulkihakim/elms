# Arsitektur — ELMS

## 1. Overview

```
┌──────────────────────┐
│       Vue 3           │
│   Composition API     │
│   Pinia + Router      │
└──────────┬────────────┘
           │ REST API (JSON, JWT di header)
           ▼
┌──────────────────────┐
│    Spring Boot         │
│                        │
│ Controller             │  ← terima request, validasi input (DTO)
│ Service                │  ← business logic, transaction
│ Repository             │  ← akses data (Spring Data JPA)
│ Security / JWT         │  ← autentikasi & otorisasi
└──────────┬────────────┘
           │
           ▼
┌──────────────────────┐
│     PostgreSQL         │
└──────────────────────┘
```

Prinsip: **Controller tipis, Service berisi logic, Repository hanya akses data.**
Jangan taruh business logic di Controller atau Repository.

## 2. Backend Layering (detail)

| Layer | Tanggung jawab | Tidak boleh |
|---|---|---|
| `controller` | Terima HTTP request, validasi format (via `@Valid`), panggil service, mapping response | Business logic, akses repository langsung |
| `service` | Business logic, orchestrasi, `@Transactional` | Membangun HTTP response, akses `HttpServletRequest` |
| `repository` | Query data (Spring Data JPA / `JpaSpecificationExecutor` bila perlu filter dinamis) | Business logic |
| `dto` | Request/Response shape untuk API | Logic apapun |
| `mapper` | Konversi Entity ↔ DTO | Business logic |
| `entity` | Representasi tabel DB (JPA) | Logic bisnis kompleks (boleh method sederhana seperti `isActive()`) |
| `exception` | Custom exception + global handler (`@ControllerAdvice`) | — |
| `config` | Security config, CORS, Swagger/OpenAPI, dll | — |

## 3. Konvensi API

- Base path: `/api/v1/...`
- Format response sukses:
  ```json
  {
    "success": true,
    "data": { ... },
    "message": "Optional message"
  }
  ```
- Format response error:
  ```json
  {
    "success": false,
    "error": {
      "code": "LEAVE_BALANCE_INSUFFICIENT",
      "message": "Sisa cuti tidak mencukupi"
    }
  }
  ```
- HTTP status code dipakai secara konsisten: `200` OK, `201` Created, `400`
  Validation error, `401` Unauthenticated, `403` Forbidden (role tidak sesuai),
  `404` Not Found, `409` Conflict (mis. status transition tidak valid).
- Pagination pakai query param `?page=0&size=20&sort=createdAt,desc`.
- Endpoint list mengembalikan struktur `Page<T>` Spring Data, jangan dibungkus manual.

### Contoh endpoint per modul (bukan daftar lengkap, tambahkan sesuai kebutuhan)

```
POST   /api/v1/auth/login
GET    /api/v1/employees
POST   /api/v1/employees
GET    /api/v1/employees/{id}
PUT    /api/v1/employees/{id}

POST   /api/v1/attendance/check-in
POST   /api/v1/attendance/check-out
GET    /api/v1/attendance?employeeId=&from=&to=

POST   /api/v1/leaves
GET    /api/v1/leaves/me
GET    /api/v1/leaves/team          (manager)
PATCH  /api/v1/leaves/{id}/approve
PATCH  /api/v1/leaves/{id}/reject

POST   /api/v1/review-periods
POST   /api/v1/reviews
GET    /api/v1/reviews/me
```

## 4. Autentikasi & Otorisasi

- Login → server mengeluarkan JWT (access token). Simpan `role` dan `employeeId`
  di claim token supaya tidak perlu query berulang untuk cek identitas.
- Setiap request ke endpoint terproteksi membawa `Authorization: Bearer <token>`.
- Otorisasi pakai `@PreAuthorize("hasRole('HR')")` atau custom
  `@PreAuthorize("hasRole('MANAGER') and @leaveSecurity.isTeamMember(#employeeId)")`
  untuk kasus "manager hanya boleh approve tim sendiri".
- Jangan taruh logic role-check manual (`if (user.getRole() == ...)`) tersebar
  di banyak service — sentralisasi lewat annotation + security bean bila
  logic-nya berulang.

## 5. Alur Bisnis Kunci (yang paling penting dipahami, bukan cuma di-generate)

### 5.1 Leave Request Flow
```
Employee submit request
        │
        ▼
Validasi: startDate <= endDate, requestedDays <= remainingBalance
        │
        ▼
Simpan status = PENDING
        │
   ┌────┴────┐
   ▼         ▼
Manager    Manager
approve    reject
   │         │
   ▼         ▼
APPROVED   REJECTED
   │
   ▼
Kurangi leave_balance employee (dalam transaction yang sama)
```
Yang perlu dipastikan saat implementasi: operasi "ubah status + kurangi balance"
harus atomic (`@Transactional`), supaya tidak ada kondisi balance berkurang
tapi status gagal ter-update (atau sebaliknya).

### 5.2 Role Resolution untuk Manager
Manager hanya boleh melihat/approve leave milik employee yang `manager_id`-nya
sama dengan dirinya. Ini dicek di service layer (query by `managerId`), bukan
difilter di frontend saja.

## 6. Error Handling Strategy

- Buat exception spesifik per kasus, bukan `RuntimeException` generik:
  `InsufficientLeaveBalanceException`, `InvalidStatusTransitionException`,
  `ResourceNotFoundException`, dll.
- Semua ditangkap di satu `GlobalExceptionHandler` (`@RestControllerAdvice`)
  dan dipetakan ke format error di atas.

## 7. Frontend Architecture

- State per domain disimpan di Pinia store terpisah (`useAuthStore`,
  `useEmployeeStore`, `useLeaveStore`, dst) — jangan satu store raksasa.
- Axios instance terpusat (`api/axios.js`) dengan interceptor untuk menyisipkan
  JWT dan menangani 401 (auto redirect ke login).
- Route guard di Vue Router mengecek role sebelum masuk halaman
  (mis. halaman `/admin/employees` hanya untuk role HR).
- Komponen dipisah: `views/` (halaman, terhubung ke route) vs `components/`
  (reusable, tidak tahu soal routing/store secara langsung kalau bisa dihindari).

## 8. Environment & Config

- Backend: `application.yml` per profile (`application-dev.yml`,
  `application-prod.yml`), jangan hardcode credential.
- Frontend: `.env` untuk base URL API (`VITE_API_BASE_URL`).
- Simpan JWT secret & DB credential lewat environment variable, bukan di file
  yang di-commit.
