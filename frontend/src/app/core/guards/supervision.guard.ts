import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// RF-26/RF-44: paneles gerenciales, sólo Jefe de Banco de Sangre o Administrador.
export const supervisionGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.esSupervisionBancoSangre()) {
    return true;
  }

  return router.parseUrl('/panel/dashboard');
};
