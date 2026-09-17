# UI Conventions — ELMS (ringan, bukan full design system)

> Tujuan dokumen ini: supaya tampilan tiap modul konsisten meskipun dibuat di
> sesi agent yang berbeda-beda. Bukan hasil proses UI/UX formal — cukup acuan
> dasar. Referensi visual: cari "admin dashboard tailwind" di Google/Dribbble
> untuk gambaran umum, tidak perlu meniru persis.

## 1. Layout Shell (dipakai di semua halaman setelah login)

```
┌─────────────────────────────────────────────┐
│  Topbar: logo | nama user & role | logout    │
├───────────┬───────────────────────────────────┤
│           │                                   │
│  Sidebar  │        Content area               │
│  (menu    │        (isi tiap halaman          │
│  per role)│         berbeda-beda)             │
│           │                                   │
└───────────┴───────────────────────────────────┘
```

- Komponen: `AppHeader.vue`, `AppSidebar.vue` di `components/layout/`
- Menu sidebar berbeda per role (Employee/Manager/HR) — filter dari `authStore`
- Dipakai lewat 1 layout wrapper (`DefaultLayout.vue`), semua `views/` di-render
  di dalamnya via `<router-view />`, kecuali halaman Login

## 2. Warna (Tailwind, cukup 2-3 warna inti)

| Peran | Contoh Tailwind class | Kapan dipakai |
|---|---|---|
| Primary | `bg-indigo-600` / `text-indigo-600` | tombol utama, link aktif, sidebar item aktif |
| Neutral | `bg-gray-50` (background), `text-gray-700` (teks), `border-gray-200` | background halaman, teks biasa, border |
| Success | `bg-green-100 text-green-700` | status `APPROVED`, `ON_TIME` |
| Warning | `bg-yellow-100 text-yellow-700` | status `PENDING`, `LATE` |
| Danger | `bg-red-100 text-red-700` | status `REJECTED`, `ABSENT`, tombol delete |

Boleh ganti warna primary sesuai selera, tapi begitu ditentukan, pakai
konsisten — jangan biarkan agent memilih warna baru tiap membuat halaman.

## 3. Komponen Dasar yang Dipakai Berulang

Buat sekali di `components/common/`, pakai di semua modul — jangan biarkan
agent bikin versi baru tiap modul:

- `BaseButton.vue` — variant: `primary`, `secondary`, `danger`
- `BaseInput.vue` / `BaseSelect.vue` — dipakai di semua form (Employee, Leave, dst)
- `BaseTable.vue` — dipakai di semua halaman list (Employee list, Attendance list, dst), minimal support kolom dinamis + slot untuk action button
- `BaseModal.vue` — untuk konfirmasi delete, form cepat
- `StatusBadge.vue` — render warna sesuai tabel status di atas (`PENDING` → kuning, dst), dipakai di Leave, Attendance, Employee status

## 4. Pola Halaman List (dipakai di Employee, Department, Attendance, dst)

```
[ Judul halaman ]              [ + Tambah ]
[ Search / filter bar ]
┌─────────────────────────────────────────┐
│  BaseTable dengan kolom relevan          │
│  + kolom "Aksi" (edit/delete/detail)     │
└─────────────────────────────────────────┘
[ Pagination ]
```

## 5. Pola Halaman Form (dipakai di Employee, Leave Request, dst)

- Form dalam `BaseModal` untuk data sederhana (Department, Position)
- Form halaman penuh (bukan modal) untuk data lebih kompleks (Employee, karena
  banyak field + relasi manager/department/position)
- Selalu tampilkan pesan error validasi per field, bukan alert generik

## 6. Aturan untuk Agent

- Sebelum bikin komponen baru, cek dulu apakah kebutuhannya sudah ter-cover
  `components/common/`. Kalau ya, pakai ulang — jangan bikin versi baru.
- Kalau perlu pola/komponen baru yang belum ada di sini, tambahkan ke dokumen
  ini juga (bukan cuma ke kode), supaya sesi berikutnya tetap konsisten.
