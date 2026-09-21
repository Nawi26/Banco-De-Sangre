import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdministracionService } from '../../../core/services/administracion.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import {
  Disponibilidad, MantenimientoEquipo, ProtocoloClinico, RespaldoBaseDatos, TurnoPersonal
} from '../../../core/models/administracion.model';
import { UsuarioAdmin } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-administracion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './administracion.component.html'
})
export class AdministracionComponent implements OnInit {
  pestanaActiva: 'turnos' | 'mantenimientos' | 'protocolos' | 'respaldos' | 'disponibilidad' = 'turnos';

  usuarios: UsuarioAdmin[] = [];
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

  constructor(private administracionService: AdministracionService, private usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.usuarioService.listarMedicos().subscribe(datos => this.usuarios = datos);
    this.cargarTurnos();
    this.cargarMantenimientos();
    this.cargarProtocolos();
    this.cargarRespaldos();
    this.cargarDisponibilidad();
  }

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
}
