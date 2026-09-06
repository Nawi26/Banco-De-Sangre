import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventarioService } from '../../../core/services/inventario.service';
import { InventarioItem, ResumenExistencias } from '../../../core/models/inventario.model';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventario.component.html'
})
export class InventarioComponent implements OnInit {
  resumen: ResumenExistencias[] = [];
  inventario: InventarioItem[] = [];

  constructor(private inventarioService: InventarioService) {}

  ngOnInit(): void {
    this.inventarioService.resumenExistencias().subscribe(datos => this.resumen = datos);
    this.inventarioService.listarInventario().subscribe(datos => this.inventario = datos);
  }

  signoRh(factorRh: string): string {
    return factorRh === 'POSITIVO' ? '+' : '-';
  }
}
