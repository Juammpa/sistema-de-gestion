import { HttpClient, HttpResponse } from '@angular/common/http';
import { DOCUMENT, inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { notifyAlways } from '../../core/errors/error-context';

@Injectable({ providedIn: 'root' })
export class ExportService {
  private http = inject(HttpClient);
  private document = inject(DOCUMENT);

  // Pide el ZIP con todos los datos, lo descarga y devuelve el nombre del archivo.
  export(): Observable<string> {
    return this.http
      .get(`${environment.apiUrl}/export`, {
        responseType: 'blob',
        observe: 'response', // Para poder leer el nombre del archivo en los headers
        // Es un GET, pero si falla hay que avisarle al usuario, ya que apreto un boton
        context: notifyAlways(),
      })
      .pipe(
        map((response) => {
          const fileName = this.fileNameOf(response);
          this.save(response.body as Blob, fileName);
          return fileName;
        }),
      );
  }

  // El nombre lo arma el servidor (exportacion_2026-10-03.zip) y viaja en Content-Disposition
  private fileNameOf(response: HttpResponse<Blob>): string {
    const header = response.headers.get('Content-Disposition') ?? '';
    const match = /filename="?([^";]+)"?/i.exec(header);

    return match?.[1] ?? `exportacion_${new Date().toISOString().slice(0, 10)}.zip`;
  }

  // Descarga un Blob: link temporal + click, y se libera la URL
  private save(blob: Blob, fileName: string) {
    const url = URL.createObjectURL(blob);
    const link = this.document.createElement('a');

    link.href = url;
    link.download = fileName;
    link.click();

    // Se libera despues del click: la descarga ya arranco
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
