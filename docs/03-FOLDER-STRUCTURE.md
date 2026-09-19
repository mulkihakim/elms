# Struktur Folder & Penamaan — ELMS

## 1. Backend (Spring Boot, package by feature)

Gunakan **package-by-feature** (per modul), bukan package-by-layer global,
supaya tiap modul (Employee, Leave, dst) mandiri dan mudah dicari.

```
src/main/java/com/elms/
├── ElmsApplication.java
│
├── common/                     # utilitas lintas modul
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── DuplicateResourceException.java
│   │   ├── InvalidStatusTransitionException.java
│   │   └── InsufficientLeaveBalanceException.java
│   ├── response/
│   │   └── ApiResponse.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   └── CorsConfig.java
│   └── seeder/
│       └── DataSeeder.java
│
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── dto/ (LoginRequest, LoginResponse)
│   └── jwt/ (JwtTokenProvider, JwtAuthFilter)
│
├── employee/
│   ├── Employee.java              # entity
│   ├── EmployeeController.java
│   ├── EmployeeService.java
│   ├── EmployeeRepository.java
│   ├── dto/
│   │   ├── EmployeeRequest.java
│   │   └── EmployeeResponse.java
│   └── mapper/
│       └── EmployeeMapper.java
│
├── organization/
│   ├── department/ (Department.java, DepartmentController.java, ...)
│   └── position/   (Position.java, PositionController.java, ...)
│
├── attendance/
│   ├── Attendance.java
│   ├── AttendanceController.java
│   ├── AttendanceService.java
│   └── AttendanceRepository.java
│
├── leave/
│   ├── LeaveRequest.java
│   ├── LeaveController.java
│   ├── LeaveService.java          # <- business logic paling penting ada di sini
│   ├── LeaveRepository.java
│   └── dto/
│
├── performance/
│   ├── ReviewPeriod.java
│   ├── PerformanceReview.java
│   ├── PerformanceController.java
│   ├── PerformanceService.java
│   └── PerformanceRepository.java
│
└── dashboard/
    ├── DashboardController.java
    └── DashboardService.java
```

**Aturan penamaan Java:**
- Class: `PascalCase` (`LeaveService`, `EmployeeResponse`)
- Method & variable: `camelCase`
- Constant: `UPPER_SNAKE_CASE`
- 1 file = 1 class/interface publik
- Controller: `XxxController`, Service: `XxxService` (interface opsional +
  `XxxServiceImpl` kalau memang butuh multiple implementation — untuk MVP
  boleh langsung class biasa tanpa interface supaya tidak over-engineering)
- DTO request: `XxxRequest`, DTO response: `XxxResponse`
- Endpoint path: kebab-case, plural untuk resource (`/employees`,
  `/leave-requests`), verb hanya untuk aksi non-CRUD (`/attendance/check-in`)

## 2. Frontend (Vue 3 + Vite)

```
src/
├── main.js
├── App.vue
│
├── api/
│   ├── axios.js              # instance axios + interceptor JWT
│   ├── employeeApi.js
│   ├── leaveApi.js
│   ├── attendanceApi.js
│   └── ...
│
├── stores/                   # Pinia, satu store per domain
│   ├── authStore.js
│   ├── employeeStore.js
│   ├── leaveStore.js
│   └── ...
│
├── router/
│   ├── index.js
│   └── guards.js              # role-based route guard
|
├── plugins/
│   └── primevue.js            # registrasi PrimeVue, tema Aura, daftar komponen yang diimport
│
├── views/                     # halaman, 1:1 dengan route
│   ├── auth/LoginView.vue
│   ├── employee/EmployeeListView.vue
│   ├── employee/EmployeeFormView.vue
│   ├── leave/LeaveRequestView.vue
│   ├── leave/LeaveApprovalView.vue      # khusus manager
│   ├── performance/ReviewFormView.vue
│   └── dashboard/DashboardView.vue
│
├── components/                # reusable, tidak terikat 1 halaman
│   ├── common/ (BaseButton.vue, BaseModal.vue, BaseTable.vue)
│   └── layout/ (AppSidebar.vue, AppHeader.vue)
│
├── composables/                # reusable logic (Composition API)
│   ├── useAuth.js
│   └── usePagination.js
│
└── utils/
    ├── formatDate.js
    └── constants.js            # enum status, role, dsb — mirror dengan backend
```
**Catatan PrimeVue:** komponen PrimeVue tidak diimport langsung di `views/`
atau `components/`, hanya lewat `components/common/Base*.vue` (lihat
`06-UI-CONVENTIONS.md` §3). Registrasi plugin & tema PrimeVue terpusat di
`plugins/primevue.js`, diimport sekali di `main.js`.

**Aturan penamaan Vue:**
- Komponen: `PascalCase.vue` (`EmployeeCard.vue`)
- Store: `camelCase` file, nama store `useXxxStore`
- Composable: prefix `use` (`useAuth.js`)
- Props: `camelCase` di script, `kebab-case` saat dipakai di template
- 1 komponen = 1 tanggung jawab; kalau file `.vue` sudah > ~200 baris,
  pertimbangkan pecah jadi sub-komponen

## 3. Konsistensi Enum FE ↔ BE

Nilai enum (`role`, `employment_status`, `leave status`, dst) harus sama persis
string-nya antara backend Java enum dan `utils/constants.js` di frontend, supaya
tidak ada mismatch saat compare status di UI (mis. `"PENDING"` harus sama persis,
bukan `"pending"`).
