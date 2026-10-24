import {
  afterNextRender,
  Component,
  DestroyRef,
  DOCUMENT,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { ParticlesBackground } from '../../components/particles-background/particles-background';

@Component({
  selector: 'app-home-page',
  imports: [RouterLink, ParticlesBackground],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePage {
  private readonly sentinel = viewChild.required<ElementRef<HTMLElement>>('sentinel');

  // El encabezado se vuelve opaco cuando se baja de la página
  protected readonly scrolled = signal(false);

  protected readonly steps = [
    {
      icon: 'fa-solid fa-address-book text-lg',
      title: 'Cargá tus datos',
      text: 'Dá de alta tus clientes con sus carnicerías, tus proveedores y los cortes con su precio por kilo.',
    },
    {
      icon: 'fa-solid fa-pen-to-square text-lg',
      title: 'Registrá cada movimiento',
      text: 'Anotá ventas, compras y pagos. La cuenta de cada cliente o proveedor actualiza su saldo al instante.',
    },
    {
      icon: 'fa-solid fa-chart-line text-lg',
      title: 'Controlá todo desde el panel',
      text: 'Mirá los saldos, el stock por tipo de compra y la caja del período, desde un solo lugar.',
    },
  ];

  protected readonly modules = [
    {
      icon: 'fa-solid fa-house',
      name: 'Inicio',
      text: 'Un resumen de tu negocio: ventas, compras y los últimos movimientos.',
    },
    {
      icon: 'fa-solid fa-users',
      name: 'Clientes',
      text: 'Tus clientes y sus carnicerías, con las ventas, los pagos y el saldo de cada cuenta.',
    },
    {
      icon: 'fa-solid fa-cart-shopping',
      name: 'Proveedores',
      text: 'Las compras y los pagos a cada proveedor, con lo que les debés.',
    },
    {
      icon: 'fa-solid fa-database',
      name: 'Stock',
      text: 'Las piezas que compraste, por especie y tipo de compra, con sus kilos.',
    },
    {
      icon: 'fa-solid fa-cow',
      name: 'Cortes',
      text: 'Los cortes que vendés y su precio por kilo.',
    },
    {
      icon: 'fa-solid fa-dollar-sign',
      name: 'Caja de ahorro',
      text: 'El movimiento de caja del período que elijas.',
    },
  ];

  constructor() {
    const destroyRef = inject(DestroyRef);
    const root = inject(DOCUMENT).documentElement;
    root.classList.add('scrollbar-none');
    destroyRef.onDestroy(() => root.classList.remove('scrollbar-none'));

    afterNextRender(() => {
      const observer = new IntersectionObserver(([entry]) =>
        this.scrolled.set(!entry.isIntersecting),
      );
      observer.observe(this.sentinel().nativeElement);
      destroyRef.onDestroy(() => observer.disconnect());
    });
  }
}
