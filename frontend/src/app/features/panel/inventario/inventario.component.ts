import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventarioService } from '../../../core/services/inventario.service';
import { ClinicoService } from '../../../core/services/clinico.service';
import {
  CamaraAlmacenamiento, CertificadoCalidad, InventarioItem, IsbtEtiqueta, RegistroTemperatura, ResumenExistencias
} from '../../../core/models/inventario.model';
import { Donacion, Hemocomponente } from '../../../core/models/clinico.model';

const BARRAS_KPI = ['var(--green-700)', 'var(--blue-700)', 'var(--purple-700)', 'var(--amber-700)'];

const TIPOS_HEMOCOMPONENTE = [
  { valor: 'CONCENTRADO_HEMATIES', etiqueta: 'Concentrado de Hematíes' },
  { valor: 'PLASMA_FRESCO_CONGELADO', etiqueta: 'Plasma Fresco Congelado' },
  { valor: 'CRIOPRECIPITADO', etiqueta: 'Crioprecipitado' },
  { valor: 'CONCENTRADO_PLAQUETAS', etiqueta: 'Concentrado de Plaquetas' }
];

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventario.component.html'
})
export class InventarioComponent implements OnInit {
  resumen: ResumenExistencias[] = [];
  inventario: InventarioItem[] = [];
  filtroEstado = '';
  filtroBusqueda = '';

  // RF-05: etiquetado ISBT 128
  codigoEtiqueta = '';
  etiqueta: IsbtEtiqueta | null = null;
  errorEtiqueta = '';

  // RF-09/RF-24: cámaras y temperatura
  camaras: CamaraAlmacenamiento[] = [];
  ultimaTemperaturaPorCamara: Record<number, RegistroTemperatura> = {};
  temperaturaIngresada: Record<number, number | null> = {};
  mensajeTemperatura = '';

  nuevaCamara = { nombre: '', tipo: 'REFRIGERACION', ubicacion: '', temperaturaMinima: 2, temperaturaMaxima: 6 };
  mensajeCamara = '';
  exitoCamara = false;

  // RF-06: fraccionamiento
  tiposHemocomponente = TIPOS_HEMOCOMPONENTE;
  donaciones: Donacion[] = [];
  donacionSeleccionada: number | null = null;
  tiposSeleccionados: Record<string, boolean> = {};
  hemocomponentesFraccion: Hemocomponente[] = [];
  mensajeFraccion = '';
  exitoFraccion = false;
  certificadosPorHemocomponente: Record<number, CertificadoCalidad> = {};

  constructor(private inventarioService: InventarioService, private clinicoService: ClinicoService) {}

  ngOnInit(): void {
    this.cargarInventario();
    this.cargarCamaras();
    this.clinicoService.listarDonaciones().subscribe(datos => this.donaciones = datos);
  }

  cargarInventario(): void {
    this.inventarioService.resumenExistencias().subscribe(datos => this.resumen = datos);
    this.inventarioService.listarInventario().subscribe(datos => this.inventario = datos);
  }

  signoRh(factorRh: string): string {
    return factorRh === 'POSITIVO' ? '+' : '-';
  }

  colorBarra(indice: number): string {
    return BARRAS_KPI[indice % BARRAS_KPI.length];
  }

  inventarioFiltrado(): InventarioItem[] {
    const busqueda = this.filtroBusqueda.trim().toLowerCase();
    return this.inventario.filter(item => {
      const coincideEstado = !this.filtroEstado || item.estado === this.filtroEstado;
      const coincideBusqueda = !busqueda
        || item.codigoProductoIsbt.toLowerCase().includes(busqueda)
        || item.grupoAbo.toLowerCase().includes(busqueda);
      return coincideEstado && coincideBusqueda;
    });
  }

