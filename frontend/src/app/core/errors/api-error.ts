import { HttpErrorResponse } from '@angular/common/http';

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

const NETWORK = 'No se pudo conectar con el servidor. Revisá tu conexión e intentá de nuevo.';

const SERVER = 'Ocurrió un problema en el servidor. Intentá de nuevo en unos minutos.';

const UNKNOWN = 'Ocurrió un error inesperado.';

const FALLBACK_4XX: Record<number, string> = {
  400: 'Los datos enviados no son válidos.',
  404: 'No se encontró el registro solicitado. Es posible que haya sido eliminado.',
  409: 'La operación no se pudo completar por el estado actual de los datos.',
};

// Convierte cualquier error HTTP en un mensaje pensado para el usuario
export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;
  if (!(error instanceof HttpErrorResponse)) return new ApiError(UNKNOWN, -1);

  if (error.status === 0) return new ApiError(NETWORK, 0);

  // En 5xx nunca mostramos lo que diga el servidor
  if (error.status >= 500) return new ApiError(SERVER, error.status);

  const body = error.error;
  const detail = typeof body?.detail === 'string' ? body.detail : null;
  const fieldErrors = body?.errors && typeof body.errors === 'object' ? body.errors : {};

  return new ApiError(detail ?? FALLBACK_4XX[error.status] ?? UNKNOWN, error.status, fieldErrors);
}

// Solo vale la pena reintentar fallas transitorias (sin conexion o servidor caido)
export function isRetryable(error: unknown): boolean {
  return error instanceof HttpErrorResponse && [0, 502, 503, 504].includes(error.status);
}
