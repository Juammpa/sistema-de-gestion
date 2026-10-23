import {
  afterNextRender,
  Component,
  computed,
  ElementRef,
  inject,
  Injector,
  input,
  linkedSignal,
  model,
  signal,
  viewChild,
} from '@angular/core';
import {
  addDays,
  addMonths,
  dayKey,
  daysInMonth,
  DayRange,
  EMPTY_RANGE,
  isSameDay,
  isSameMonth,
  monthCells,
  parseDayKey,
  startOfDay,
  startOfMonth,
} from './calendar-dates';

interface DayCell {
  date: Date;
  key: string;
  label: string;
  day: number;
  disabled: boolean;
  today: boolean;
  picked: boolean;
  inBand: boolean;
  tabbable: boolean;
  cellClass: string;
  buttonClass: string;
}

const DAY_BASE =
  'group relative grid size-full cursor-pointer place-items-center rounded-lg text-sm tabular-nums outline-none transition-ui focus-visible:ring-2 focus-visible:ring-accent-400/70 motion-safe:active:scale-[0.95]';

@Component({
  selector: 'calendar',
  imports: [],
  templateUrl: './calendar.html',
  host: { class: 'block w-full max-w-xs select-none p-3' },
})
export class Calendar {
  private readonly injector = inject(Injector);
  private readonly grid = viewChild.required<ElementRef<HTMLElement>>('grid');

  // Inputs
  mode = input<'single' | 'range'>('single');
  defaultMonth = input<Date>();
  minDate = input<Date>();
  maxDate = input<Date>();
  isDateDisabled = input<(date: Date) => boolean>();
  weekStartsOn = input<0 | 1>(1);
  locale = input('es-AR');
  // Rango que solo se dibuja (extremos y banda); no se puede editar desde acá. Sirve para que un
  // campo "Desde" o "Hasta" muestre el rango completo del filtro. Pisa a la selección al dibujar.
  highlight = input<DayRange | null>(null);

  // Valor en modo "single" ([(value)]) y en modo "range" ([(range)])
  value = model<Date | null>(null);
  range = model<DayRange>(EMPTY_RANGE);

  private readonly today = startOfDay(new Date());

  protected readonly selection = computed<DayRange>(() =>
    this.mode() === 'range' ? this.range() : { from: this.value(), to: null },
  );

  // Mes visible: arranca en la selección (o en `defaultMonth`/hoy) y sigue los cambios externos
  protected readonly view = linkedSignal(() =>
    startOfMonth(this.selection().from ?? this.defaultMonth() ?? this.today),
  );
  // Día que recibe el foco con Tab (roving tabindex)
  private readonly focusDate = linkedSignal(() => this.selection().from ?? this.today);
  protected readonly hover = signal<Date | null>(null);
  // Sentido del deslizamiento al cambiar de mes: 1 = hacia adelante, -1 = hacia atrás
  protected readonly dir = signal(1);

  private readonly min = computed(() => {
    const min = this.minDate();
    return min ? startOfDay(min) : null;
  });
  private readonly max = computed(() => {
    const max = this.maxDate();
    return max ? startOfDay(max) : null;
  });

  // Lista de un solo elemento cuya clave cambia con el mes: al cambiar, `@for` destruye y crea el
  // bloque de nuevo, y eso dispara la animación de entrada (trackear por propiedad evita NG0956)
  protected readonly slots = computed(() => [
    { key: this.view().getFullYear() * 12 + this.view().getMonth() },
  ]);

  protected readonly canPrev = computed(() => {
    const min = this.min();
    const view = this.view();
    return !min || new Date(view.getFullYear(), view.getMonth(), 0) >= min;
  });
  protected readonly canNext = computed(() => {
    const max = this.max();
    const view = this.view();
    return !max || new Date(view.getFullYear(), view.getMonth() + 1, 1) <= max;
  });

  private readonly monthFormat = computed(
    () => new Intl.DateTimeFormat(this.locale(), { month: 'long', year: 'numeric' }),
  );
  private readonly dayFormat = computed(
    () => new Intl.DateTimeFormat(this.locale(), { dateStyle: 'full' }),
  );
  protected readonly monthLabel = computed(() => this.monthFormat().format(this.view()));

  protected readonly weekdays = computed(() => {
    const format = new Intl.DateTimeFormat(this.locale(), { weekday: 'narrow' });
    // El 1/8/2021 fue domingo
    return Array.from({ length: 7 }, (_, i) =>
      format.format(new Date(2021, 7, 1 + ((this.weekStartsOn() + i) % 7))),
    );
  });

