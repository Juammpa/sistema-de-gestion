// client.interface.ts
export interface Client {
  id: number;
  name: string;
  lastName: string;
  phoneNumber: string;
}

export interface ClientRequest {
  name: string;
  lastName: string;
  phoneNumber: string;
}
