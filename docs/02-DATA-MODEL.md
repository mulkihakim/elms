# Data Model — ELMS

## 1. Entity Relationship (ringkasan)

```
Department 1───* Position
Department 1───* Employee
Position   1───* Employee
Employee   1───* Employee        (self-reference: manager_id)
Employee   1───* Attendance
Employee   1───* LeaveRequest
Employee   1───* PerformanceReview   (sebagai reviewee)
Employee   1───* PerformanceReview   (sebagai reviewer, via manager)
ReviewPeriod 1───* PerformanceReview
```

## 2. Detail Entity

### 2.1 Employee
| Field | Tipe | Catatan |
|---|---|---|
| id | UUID | PK |
| full_name | varchar | |
| email | varchar, unique | dipakai juga untuk login |
| phone | varchar | |
| department_id | FK → department | |
| position_id | FK → position | |
| manager_id | FK → employee (nullable) | self-reference |
| join_date | date | |
| employment_status | enum | `ACTIVE`, `ON_LEAVE`, `RESIGNED`, `TERMINATED` |
| role | enum | `EMPLOYEE`, `MANAGER`, `HR` |
| leave_balance | int | sisa jatah cuti tahun berjalan (hari) |
| password_hash | varchar | untuk login (BCrypt) |
| created_at / updated_at | timestamp | audit dasar |

### 2.2 Department
| Field | Tipe |
|---|---|
| id | PK |
| name | varchar, unique |

### 2.3 Position
| Field | Tipe |
|---|---|
| id | PK |
| title | varchar |
| department_id | FK → department |

### 2.4 Attendance
| Field | Tipe | Catatan |
|---|---|---|
| id | PK | |
| employee_id | FK → employee | |
| date | date | 1 baris per employee per hari |
| check_in | timestamp | nullable sebelum check-in |
| check_out | timestamp | nullable sebelum check-out |
| status | enum | `ON_TIME`, `LATE`, `ABSENT` — dihitung, bukan diinput manual |

Constraint: unique `(employee_id, date)` supaya tidak ada double check-in per hari.

### 2.5 LeaveRequest
| Field | Tipe | Catatan |
|---|---|---|
| id | PK | |
| employee_id | FK → employee | pemohon |
| leave_type | enum | `ANNUAL`, `SICK`, `UNPAID`, dst (mulai dari `ANNUAL` saja untuk MVP) |
| start_date | date | |
| end_date | date | |
| requested_days | int | dihitung dari start/end date (exclude weekend jika mau lebih realistis, opsional) |
| reason | text | |
| status | enum | `PENDING`, `APPROVED`, `REJECTED` |
| approved_by | FK → employee (nullable) | diisi saat approve/reject |
| decided_at | timestamp (nullable) | |
| created_at | timestamp | |

**Business rule (wajib divalidasi di service layer, bukan hanya di DB):**
- `requested_days <= employee.leave_balance` sebelum status bisa jadi `APPROVED`.
- Hanya bisa transisi `PENDING → APPROVED` atau `PENDING → REJECTED`. Transisi
  lain (mis. `APPROVED → PENDING`) ditolak dengan `InvalidStatusTransitionException`.
- Saat `APPROVED`: `employee.leave_balance -= requested_days`, dilakukan dalam
  transaction yang sama dengan update status.

### 2.6 ReviewPeriod
| Field | Tipe |
|---|---|
| id | PK |
| name | varchar (mis. "Q1 2026") |
| start_date | date |
| end_date | date |

### 2.7 PerformanceReview
| Field | Tipe | Catatan |
|---|---|---|
| id | PK | |
| review_period_id | FK → review_period | |
| employee_id | FK → employee | yang dinilai |
| reviewer_id | FK → employee | manager yang menilai |
| technical_skill | int (1-5) | |
| communication | int (1-5) | |
| teamwork | int (1-5) | |
| problem_solving | int (1-5) | |
| overall_score | decimal | rata-rata, dihitung di service, bukan diinput manual |
| comments | text (nullable) | |

Constraint: unique `(review_period_id, employee_id)` — satu employee hanya
punya satu review per periode.

## 3. Catatan Desain

- Gunakan enum Java untuk status/role, bukan string bebas, supaya validasi
  terjadi di level compile-time & JPA (`@Enumerated(EnumType.STRING)` —
  **jangan** `ORDINAL`, supaya aman kalau urutan enum berubah).
- `leave_balance` sebaiknya bertambah otomatis lewat scheduled job tahunan
  (opsional, boleh manual dulu untuk MVP — set default saat employee dibuat).
- Semua tabel transaksional (`Attendance`, `LeaveRequest`, `PerformanceReview`)
  sebaiknya punya `created_at`; kalau nanti mau audit log, ini jadi fondasinya.