  protected readonly weeks = computed<(DayCell | null)[][]>(() => {
    const view = this.view();
    const highlight = this.highlight();
    const { from, to } = highlight ?? this.selection();
    const isRange = this.mode() === 'range' || !!highlight;
    // La banda sigue al mouse solo cuando se está eligiendo un rango de verdad
    const hover = this.mode() === 'range' ? this.hover() : null;
    // Mientras se elige el fin del rango, la banda sigue al mouse
    const end = isRange && from ? (to ?? (hover && hover > from ? hover : null)) : null;
    const tabDate = isSameMonth(this.focusDate(), view) ? this.focusDate() : startOfMonth(view);
    const total = daysInMonth(view);
    const cells = monthCells(view, this.weekStartsOn());

    return Array.from({ length: 6 }, (_, row) =>
      cells.slice(row * 7, row * 7 + 7).map((date, col) => {
        if (!date) return null;
        const isStart = isSameDay(date, from);
        const isEnd = !isStart && isRange && isSameDay(date, to);
        const picked = isStart || isEnd;
        const inBand =
          isRange && !!from && !!end && !isSameDay(from, end) && date >= from && date <= end;
        const disabled = this.disabled(date);

        const roundLeft = inBand && (isStart || col === 0 || date.getDate() === 1);
        const roundRight =
          inBand && (isSameDay(date, end) || col === 6 || date.getDate() === total);

        return {
          date,
          key: dayKey(date),
          label: this.dayFormat().format(date),
          day: date.getDate(),
          disabled,
          today: isSameDay(date, this.today),
          picked,
          inBand,
          tabbable: isSameDay(date, tabDate),
          cellClass: [
            'relative aspect-square',
            inBand ? 'bg-accent-500/15' : '',
            roundLeft ? 'rounded-l-lg' : '',
            roundRight ? 'rounded-r-lg' : '',
          ].join(' '),
          buttonClass: [
            DAY_BASE,
            picked ? 'bg-accent-600 text-white hover:bg-accent-500' : 'text-slate-100',
            !picked && !disabled ? (inBand ? 'hover:bg-accent-500/25' : 'hover:bg-raised') : '',
            disabled ? '!cursor-not-allowed opacity-30' : '',
          ].join(' '),
        };
      }),
    );
  });

  protected go(step: 1 | -1) {
    this.goTo(addMonths(this.view(), step), step);
  }

  protected pick(date: Date) {
    if (this.disabled(date)) return;
    this.focusDate.set(date);
    if (this.mode() === 'single') {
      this.value.set(date);
      return;
    }
    const { from, to } = this.range();
    // Primer clic, tercer clic o fecha anterior al inicio: empieza un rango nuevo
    if (!from || to || date < from) this.range.set({ from: date, to: null });
    else this.range.set({ from, to: date });
  }

  protected onKeyDown(event: KeyboardEvent) {
    const el = (event.target as HTMLElement).closest<HTMLElement>('[data-date]');
    if (!el?.dataset['date']) return;
    const current = parseDayKey(el.dataset['date']);
    const col = (current.getDay() - this.weekStartsOn() + 7) % 7;

    let next: Date | null = null;
    switch (event.key) {
      case 'ArrowLeft':
        next = addDays(current, -1);
        break;
      case 'ArrowRight':
        next = addDays(current, 1);
        break;
      case 'ArrowUp':
        next = addDays(current, -7);
        break;
      case 'ArrowDown':
        next = addDays(current, 7);
        break;
      case 'Home':
        next = addDays(current, -col);
        break;
      case 'End':
        next = addDays(current, 6 - col);
        break;
      case 'PageUp':
        next = addMonths(current, event.shiftKey ? -12 : -1);
        break;
      case 'PageDown':
        next = addMonths(current, event.shiftKey ? 12 : 1);
        break;
    }
    if (!next) return;

    event.preventDefault();
    const min = this.min();
    const max = this.max();
    if (min && next < min) next = min;
    if (max && next > max) next = max;

    this.focusDate.set(next);
    if (!isSameMonth(next, this.view())) this.goTo(next);
    this.focusDay(next);
  }

  private goTo(target: Date, direction?: number) {
    this.dir.set(direction ?? (target > this.view() ? 1 : -1));
    this.view.set(startOfMonth(target));
  }

  private disabled(date: Date) {
    const min = this.min();
    const max = this.max();
    return (!!min && date < min) || (!!max && date > max) || !!this.isDateDisabled()?.(date);
  }

  // Espera a que Angular pinte el mes nuevo antes de mover el foco al día
  private focusDay(date: Date) {
    afterNextRender(
      () =>
        this.grid()
          .nativeElement.querySelector<HTMLElement>(`[data-date="${dayKey(date)}"]`)
          ?.focus(),
      { injector: this.injector },
    );
  }
}
