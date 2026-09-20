# Design — ELMS UI Conventions

> Tujuan: tampilan konsisten meski dibuat di sesi agent berbeda. Ini acuan dasar, bukan design system formal.
> Kalau butuh pola/komponen baru, **tambahkan ke dokumen ini dulu**, baru ke kode.

## 1. Prinsip Visual

- Tampilan **bersih, padat informasi, bergaya admin dashboard** (mirip template admin Tailwind/PrimeVue) — bukan landing page.
- Satu warna aksen (indigo), sisanya netral. Warna lain hanya untuk **status** (§ 3).
- **PrimeVue** = tampilan & perilaku komponen. **Tailwind** = layout di sekitarnya (grid, spacing, positioning).
- Desktop-first; tetap layak di layar tablet (sidebar bisa collapse di bawah breakpoint `lg`).
- Tidak ada dark mode di MVP; jangan tambahkan class `dark:`.

## 2. Design Tokens

### Warna

| Peran | Nilai | Dipakai untuk |
|---|---|---|
| Primary | `indigo-600` (hover `indigo-700`) | tombol utama, link, item sidebar aktif |
| Background halaman | `bg-gray-50` | area konten |
| Surface | `bg-white` + `border border-gray-200` | card, panel, sidebar, header |
| Teks utama / sekunder | `text-gray-900` / `text-gray-600` | judul / isi & label |
| Border | `border-gray-200` | pemisah |
| Success / Warning / Danger / Info | lihat § 3 | status |

### Tipografi
- Font: default sistem (Tailwind `font-sans`); tidak menambah font eksternal.
- Judul halaman `text-2xl font-semibold` · judul section/card `text-lg font-medium` · body `text-sm`/`text-base` · caption `text-xs text-gray-500`.

### Spacing & bentuk
- Skala Tailwind: gap antar section `gap-6`, padding card `p-4`–`p-6`, gap form `gap-4`.
- Radius & shadow mengikuti default PrimeVue Aura; card memakai `rounded-lg border` (tanpa shadow berat).
- Lebar sidebar `w-64` (desktop); tinggi header `h-16`.

### Tema PrimeVue (`src/plugins/primevue.js`)

Primary Aura di-set ke `indigo` supaya senada dengan Tailwind:

```js
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import Aura from '@primeuix/themes/aura'      // ⚠ paket tema: verifikasi nama terbaru di docs PrimeVue
import { definePreset } from '@primeuix/themes'

const ElmsPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{indigo.50}', 100: '{indigo.100}', 200: '{indigo.200}', 300: '{indigo.300}',
      400: '{indigo.400}', 500: '{indigo.500}', 600: '{indigo.600}', 700: '{indigo.700}',
      800: '{indigo.800}', 900: '{indigo.900}', 950: '{indigo.950}',
    },
  },
})

export default {
  install(app) {
    app.use(PrimeVue, { theme: { preset: ElmsPreset } })   // dark mode: nonaktifkan lewat opsi darkModeSelector (cek docs)
    app.use(ToastService)
  },
}
```

Ikon: `primeicons` (`<i class="pi pi-user" />`). Tidak mencampur library ikon lain.

## 3. Status → Warna (pakai `StatusBadge.vue`, jangan hardcode warna)

`StatusBadge` merender PrimeVue `Tag` dengan `severity`. Warna berasal dari tema, bukan class Tailwind.

| Nilai | Label UI | Severity | Kesan |
|---|---|---|---|
| `APPROVED`, `ON_TIME`, `ACTIVE` | Disetujui / Tepat waktu / Aktif | `success` | hijau |
| `PENDING`, `LATE` | Menunggu / Terlambat | `warn` | kuning |
| `REJECTED`, `ABSENT`, `TERMINATED` | Ditolak / Absen / Diberhentikan | `danger` | merah |
| `ON_LEAVE` | Cuti | `info` | biru |
| `RESIGNED` | Mengundurkan diri | `secondary` | abu |

Label UI berbahasa Indonesia; **nilai** yang dikirim/dibandingkan tetap string enum backend.

## 4. Komponen Dasar (`components/common/`)

Dibangun sebagai **wrapper tipis di atas PrimeVue** — kalau library diganti, cukup ubah wrapper.
Buat sekali, pakai di semua modul. **Halaman/komponen lain dilarang `import ... from 'primevue/...'`.**

| Komponen | Di atas (PrimeVue) | Catatan |
|---|---|---|
| `BaseButton.vue` | `Button` | prop `variant`: `primary` \| `secondary` \| `danger` → `severity`; prop `loading` |
| `BaseInput.vue` | `InputText` | label + pesan error per field bawaan |
| `BaseSelect.vue` | `Select` | (`Dropdown` sudah diganti `Select`); opsi via props `options`, `optionLabel`, `optionValue` |
| `BaseTable.vue` | `DataTable` + `Column` | kolom dinamis, slot `actions`, pagination server-side (lazy), loading & empty state |
| `BaseModal.vue` | `Dialog` | form cepat & konfirmasi delete |
| `StatusBadge.vue` | `Tag` | mapping § 3 |

