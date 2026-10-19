import { environment } from '../../../environments/environment';

// API de cuenta de Keycloak: Perfil y sesiones del propio usuario
export const ACCOUNT_URL = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/account`;
