export type UserRole = 'ADMIN' | 'USER';

// Usuario tal como lo devuelve el backend
export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  role: UserRole | null;
  createdAt: string | null;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  temporaryPassword: string;
}
