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
| PrimeVue theme primary | preset Aura, token `primary` di-set ke `indigo` | supaya warna komponen PrimeVue (button, badge, dst senada dengan `bg-indigo-600` di atas |
| Neutral | `bg-gray-50` (background), `text-gray-700` (teks), `border-gray-200` | background halaman, teks biasa, border |
| Success | `bg-green-100 text-green-700` | status `APPROVED`, `ON_TIME` |
| Warning | `bg-yellow-100 text-yellow-700` | status `PENDING`, `LATE` |
| Danger | `bg-red-100 text-red-700` | status `REJECTED`, `ABSENT`, tombol delete |

Boleh ganti warna primary sesuai selera, tapi begitu ditentukan, pakai
konsisten — jangan biarkan agent memilih warna baru tiap membuat halaman.

## 3. Komponen Dasar yang Dipakai Berulang

Sejak [tanggal keputusan], komponen dasar dibangun sebagai **wrapper tipis di
atas PrimeVue**, bukan dari HTML+Tailwind murni. Tujuannya: kalau suatu saat
ganti library, cukup ubah wrapper-nya, bukan seluruh halaman yang memakainya.

Buat sekali di `components/common/`, pakai di semua modul — jangan biarkan
agent bikin versi baru tiap modul:

| Komponen project | Dibangun di atas (PrimeVue) | Catatan |
|---|---|---|
| `BaseButton.vue` | `Button` | variant: `primary`, `secondary`, `danger` → mapping ke `severity` PrimeVue |
| `BaseInput.vue` | `InputText` | dipakai di semua form |
| `BaseSelect.vue` | `Select` (dulu `Dropdown`) | dipakai di semua form |
| `BaseTable.vue` | `DataTable` + `Column` | dipakai di semua halaman list, minimal support kolom dinamis + slot action button |
| `BaseModal.vue` | `Dialog` | untuk konfirmasi delete, form cepat |
| `StatusBadge.vue` | `Tag` | render warna sesuai tabel status di §2 (`PENDING` → kuning, dst) |

Aturan: komponen halaman **tidak boleh** import komponen PrimeVue langsung
(mis. `import Button from 'primevue/button'` di dalam `EmployeeListView.vue`).
Selalu lewat `Base*.vue` di atas, supaya styling & behavior tetap sentral.

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
