# Employee Lifecycle Management System (ELMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3.x%20(Composition%20API)-4FC08D.svg)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-black.svg)](LICENSE)

ELMS adalah aplikasi portofolio Full-Stack yang memodelkan siklus hidup karyawan (*employee lifecycle*): **Onboarding/Organisasi → Presensi Harian → Manajemen Cuti (Approval Workflow) → Evaluasi Kinerja (Performance Review) → Executive Dashboard**.

Proyek ini dirancang untuk menunjukkan implementasi *clean architecture*, penanganan logika bisnis non-trivial (transaksional atomik, pencegahan *concurrency race condition*), serta keamanan berbasis peran (*Two-Tier Role-Based Access Control*).

---

## 🌟 Highlight Teknis & Keputusan Arsitektur

* **Controller Tipis, Service Kaya Domain**: Controller hanya mengurus validasi format payload dan HTTP response status. Semua aturan bisnis, orkestrasi validasi, dan transaksi berada di Service Layer.
* **Strict Two-Tier Authorization**:
  1. *Coarse-grained (Role-based)*: Dicek di level endpoint via Spring Security `@PreAuthorize("hasRole('...')")`.
  2. *Fine-grained (Data-scope)*: Dicek di level query/service bean (contoh: Manager hanya dapat menyetujui cuti dan melihat absensi tim yang melaporkan langsung kepadanya via `manager_id`).
* **Atomic Leave Deduction & Concurrency Handling**:
  * Pengajuan cuti tidak langsung memotong saldo (status `PENDING`).
  * Saat *approval*, baris pemohon dikunci (*pessimistic locking* / transactional barrier) dan saldo divalidasi ulang sebelum dikurangi untuk mencegah *over-allocation* jika ada approval paralel.
* **Standardized API Contract**: Seluruh *response* dan *exception* dibungkus seragam (`ApiResponse<T>`) dengan kode error domain terprediksi (`LEAVE_BALANCE_INSUFFICIENT`, `ALREADY_CHECKED_IN`, dll.), tanpa pernah membocorkan *stack trace* ke client.
* **Thin Reusable UI Wrapper**: Mengisolasi *third-party component library* (PrimeVue) ke dalam `Base*.vue` wrappers, mencegah vendor lock-in dan menjaga konsistensi desain Tailwind.

---

## 🛠️ Tech Stack

### Backend
- **Framework**: Java 21, Spring Boot 4.1.1
- **Modules**: Spring Web, Spring Data JPA, Spring Security, Validation (Hibernate Validator), Lombok
- **Security**: Stateless JWT (`jjwt`), BCrypt Password Hashing
- **Database**: PostgreSQL 18
- **Dokumentasi API**: Springdoc OpenAPI (Swagger UI)

### Frontend
- **Core**: Vue 3 (Composition API `<script setup>`), Vite
- **State Management**: Pinia (Modular stores per domain)
- **Routing**: Vue Router (Navigation Guards untuk route protection & role check)
- **UI & Styling**: PrimeVue (Aura Preset, Primary Indigo) + Tailwind CSS
- **HTTP Client**: Axios (Centralized interceptors untuk JWT injection & auto-logout 401)

---

## 📐 Arsitektur & Struktur Monorepo

```text
elms/
├── backend/                  # Maven multi-package Spring Boot
│   └── src/main/java/com/elms/
│       ├── common/           # Response wrapper, GlobalExceptionHandler, Config, JWT
│       ├── auth/             # Controller, Service, DTO, Filter
│       ├── organization/     # Department & Position management
│       ├── employee/         # Employee CRUD, Self-reference Manager, Profiles
│       ├── attendance/       # Daily check-in/out, Work duration, Late calculation
│       ├── leave/            # Leave submission, Balance validation, Approvals
│       ├── performance/      # Review periods, Evaluations, Scoring logic
│       └── dashboard/        # Summary statistics & department distribution
├── frontend/                 # Vite + Vue 3 project
│   └── src/
│       ├── api/              # Axios instance & domain-specific API calls
│       ├── components/       # Layouts (Sidebar, Header) & Reusable Base*.vue
│       ├── stores/           # Pinia modular stores (auth, leave, attendance, etc.)
│       ├── views/            # Route pages
│       └── router/           # Route definitions & RBAC guards
└── docs/                     # PRD, Architecture, Data Model, Tasks
```

---

## 📋 Matriks Hak Akses (RBAC)

