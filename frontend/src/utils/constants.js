/**
 * Konstanta enum yang diselaraskan dengan backend Java enum (docs/03-FOLDER-STRUCTURE.md §3)
 */

export const EMPLOYMENT_STATUS = {
  ACTIVE: 'ACTIVE',
  ON_LEAVE: 'ON_LEAVE',
  RESIGNED: 'RESIGNED',
  TERMINATED: 'TERMINATED',
}

export const EMPLOYMENT_STATUS_OPTIONS = [
  { label: 'Aktif (ACTIVE)', value: 'ACTIVE' },
  { label: 'Cuti (ON_LEAVE)', value: 'ON_LEAVE' },
  { label: 'Mengundurkan Diri (RESIGNED)', value: 'RESIGNED' },
  { label: 'Diberhentikan (TERMINATED)', value: 'TERMINATED' },
]

export const ROLES = {
  EMPLOYEE: 'EMPLOYEE',
  MANAGER: 'MANAGER',
  HR: 'HR',
}

export const ROLE_OPTIONS = [
  { label: 'Staff / Karyawan (EMPLOYEE)', value: 'EMPLOYEE' },
  { label: 'Manajer (MANAGER)', value: 'MANAGER' },
  { label: 'HR / Administrator (HR)', value: 'HR' },
]
