import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../../core/services/usuario.service';
import { UsuarioAdmin } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-medicos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './medicos.component.html'
})
export class MedicosComponent implements OnInit {
  medicos: UsuarioAdmin[] = [];

  nuevoMedico = { dni: '', nombres: '', apellidos: '', email: '', password: '', colegiatura: '', rolId: 3 };
  mensaje = '';
  exito = false;

  constructor(private usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.cargarMedicos();
  }

  cargarMedicos(): void {
    this.usuarioService.listarMedicos().subscribe(datos => this.medicos = datos);
  }

  registrar(): void {
    this.mensaje = '';

    this.usuarioService.crearMedico(this.nuevoMedico).subscribe({
      next: () => {
        this.exito = true;
        this.mensaje = 'Médico registrado correctamente.';
        this.nuevoMedico = { dni: '', nombres: '', apellidos: '', email: '', password: '', colegiatura: '', rolId: 3 };
        this.cargarMedicos();
      },
      error: (err) => {
        this.exito = false;
        this.mensaje = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }
}
