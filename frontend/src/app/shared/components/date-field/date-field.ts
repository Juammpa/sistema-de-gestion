import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  forwardRef,
  inject,
  Injector,
  input,
  model,
  signal,
  viewChild,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { formatIsoDate, parseIsoDate, toIsoDate } from '../../utils/date.utils';
import { Calendar } from '../calendar/calendar';
import { DayRange } from '../calendar/calendar-dates';

// Separación entre el botón y el popover, y distancia mínima a los bordes de la pantalla
const POPOVER_GAP = 8;
const VIEWPORT_MARGIN = 8;

const TRIGGER_BASE =
  'flex w-full cursor-pointer items-center gap-2.5 border border-slate-700/70 bg-canvas text-left text-base text-slate-100 outline-none transition-ui hover:border-slate-600 focus:border-accent-500 focus:ring-4 focus:ring-accent-500/15 disabled:cursor-not-allowed disabled:opacity-60 motion-reduce:transition-none sm:text-sm';

@Component({
  selector: 'date-field',
  imports: [Calendar],
  templateUrl: './date-field.html',
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DateField), multi: true },
  ],
  host: {
    class: 'relative block',
    '(document:click)': 'onDocumentClick($event)',
    '(keydown.escape)': 'onEscape($event)',
    '(focusout)': 'onFocusOut($event)',
  },
})
export class DateField implements ControlValueAccessor {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);
  private readonly trigger = viewChild.required<ElementRef<HTMLButtonElement>>('trigger');
  private readonly popover = viewChild<ElementRef<HTMLElement>>('popover');

  constructor() {
    inject(DestroyRef).onDestroy(() => this.release());
  }

  // Inputs
  inputId = input<string>(); // id del botón, para que un <label for> lo apunte
  min = input<string>(); // yyyy-MM-dd
  max = input<string>(); // yyyy-MM-dd
  placeholder = input('Seleccionar fecha');
  invalid = input<boolean | null>(false); // acepta lo que devuelve FormUtils.isValidField
  clearable = input(false); // muestra un botón para dejar la fecha vacía
  // Rango del filtro al que pertenece este campo (yyyy-MM-dd). El calendario lo dibuja como
  // rastro; cada campo sigue editando solo su propia fecha
  rangeFrom = input<string>();
  rangeTo = input<string>();
  // 'form': campo de formulario (modales). 'compact': campo de filtros, más bajo en escritorio
  size = input<'form' | 'compact'>('form');

  // Fecha elegida en formato yyyy-MM-dd ('' = vacía). Se usa con [(value)] o con formControlName
  value = model('');

  protected readonly disabled = signal(false);
  protected readonly open = signal(false);

  protected readonly selectedDate = computed(() => parseIsoDate(this.value()));
  protected readonly minDate = computed(() => parseIsoDate(this.min()) ?? undefined);
  protected readonly maxDate = computed(() => parseIsoDate(this.max()) ?? undefined);
  protected readonly highlight = computed<DayRange | null>(() => {
    const from = parseIsoDate(this.rangeFrom());
    const to = parseIsoDate(this.rangeTo());
    return from || to ? { from, to } : null;
  });
  // Si este campo está vacío, el calendario abre cerca del otro extremo del rango
  protected readonly defaultMonth = computed(
    () => this.highlight()?.from ?? this.highlight()?.to ?? undefined,
  );
  protected readonly label = computed(() => (this.value() ? formatIsoDate(this.value()) : ''));

  protected readonly triggerClass = computed(() =>
    [
      TRIGGER_BASE,
      this.size() === 'compact' ? 'h-11 rounded-lg px-3 sm:h-9' : 'rounded-xl px-3.5 py-3',
      this.clearable() && this.value() ? 'pr-8' : '',
    ].join(' '),
  );

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  writeValue(value: string | null) {
    this.value.set(value ?? '');
  }
  registerOnChange(fn: (value: string) => void) {
    this.onChange = fn;
  }
  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }
  setDisabledState(isDisabled: boolean) {
    this.disabled.set(isDisabled);
    if (isDisabled) {
      this.open.set(false);
      this.release();
    }
  }

  protected toggle() {
    if (this.disabled()) return;
    if (this.open()) this.close(true);
    else this.openPopover();
  }

  protected touch() {
    if (!this.open()) this.onTouched();
  }

  protected pick(date: Date | null) {
    if (!date) return;
    this.commit(toIsoDate(date));
    this.close(true);
  }

  protected clear() {
    this.commit('');
    this.trigger().nativeElement.focus();
  }

  protected onDocumentClick(event: MouseEvent) {
    // composedPath() se calcula al empezar el evento: sigue sirviendo aunque el día clickeado
    // ya haya sido sacado del DOM por el cierre del popover
    if (this.open() && !event.composedPath().includes(this.host.nativeElement)) this.close(false);
  }

  // Escape cierra solo el calendario: se frena acá para que no cierre también el modal o el panel
  protected onEscape(event: Event) {
    if (!this.open()) return;
    event.stopPropagation();
    this.close(true);
  }

  // Si el foco sale del campo (Tab hacia afuera), se cierra el calendario
  protected onFocusOut(event: FocusEvent) {
    const next = event.relatedTarget as Node | null;
    if (this.open() && next && !this.host.nativeElement.contains(next)) this.close(false);
  }

  private commit(iso: string) {
    this.value.set(iso);
    this.onChange(iso);
  }

  private openPopover() {
    this.open.set(true);
    afterNextRender(
      () => {
        const popover = this.popover()?.nativeElement;
        if (!popover) return;
        popover.showPopover();
        this.place();
        // Si algún padre scrollea o cambia el tamaño, el campo se mueve y el popover lo sigue
        window.addEventListener('scroll', this.place, true);
        window.addEventListener('resize', this.place);
        // El foco entra al calendario, en el día que se alcanza con Tab (la selección o hoy)
        popover.querySelector<HTMLElement>('[data-date][tabindex="0"]')?.focus();
      },
      { injector: this.injector },
    );
  }

  // Posiciona el popover (fixed, en la capa superior) cerca del botón y siempre dentro de la
  // pantalla: debajo si entra; si no, arriba si entra; si no, pegado al borde (tapa el campo,
  // pero se ve completo). Si la pantalla es más baja que el calendario, se limita el alto y
  // el calendario scrollea.
  private readonly place = () => {
    const popover = this.popover()?.nativeElement;
    if (!popover) return;

    const { innerWidth, innerHeight } = window;
    popover.style.maxHeight = `${innerHeight - 2 * VIEWPORT_MARGIN}px`;

    const anchor = this.trigger().nativeElement.getBoundingClientRect();
    const { width, height } = popover.getBoundingClientRect();

    let left = anchor.left;
    if (left + width > innerWidth - VIEWPORT_MARGIN) left = anchor.right - width;
    left = Math.min(Math.max(left, VIEWPORT_MARGIN), innerWidth - width - VIEWPORT_MARGIN);

    const below = anchor.bottom + POPOVER_GAP;
    const above = anchor.top - POPOVER_GAP - height;
    let top: number;
    if (below + height <= innerHeight - VIEWPORT_MARGIN) top = below;
    else if (above >= VIEWPORT_MARGIN) top = above;
    else top = innerHeight - height - VIEWPORT_MARGIN;

    popover.style.left = `${left}px`;
    popover.style.top = `${top}px`;
  };

  private release() {
    window.removeEventListener('scroll', this.place, true);
    window.removeEventListener('resize', this.place);
  }

  private close(refocus: boolean) {
    this.open.set(false);
    this.release();
    this.onTouched();
    if (refocus) this.trigger().nativeElement.focus();
  }
}
