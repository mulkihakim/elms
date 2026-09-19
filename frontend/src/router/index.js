import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
      children: [
        {
          path: '',
          redirect: '/dashboard',
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
        },
        {
          path: 'departments',
          name: 'departments',
          component: () => import('@/views/organization/DepartmentListView.vue'),
        },
        {
          path: 'positions',
          name: 'positions',
          component: () => import('@/views/organization/PositionListView.vue'),
        },
        {
          path: 'employees',
          name: 'employees',
          component: () => import('@/views/employee/EmployeeListView.vue'),
        },
        {
          path: 'employees/create',
          name: 'employee-create',
          component: () => import('@/views/employee/EmployeeFormView.vue'),
        },
        {
          path: 'employees/:id/edit',
          name: 'employee-edit',
          component: () => import('@/views/employee/EmployeeFormView.vue'),
        },
        {
          path: 'attendance',
          name: 'attendance',
          component: () => import('@/views/placeholder/PlaceholderView.vue'),
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

export default router
