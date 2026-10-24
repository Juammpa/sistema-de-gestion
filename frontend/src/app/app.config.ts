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
import { ACCOUNT_URL } from './core/auth/keycloak-urls';
import { AccountActionService } from './core/auth/account-action.service';

// Datos de formato (separadores, simbolo de moneda, fechas) para es-AR
registerLocaleData(localeEsAr);

// El token se envia solo a nuestra API y a la API de cuenta de Keycloak, nunca a terceros.
const escapeRegExp = (text: string) => text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const tokenConditionFor = (baseUrl: string) =>
  createInterceptorCondition<IncludeBearerTokenCondition>({
    urlPattern: new RegExp(`^${escapeRegExp(baseUrl)}([/?].*)?$`, 'i'),
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
      // Debe crearce antes de que Keycloak arranque
      inject(AccountActionService);
    }),
    provideKeycloak({
      config: environment.keycloak,
      initOptions: {
        // Detecta en silencio si ya hay una sesion, sin obligar a iniciarla (la portada es publica)
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
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
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [tokenConditionFor(environment.apiUrl), tokenConditionFor(ACCOUNT_URL)],
    },
  ],
};
