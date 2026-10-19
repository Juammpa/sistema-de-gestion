// Perfil del usuario segun la API de cuenta de Keycloak (solo campos que se utilizan)
export interface AccountProfile {
  username: string;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
}

// Una sesion abierta en Keycloak: un inicio de sesion en un navegador
export interface AccountSession {
  id: string;
  ipAddress: string;
  started: number;
  lastAccess: number;
  current: boolean | null;
}

// Dispositivo desde el que se inicio sesion.
export interface AccountDevice {
  ipAddress: string;
  os: string | null;
  browser: string | null;
  mobile: boolean;
  sessions: AccountSession[];
}
