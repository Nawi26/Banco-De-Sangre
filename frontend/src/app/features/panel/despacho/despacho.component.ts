import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicoService } from '../../../core/services/clinico.service';
import { AuthService } from '../../../core/services/auth.service';
import { Despacho, Solicitud, Transfusion } from '../../../core/models/clinico.model';

// RF-13: doble verificación electrónica del despacho de hemocomponentes.
@Component({
  selector: 'app-despacho',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './despacho.component.html'
})
export class DespachoComponent implements OnInit {
  pestanaActiva: 'despachos' | 'transfusiones' = 'despachos';

  solicitudes: Solicitud[] = [];
  transfusiones: Transfusion[] = [];
  despachosPendientes: Despacho[] = [];

  nuevoDespacho = { solicitudId: null as number | null, codigoProductoIsbt: '', claveConfirmacion: '' };
  mensajeDespacho = '';
  exitoDespacho = false;
  clavesConfirmacion: Record<number, string> = {};

  nuevaTransfusion = { solicitudId: null as number | null, codigoProductoIsbt: '', reaccionAdversa: false, detallesReaccion: '' };
  mensajeTransfusion = '';
  exitoTransfusion = false;

  constructor(private clinicoService: ClinicoService, public authService: AuthService) {}

  ngOnInit(): void {
    this.cargarSolicitudes();
    this.cargarTransfusiones();
    this.cargarDespachosPendientes();
  }

  cargarSolicitudes(): void {
    this.clinicoService.listarSolicitudes().subscribe(datos => this.solicitudes = datos);
  }

  cargarTransfusiones(): void {
    this.clinicoService.listarTransfusiones().subscribe(datos => this.transfusiones = datos);
  }

  solicitudesAprobadas(): Solicitud[] {
    return this.solicitudes.filter(s => s.estado === 'APROBADA');
  }

  cargarDespachosPendientes(): void {
    this.clinicoService.listarDespachosPendientes().subscribe(datos => this.despachosPendientes = datos);
  }

  iniciarDespacho(): void {
    this.mensajeDespacho = '';
    if (!this.nuevoDespacho.solicitudId) return;

    this.clinicoService.iniciarDespacho(this.nuevoDespacho as any).subscribe({
      next: () => {
        this.exitoDespacho = true;
        this.mensajeDespacho = 'Primera verificación registrada. Falta la segunda verificación de un responsable distinto.';
        this.nuevoDespacho = { solicitudId: null, codigoProductoIsbt: '', claveConfirmacion: '' };
        this.cargarDespachosPendientes();
      },
      error: (err) => {
        this.exitoDespacho = false;
        this.mensajeDespacho = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  confirmarDespacho(id: number): void {
    this.mensajeDespacho = '';
    const clave = this.clavesConfirmacion[id];
    this.clinicoService.confirmarDespacho(id, { claveConfirmacion: clave }).subscribe({
      next: () => {
        this.exitoDespacho = true;
        this.mensajeDespacho = 'Despacho confirmado con doble verificación electrónica.';
        delete this.clavesConfirmacion[id];
        this.cargarDespachosPendientes();
      },
      error: (err) => {
        this.exitoDespacho = false;
        this.mensajeDespacho = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  registrarTransfusion(): void {
    this.mensajeTransfusion = '';
    if (!this.nuevaTransfusion.solicitudId) return;

    this.clinicoService.crearTransfusion(this.nuevaTransfusion as any).subscribe({
      next: () => {
        this.exitoTransfusion = true;
        this.mensajeTransfusion = 'Transfusión registrada correctamente.';
        this.nuevaTransfusion = { solicitudId: null, codigoProductoIsbt: '', reaccionAdversa: false, detallesReaccion: '' };
        this.cargarTransfusiones();
        this.cargarSolicitudes();
      },
      error: (err) => {
        this.exitoTransfusion = false;
        this.mensajeTransfusion = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }
}
