// Se trabaja en centavos (enteros) para comparar sumas sin errores de punto flotante
export const toCents = (kilograms: number) => Math.round(kilograms * 100);

// Como decimal, como lo escribe el usuario
export const formatKilograms = (cents: number) => (cents / 100).toFixed(2).replace('.', ',');
