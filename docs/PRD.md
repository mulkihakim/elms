# PRD — Employee Lifecycle Management System (ELMS)

## 1. Overview

ELMS adalah HR system MVP yang merepresentasikan siklus hidup employee:
**join → bekerja (absensi) → cuti → dinilai (performance review)**.

Project ini dibuat untuk eksplorasi Spring Boot + Vue 3 dengan studi kasus realistis (bukan sekadar CRUD).
Fokus belajar: layering backend yang rapi, business logic non-trivial (approval workflow, validasi saldo cuti),
dan role-based access control.

### Non-Goals
Bukan HRIS lengkap/komersial. Lihat § 8 Out of Scope.

## 2. Persona

| Role | Deskripsi |
|---|---|
| **HR / Admin** | Kelola master data (employee, department, position), lihat semua data, buat periode review |
| **Manager** | Kelola tim langsung: approve/reject cuti, isi performance review, lihat attendance tim |
| **Employee** | Lihat profil sendiri, check-in/out, request cuti, lihat hasil review sendiri |

## 3. Requirements

### Fungsional
Dirinci per fase di § 5. Ringkasnya: manajemen employee & organisasi, absensi, cuti dengan approval,
performance review, dashboard.

### Non-Fungsional
- **Keamanan:** autentikasi JWT; otorisasi berbasis role + scope data (tim/pemilik) yang dicek di **backend**, bukan hanya disembunyikan di UI.
- **Konsistensi data:** operasi multi-tabel atomic (`@Transactional`), terutama approve cuti + kurangi saldo.
- **Zona waktu:** aturan jam kerja/telat dihitung di zona `Asia/Jakarta` (lihat `DATA_MODEL.md` §4).
- **Bahasa:** label UI & pesan error API berbahasa Indonesia; kode/identifier berbahasa Inggris.
- **Feedback UI:** setiap request punya state loading & error yang terlihat user.
- **Pagination** untuk semua endpoint list.

## 4. Permission (ringkasan)

| Aksi | Employee | Manager | HR/Admin |
|---|:---:|:---:|:---:|
| Lihat profil sendiri | ✅ | ✅ | ✅ |
| Check-in/out | ✅ | ✅ | ✅ |
| Request leave | ✅ | ✅ | ✅ |
| Approve/reject leave | ❌ | ✅ (tim sendiri) | ✅ (semua) |
| CRUD Employee/Department/Position | ❌ | ❌ | ✅ |
| Isi performance review | ❌ | ✅ (tim sendiri) | ❌ |
| Lihat attendance | ❌ (hanya milik sendiri) | ✅ (tim sendiri) | ✅ (semua) |
| Lihat dashboard | ❌ | Sebagian (tim) | ✅ (semua) |

Matriks per endpoint ada di `ARCHITECTURE.md` § 4.

## 5. Core Features (per Fase — urutan pengerjaan)

> Nomor fase sama dengan `TASKS.md`. Acceptance criteria (AC) di sini adalah patokan "selesai" untuk tiap fase.

### Fase 0 — Setup
Scaffold monorepo, PostgreSQL lokal (native), backend & frontend bisa jalan.
- AC: `spring-boot:run` konek DB tanpa error; `npm run dev` menampilkan halaman dengan 1 komponen PrimeVue.

### Fase 1 — Foundation & Auth
- Response/exception standar, entity dasar (Department, Position, Employee), login JWT, seed 1 akun HR (dev).
- FE: halaman login, layout shell (header + sidebar per role), komponen dasar `Base*`.
- AC: HR bisa login → masuk layout; token salah/kadaluarsa → redirect ke login; endpoint tanpa token = 401; role salah = 403.

### Fase 2 — Organization (Department & Position)
- CRUD Department & Position. Department punya banyak Position dan banyak Employee.
- AC: HR bisa CRUD; menghapus department/position yang masih dipakai employee ditolak (409) dengan pesan jelas.

### Fase 3 — Employee Management
- CRUD employee: nama, email, phone, department, position, manager, join date, status, role, saldo cuti awal.
- Employee punya maksimal 1 manager (self-reference). Status: `ACTIVE`, `ON_LEAVE`, `RESIGNED`, `TERMINATED`.
- Tidak ada hard delete — nonaktifkan lewat perubahan status.
- Employee/manager bisa melihat profil sendiri.
- AC: HR membuat 1 manager + beberapa employee dengan `manager_id` benar; list mendukung search, filter, pagination; email unik.

