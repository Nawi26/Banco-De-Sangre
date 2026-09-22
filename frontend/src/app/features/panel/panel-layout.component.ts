import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavigationStart, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AccesibilidadService, Idioma } from '../../core/services/accesibilidad.service';
import { NotificacionesService } from '../../core/services/notificaciones.service';
import { ClinicoService } from '../../core/services/clinico.service';
import { Mensaje, Solicitud } from '../../core/models/clinico.model';

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

  // RF-48: mensajería interna banco de sangre <-> servicio asistencial, por solicitud.
  mensajeriaAbierta = signal(false);
  solicitudes: Solicitud[] = [];
  solicitudIdMensajes: number | null = null;
  mensajes: Mensaje[] = [];
  nuevoMensaje = '';
  errorMensajes = '';

  constructor(public authService: AuthService, public accesibilidad: AccesibilidadService,
              public notificaciones: NotificacionesService, private clinicoService: ClinicoService, router: Router) {
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

  abrirMensajeria(): void {
    this.mensajeriaAbierta.set(true);
    if (this.solicitudes.length === 0) {
      this.clinicoService.listarSolicitudes().subscribe(datos => this.solicitudes = datos);
    }
  }

  cerrarMensajeria(): void {
    this.mensajeriaAbierta.set(false);
  }

  cargarMensajes(): void {
    this.errorMensajes = '';
    if (!this.solicitudIdMensajes) {
      this.mensajes = [];
      return;
    }
    this.clinicoService.listarMensajes(this.solicitudIdMensajes).subscribe({
      next: datos => this.mensajes = datos,
      error: (err) => this.errorMensajes = err.error?.mensaje ?? 'No se pudo contactar con el servidor.'
    });
  }

  enviarMensaje(): void {
    if (!this.solicitudIdMensajes || !this.nuevoMensaje.trim()) return;
    const solicitudId = this.solicitudIdMensajes;
    this.clinicoService.enviarMensaje(solicitudId, { contenido: this.nuevoMensaje }).subscribe({
      next: () => {
        this.nuevoMensaje = '';
        this.cargarMensajes();
      },
      error: (err) => this.errorMensajes = err.error?.mensaje ?? 'No se pudo contactar con el servidor.'
    });
  }
}
