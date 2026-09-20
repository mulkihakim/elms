# Data Model & Business Rules — ELMS

> Item bertanda **⚠ PROPOSED** belum dikonfirmasi user — lihat `MEMORY.md` § Open Questions sebelum mengimplementasikannya.

## 1. Entity Relationship

```
Department 1───* Position
Department 1───* Employee
Position   1───* Employee
Employee   1───* Employee            (self-reference: manager_id)
Employee   1───* Attendance
Employee   1───* LeaveRequest        (pemohon)
Employee   1───* LeaveRequest        (approver, via approved_by)
Employee   1───* PerformanceReview   (reviewee)
Employee   1───* PerformanceReview   (reviewer)
ReviewPeriod 1───* PerformanceReview
```

## 2. Entity

**Konvensi umum:** PK `id`: **Hybrid** — `Long` auto-increment untuk master data (`Department`, `Position`), dan `UUID` untuk entitas personal & transaksional (`Employee`, `Attendance`, `LeaveRequest`, `PerformanceReview`) · enum `@Enumerated(EnumType.STRING)` (jangan `ORDINAL`) ·
`created_at`/`updated_at` timestamp · tanggal murni (`date`) sebagai `LocalDate`.

### 2.1 Employee
| Field | Tipe | Catatan |
|---|---|---|
| id | UUID | PK (generate UUID) |
| full_name | varchar, not null | |
| email | varchar, unique, not null | disimpan lowercase; dipakai login |
| phone | varchar | |
| department_id | FK → department | not null |
| position_id | FK → position | not null; harus milik department yang sama |
| manager_id | FK → employee, nullable | self-reference |
| join_date | date | |
| employment_status | enum | `ACTIVE`, `ON_LEAVE`, `RESIGNED`, `TERMINATED` |
| role | enum | `EMPLOYEE`, `MANAGER`, `HR` |
| leave_balance | int, ≥ 0 | sisa jatah cuti tahun berjalan (hari) |
| password_hash | varchar | BCrypt; **tidak pernah** masuk DTO response |
| created_at / updated_at | timestamp | |

Aturan:
- `manager_id` ≠ `id` sendiri; manager harus berstatus `ACTIVE` dan berrole `MANAGER`.
- Tidak ada hard delete. Nonaktifkan lewat `employment_status`.

### 2.2 Department
`id` PK (`Long` auto-increment) · `name` varchar unique not null.

### 2.3 Position
`id` PK (`Long` auto-increment) · `title` varchar not null · `department_id` FK → department not null. Unique `(department_id, title)`.

### 2.4 Attendance
| Field | Tipe | Catatan |
|---|---|---|
| id | UUID | PK (generate UUID) |
| employee_id | FK → employee | |
| date | date | tanggal kerja (di zona `Asia/Jakarta`) |
| check_in | timestamp | not null (baris dibuat saat check-in) |
| check_out | timestamp | nullable sampai check-out |
| status | enum | `ON_TIME`, `LATE` — dihitung server saat check-in, bukan input manual |
| work_minutes | int, nullable | durasi menit kerja |
| notes | varchar, nullable | catatan opsional saat check-in |
| created_at / updated_at | timestamp | |

Constraint: **unique `(employee_id, date)`** — satu baris per employee per hari.

### 2.5 LeaveRequest
| Field | Tipe | Catatan |
|---|---|---|
| id | UUID | PK (generate UUID) |
| employee_id | FK → employee | pemohon |
| leave_type | enum | `ANNUAL` saja untuk MVP (enum boleh sudah memuat `SICK`, `UNPAID` untuk masa depan) |
| start_date, end_date | date | `start ≤ end` |
| requested_days | int | dihitung server, bukan input client |
| reason | text | |
| status | enum | `PENDING`, `APPROVED`, `REJECTED` |
| approved_by | FK → employee, nullable | diisi saat approve/reject |
| decided_at | timestamp, nullable | |
| created_at | timestamp | |

### 2.6 ReviewPeriod
`id` PK · `name` varchar (mis. "Q1 2026") · `start_date`, `end_date` date (`start ≤ end`).

### 2.7 PerformanceReview
| Field | Tipe | Catatan |
|---|---|---|
| id | UUID | PK (generate UUID) |
| review_period_id | FK → review_period | not null |
| employee_id | FK → employee | yang dinilai, not null |
| reviewer_id | FK → employee | manager langsung employee tersebut, not null |
| technical_skill, communication, teamwork, problem_solving | int 1–5 | validasi `@Min(1) @Max(5)` |
| overall_score | decimal(3,2) | rata-rata 4 aspek, **dihitung di service** |
| comments | text, nullable | |
| created_at / updated_at | timestamp | |

Constraint: **unique `(review_period_id, employee_id)`**.

## 3. Business Rules — Leave (🛑 gate: konfirmasi rancangan ke user sebelum kode)

Validasi wajib di **service layer** (bukan hanya constraint DB).

