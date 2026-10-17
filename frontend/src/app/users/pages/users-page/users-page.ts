import { Component, computed, DestroyRef, effect, inject, signal, untracked } from '@angular/core';
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
import { UserManageModal } from '../../components/user-manage-modal/user-manage-modal';
import { FilterChange, SearchFilter } from '../../../shared/interfaces/search-filter.interface';
import { defaultFilterValues } from '../../../shared/utils/search-filter.utils';

const PAGE_SIZE = 20;
const SEARCH_DEBOUNCE_MS = 300;

@Component({
  selector: 'users-page',
  imports: [
    SearchInput,
    ListSkeleton,
    EmptyState,
    ErrorState,
    UserListItem,
    UserFormModal,
    UserManageModal,
  ],
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

  // Filtro por estado de la cuenta
  filters: SearchFilter[] = [
    {
      key: 'status',
      label: 'Filtrar por estado',
      options: [
        { value: 'all', label: 'Todos' },
        { value: 'disabled', label: 'Deshabilitados' },
        { value: 'enabled', label: 'Habilitados' },
      ],
    },
  ];

  // Valor elegido de cada filtro
  filterValues = signal<Record<string, string>>(defaultFilterValues(this.filters));

  onFilterChange({ key, value }: FilterChange) {
    this.filterValues.update((current) => ({ ...current, [key]: value }));
  }

  // true = solo habilitados, false = solo deshabilitados, null = todos
  private enabledFilter = computed<boolean | null>(() => {
    switch (this.filterValues()['status']) {
      case 'enabled':
        return true;
      case 'disabled':
        return false;
      default:
        return null;
    }
  });

  // Modal de alta
  showModal = signal<boolean>(false);

  // Usuario elegido para gestionar su rol y acceso
  selectedUser = signal<User | null>(null);

  // Reemplaza el usuario en la lista sin recargar
  onUserUpdated(updated: User) {
    const filter = this.enabledFilter();

    this.users.update((list) =>
      filter !== null && updated.enabled !== filter
        ? list.filter((user) => user.id !== updated.id)
        : list.map((user) => (user.id === updated.id ? updated : user)),
    );
  }

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
      this.enabledFilter();
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
      .getUsers(this.debouncedSearch(), this.enabledFilter(), this.nextPage, PAGE_SIZE)
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
