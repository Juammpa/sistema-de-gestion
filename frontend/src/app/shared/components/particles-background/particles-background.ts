import {
  afterNextRender,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  NgZone,
  viewChild,
} from '@angular/core';

const PARTICLE_COUNT = 45;
const LINK_DISTANCE = 110;
const POINTER_RADIUS = 120;

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
}

// Fondo animado: puntos que se conectan entre sí y se apartan del puntero
@Component({
  selector: 'hero-particles',
  template: '<canvas #canvas class="block size-full"></canvas>',
  host: { class: 'block', 'aria-hidden': 'true' },
})
export class ParticlesBackground {
  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly zone = inject(NgZone);

  constructor() {
    const destroyRef = inject(DestroyRef);

    afterNextRender(() => {
      // Con "reducir movimiento" activado el fondo no se anima
      if (matchMedia('(prefers-reduced-motion: reduce)').matches) return;

      // Fuera de la zona de Angular: 60 cuadros por segundo no deben disparar la detección de cambios
      const stop = this.zone.runOutsideAngular(() => this.start());
      destroyRef.onDestroy(stop);
    });
  }

  // Arranca la animación y devuelve la función que la detiene
  private start(): () => void {
    const canvas = this.canvas().nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return () => undefined;

    // Coordenadas del puntero (el fondo ocupa toda la pantalla, así que coinciden con las de la ventana)
    const pointer = { x: -9999, y: -9999 };
    let particles: Particle[] = [];
    let width = 0;
    let height = 0;
    let frame = 0;

    const resize = () => {
      const rect = this.host.nativeElement.getBoundingClientRect();
      const ratio = window.devicePixelRatio || 1;
      width = rect.width;
      height = rect.height;
      canvas.width = width * ratio;
      canvas.height = height * ratio;
      ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
      particles = Array.from({ length: PARTICLE_COUNT }, () => ({
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.4,
        vy: (Math.random() - 0.5) * 0.4,
        radius: Math.random() * 1.6 + 0.8,
      }));
    };

    const step = () => {
      ctx.clearRect(0, 0, width, height);

      for (const p of particles) {
        p.x += p.vx;
        p.y += p.vy;
        if (p.x < 0 || p.x > width) p.vx *= -1;
        if (p.y < 0 || p.y > height) p.vy *= -1;

        // Se aparta suavemente del puntero
        const dx = pointer.x - p.x;
        const dy = pointer.y - p.y;
        const distance = Math.hypot(dx, dy);
        if (distance > 0 && distance < POINTER_RADIUS) {
          p.x -= (dx / distance) * 0.6;
          p.y -= (dy / distance) * 0.6;
        }

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(56, 189, 248, 0.45)';
        ctx.fill();
      }

      // Líneas entre los puntos cercanos
      for (let i = 0; i < particles.length; i++) {
        for (let j = i + 1; j < particles.length; j++) {
          const a = particles[i];
          const b = particles[j];
          const distance = Math.hypot(a.x - b.x, a.y - b.y);
          if (distance >= LINK_DISTANCE) continue;

          ctx.beginPath();
          ctx.strokeStyle = `rgba(37, 99, 235, ${0.18 * (1 - distance / LINK_DISTANCE)})`;
          ctx.lineWidth = 0.8;
          ctx.moveTo(a.x, a.y);
          ctx.lineTo(b.x, b.y);
          ctx.stroke();
        }
      }

      frame = requestAnimationFrame(step);
    };

    const onPointerMove = (event: PointerEvent) => {
      pointer.x = event.clientX;
      pointer.y = event.clientY;
    };
    const onPointerLeave = () => {
      pointer.x = -9999;
      pointer.y = -9999;
    };

    const observer = new ResizeObserver(resize);
    observer.observe(this.host.nativeElement);
    window.addEventListener('pointermove', onPointerMove, { passive: true });
    document.documentElement.addEventListener('pointerleave', onPointerLeave);
    frame = requestAnimationFrame(step);

    return () => {
      cancelAnimationFrame(frame);
      observer.disconnect();
      window.removeEventListener('pointermove', onPointerMove);
      document.documentElement.removeEventListener('pointerleave', onPointerLeave);
    };
  }
}
