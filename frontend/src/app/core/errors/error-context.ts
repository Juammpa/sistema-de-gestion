import { HttpContext, HttpContextToken } from '@angular/common/http';

// 'auto' --> Avisa con toast solo si es estructura (los GET se muestran en pantalla)
// 'always' --> Avisa siempre (GET de detalle que abre un modal)
// 'never' --> Nunca avisa
export const ERROR_NOTIFICATION = new HttpContextToken<'auto' | 'always' | 'never'>(() => 'auto');

export const notifyAlways = () => new HttpContext().set(ERROR_NOTIFICATION, 'always');
