import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TitleCasePipe } from '@angular/common';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { SaleService } from '../../services/sale.service';
import { CutMeatService } from '../../../../cut-meat/services/cut-meat.service';
import { StockService } from '../../../../stock/services/stock.service';
import { AvailableCut } from '../../../../stock/interfaces/stock.interface';
import { Sale, SaleRequest } from '../../interfaces/sales.interface';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';
import { DateField } from '../../../../shared/components/date-field/date-field';
import { toIsoDate } from '../../../../shared/utils/date.utils';
import { AppValidators } from '../../../../shared/validators/app-validators';
import { SaleUnitsPicker } from '../sale-units-picker/sale-units-picker';
import { groupByCut, mergeUnits, proposeOldest, unitsOfCut } from '../../utils/sale-units.utils';

@Component({
  selector: 'edit-sale-modal',
  imports: [ReactiveFormsModule, DeleteModal, DateField, TitleCasePipe, SaleUnitsPicker],
  templateUrl: './edit-sale-modal.html',
})
export class EditSaleModal implements OnInit {
  private saleService = inject(SaleService);
  private cutMeatService = inject(CutMeatService);
  private stockService = inject(StockService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  sale = input.required<Sale>();
  butcherShopId = input.required<number>(); // necesario para el SaleRequest del update

  // Outputs
  cancel = output();
  saved = output();
  deleted = output();

  // Tipos de cortes (para el precio por kilo)
  cutMeatOptions = rxResource({
    stream: () => this.cutMeatService.getAllCutMeats(),
  });

  // Unidades de stock sin vender, de la más antigua a la más nueva
  availableUnits = rxResource({
    stream: () => this.stockService.getAvailableCuts(),
  });

  // Tope del selector de fecha: no se puede elegir un dia futuro
  today = toIsoDate(new Date());

  saleForm: FormGroup = this.fb.group({
    date: [null as string | null, [Validators.required, AppValidators.notFutureDate]],
    cutMeatId: [null as number | null, [Validators.required]],
  });

  // Unidades que la venta tiene hoy (vienen con el detalle de la venta)
  private ownUnits = signal<AvailableCut[]>([]);
  loaded = signal(false);

  // Corte elegido y unidades elegidas para venderlo
  cutMeatId = signal<number | null>(null);
  selectedIds = signal<number[]>([]);

  // Para elegir: las libres más las de esta venta
  private units = computed(() =>
    mergeUnits(this.availableUnits.hasValue() ? this.availableUnits.value() : [], this.ownUnits()),
  );

  cutOptions = computed(() => groupByCut(this.units()));

  // Unidades del corte elegido
  cutUnits = computed(() => unitsOfCut(this.units(), this.cutMeatId()));

  // Si el corte no cambia se conserva el precio de la venta; si cambia, rige el precio actual del corte
  pricePerKilo = computed(() => {
    if (this.cutMeatId() === this.sale().cutMeatId) return this.sale().pricePerKilo;

    const cutMeats = this.cutMeatOptions.hasValue() ? this.cutMeatOptions.value() : [];
    return cutMeats.find((cutMeat) => cutMeat.id === this.cutMeatId())?.pricePerKilo ?? null;
  });

  showDeleteConfirm = signal(false);
  deleting = signal(false);

  constructor() {
    // Al cambiar de corte se propone la unidad más antigua; al volver al original se recuperan las de la venta
    this.saleForm.controls['cutMeatId'].valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((cutMeatId: number | null) => {
        this.cutMeatId.set(cutMeatId);
        this.selectedIds.set(
          cutMeatId === this.sale().cutMeatId
            ? this.ownUnits().map((unit) => unit.id)
            : proposeOldest(this.units(), cutMeatId, 1),
        );
      });
  }

  // Precarga el form con los datos actuales de la venta y trae sus unidades
  ngOnInit() {
    const current = this.sale();

    this.cutMeatId.set(current.cutMeatId);
    this.saleForm.patchValue(
      { date: current.date, cutMeatId: current.cutMeatId },
      { emitEvent: false },
    );

    this.saleService.getSaleById(current.id).subscribe({
      next: (detail) => {
        const own = detail.stockCuts ?? [];
        this.ownUnits.set(own);
        this.selectedIds.set(own.map((unit) => unit.id));
        this.loaded.set(true);
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.saleForm.invalid || !this.loaded() || this.selectedIds().length === 0) {
      this.saleForm.markAllAsTouched();
      return;
    }

    const { date, cutMeatId } = this.saleForm.value;
    const request: SaleRequest = {
      date,
      cutMeatId,
      stockCutIds: this.selectedIds(),
      clientId: this.sale().clientId,
      butcherShopId: this.butcherShopId(),
    };

    this.saleService.updateSale(this.sale().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.saleService.deleteSaleById(this.sale().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: () => this.deleting.set(false), // El aviso lo muestra el interceptor
    });
  }
}
