# Setup Project — ELMS (Monorepo)

## 1. Struktur Root Repo

```
elms/
├── AGENTS.md
├── docs/
├── backend/
├── frontend/
├── docker-compose.yml     # opsional: PostgreSQL lokal
└── README.md
```

Kenapa monorepo: satu repo lebih praktis untuk portfolio (satu link, docs
langsung relevan untuk FE & BE, tidak perlu sinkronisasi antar repo). Kalau
nanti butuh deploy dengan versioning independen, migrasi ke polyrepo tetap
mudah karena `backend/` dan `frontend/` sudah terisolasi sejak awal.

---

## 2. Backend Setup (Spring Boot)

### 2.1 Generate Project

Buka [start.spring.io](https://start.spring.io) dengan konfigurasi:

| Field | Nilai |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | versi stable terbaru (3.3.x atau lebih baru saat kamu setup) |
| Group | `com.elms` |
| Artifact | `backend` |
| Packaging | Jar |
| Java | 21 (atau 17 kalau environment-mu belum support 21) |

### 2.2 Dependency yang Dipilih di Initializr

| Dependency | Alasan dipakai di project ini |
|---|---|
| **Spring Web** | Bikin REST API (Controller) |
| **Spring Data JPA** | Akses PostgreSQL lewat Repository, tanpa nulis SQL manual untuk query dasar |
| **Spring Security** | Dasar untuk autentikasi & otorisasi (nanti ditambah JWT manual) |
| **PostgreSQL Driver** | Driver koneksi ke PostgreSQL |
| **Validation** | Untuk `@NotNull`, `@Email`, dll di DTO |
| **Lombok** | Mengurangi boilerplate getter/setter/constructor |
| **Spring Boot DevTools** | Auto-restart saat development (jangan ikut ke production build) |

### 2.3 Dependency yang TIDAK ada di Initializr (tambah manual ke `pom.xml`)

```xml
<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.6</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.6</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.6</version>
  <scope>runtime</scope>
</dependency>

<!-- Dokumentasi API otomatis (opsional tapi sangat membantu saat integrasi dengan FE) -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.6.0</version>
</dependency>
```

> Cek versi terbaru masing-masing di [Maven Central](https://mvnrepository.com) sebelum
> menambahkan — angka di atas adalah versi yang umum dipakai saat dokumen ini
> ditulis, bukan patokan mutlak.

**Tidak perlu dulu (hindari over-engineering di awal):** MapStruct — untuk MVP,
mapping Entity↔DTO manual saja cukup dan lebih mudah dipahami saat belajar.
Tambahkan MapStruct nanti kalau mapping-nya sudah terasa repetitif.

### 2.4 Konfigurasi Awal (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/elms_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update      # ganti ke 'validate' kalau sudah pakai Flyway/Liquibase
    show-sql: true
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: ${JWT_SECRET:change-this-in-env-file}
  expiration-ms: 86400000   # 24 jam

server:
  port: 8080
```

Simpan `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` di file `.env` (backend) atau
environment variable OS — **jangan** hardcode di `application.yml` yang di-commit.

### 2.5 Langkah Setelah Generate

1. Extract hasil download dari Spring Initializr ke folder `backend/`
2. Setup PostgreSQL lokal (bisa lewat `docker-compose.yml` di root, lihat § 4)
3. Buat database `elms_db`
4. Jalankan `./mvnw spring-boot:run` dari folder `backend/`
5. Cek endpoint dasar (mis. actuator/health kalau ditambahkan) untuk pastikan koneksi DB jalan
6. Baru mulai buat package per modul sesuai `docs/03-FOLDER-STRUCTURE.md`

---

## 3. Frontend Setup (Vue 3) — untuk yang belum pernah pakai Vue

### 3.1 Scaffold Project

Dari folder root repo:

```bash
npm create vue@latest
```

Command ini akan bertanya beberapa hal secara interaktif. Berikut pilihan yang
disarankan untuk project ini, beserta artinya:

| Pertanyaan | Pilih | Kenapa |
|---|---|---|
| Project name | `frontend` | supaya folder-nya jadi `frontend/` sesuai struktur monorepo |
| TypeScript? | **No** (boleh Yes kalau mau sekalian belajar TS, tapi akan menambah kurva belajar) | fokus dulu ke Vue-nya, TS bisa menyusul |
| JSX Support? | No | tidak dibutuhkan untuk project ini |
| Vue Router? | **Yes** | wajib, dipakai untuk navigasi antar halaman (Employee, Leave, dst) |
| Pinia? | **Yes** | wajib, state management yang dipakai di seluruh docs |
| Vitest (unit testing)? | Optional — Yes kalau mau ikut menulis test FE, No kalau ingin fokus fitur dulu |
| E2E Testing? | No (untuk MVP, skip dulu) |
| ESLint? | **Yes** | membantu menjaga konsistensi kode |
| Prettier? | **Yes** | auto-format, penting kalau sering dibantu agent supaya style konsisten |

Setelah selesai:

```bash
cd frontend
npm install
npm run dev
```

Vue akan jalan di `http://localhost:5173` secara default.

### 3.2 Install Axios (HTTP client ke backend)

```bash
npm install axios
```

### 3.3 Install Tailwind CSS (versi 4 — setup lebih sederhana dari versi lama)

```bash
npm install tailwindcss @tailwindcss/vite
```

Edit `vite.config.js`:

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
})
```

Tambahkan di baris paling atas file CSS utama (`src/assets/main.css` atau
`src/style.css`, sesuaikan dengan yang dibuat oleh scaffolding):

```css
@import "tailwindcss";
```
### 3.3a Install PrimeVue (UI Component Library)

```bash
npm install primevue @primevue/themes primeicons
```

Edit `main.js`:

```js
import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import 'primeicons/primeicons.css'
import App from './App.vue'

