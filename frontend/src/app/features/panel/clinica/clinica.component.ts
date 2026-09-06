import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicoService } from '../../../core/services/clinico.service';
import { AuthService } from '../../../core/services/auth.service';
import { Paciente, Solicitud, Transfusion } from '../../../core/models/clinico.model';

@Component({
  selector: 'app-clinica',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clinica.component.html'
})
export class ClinicaComponent implements OnInit {
  pestanaActiva: 'pacientes' | 'solicitudes' | 'transfusiones' = 'solicitudes';

  pacientes: Paciente[] = [];
  solicitudes: Solicitud[] = [];
  transfusiones: Transfusion[] = [];

  nuevoPaciente = { tipoDoc: 'DNI', numDoc: '', nombres: '', apellidos: '', fechaNacimiento: null as string | null, sexo: '', grupoAbo: 'O', factorRh: 'POSITIVO' };
  mensajePaciente = '';
  exitoPaciente = false;

  nuevaSolicitud = { pacienteId: null as number | null, tipoHemocomponente: 'PAQUETE GLOBULAR', unidadesSolicitadas: 1, prioridad: 'RUTINA', indicacionClinica: '' };
  mensajeSolicitud = '';
  exitoSolicitud = false;

  nuevaTransfusion = { solicitudId: null as number | null, codigoProductoIsbt: '', resultadoPruebaCruzada: 'COMPATIBLE', reaccionAdversa: false, detallesReaccion: '' };
  mensajeTransfusion = '';
  exitoTransfusion = false;

  constructor(private clinicoService: ClinicoService, public authService: AuthService) {}

  ngOnInit(): void {
    this.cargarPacientes();
    this.cargarSolicitudes();
    this.cargarTransfusiones();
  }

  cargarPacientes(): void {
    this.clinicoService.listarPacientes().subscribe(datos => this.pacientes = datos);
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

  registrarPaciente(): void {
    this.mensajePaciente = '';
    this.clinicoService.crearPaciente(this.nuevoPaciente).subscribe({
      next: () => {
        this.exitoPaciente = true;
        this.mensajePaciente = 'Paciente registrado correctamente.';
        this.nuevoPaciente = { tipoDoc: 'DNI', numDoc: '', nombres: '', apellidos: '', fechaNacimiento: null, sexo: '', grupoAbo: 'O', factorRh: 'POSITIVO' };
        this.cargarPacientes();
      },
      error: (err) => {
        this.exitoPaciente = false;
        this.mensajePaciente = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  registrarSolicitud(): void {
    this.mensajeSolicitud = '';
    if (!this.nuevaSolicitud.pacienteId) return;

    this.clinicoService.crearSolicitud(this.nuevaSolicitud as any).subscribe({
      next: () => {
        this.exitoSolicitud = true;
        this.mensajeSolicitud = 'Solicitud registrada correctamente.';
        this.nuevaSolicitud = { pacienteId: null, tipoHemocomponente: 'PAQUETE GLOBULAR', unidadesSolicitadas: 1, prioridad: 'RUTINA', indicacionClinica: '' };
        this.cargarSolicitudes();
      },
      error: (err) => {
        this.exitoSolicitud = false;
        this.mensajeSolicitud = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  cambiarEstadoSolicitud(id: number, estado: string): void {
    this.clinicoService.actualizarEstadoSolicitud(id, estado).subscribe(() => this.cargarSolicitudes());
  }

  eliminarSolicitud(id: number, codigo: string): void {
    if (!confirm(`¿Eliminar la solicitud ${codigo}? Esta acción no se puede deshacer.`)) return;

    this.clinicoService.eliminarSolicitud(id).subscribe({
      next: () => this.cargarSolicitudes(),
      error: (err) => {
        this.exitoSolicitud = false;
        this.mensajeSolicitud = err.error?.mensaje ?? 'No se pudo eliminar la solicitud.';
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
        this.nuevaTransfusion = { solicitudId: null, codigoProductoIsbt: '', resultadoPruebaCruzada: 'COMPATIBLE', reaccionAdversa: false, detallesReaccion: '' };
        this.cargarTransfusiones();
        this.cargarSolicitudes();
      },
      error: (err) => {
        this.exitoTransfusion = false;
        this.mensajeTransfusion = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  colorEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'secondary';
      case 'APROBADA': return 'info';
      case 'ATENDIDA': return 'success';
      case 'RECHAZADA': return 'danger';
      default: return 'secondary';
    }
  }
}
