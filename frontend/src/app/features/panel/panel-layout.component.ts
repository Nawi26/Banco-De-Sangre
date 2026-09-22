import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavigationStart, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AccesibilidadService, Idioma } from '../../core/services/accesibilidad.service';
import { NotificacionesService } from '../../core/services/notificaciones.service';

// RF-20/RNF-11: layout responsive — en pantallas móviles el sidebar se colapsa
// detrás de un botón de menú, en vez de comprimir el contenido.
@Component({
  selector: 'app-panel-layout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './panel-layout.component.html'
})
export class PanelLayoutComponent {
  sidebarAbierto = signal(false);

  constructor(public authService: AuthService, public accesibilidad: AccesibilidadService,
              public notificaciones: NotificacionesService, router: Router) {
    router.events.subscribe(evento => {
      if (evento instanceof NavigationStart) {
        this.sidebarAbierto.set(false);
      }
    });
  }

  alternarSidebar(): void {
    this.sidebarAbierto.set(!this.sidebarAbierto());
  }

  cerrarSesion(): void {
    this.authService.logout();
  }

  cambiarIdioma(idioma: string): void {
    this.accesibilidad.cambiarIdioma(idioma as Idioma);
  }
}
