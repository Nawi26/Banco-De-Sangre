import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  identificador = '';
  password = '';
  cargando = false;
  mensajeError = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.mensajeError = '';
    this.cargando = true;

    this.authService.login(this.identificador, this.password).subscribe({
      next: () => {
        this.router.navigateByUrl(this.authService.rutaPanel());
      },
      error: (err) => {
        this.cargando = false;
        this.mensajeError = err.error?.mensaje
          ?? 'Error: No se pudo contactar con el servidor central (verifique que el backend esté encendido).';
      }
    });
  }
}
