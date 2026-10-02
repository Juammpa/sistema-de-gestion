import { Component } from '@angular/core';

// Marcador de posición con la forma de `list-item-card` mientras carga una lista
@Component({
  selector: 'list-skeleton',
  templateUrl: './list-skeleton.html',
  host: { class: 'block' },
})
export class ListSkeleton {
  protected rows = [0, 1, 2, 3, 4];
}
