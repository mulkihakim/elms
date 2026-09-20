import { useToast } from 'primevue/usetoast'

export function useNotify() {
  const toast = useToast()

  function showSuccess(detail, summary = 'Berhasil', life = 3000) {
    toast.add({
      severity: 'success',
      summary,
      detail,
      life,
    })
  }

  function showError(detail, summary = 'Gagal', life = 4000) {
    toast.add({
      severity: 'error',
      summary,
      detail,
      life,
    })
  }

  function showWarn(detail, summary = 'Peringatan', life = 3500) {
    toast.add({
      severity: 'warn',
      summary,
      detail,
      life,
    })
  }

  function showInfo(detail, summary = 'Informasi', life = 3000) {
    toast.add({
      severity: 'info',
      summary,
      detail,
      life,
    })
  }

  return {
    showSuccess,
    showError,
    showWarn,
    showInfo,
    toast,
  }
}

export default useNotify
