import { Component, inject } from '@angular/core';
import { NotificationService, ToastKind } from '../notification.service';

@Component({
  selector: 'toast-container',
  templateUrl: './toast-container.html',
})
export class ToastContainer {
  protected notifications = inject(NotificationService);

  // Clases completas y estaticas: Tailwind no detecta clases armadas por partes
  protected styles: Record<ToastKind, { box: string; icon: string }> = {
    error: {
      box: 'border-red-500/30 bg-red-950/85 text-red-100',
      icon: 'fa-solid fa-circle-exclamation text-red-300',
    },
    success: {
      box: 'border-emerald-500/30 bg-emerald-950/85 text-emerald-100',
      icon: 'fa-solid fa-circle-check text-emerald-300',
    },
    info: {
      box: 'border-blue-500/30 bg-slate-900/90 text-slate-100',
      icon: 'fa-solid fa-circle-info text-blue-300',
    },
  };
}
