// Una pagina de resultados (scroll infinito)
export interface PageResponse<T> {
  content: T[];
  page: number;
  hasNext: boolean;
}
