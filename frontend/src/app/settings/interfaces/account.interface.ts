// Perfil del usuario segun la API de cuenta de Keycloak (solo campos que se utilizan)
export interface AccountProfile {
  username: string;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
}
