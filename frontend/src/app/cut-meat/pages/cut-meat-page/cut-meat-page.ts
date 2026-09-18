import { Component, inject, signal } from '@angular/core';
import { SearchInput } from '../../../shared/components/search-input/search-input';
import { CutMeatService } from '../../services/cut-meat.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { CutMeatListItem } from '../../components/cut-meat-list-item/cut-meat-list-item';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { CutMeatFormModal } from '../../components/cut-meat-form-modal/cut-meat-form-modal';
import { EditCutMeatModal } from '../../components/edit-cut-meat-modal/edit-cut-meat-modal';
import { CutMeat } from '../../interfaces/cut-meat.interface';
import { of } from 'rxjs';

@Component({
  selector: 'cut-meat-page',
  imports: [
    SearchInput,
    CutMeatListItem,
    LoadingSpinner,
    EmptyState,
    CutMeatFormModal,
    EditCutMeatModal,
  ],
  templateUrl: './cut-meat-page.html',
})
export default class CutMeatPage {
  private cutMeatService = inject(CutMeatService);

  cutMeatResource = rxResource({
    stream: () => this.cutMeatService.getAllCutMeats(),
  });

  reload() {
    this.cutMeatResource.reload();
  }

  showModal = signal<boolean>(false);

  showCutMeatDetail = signal<boolean>(false);
  selectedCutMeat = signal<CutMeat | null>(null);

  openCutMeatDetail(cutMeat: CutMeat) {
    this.selectedCutMeat.set(cutMeat);
    this.showCutMeatDetail.set(true);
  }

  cutMeatDetail = rxResource({
    params: () => this.selectedCutMeat()?.id ?? null,
    stream: ({ params }) =>
      params !== null ? this.cutMeatService.getCutMeatById(params) : of(undefined),
  });

  onCutMeatChanged() {
    this.showCutMeatDetail.set(false);
    this.reload();
  }
}
