import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { supervisionGuard } from './core/guards/supervision.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent)
  },
  {
    // RF-41: verificación pública de certificados de calidad, sin autenticación.
    path: 'verificar/:codigo',
    loadComponent: () => import('./features/verificar/verificar-certificado.component').then(m => m.VerificarCertificadoComponent)
  },
  {
    path: 'panel',
    loadComponent: () => import('./features/panel/panel-layout.component').then(m => m.PanelLayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/panel/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'inventario',
        loadComponent: () => import('./features/panel/inventario/inventario.component').then(m => m.InventarioComponent)
      },
      {
        path: 'red',
        loadComponent: () => import('./features/panel/red/red.component').then(m => m.RedComponent)
      },
      {
        path: 'donaciones',
        loadComponent: () => import('./features/panel/donaciones/donaciones.component').then(m => m.DonacionesComponent)
      },
      {
        path: 'etiquetado',
        loadComponent: () => import('./features/panel/etiquetado/etiquetado.component').then(m => m.EtiquetadoComponent)
      },
      {
        path: 'solicitudes',
        loadComponent: () => import('./features/panel/solicitudes-clinicas/solicitudes-clinicas.component').then(m => m.SolicitudesClinicasComponent)
      },
      {
        path: 'despacho',
        loadComponent: () => import('./features/panel/despacho/despacho.component').then(m => m.DespachoComponent)
      },
      {
        path: 'movil',
        loadComponent: () => import('./features/panel/app-movil/app-movil.component').then(m => m.AppMovilComponent)
      },
      {
        path: 'hemovigilancia',
        loadComponent: () => import('./features/panel/hemovigilancia/hemovigilancia.component').then(m => m.HemovigilanciaComponent)
      },
      {
        path: 'soporte',
        loadComponent: () => import('./features/panel/soporte/soporte.component').then(m => m.SoporteComponent)
      },
      {
        path: 'reportes',
        loadComponent: () => import('./features/panel/reportes/reportes.component').then(m => m.ReportesComponent),
        canActivate: [supervisionGuard]
      },
      {
        path: 'auditoria',
        loadComponent: () => import('./features/panel/auditoria/auditoria.component').then(m => m.AuditoriaComponent),
        canActivate: [supervisionGuard]
      },
      {
        path: 'administracion',
        loadComponent: () => import('./features/panel/administracion/administracion.component').then(m => m.AdministracionComponent),
        canActivate: [adminGuard]
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
