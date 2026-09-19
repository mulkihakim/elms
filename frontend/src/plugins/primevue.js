import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import { definePreset } from '@primevue/themes'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'

// Custom Preset Aura dengan Primary Palette Indigo sesuai 06-UI-CONVENTIONS.md
const IndigoPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{indigo.50}',
      100: '{indigo.100}',
      200: '{indigo.200}',
      300: '{indigo.300}',
      400: '{indigo.400}',
      500: '{indigo.500}',
      600: '{indigo.600}',
      700: '{indigo.700}',
      800: '{indigo.800}',
      900: '{indigo.900}',
      950: '{indigo.950}',
    },
  },
})

export function setupPrimeVue(app) {
  app.use(PrimeVue, {
    theme: {
      preset: IndigoPreset,
      options: {
        darkModeSelector: '.dark-mode-disabled', // Light mode default
      },
    },
  })
  app.use(ToastService)
  app.use(ConfirmationService)
}

export default setupPrimeVue
