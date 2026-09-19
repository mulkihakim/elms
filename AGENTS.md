# AGENTS.md — Panduan untuk AI Coding Agent

> Baca file ini SEBELUM mengerjakan task apapun di project ini.
> File ini adalah entry point. Detail lengkap ada di folder `docs/`.

## Tentang Project Ini

**Nama:** Employee Lifecycle Management System (ELMS)
**Tujuan:** Portfolio project untuk belajar Spring Boot + Vue 3, dengan domain HR
(Employee → Attendance → Leave → Approval → Performance Review → Dashboard).

**Stack:**
- Backend: Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, PostgreSQL, Maven
- Frontend: Vue 3 (Composition API), Vite, Pinia, Vue Router, Tailwind CSS, Axios

**Baca dulu:**
1. `docs/00-PRD.md` — fitur apa saja, siapa user-nya, apa yang di luar scope
2. `docs/01-ARCHITECTURE.md` — layering backend, konvensi API, alur auth
3. `docs/02-DATA-MODEL.md` — entity, relasi, business rule (mis. leave balance)
4. `docs/03-FOLDER-STRUCTURE.md` — di mana file baru harus diletakkan
5. `docs/04-CODING-GUIDELINES.md` — aturan penamaan, style, dan batasan kerja agent

## Aturan Interaksi dengan Agent (Penting)

Project ini dipakai untuk **belajar**, bukan sekadar menghasilkan aplikasi jadi.
Karena itu, ikuti aturan berikut setiap kali membantu:

1. **Modul "zona wajib manual"**: `Leave Management` (state transition + validasi
   leave balance) dan `Security/JWT/RBAC` adalah bagian yang ingin dipahami penuh
   oleh pemilik project. Untuk dua modul ini:
   - Jangan langsung generate seluruh implementasi.
   - Tawarkan dulu rancangan/pseudocode, jelaskan trade-off, baru tulis kode
     kalau diminta eksplisit.
   - Selalu sertakan penjelasan singkat *kenapa* struktur itu dipilih
     (misal kenapa `@Transactional` diletakkan di service, bukan controller).
2. Modul lain (Employee CRUD, Department, Dashboard) boleh dikerjakan lebih cepat/otomatis.
3. Kalau membuat kode baru, cek dulu `docs/03-FOLDER-STRUCTURE.md` supaya
   file diletakkan di package/folder yang konsisten dengan pola yang sudah ada.
4. Kalau ragu antara dua pendekatan (misal DTO manual vs MapStruct), tanyakan
   preferensi user alih-alih memilih sendiri secara diam-diam — kecuali sudah
   ada preseden di codebase yang sedang berjalan.
5. Setelah mengerjakan sesuatu, tulis ringkasan singkat: file apa yang dibuat/diubah,
   dan apa yang masih perlu di-review manual oleh user.

## Status Project

Isi/update bagian ini secara manual sesuai progres nyata:

- [x] Setup project skeleton (BE + FE)
- [ ] Auth (JWT + RBAC)
- [x] Modul Employee (Backend & Frontend CRUD selesai)
- [x] Modul Organization (Department & Position Backend & Frontend CRUD selesai)
- [ ] Modul Attendance
- [ ] Modul Leave Management
- [ ] Modul Performance Review
- [ ] Dashboard
