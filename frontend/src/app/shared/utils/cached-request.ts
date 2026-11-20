import { Observable, shareReplay } from 'rxjs';

// Guarda en memoria el resultado de un pedido al servidor y lo reutiliza
// - Si dos pantallas lo piden a la vez, comparten una sola peticion.
// - Se descarta al llamar a invalidate() (hacerlo tras crear, editar o borrar) o pasado el tiempo de vida
// - Si el pedido falla, no se guarda el error: el siguiente pedido vuelve a intentar
export class CachedRequest<T> {
  private shared$: Observable<T> | null = null;
  private loadedAt = 0;

  constructor(
    private readonly load: () => Observable<T>,
    private readonly ttlMs = 5 * 60 * 1000,
  ) {}

  get(): Observable<T> {
    if (this.shared$ && Date.now() - this.loadedAt > this.ttlMs) this.invalidate();

    if (!this.shared$) {
      this.loadedAt = Date.now();
      this.shared$ = this.load().pipe(shareReplay({ bufferSize: 1, refCount: false }));
    }

    return this.shared$;
  }

  // Descarta el resultado guardado: el proximo get() vuelve a pedir los datos al servidor
  invalidate() {
    this.shared$ = null;
  }
}
