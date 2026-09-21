import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SoporteService } from '../../../core/services/soporte.service';
import { AuthService } from '../../../core/services/auth.service';
import { TicketSoporte } from '../../../core/models/soporte.model';

@Component({
  selector: 'app-soporte',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './soporte.component.html'
})
export class SoporteComponent implements OnInit {
  pestanaActiva: 'propios' | 'todos' = 'propios';

  ticketsPropios: TicketSoporte[] = [];
  ticketsTodos: TicketSoporte[] = [];

  nuevoTicket = { asunto: '', descripcion: '', prioridad: 'MEDIA' };
  mensaje = '';
  exito = false;

  respuestas: Record<number, { estado: string; respuesta: string }> = {};

  constructor(private soporteService: SoporteService, public authService: AuthService) {}

  ngOnInit(): void {
    this.cargarPropios();
    if (this.authService.esAdministrador()) {
      this.cargarTodos();
    }
  }

  cargarPropios(): void {
    this.soporteService.listarPropios().subscribe(datos => this.ticketsPropios = datos);
  }

  cargarTodos(): void {
    this.soporteService.listarTodos().subscribe(datos => {
      this.ticketsTodos = datos;
      datos.forEach(t => this.respuestas[t.id] = { estado: t.estado, respuesta: t.respuesta ?? '' });
    });
  }

  crear(): void {
    this.mensaje = '';
    this.soporteService.crear(this.nuevoTicket).subscribe({
      next: () => {
        this.exito = true;
        this.mensaje = 'Incidencia reportada correctamente.';
        this.nuevoTicket = { asunto: '', descripcion: '', prioridad: 'MEDIA' };
        this.cargarPropios();
      },
      error: (err) => {
        this.exito = false;
        this.mensaje = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  responder(id: number): void {
    const datos = this.respuestas[id];
    this.soporteService.responder(id, datos).subscribe(() => this.cargarTodos());
  }

  colorEstado(estado: string): string {
    switch (estado) {
      case 'ABIERTO': return 'secondary';
      case 'EN_PROCESO': return 'warning';
      case 'RESUELTO': return 'success';
      case 'CERRADO': return 'dark';
      default: return 'secondary';
    }
  }
}
