import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventarioService } from '../../../core/services/inventario.service';
import { IntercambioService } from '../../../core/services/intercambio.service';
import { RedBusqueda } from '../../../core/models/inventario.model';
import {
  ESTADOS_INTERCAMBIO,
  Hospital,
  Intercambio,
  SIGUIENTES_ESTADOS
} from '../../../core/models/intercambio.model';

@Component({
  selector: 'app-red',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './red.component.html'
})
export class RedComponent implements OnInit {
  // Búsqueda puntual por código ISBT
  codigoBusqueda = '';
  resultadoBusqueda: RedBusqueda | null = null;
  errorBusqueda = '';
  buscando = false;

  // Solicitud de intercambio
  hospitales: Hospital[] = [];
  nuevoIntercambio = { ipressSolicitanteId: null as number | null, ipressProveedoraId: null as number | null, codigoProductoIsbt: '', responsableTransporte: '' };
  mensajeIntercambio = '';
  intercambioExitoso = false;

  // Tablero
  intercambios: Intercambio[] = [];
  estados = ESTADOS_INTERCAMBIO;
  siguientesEstados = SIGUIENTES_ESTADOS;

  constructor(private inventarioService: InventarioService, private intercambioService: IntercambioService) {}

  ngOnInit(): void {
    this.intercambioService.listarHospitales().subscribe(datos => this.hospitales = datos);
    this.cargarIntercambios();
  }

  buscar(): void {
    this.errorBusqueda = '';
    this.resultadoBusqueda = null;
    this.buscando = true;

    this.inventarioService.buscarEnRed(this.codigoBusqueda.trim()).subscribe({
      next: (datos) => {
        this.resultadoBusqueda = datos;
        this.buscando = false;
      },
      error: (err) => {
        this.errorBusqueda = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
        this.buscando = false;
      }
    });
  }

  solicitarIntercambio(): void {
    this.mensajeIntercambio = '';

    const { ipressSolicitanteId, ipressProveedoraId, codigoProductoIsbt, responsableTransporte } = this.nuevoIntercambio;
    if (!ipressSolicitanteId || !ipressProveedoraId || !codigoProductoIsbt.trim()) return;

    this.intercambioService.crearIntercambio({
      ipressSolicitanteId,
      ipressProveedoraId,
      codigoProductoIsbt: codigoProductoIsbt.trim(),
      responsableTransporte
    }).subscribe({
      next: () => {
        this.intercambioExitoso = true;
        this.mensajeIntercambio = 'Solicitud de intercambio registrada.';
        this.nuevoIntercambio = { ipressSolicitanteId: null, ipressProveedoraId: null, codigoProductoIsbt: '', responsableTransporte: '' };
        this.cargarIntercambios();
      },
      error: (err) => {
        this.intercambioExitoso = false;
        this.mensajeIntercambio = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  cargarIntercambios(): void {
    this.intercambioService.listarIntercambios().subscribe(datos => this.intercambios = datos);
  }

  itemsPorEstado(clave: string): Intercambio[] {
    return this.intercambios.filter(i => i.estado === clave);
  }

  avanzarEstado(id: number, estado: string): void {
    this.intercambioService.actualizarEstado(id, estado).subscribe(() => this.cargarIntercambios());
  }
}