| Modul / Tindakan | Employee | Manager | HR / Admin |
|---|:---:|:---:|:---:|
| Lihat Profil Sendiri | ✅ | ✅ | ✅ |
| Presensi Harian (Check-in / Check-out) | ✅ | ✅ | ✅ |
| Pengajuan Cuti Sendiri | ✅ | ✅ | ✅ |
| Approval / Rejection Cuti Tim | ❌ | ✅ (Hanya direct report) | ✅ (Semua) |
| CRUD Master Data (Employee, Dept, Pos) | ❌ | ❌ | ✅ |
| Input Evaluasi Kinerja (Performance Review)| ❌ | ✅ (Hanya direct report) | ❌ |
| Monitoring Presensi & Dashboard | ❌ | ✅ (Cakupan Tim) | ✅ (Global) |

---

## 💼 Business Rules Utama

1. **Self-Referencing Org Hierarchy**:
   * Setiap karyawan merujuk ke karyawan lain sebagai manajer via relasi `manager_id`. Manajer wajib berstatus `ACTIVE` dan ber-role `MANAGER`.
2. **Leave Balance Validation**:
   * Durasi cuti dihitung server-side (Senin–Jumat, mengabaikan weekend).
   * Cuti tidak boleh *overlap* dengan status `PENDING` atau `APPROVED` lainnya.
   * Approval cuti memotong saldo secara atomik:
     $$\text{Sisa Saldo Baru} = \text{Sisa Saldo Lama} - \text{Hari Cuti Disetujui}$$
3. **Presensi Otomatis**:
   * Menolak *double check-in* pada hari lokal yang sama (`Asia/Jakarta`).
   * Menghitung status keterlambatan (`LATE` vs `ON_TIME`) otomatis berdasarkan ambang batas waktu server (`elms.attendance.work-start: 09:00`).

---

## 🚀 Panduan Instalasi Lokal

### Prasyarat
- Java Development Kit (JDK 17 atau 21)
- Node.js (v18+ atau LTS terbaru)
- PostgreSQL 18 terpasang lokal (Port default: `5432`)

### 1. Konfigurasi Database
Buat database di instance PostgreSQL lokal Anda:
```sql
CREATE DATABASE elms_db;
```

### 2. Konfigurasi Backend
1. Masuk ke direktori `backend/`:
   ```bash
   cd backend
   ```
2. Sesuaikan konfigurasi koneksi pada `src/main/resources/application-dev.yml` (atau lewat environment variable):
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/elms_db
       username: postgres
       password: your_password
   elms:
     jwt:
       secret: "SetMinimal32ByteSecretKeyForHS256AlgorithmSecurityHere!"
       expiration-ms: 86400000 # 24 Jam
   ```
3. Jalankan aplikasi Spring Boot:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Backend akan berjalan di:* `http://localhost:8080`  
   *Swagger UI (OpenAPI):* `http://localhost:8080/swagger-ui.html`

### 3. Konfigurasi Frontend
1. Buka terminal baru dan masuk ke direktori `frontend/`:
   ```bash
   cd frontend
   ```
2. Buat file `.env` dari `.env.example`:
   ```bash
   cp .env.example .env
   ```
   Pastikan mengarah ke backend: `VITE_API_BASE_URL=http://localhost:8080/api/v1`
3. Pasang dependensi dan jalankan server pengembang:
   ```bash
   npm install
   npm run dev
   ```
   *Frontend akan berjalan di:* `http://localhost:5173`

---

## 🔑 Kredensial Akun Default (Seeder Profil Dev)

Saat aplikasi dijalankan dengan profil `dev`, akun dasar otomatis di-seed untuk mempermudah review:

| Role | Email | Password | Deskripsi |
|---|---|---|---|
| **HR / Admin** | `admin.hr@elms.local` | `Password123!` | Akses penuh master data & dashboard global |
| **Manager** | `manager.eng@elms.local` | `Password123!` | Manajer tim dengan bawahan langsung |
| **Employee** | `staff.eng@elms.local` | `Password123!` | Karyawan biasa (bawahan Manager) |

---

## 📡 Konvensi API & Format Response

Semua endpoint REST mengikuti format envelope standar:

### Success Response (`200 OK`, `201 Created`)
```json
{
  "success": true,
  "message": "Pengajuan cuti berhasil disetujui",
  "data": {
    "id": 102,
    "status": "APPROVED",
    "requestedDays": 3,
    "decidedAt": "2026-09-20T14:30:00Z"
  }
}
```

### Error Response (`400`, `403`, `404`, `409`)
```json
{
  "success": false,
  "error": {
    "code": "LEAVE_BALANCE_INSUFFICIENT",
    "message": "Sisa saldo cuti tidak mencukupi untuk pengajuan ini",
    "fields": null
  }
}
```

---

## 🧪 Testing

Jalankan rangkaian unit & integration test (fokus pada logic validasi leave & status transition):
```bash
cd backend
./mvnw test
```