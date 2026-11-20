import { defer, Observable, of, Subject, throwError } from 'rxjs';
import { CachedRequest } from './cached-request';

describe('CachedRequest', () => {
  // Cuenta cuantas veces se "pidio al servidor" y devuelve lo que se le indique
  function loaderOf<T>(respond: (call: number) => Observable<T>) {
    const loader = { calls: 0 };
    const request = new CachedRequest<T>(() =>
      defer(() => {
        loader.calls++;
        return respond(loader.calls);
      }),
    );
    return { loader, request };
  }

  describe('get()', () => {
    it('reuses the stored result instead of asking the server again', () => {
      // Arrange
      const { loader, request } = loaderOf(() => of(['Asado']));
      const results: string[][] = [];

      // Act: dos pantallas piden lo mismo, una despues de la otra
      request.get().subscribe((value) => results.push(value));
      request.get().subscribe((value) => results.push(value));

      // Assert
      expect(loader.calls).toBe(1);
      expect(results).toEqual([['Asado'], ['Asado']]);
    });

    it('shares one request between screens that ask while it is still in flight', () => {
      // Arrange: el servidor todavia no respondio
      const response = new Subject<string[]>();
      let calls = 0;
      const request = new CachedRequest<string[]>(() =>
        defer(() => {
          calls++;
          return response;
        }),
      );
      const results: string[][] = [];

      // Act
      request.get().subscribe((value) => results.push(value));
      request.get().subscribe((value) => results.push(value));
      response.next(['Asado']);

      // Assert: una sola peticion y las dos pantallas recibieron la respuesta
      expect(calls).toBe(1);
      expect(results.length).toBe(2);
    });

    it('asks the server again after the time to live has passed', () => {
      // Arrange
      jasmine.clock().install();
      jasmine.clock().mockDate(new Date(2026, 0, 1, 10, 0, 0));
      const { loader, request } = loaderOf(() => of(['Asado']));
      request.get().subscribe();

      // Act: pasan 6 minutos (el limite es de 5)
      jasmine.clock().mockDate(new Date(2026, 0, 1, 10, 6, 0));
      request.get().subscribe();

      // Assert
      expect(loader.calls).toBe(2);
      jasmine.clock().uninstall();
    });

    it('does not store a failed request, so the next call tries again', () => {
      // Arrange: la primera vez falla y la segunda responde bien
      const { loader, request } = loaderOf((call) =>
        call === 1 ? throwError(() => new Error('sin conexion')) : of(['Asado']),
      );
      let failed = false;
      let result: string[] | undefined;

      // Act
      request.get().subscribe({ error: () => (failed = true) });
      request.get().subscribe((value) => (result = value));

      // Assert
      expect(failed).toBeTrue();
      expect(loader.calls).toBe(2);
      expect(result).toEqual(['Asado']);
    });
  });

  describe('invalidate()', () => {
    it('makes the next call ask the server again', () => {
      // Arrange
      const { loader, request } = loaderOf((call) => of([`version ${call}`]));
      request.get().subscribe();

      // Act: se creo, edito o borro algo
      request.invalidate();
      let result: string[] | undefined;
      request.get().subscribe((value) => (result = value));

      // Assert
      expect(loader.calls).toBe(2);
      expect(result).toEqual(['version 2']);
    });

    it('drops a request that was still in flight so its old answer is not reused', () => {
      // Arrange: la primera respuesta todavia no llego
      const responses: Subject<string[]>[] = [];
      const request = new CachedRequest<string[]>(() =>
        defer(() => {
          const response = new Subject<string[]>();
          responses.push(response);
          return response;
        }),
      );
      request.get().subscribe();

      // Act: mientras tanto se modifica algo y se invalida
      request.invalidate();
      request.get().subscribe();

      // Assert: se hicieron dos peticiones distintas
      expect(responses.length).toBe(2);
    });
  });
});
