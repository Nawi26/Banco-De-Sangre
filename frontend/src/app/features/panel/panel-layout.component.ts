import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AccesibilidadService, Idioma } from '../../core/services/accesibilidad.service';

@Component({
  selector: 'app-panel-layout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './panel-layout.component.html'
})
export class PanelLayoutComponent {
  constructor(public authService: AuthService, public accesibilidad: AccesibilidadService) {}

  cerrarSesion(): void {
    this.authService.logout();
  }

  cambiarIdioma(idioma: string): void {
    this.accesibilidad.cambiarIdioma(idioma as Idioma);
  }
}
