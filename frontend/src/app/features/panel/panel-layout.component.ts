import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-panel-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './panel-layout.component.html'
})
export class PanelLayoutComponent {
  constructor(public authService: AuthService) {}

  cerrarSesion(): void {
    this.authService.logout();
  }
}
