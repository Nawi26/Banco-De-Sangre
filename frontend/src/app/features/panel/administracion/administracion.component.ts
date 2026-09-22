import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdministracionService } from '../../../core/services/administracion.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { SoporteService } from '../../../core/services/soporte.service';
import {
  Disponibilidad, MantenimientoEquipo, ProtocoloClinico, RespaldoBaseDatos, TurnoPersonal
} from '../../../core/models/administracion.model';
import { UsuarioAdmin } from '../../../core/models/usuario.model';
import { TicketSoporte } from '../../../core/models/soporte.model';

@Component({
  selector: 'app-administracion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './administracion.component.html'
})
export class AdministracionComponent implements OnInit {
  pestanaActiva: 'usuarios' | 'turnos' | 'sistema' | 'ayuda' = 'usuarios';

  // RF-28: gestión de usuarios y roles (antes "Médicos")
  usuarios: UsuarioAdmin[] = [];
  nuevoMedico = { dni: '', nombres: '', apellidos: '', email: '', password: '', colegiatura: '', rolId: 2 };
  mensajeMedico = '';
  exitoMedico = false;

  turnos: TurnoPersonal[] = [];
  mantenimientos: MantenimientoEquipo[] = [];
  protocolos: ProtocoloClinico[] = [];
  respaldos: RespaldoBaseDatos[] = [];
  disponibilidad: Disponibilidad | null = null;

  nuevoTurno = { usuarioId: null as number | null, fechaInicio: '', fechaFin: '', tipoTurno: 'MAÑANA', observaciones: '' };
  mensajeTurno = '';
  exitoTurno = false;

  nuevoMantenimiento = { nombreEquipo: '', tipoEquipo: 'CAMARA_REFRIGERACION', tipoMantenimiento: 'PREVENTIVO', fechaRealizado: '', fechaProximoVencimiento: null as string | null, observaciones: '' };
  mensajeMantenimiento = '';
  exitoMantenimiento = false;

  nuevoProtocolo = { nombre: '', version: '', contenidoUrl: '', notasCambio: '' };
  mensajeProtocolo = '';
  exitoProtocolo = false;

  mensajeRespaldo = '';
  verificaciones: Record<number, string> = {};

  // RF-33: mesa de ayuda — vista administrativa de todas las incidencias.
  ticketsTodos: TicketSoporte[] = [];
  respuestasTicket: Record<number, { estado: string; respuesta: string }> = {};

  constructor(private administracionService: AdministracionService, private usuarioService: UsuarioService,
              private soporteService: SoporteService) {}

  ngOnInit(): void {
    this.cargarUsuarios();
    this.cargarTurnos();
    this.cargarMantenimientos();
    this.cargarProtocolos();
    this.cargarRespaldos();
    this.cargarDisponibilidad();
    this.cargarTickets();
  }

  // ---------- Usuarios y roles ----------
  cargarUsuarios(): void {
    this.usuarioService.listarMedicos().subscribe(datos => this.usuarios = datos);
  }