### 3.1 Submit (`LeaveService.submit`, `@Transactional`)
```
1. startDate <= endDate                                        else 400
2. startDate >= hari ini (zona Asia/Jakarta)                   else 400 (tanpa backdate)
3. requestedDays = jumlah hari kerja Senin–Jumat di rentang    (weekend tidak dihitung; tanpa tabel libur nasional)
4. requestedDays > 0                                           else 400
5. tidak overlap dengan LeaveRequest PENDING/APPROVED milik employee yang sama   else 409 LEAVE_OVERLAP
6. requestedDays <= employee.leaveBalance                      else 409 LEAVE_BALANCE_INSUFFICIENT
7. simpan status = PENDING (saldo BELUM berkurang)
```

### 3.2 Decide (`approve` / `reject`, satu `@Transactional`)
```
1. load LeaveRequest                                           else 404
2. otorisasi: pemutus adalah HR, ATAU manager langsung pemohon (pemohon.managerId == pemutus.id)
   pemutus ≠ pemohon (tidak boleh approve cuti sendiri)        else 403
3. status harus PENDING                                        else 409 INVALID_STATUS_TRANSITION
4. approve:
     a. kunci baris employee pemohon (PESSIMISTIC_WRITE) — cegah dua approval bersamaan melewati saldo
     b. cek ulang requestedDays <= leaveBalance                else 409 LEAVE_BALANCE_INSUFFICIENT
     c. leaveBalance -= requestedDays
   reject: saldo tidak berubah
5. set status, approvedBy, decidedAt
```

Kenapa cek saldo dua kali: cuti `PENDING` tidak "menahan" saldo, jadi dua request yang masing-masing lolos saat submit
bisa sama-sama melewati saldo kalau keduanya di-approve. Cek ulang saat approve menutup celah ini.

### 3.3 Siapa pemutus?
- Employee biasa → manager langsung (`manager_id`) atau HR.
- Manager → manager di atasnya bila ada, jika tidak → HR.
- HR → HR lain (bukan dirinya).

### 3.4 Test wajib (`LeaveServiceTest`, JUnit + Mockito)
Saldo cukup · saldo tidak cukup · approve dua kali (409) · reject tidak mengubah saldo · bukan manager langsung (403)
· approve cuti sendiri (403) · saldo berkurang tepat sebesar `requestedDays`.

## 4. Business Rules — Attendance

- Zona kerja: `elms.timezone` (default `Asia/Jakarta`). `date` = tanggal lokal saat check-in.
- **Check-in:** buat baris baru; jika sudah ada baris hari itu → 409 `ALREADY_CHECKED_IN`. `status = LATE` bila jam lokal check-in > `elms.attendance.work-start` (⚠ PROPOSED 09:00, tanpa grace period), selain itu `ON_TIME`.
- **Check-out:** wajib sudah check-in (`NOT_CHECKED_IN` 409); jika sudah check-out (`ALREADY_CHECKED_OUT` 409); `check_out > check_in`.
- **Work duration** dihitung saat query/mapping ke DTO (`check_out − check_in`), tidak disimpan.
- **ABSENT** tidak disimpan di DB (⚠ PROPOSED): dihitung di laporan/dashboard = hari kerja tanpa baris attendance dan tanpa cuti `APPROVED` yang menutupi hari itu.
- Employee berstatus non-`ACTIVE` tidak bisa check-in.

## 5. Business Rules — Performance Review

- Hanya manager langsung employee tersebut yang boleh membuat/mengedit review-nya.
- `overall_score = (technical_skill + communication + teamwork + problem_solving) / 4`, dibulatkan 2 desimal, dihitung di service.
- Satu review per `(period, employee)`; duplikat → 409 `REVIEW_ALREADY_EXISTS`.
- Review hanya boleh dibuat/diedit selama hari ini berada dalam rentang periode.
- Employee hanya bisa membaca review miliknya sendiri.

## 6. Business Rules — Dashboard

- **Hadir hari ini** = jumlah baris attendance hari ini (zona lokal).
- **Sedang cuti** = jumlah employee dengan `LeaveRequest` `APPROVED` yang rentang tanggalnya mencakup hari ini
  (dihitung dari data cuti; `employment_status = ON_LEAVE` tidak di-mutasi otomatis di MVP — ⚠ PROPOSED).
- **Distribusi per department** = count employee `ACTIVE` group by department.
- **Attendance mingguan** = per hari (7 hari terakhir atau minggu berjalan): jumlah hadir, jumlah telat.
- Manager: semua angka dibatasi ke timnya.

## 7. Catatan Desain

- `leave_balance` diisi default (`elms.leave.default-balance`) saat employee dibuat; reset tahunan otomatis = roadmap.
- Query dengan filter dinamis (employee list, attendance list) → `Specification`, bukan banyak method `findByXAndY`.
- Semua tabel transaksional punya `created_at` sebagai fondasi audit log di masa depan.
- Mapping Entity↔DTO manual di kelas `XxxMapper`.
