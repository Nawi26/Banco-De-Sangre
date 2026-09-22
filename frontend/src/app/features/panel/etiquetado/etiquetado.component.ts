import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InventarioService } from '../../../core/services/inventario.service';
import { IsbtEtiqueta } from '../../../core/models/inventario.model';

// RF-05: generación/consulta de la etiqueta ISBT 128 de una unidad.
@Component({
  selector: 'app-etiquetado',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './etiquetado.component.html'
})
export class EtiquetadoComponent {
  codigoEtiqueta = '';
  etiqueta: IsbtEtiqueta | null = null;
  errorEtiqueta = '';

  constructor(private inventarioService: InventarioService) {}

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
}
