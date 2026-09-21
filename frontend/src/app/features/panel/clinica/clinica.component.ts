import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicoService } from '../../../core/services/clinico.service';
import { AuthService } from '../../../core/services/auth.service';
import { Despacho, Paciente, PruebaCompatibilidad, ReservaQuirurgica, Solicitud, Transfusion } from '../../../core/models/clinico.model';

@Component({
  selector: 'app-clinica',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clinica.component.html'
})
export class ClinicaComponent implements OnInit {
  pestanaActiva: 'pacientes' | 'solicitudes' | 'pruebasCruzadas' | 'despachos' | 'transfusiones' | 'reservas' = 'solicitudes';

  pacientes: Paciente[] = [];
  solicitudes: Solicitud[] = [];
  transfusiones: Transfusion[] = [];
  pruebasCruzadas: PruebaCompatibilidad[] = [];
  despachosPendientes: Despacho[] = [];
  reservasQuirurgicas: ReservaQuirurgica[] = [];

  nuevoPaciente = { tipoDoc: 'DNI', numDoc: '', nombres: '', apellidos: '', fechaNacimiento: null as string | null, sexo: '', grupoAbo: 'O', factorRh: 'POSITIVO' };
  mensajePaciente = '';
  exitoPaciente = false;

  nuevaSolicitud = { pacienteId: null as number | null, tipoHemocomponente: 'PAQUETE GLOBULAR', unidadesSolicitadas: 1, prioridad: 'RUTINA', indicacionClinica: '', diagnosticoCie10: '' };
  mensajeSolicitud = '';
  exitoSolicitud = false;

  nuevaPruebaCruzada = { solicitudId: null as number | null, codigoProductoIsbt: '', resultadoRai: 'NEGATIVO', resultadoPruebaCruzada: 'COMPATIBLE' };
  mensajePruebaCruzada = '';
  exitoPruebaCruzada = false;

  nuevoDespacho = { solicitudId: null as number | null, codigoProductoIsbt: '', claveConfirmacion: '' };
  mensajeDespacho = '';
  exitoDespacho = false;
  clavesConfirmacion: Record<number, string> = {};

  nuevaTransfusion = { solicitudId: null as number | null, codigoProductoIsbt: '', reaccionAdversa: false, detallesReaccion: '' };
  mensajeTransfusion = '';
  exitoTransfusion = false;

  nuevaReserva = {
    pacienteId: null as number | null, tipoHemocomponente: 'PAQUETE GLOBULAR', grupoAbo: 'O', factorRh: 'POSITIVO',
    unidadesSolicitadas: 1, fechaCirugiaProgramada: '', horasValidezPostCirugia: null as number | null
  };
  mensajeReserva = '';
  exitoReserva = false;

  constructor(private clinicoService: ClinicoService, public authService: AuthService) {}

  ngOnInit(): void {
    this.cargarPacientes();
    this.cargarSolicitudes();
    this.cargarTransfusiones();
    this.cargarDespachosPendientes();
    this.cargarReservasQuirurgicas();
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
        this.nuevaSolicitud = { pacienteId: null, tipoHemocomponente: 'PAQUETE GLOBULAR', unidadesSolicitadas: 1, prioridad: 'RUTINA', indicacionClinica: '', diagnosticoCie10: '' };
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

  // RF-12: prueba cruzada y RAI
  cargarPruebasCruzadas(): void {
    this.mensajePruebaCruzada = '';
    if (!this.nuevaPruebaCruzada.solicitudId) {
      this.pruebasCruzadas = [];
      return;
    }
    this.clinicoService.listarPruebasCruzadas(this.nuevaPruebaCruzada.solicitudId).subscribe(datos => this.pruebasCruzadas = datos);
  }

  registrarPruebaCruzada(): void {
    this.mensajePruebaCruzada = '';
    if (!this.nuevaPruebaCruzada.solicitudId) return;

    const solicitudId = this.nuevaPruebaCruzada.solicitudId;
    this.clinicoService.registrarPruebaCruzada(solicitudId, {
      codigoProductoIsbt: this.nuevaPruebaCruzada.codigoProductoIsbt,
      resultadoRai: this.nuevaPruebaCruzada.resultadoRai,
      resultadoPruebaCruzada: this.nuevaPruebaCruzada.resultadoPruebaCruzada
    }).subscribe({
      next: () => {
        this.exitoPruebaCruzada = true;
        this.mensajePruebaCruzada = 'Prueba cruzada registrada correctamente.';
        this.nuevaPruebaCruzada.codigoProductoIsbt = '';
        this.cargarPruebasCruzadas();
      },
      error: (err) => {
        this.exitoPruebaCruzada = false;
        this.mensajePruebaCruzada = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  // RF-13: despacho con doble verificación electrónica
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

  // RF-25: reserva quirúrgica con liberación automática
  cargarReservasQuirurgicas(): void {
    this.clinicoService.listarReservasQuirurgicas().subscribe(datos => this.reservasQuirurgicas = datos);
  }

  reservarQuirurgica(): void {
    this.mensajeReserva = '';
    if (!this.nuevaReserva.pacienteId || !this.nuevaReserva.fechaCirugiaProgramada) return;

    this.clinicoService.crearReservaQuirurgica(this.nuevaReserva as any).subscribe({
      next: () => {
        this.exitoReserva = true;
        this.mensajeReserva = 'Reserva quirúrgica registrada correctamente.';
        this.nuevaReserva = {
          pacienteId: null, tipoHemocomponente: 'PAQUETE GLOBULAR', grupoAbo: 'O', factorRh: 'POSITIVO',
          unidadesSolicitadas: 1, fechaCirugiaProgramada: '', horasValidezPostCirugia: null
        };
        this.cargarReservasQuirurgicas();
      },
      error: (err) => {
        this.exitoReserva = false;
        this.mensajeReserva = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  liberarReserva(id: number): void {
    if (!confirm('¿Liberar esta reserva quirúrgica? Las unidades apartadas volverán al inventario disponible.')) return;
    this.clinicoService.liberarReservaQuirurgica(id).subscribe({
      next: () => this.cargarReservasQuirurgicas(),
      error: (err) => {
        this.exitoReserva = false;
        this.mensajeReserva = err.error?.mensaje ?? 'No se pudo liberar la reserva.';
      }
    });
  }

  marcarReservaUtilizada(id: number): void {
    this.clinicoService.marcarReservaQuirurgicaUtilizada(id).subscribe({
      next: () => this.cargarReservasQuirurgicas(),
      error: (err) => {
        this.exitoReserva = false;
        this.mensajeReserva = err.error?.mensaje ?? 'No se pudo actualizar la reserva.';
      }
    });
  }

  colorEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'secondary';
      case 'APROBADA': return 'info';
      case 'DESPACHADA': return 'primary';
      case 'ATENDIDA': return 'success';
      case 'RECHAZADA': return 'danger';
      case 'RESERVADA': return 'info';
      case 'UTILIZADA': return 'success';
      case 'LIBERADA': return 'secondary';
      default: return 'secondary';
    }
  }
}
