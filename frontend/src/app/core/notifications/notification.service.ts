import { Injectable, signal } from '@angular/core';

export type ToastKind = 'error' | 'success' | 'info';

export interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
}

const MAX_TOASTS = 4;

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 0;
  private timers = new Map<number, ReturnType<typeof setTimeout>>();
  private readonly _toasts = signal<Toast[]>([]);

  readonly toasts = this._toasts.asReadonly();

  error(message: string) {
    this.push('error', message, 7000);
  }

  success(message: string) {
    this.push('success', message, 4000);
  }

  dismiss(id: number) {
    clearTimeout(this.timers.get(id));
    this.timers.delete(id);
    this._toasts.update((list) => list.filter((toast) => toast.id !== id));
  }

  private push(kind: ToastKind, message: string, durationMs: number) {
    // Si el mismo aviso ya esta visible no lo repetimos (ej. 3 requests que fallan juntas)
    if (this._toasts().some((toast) => toast.kind === kind && toast.message === message)) return;

    // Tope de avisos en pantalla: descartamos el mas viejo
    const current = this._toasts();
    if (current.length >= MAX_TOASTS) this.dismiss(current[0].id);

    const id = this.nextId++;
    this._toasts.update((list) => [...list, { id, kind, message }]);
    this.timers.set(
      id,
      setTimeout(() => this.dismiss(id), durationMs),
    );
  }
}