### Fase 4 — Attendance
- Check-in / check-out (timestamp otomatis dari server). Status `ON_TIME`/`LATE` dihitung dari jam kerja; work duration dihitung dari check-in→check-out.
- HR/Manager melihat riwayat (filter date range, employee, department); Employee melihat riwayat sendiri.
- AC: double check-in di hari yang sama ditolak; check-out tanpa check-in ditolak; manager hanya melihat timnya.

### Fase 5 — Leave Management ⭐ (fokus belajar utama)
- Employee mengajukan cuti (tanggal mulai, selesai, jenis, alasan). Validasi `requested_days <= remaining_leave_balance`.
- Status `PENDING → APPROVED | REJECTED`; hanya manager langsung (atau HR) yang memutuskan.
- Saat approved, saldo cuti berkurang otomatis dalam transaction yang sama.
- Riwayat pengajuan per employee.
- AC: lihat § 6.2 dan `DATA_MODEL.md` §3.

### Fase 6 — Performance Review
- HR membuat periode review (mis. "Q1 2026").
- Manager mengisi review untuk anggota timnya: 4 aspek (skala 1–5) + overall (dihitung otomatis) + komentar.
- Employee melihat hasil review-nya (read-only).
- AC: satu employee hanya punya satu review per periode; manager tidak bisa mereview non-anggota timnya; skor di luar 1–5 ditolak.

### Fase 7 — Dashboard
- Jumlah employee, yang hadir hari ini, yang sedang cuti; distribusi employee per department; grafik attendance mingguan.
- HR melihat semua; Manager melihat lingkup timnya.
- AC: angka cocok dengan data hasil alur § 6.1.

### Fase 8 — Hardening
- Jalankan alur E2E § 6.1 dari awal sampai akhir, rapikan bug, siapkan data demo, lengkapi README.

## 6. User Flow

### 6.1 Alur E2E MVP (definisi "selesai")
MVP selesai kalau alur ini jalan end-to-end tanpa bug:

1. HR login → buat department, position, dan 3–5 employee (termasuk 1 manager)
2. Employee login → check-in, request leave
3. Manager login → lihat request leave tim → approve salah satu
4. Sistem menolak request leave berikutnya jika saldo tidak cukup
5. HR membuat periode review → manager mengisi review untuk 1 employee
6. Employee login → melihat hasil review-nya
7. Dashboard menampilkan angka yang sesuai dengan data di atas

### 6.2 Leave Request Flow

```
Employee submit request
        │
        ▼
Validasi (tanggal valid, hari > 0, tidak overlap, requestedDays <= saldo)
        │ gagal → 400/409 + kode error
        ▼
Simpan status = PENDING
        │
   ┌────┴────┐
   ▼         ▼
Manager    Manager
approve    reject
   │         │
   ▼         ▼
(cek ulang saldo,      REJECTED
 kurangi saldo,        (saldo tidak berubah)
 dalam 1 transaction)
   │
   ▼
APPROVED
```

Transisi selain `PENDING → APPROVED/REJECTED` ditolak (`InvalidStatusTransitionException`, 409).

## 7. Tech Stack (ringkas)

Spring Boot + Spring Data JPA + Spring Security (JWT) + PostgreSQL · Vue 3 (Composition API) + Pinia + Vue Router
+ PrimeVue (komponen) + Tailwind (layout). Detail & versi ada di `ARCHITECTURE.md` § 1.

## 8. Out of Scope (MVP)

- Payroll / penggajian
- Rekrutmen / applicant tracking
- Integrasi absensi fisik (fingerprint/RFID)
- Notifikasi email/push
- Multi-company / multi-tenant
- Pembatalan cuti oleh employee, tabel hari libur nasional, jenis cuti selain `ANNUAL`
- Dark mode, i18n multi-bahasa

## 9. Roadmap Setelah MVP (jangan dikerjakan dulu)

- Audit log (siapa mengubah apa, kapan)
- Notifikasi (email saat leave di-approve/reject)
- Export laporan (attendance, leave) ke Excel/PDF
- Migrasi schema ke Flyway/Liquibase (`ddl-auto: validate`)
- Jenis cuti lain (`SICK`, `UNPAID`), pembatalan cuti, hari libur nasional
- Scheduled job reset saldo cuti tahunan