  registrarUsuario(): void {
    this.mensajeMedico = '';
    this.usuarioService.crearMedico(this.nuevoMedico).subscribe({
      next: () => {
        this.exitoMedico = true;
        this.mensajeMedico = 'Usuario registrado correctamente.';
        this.nuevoMedico = { dni: '', nombres: '', apellidos: '', email: '', password: '', colegiatura: '', rolId: 2 };
        this.cargarUsuarios();
      },
      error: (err) => {
        this.exitoMedico = false;
        this.mensajeMedico = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  // ---------- Turnos del personal (RF-42) ----------
  cargarTurnos(): void {
    this.administracionService.listarTurnos().subscribe(datos => this.turnos = datos);
  }

  programarTurno(): void {
    this.mensajeTurno = '';
    if (!this.nuevoTurno.usuarioId) return;
    this.administracionService.programarTurno(this.nuevoTurno as any).subscribe({
      next: () => {
        this.exitoTurno = true;
        this.mensajeTurno = 'Turno programado correctamente.';
        this.nuevoTurno = { usuarioId: null, fechaInicio: '', fechaFin: '', tipoTurno: 'MAÑANA', observaciones: '' };
        this.cargarTurnos();
      },
      error: (err) => {
        this.exitoTurno = false;
        this.mensajeTurno = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  eliminarTurno(id: number): void {
    this.administracionService.eliminarTurno(id).subscribe(() => this.cargarTurnos());
  }

  // ---------- Sistema y mantenimiento (RF-34/RF-39/RF-47/RF-49) ----------
  cargarMantenimientos(): void {
    this.administracionService.listarMantenimientos().subscribe(datos => this.mantenimientos = datos);
  }

  registrarMantenimiento(): void {
    this.mensajeMantenimiento = '';
    this.administracionService.registrarMantenimiento(this.nuevoMantenimiento).subscribe({
      next: () => {
        this.exitoMantenimiento = true;
        this.mensajeMantenimiento = 'Mantenimiento registrado correctamente.';
        this.nuevoMantenimiento = { nombreEquipo: '', tipoEquipo: 'CAMARA_REFRIGERACION', tipoMantenimiento: 'PREVENTIVO', fechaRealizado: '', fechaProximoVencimiento: null, observaciones: '' };
        this.cargarMantenimientos();
      },
      error: (err) => {
        this.exitoMantenimiento = false;
        this.mensajeMantenimiento = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  cargarProtocolos(): void {
    this.administracionService.listarProtocolos().subscribe(datos => this.protocolos = datos);
  }

  publicarProtocolo(): void {
    this.mensajeProtocolo = '';
    this.administracionService.publicarProtocolo(this.nuevoProtocolo).subscribe({
      next: () => {
        this.exitoProtocolo = true;
        this.mensajeProtocolo = 'Protocolo publicado correctamente.';
        this.nuevoProtocolo = { nombre: '', version: '', contenidoUrl: '', notasCambio: '' };
        this.cargarProtocolos();
      },
      error: (err) => {
        this.exitoProtocolo = false;
        this.mensajeProtocolo = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  cargarRespaldos(): void {
    this.administracionService.listarRespaldos().subscribe(datos => this.respaldos = datos);
  }

  ejecutarRespaldo(): void {
    this.mensajeRespaldo = '';
    this.administracionService.ejecutarRespaldo().subscribe({
      next: () => {
        this.mensajeRespaldo = 'Respaldo ejecutado correctamente.';
        this.cargarRespaldos();
      },
      error: (err) => this.mensajeRespaldo = err.error?.mensaje ?? 'No se pudo ejecutar el respaldo.'
    });
  }

  verificarIntegridad(id: number): void {
    this.administracionService.verificarIntegridad(id).subscribe(v => {
      this.verificaciones[id] = v.mensaje;
    });
  }

  cargarDisponibilidad(): void {
    this.administracionService.obtenerDisponibilidad().subscribe(datos => this.disponibilidad = datos);
  }

  formatoDuracion(segundos: number): string {
    const horas = Math.floor(segundos / 3600);
    const minutos = Math.floor((segundos % 3600) / 60);
    return `${horas}h ${minutos}m`;
  }

  // ---------- Mesa de ayuda (RF-33) ----------
  cargarTickets(): void {
    this.soporteService.listarTodos().subscribe(datos => {
      this.ticketsTodos = datos;
      datos.forEach(t => this.respuestasTicket[t.id] = { estado: t.estado, respuesta: t.respuesta ?? '' });
    });
  }

  responderTicket(id: number): void {
    const datos = this.respuestasTicket[id];
    this.soporteService.responder(id, datos).subscribe(() => this.cargarTickets());
  }

  colorEstadoTicket(estado: string): string {
    switch (estado) {
      case 'ABIERTO': return 'secondary';
      case 'EN_PROCESO': return 'warning';
      case 'RESUELTO': return 'success';
      case 'CERRADO': return 'dark';
      default: return 'secondary';
    }
  }
}
