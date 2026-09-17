# PRD — Employee Lifecycle Management System (ELMS)

## 1. Latar Belakang & Tujuan

Project ini dibuat untuk eksplorasi Spring Boot + Vue 3 dengan studi kasus HR System
yang realistis (bukan sekadar CRUD). Fokus belajar: layering backend yang rapi,
business logic non-trivial (approval workflow, validasi saldo cuti), dan
role-based access control.

**Bukan tujuan project ini:** membuat HRIS lengkap/komersial. Cukup MVP yang
merepresentasikan siklus hidup employee (join → bekerja → cuti → dinilai).

## 2. Target User (Persona)

| Role | Deskripsi |
|---|---|
| HR / Admin | Mengelola master data (employee, department, position), melihat semua data, mengatur leave policy |
| Manager | Mengelola tim langsungnya: approve/reject leave, mengisi performance review, melihat attendance tim |
| Employee | Melihat profil sendiri, check-in/out, request leave, melihat hasil review sendiri |

## 3. Modul & Fitur (MVP — urutan pengerjaan)

### 3.1 Employee Management
- CRUD employee (nama, email, phone, department, position, manager, join date, status)
- Employee punya 1 manager (self-referencing relation)
- Employment status: `ACTIVE`, `ON_LEAVE`, `RESIGNED`, `TERMINATED`

### 3.2 Organization
- CRUD Department, CRUD Position
- Department bisa punya banyak Position dan banyak Employee

### 3.3 Attendance
- Employee check-in / check-out (timestamp otomatis)
- Kalkulasi: telat (late) jika check-in > jam kerja yang ditentukan, work duration
- HR/Manager bisa melihat riwayat attendance (filter by date range, employee, department)

### 3.4 Leave Management (fokus belajar utama)
- Employee mengajukan cuti (tanggal mulai, selesai, jenis cuti, alasan)
- Validasi: `requested_days <= remaining_leave_balance`
- Status: `PENDING → APPROVED / REJECTED` (hanya manager langsung yang bisa approve)
- Saat approved, leave balance employee otomatis berkurang
- Riwayat pengajuan cuti per employee

### 3.5 Performance Review
- HR membuat periode review (mis. Q1 2026)
- Manager mengisi review untuk anggota timnya (beberapa aspek, skala 1-5 + overall)
- Employee bisa melihat (read-only) hasil review-nya sendiri

### 3.6 Dashboard
- Ringkasan jumlah employee, yang hadir hari ini, yang sedang cuti
- Distribusi employee per department
- Grafik attendance mingguan

## 4. Role & Permission (ringkasan)

| Aksi | Employee | Manager | HR/Admin |
|---|:---:|:---:|:---:|
| Lihat profil sendiri | ✅ | ✅ | ✅ |
| Check-in/out | ✅ | ✅ | ✅ |
| Request leave | ✅ | ✅ | ✅ |
| Approve/reject leave tim | ❌ | ✅ (tim sendiri) | ✅ (semua) |
| CRUD Employee/Department/Position | ❌ | ❌ | ✅ |
| Isi performance review | ❌ | ✅ (tim sendiri) | ❌ |
| Lihat semua attendance | ❌ | ✅ (tim sendiri) | ✅ (semua) |
| Lihat dashboard | ❌ | Sebagian (tim) | ✅ (semua) |

Detail permission per endpoint ada di `01-ARCHITECTURE.md` § Authorization.

## 5. Out of Scope (MVP)

- Payroll / penggajian
- Rekrutmen / applicant tracking
- Integrasi absensi fisik (fingerprint/RFID)
- Notifikasi email/push (bisa jadi fase 2)
- Multi-company / multi-tenant

## 6. Definisi "Selesai" untuk MVP

MVP dianggap selesai kalau alur berikut bisa dijalankan end-to-end tanpa bug:

1. HR login → membuat department, position, dan 3-5 employee (termasuk 1 manager)
2. Employee login → check-in, request leave
3. Manager login → melihat request leave tim → approve salah satu
4. Sistem menolak request leave kedua jika saldo tidak cukup
5. HR membuat periode review → manager mengisi review untuk 1 employee
6. Employee login → melihat hasil review-nya
7. Dashboard menampilkan angka yang sesuai dengan data di atas

## 7. Roadmap Setelah MVP (opsional, jangan dikerjakan dulu)

- Audit log (siapa mengubah apa, kapan)
- Notifikasi (email saat leave di-approve/reject)
- Export laporan (attendance, leave) ke Excel/PDF
- Pagination & filtering lanjutan (specification pattern di JPA)