Ditambahkan **saat dibutuhkan** (daftarkan di sini ketika dibuat):

| Komponen | Di atas | Dibutuhkan di |
|---|---|---|
| `BaseDatePicker.vue` | `DatePicker` | Leave, Attendance filter, ReviewPeriod |
| `BaseTextarea.vue` | `Textarea` | Leave reason, Review comments |
| `AppToast.vue` + `useNotify.js` | `Toast` + `ToastService` | notifikasi sukses/gagal (dipasang sekali di `App.vue`) |
| `BaseChart.vue` | `Chart` | Dashboard — ⚠ butuh dependency `chart.js`; **beri tahu user sebelum install** |
| `PageHeader.vue` | — (Tailwind) | judul halaman + tombol aksi kanan |
| `EmptyState.vue` | — (Tailwind) | list kosong |

## 5. Layout Shell (semua halaman setelah login)

```
┌─────────────────────────────────────────────┐
│  AppHeader: logo | nama user & role | logout │
├───────────┬───────────────────────────────────┤
│           │                                   │
│ AppSidebar│         Content area              │
│ (menu per │  (di-render lewat <router-view /> │
│  role)    │   dalam DefaultLayout.vue)        │
└───────────┴───────────────────────────────────┘
```

- File: `components/layout/AppHeader.vue`, `AppSidebar.vue`, `DefaultLayout.vue`. Halaman Login berdiri sendiri (tanpa layout).
- Menu sidebar difilter dari `authStore.role`:

| Menu | Employee | Manager | HR |
|---|:---:|:---:|:---:|
| Dashboard | — | ✅ (tim) | ✅ |
| Profil Saya | ✅ | ✅ | ✅ |
| Absensi (check-in/out + riwayat) | ✅ | ✅ | ✅ |
| Cuti Saya (ajukan + riwayat) | ✅ | ✅ | ✅ |
| Persetujuan Cuti | — | ✅ | ✅ |
| Riwayat Absensi Tim/Semua | — | ✅ | ✅ |
| Review Saya | ✅ | ✅ | ✅ |
| Review Tim | — | ✅ | — |
| Periode Review | — | — | ✅ |
| Employee / Department / Position | — | — | ✅ |

## 6. Pola Halaman

### List (Employee, Department, Position, Attendance, Leave, ...)
```
[ PageHeader: Judul ]                 [ + Tambah ]
[ Filter bar: search / select / date range ]
┌──────────────────────────────────────────┐
│ BaseTable (kolom relevan + kolom Aksi)    │
└──────────────────────────────────────────┘
[ Pagination ]
```

### Form
- **Modal** (`BaseModal`) untuk data sederhana: Department, Position, ReviewPeriod.
- **Halaman penuh** untuk data kompleks: Employee (banyak field + relasi department/position/manager), Review.
- Selalu tampilkan **pesan error per field** (dari `error.fields` API + validasi client), bukan alert generik.
- Tombol: `Simpan` (primary, ada loading) di kanan bawah, `Batal` (secondary) di sebelahnya.

### Konfirmasi aksi destruktif
`BaseModal` dengan judul jelas, teks dampak, tombol `Hapus` (danger) + `Batal`.

### Dashboard
Baris kartu ringkasan (angka besar + label) → grafik/tabel di bawahnya (`grid gap-6`, 1 kolom mobile, 2–3 kolom desktop).

## 7. State & Feedback (wajib di setiap halaman)

| Kondisi | Tampilan |
|---|---|
| Loading | `loading` pada `BaseTable`/`BaseButton` (bukan halaman kosong diam) |
| Kosong | `EmptyState` dengan teks ramah + aksi utama bila relevan |
| Error load | pesan error di area konten + tombol "Coba lagi" |
| Sukses simpan/aksi | Toast hijau singkat |
| Gagal simpan/aksi | Toast merah berisi `error.message` dari API; field error tetap di bawah field |
| 401 | otomatis logout & redirect ke login (interceptor) |
| 403 | halaman/pesan "Tidak punya akses" |

## 8. Format Data di UI

- Tanggal: `dd MMM yyyy` (mis. `20 Sep 2026`); jam: `HH:mm` (zona `Asia/Jakarta`). Semua lewat `utils/formatDate.js`.
- Durasi kerja: `8j 30m`. Saldo cuti: `12 hari`. Skor review: 2 desimal, skala 1–5.
- Bahasa label UI: Indonesia.

## 9. Aturan untuk Agent

1. Sebelum membuat komponen baru, cek § 4 — pakai ulang bila sudah ada.
2. Jangan memilih warna/gaya baru per halaman; ikuti § 2–3.
3. Jangan import PrimeVue di luar `Base*.vue` / `plugins/primevue.js`.
4. Pola/komponen baru → tambahkan ke dokumen ini dulu.