  // ---------- RF-05: Etiquetado ISBT 128 ----------
  consultarEtiqueta(): void {
    this.errorEtiqueta = '';
    this.etiqueta = null;
    if (!this.codigoEtiqueta.trim()) return;

    this.inventarioService.leerEtiqueta(this.codigoEtiqueta.trim()).subscribe({
      next: (datos) => this.etiqueta = datos,
      error: (err) => this.errorEtiqueta = err.error?.mensaje ?? 'No se encontró una unidad con ese código ISBT 128.'
    });
  }

  imprimirEtiqueta(): void {
    window.print();
  }

  // ---------- RF-09/RF-24: Cámaras y temperatura ----------
  cargarCamaras(): void {
    this.inventarioService.listarCamaras().subscribe(datos => {
      this.camaras = datos;
      datos.forEach(camara => this.cargarUltimaTemperatura(camara.id));
    });
  }

  cargarUltimaTemperatura(camaraId: number): void {
    this.inventarioService.listarTemperaturas(camaraId).subscribe(registros => {
      if (registros.length > 0) {
        this.ultimaTemperaturaPorCamara[camaraId] = registros[registros.length - 1];
      }
    });
  }

  registrarCamara(): void {
    this.mensajeCamara = '';
    this.inventarioService.crearCamara(this.nuevaCamara).subscribe({
      next: () => {
        this.exitoCamara = true;
        this.mensajeCamara = 'Cámara registrada correctamente.';
        this.nuevaCamara = { nombre: '', tipo: 'REFRIGERACION', ubicacion: '', temperaturaMinima: 2, temperaturaMaxima: 6 };
        this.cargarCamaras();
      },
      error: (err) => {
        this.exitoCamara = false;
        this.mensajeCamara = err.error?.mensaje ?? 'No se pudo registrar la cámara.';
      }
    });
  }

  registrarTemperatura(camaraId: number): void {
    this.mensajeTemperatura = '';
    const valor = this.temperaturaIngresada[camaraId];
    if (valor === null || valor === undefined) return;

    this.inventarioService.registrarTemperatura(camaraId, valor).subscribe({
      next: (registro) => {
        this.ultimaTemperaturaPorCamara[camaraId] = registro;
        this.temperaturaIngresada[camaraId] = null;
        this.mensajeTemperatura = registro.dentroDeRango
          ? 'Temperatura registrada dentro del rango seguro.'
          : '⚠ Temperatura fuera de rango — se generó una alerta.';
      },
      error: (err) => this.mensajeTemperatura = err.error?.mensaje ?? 'No se pudo registrar la temperatura.'
    });
  }

  // ---------- RF-06: Fraccionamiento ----------
  fraccionar(): void {
    this.mensajeFraccion = '';
    const tipos = Object.keys(this.tiposSeleccionados).filter(tipo => this.tiposSeleccionados[tipo]);
    if (!this.donacionSeleccionada || tipos.length === 0) return;

    this.clinicoService.fraccionarDonacion(this.donacionSeleccionada, { tiposHemocomponente: tipos }).subscribe({
      next: (datos) => {
        this.exitoFraccion = true;
        this.hemocomponentesFraccion = datos;
        this.mensajeFraccion = `Se generaron ${datos.length} hemocomponente(s) a partir de la donación.`;
        this.tiposSeleccionados = {};
        this.cargarInventario();
      },
      error: (err) => {
        this.exitoFraccion = false;
        this.mensajeFraccion = err.error?.mensaje ?? 'No se pudo fraccionar la donación.';
      }
    });
  }

  // ---------- RF-41: Certificado de calidad (QR de verificación pública) ----------
  generarCertificado(hemocomponenteId: number): void {
    this.inventarioService.obtenerCertificado(hemocomponenteId).subscribe(certificado => {
      this.certificadosPorHemocomponente[hemocomponenteId] = certificado;
    });
  }

  urlVerificacion(certificado: CertificadoCalidad): string {
    return `${window.location.origin}/verificar/${certificado.codigoVerificacion}`;
  }
}
