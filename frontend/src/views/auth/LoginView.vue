<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const isSubmitting = ref(false)

const demoAccounts = [
  {
    role: 'HR Admin',
    email: 'budi.hr@elms.com',
    password: 'password123',
    badgeClass: 'bg-purple-100 text-purple-700 border-purple-200',
  },
  {
    role: 'Manager',
    email: 'siti.manager@elms.com',
    password: 'password123',
    badgeClass: 'bg-blue-100 text-blue-700 border-blue-200',
  },
  {
    role: 'Staff Employee',
    email: 'ahmad.emp@elms.com',
    password: 'password123',
    badgeClass: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  },
]

function fillDemo(acc) {
  email.value = acc.email
  password.value = acc.password
  errorMessage.value = ''
}

async function handleLogin() {
  errorMessage.value = ''

  if (!email.value.trim() || !password.value) {
    errorMessage.value = 'Harap isi email dan password.'
    return
  }

  isSubmitting.value = true
  try {
    await authStore.login({
      email: email.value.trim(),
      password: password.value,
    })

    const redirectPath = route.query.redirect || '/dashboard'
    router.push(redirectPath)
  } catch (err) {
    errorMessage.value = err.message || 'Email atau kata sandi tidak sesuai.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <!-- Logo Branding -->
      <div class="flex justify-center items-center gap-3">
        <div
          class="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white font-bold text-lg shadow-md"
        >
          E
        </div>
        <span class="text-2xl font-extrabold text-slate-800 tracking-tight">ELMS</span>
      </div>
      <h2 class="mt-3 text-center text-xl font-bold tracking-tight text-slate-800">
        Employee Lifecycle Management
      </h2>
      <p class="mt-1 text-center text-xs text-slate-500">
        Silakan masuk untuk mengakses portal kerja Anda
      </p>
    </div>

    <div class="mt-6 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
      <div class="bg-white py-8 px-6 shadow-sm rounded-2xl border border-slate-200 sm:px-8">
        <!-- Error Alert -->
        <div
          v-if="errorMessage"
          class="mb-5 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2"
        >
          <i class="pi pi-exclamation-circle text-sm shrink-0"></i>
          <span>{{ errorMessage }}</span>
        </div>

        <form class="space-y-4" @submit.prevent="handleLogin">
          <BaseInput
            id="email"
            v-model="email"
            type="email"
            label="Alamat Email"
            placeholder="nama@elms.com"
            required
            autocomplete="email"
          />

          <BaseInput
            id="password"
            v-model="password"
            type="password"
            label="Kata Sandi"
            placeholder="••••••••"
            required
            autocomplete="current-password"
          />

          <div class="pt-2">
            <BaseButton
              type="submit"
              label="Masuk ke Sistem"
              icon="pi pi-sign-in"
              class="w-full justify-center shadow-xs"
              :loading="isSubmitting"
            />
          </div>
        </form>

        <!-- Demo Accounts Section -->
        <div class="mt-8 pt-6 border-t border-slate-100">
          <p class="text-[11px] font-semibold text-slate-400 uppercase tracking-wider text-center mb-3">
            Akun Percobaan (Klik untuk Isi Otomatis)
          </p>
          <div class="grid grid-cols-3 gap-2">
            <button
              v-for="acc in demoAccounts"
              :key="acc.email"
              type="button"
              class="p-2 rounded-lg border text-center transition-all hover:shadow-xs focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
              :class="acc.badgeClass"
              @click="fillDemo(acc)"
            >
              <div class="text-xs font-bold">{{ acc.role }}</div>
              <div class="text-[10px] opacity-80 truncate mt-0.5">{{ acc.email.split('@')[0] }}</div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
