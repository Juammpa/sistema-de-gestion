import { Component, DestroyRef, effect, inject, signal, untracked } from '@angular/core';
import { UserService } from '../../services/user.service';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, map, Subscription } from 'rxjs';
import { User } from '../../interfaces/user.interface';
import { SearchInput } from '../../../shared/components/search-input/search-input';
import { ListSkeleton } from '../../../shared/components/list-skeleton/list-skeleton';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorState } from '../../../shared/components/error-state/error-state';
import { UserListItem } from '../../components/user-list-item/user-list-item';
import { UserFormModal } from '../../components/user-form-modal/user-form-modal';

const PAGE_SIZE = 20;
const SEARCH_DEBOUNCE_MS = 300;

@Component({
  selector: 'users-page',
  imports: [SearchInput, ListSkeleton, EmptyState, ErrorState, UserListItem, UserFormModal],
  templateUrl: './users-page.html',
})
export default class UsersPage {
  private userService = inject(UserService);

  // Texto del buscador.
  searchText = signal<string>('');
  // Se consulta cuando termina de escribir
  private debouncedSearch = toSignal(
    toObservable(this.searchText).pipe(
      debounceTime(SEARCH_DEBOUNCE_MS),
      map((text) => text.trim()),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );

  // Modal de alta
  showModal = signal<boolean>(false);

  // ====== Paginacion ======

  users = signal<User[]>([]);
  isLoading = signal<boolean>(false); // Primera pagina
  isLoadingMore = signal<boolean>(false); // Cargando una pagina siguiente
  error = signal<unknown>(null);
  hasNext = signal<boolean>(false);

  private nextPage = 0;
  private request?: Subscription;

  constructor() {
    // Cada vez que cambia la busqueda se empieza de cero.
    effect(() => {
      this.debouncedSearch();
      untracked(() => this.reload());
    });

    inject(DestroyRef).onDestroy(() => this.request?.unsubscribe());
  }

  // Vuelve a cargar desde la primer pagina.
  reload() {
    this.request?.unsubscribe();
    this.users.set([]);
    this.nextPage = 0;
    this.error.set(null);
    this.isLoading.set(false);
    this.isLoadingMore.set(false);
    this.loadNextPage();
  }

  // Pide la siguiente pagina y la agrega al final de la lista
  loadNextPage() {
    if (this.isLoading() || this.isLoadingMore()) return;

    const firstPage = this.nextPage === 0;
    if (!firstPage && !this.hasNext()) return;

    (firstPage ? this.isLoading : this.isLoadingMore).set(true);
    this.error.set(null);

    this.request = this.userService
      .getUsers(this.debouncedSearch(), this.nextPage, PAGE_SIZE)
      .subscribe({
        next: (page) => {
          this.users.update((current) => [...current, ...page.content]);
          this.hasNext.set(page.hasNext);
          this.nextPage++;
          this.isLoading.set(false);
          this.isLoadingMore.set(false);
        },
        error: (error) => {
          this.error.set(error);
          this.isLoading.set(false);
          this.isLoadingMore.set(false);
        },
      });
  }
}
