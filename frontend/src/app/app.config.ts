import {
  ApplicationConfig,
  ErrorHandler,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import localeEsAr from '@angular/common/locales/es-AR';
import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from './core/errors/error.interceptor';
import { GlobalErrorHandler } from './core/errors/global-error-handler';
import { registerLocaleData } from '@angular/common';
import { ThemeService } from './core/theme/theme.service';

// Datos de formato (separadores, simbolo de moneda, fechas) para es-AR
registerLocaleData(localeEsAr);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([errorInterceptor])),
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    { provide: LOCALE_ID, useValue: 'es-AR' },
    provideAppInitializer(() => {
      inject(ThemeService);
    }),
  ],
};
