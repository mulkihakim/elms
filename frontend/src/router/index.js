import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { setupRouteGuards } from './guards'
import { useAuthStore } from '@/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/',
      component: DefaultLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: () => {
            const authStore = useAuthStore()
            return authStore.isEmployee ? '/attendance' : '/dashboard'
          },
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { roles: ['HR', 'MANAGER'] },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/employee/ProfileView.vue'),
        },
        {
          path: 'departments',
          name: 'departments',
          component: () => import('@/views/organization/DepartmentListView.vue'),
          meta: { roles: ['HR'] },
        },
        {
          path: 'positions',
          name: 'positions',
          component: () => import('@/views/organization/PositionListView.vue'),
          meta: { roles: ['HR'] },
        },
        {
          path: 'employees',
          name: 'employees',
          component: () => import('@/views/employee/EmployeeListView.vue'),
          meta: { roles: ['HR'] },
        },
        {
          path: 'employees/create',
          name: 'employee-create',
          component: () => import('@/views/employee/EmployeeFormView.vue'),
          meta: { roles: ['HR'] },
        },
        {
          path: 'employees/:id/edit',
          name: 'employee-edit',
          component: () => import('@/views/employee/EmployeeFormView.vue'),
          meta: { roles: ['HR'] },
        },
        {
          path: 'attendance',
          name: 'attendance',
          component: () => import('@/views/attendance/AttendanceView.vue'),
        },
        {
          path: 'leaves',
          name: 'leaves',
          component: () => import('@/views/placeholder/PlaceholderView.vue'),
        },
        {
          path: 'reviews',
          name: 'reviews',
          component: () => import('@/views/placeholder/PlaceholderView.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard',
    },
  ],
})

// Pasang route guard untuk autentikasi dan RBAC
setupRouteGuards(router)

export default router