const app = createApp(App)

app.use(PrimeVue, {
  theme: {
    preset: Aura,
  },
})

app.mount('#app')
```

**Pembagian tanggung jawab styling:**
- **PrimeVue** → tampilan & perilaku komponen (button, table, dialog, form, chart)
- **Tailwind** → layout di sekitar komponen (grid, spacing, sidebar, positioning)

Jangan override styling internal komponen PrimeVue pakai class Tailwind — biarkan
ikut tema Aura. Kalau warna/tampilan perlu disesuaikan, ubah lewat konfigurasi
tema PrimeVue (lihat `06-UI-CONVENTIONS.md` §2a), bukan lewat Tailwind class di
template.

Import komponen satu per satu sesuai kebutuhan (bukan seluruh library sekaligus),
misalnya:

```js
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
```

### 3.4 Buat `.env` untuk Base URL API

```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Dipakai nanti di `src/api/axios.js`:

```js
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

export default api
```

### 3.5 Urutan Belajar yang Disarankan (karena pengalaman Vue = 0)

Sebelum minta agent generate banyak komponen sekaligus, ada baiknya kamu
sendiri coba dulu di skala kecil supaya paham mental model-nya:

1. Baca ["Quick Start" & "Introduction"](https://vuejs.org/guide/introduction.html) di dokumentasi resmi Vue — fokus ke bagian **Composition API** (`<script setup>`), karena itu yang dipakai di seluruh docs project ini.
2. Coba buat 1 komponen sederhana manual (misal komponen `EmployeeCard` menampilkan nama & jabatan dari data dummy) sebelum minta agent buatkan versi lengkapnya.
3. Baru lanjut ke Pinia (state management) — dokumentasi resminya juga singkat dan jelas.
4. Setelah dua hal itu terasa nyaman, biarkan agent membantu mempercepat sisanya (form, table, dsb) mengikuti pola yang sudah kamu pahami.

---

## 4. `docker-compose.yml` (opsional, untuk PostgreSQL lokal)

Taruh di root repo supaya satu perintah untuk seluruh tim/diri sendiri:

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: elms_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

Jalankan dengan `docker compose up -d` sebelum menjalankan backend.
