import { Component, computed, inject, input } from '@angular/core';
import { ThemeService } from '../../../core/theme/theme.service';

@Component({
  selector: 'app-logo',
  templateUrl: './logo.html',
  host: { class: 'inline-block shrink-0' },
})
export class Logo {
  private theme = inject(ThemeService);

  // Lado en px
  size = input(40);

  // Vacio si es decorativo (el nombre de la app ya esta escrito al lado)
  alt = input('');

  protected readonly src = computed(() =>
    this.theme.resolved() === 'light' ? 'logo.png' : 'logo_blanco.png',
  );
}
