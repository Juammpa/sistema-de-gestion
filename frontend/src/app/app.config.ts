import {
  ApplicationConfig,
  ErrorHandler,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import {
  createInterceptorCondition,
  IncludeBearerTokenCondition,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  includeBearerTokenInterceptor,
  provideKeycloak,
  AutoRefreshTokenService,
  UserActivityService,
  withAutoRefreshToken,
} from 'keycloak-angular';
import { environment } from '../environments/environment';
import { provideRouter, withViewTransitions } from '@angular/router';
import localeEsAr from '@angular/common/locales/es-AR';
import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from './core/errors/error.interceptor';
import { GlobalErrorHandler } from './core/errors/global-error-handler';
import { registerLocaleData } from '@angular/common';
import { ThemeService } from './core/theme/theme.service';
import { AccentService } from './core/theme/accent.service';
import { unauthorizedInterceptor } from './core/auth/unauthorized.interceptor';

// Datos de formato (separadores, simbolo de moneda, fechas) para es-AR
registerLocaleData(localeEsAr);

// El token se envia solo a nuestra API, nunca a terceros
const escapeRegExp = (text: string) => text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const apiTokenCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: new RegExp(`^${escapeRegExp(environment.apiUrl)}(/.*)?$`, 'i'),
  bearerPrefix: 'Bearer',
});

const SESSION_INACTIVITY_TIMEOUT_MS = 30 * 60 * 1000;

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withViewTransitions()),
    provideHttpClient(
      withFetch(),
      withInterceptors([includeBearerTokenInterceptor, errorInterceptor, unauthorizedInterceptor]),
    ),
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    { provide: LOCALE_ID, useValue: 'es-AR' },
    provideAppInitializer(() => {
      inject(ThemeService);
      inject(AccentService);
    }),
    provideKeycloak({
      config: environment.keycloak,
      initOptions: {
        onLoad: 'login-required',
        pkceMethod: 'S256',
      },
      features: [
        withAutoRefreshToken({
          // Si no tocamos la app en 30min, se cierra la sesion.
          sessionTimeout: SESSION_INACTIVITY_TIMEOUT_MS,
          onInactivityTimeout: 'logout',
        }),
      ],
      providers: [AutoRefreshTokenService, UserActivityService],
    }),
    { provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG, useValue: [apiTokenCondition] },
  ],
};
