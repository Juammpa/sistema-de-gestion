// Traduccion de cada valor del enum CutMeatType (backend) a español
const CUT_MEAT_LABELS: Record<string, string> = {
  ROAST: 'Asado',
  FRONT: 'Delantero',
  HALF_VEAL: 'Media ternera',
  GRILL: 'Parrillero',
  CHOP: 'Chuletero',
  FRONT_ROAST: 'Delantero + Asado',
  MAGRON: 'Magrón',
  GUN: 'Pistola',
  WHEEL: 'Rueda',
};

// Traduce el tipo de corte; si aparece uno nuevo sin mapear, muestra el valor crudo
export function translateCutMeatType(cutMeatType: string): string {
  return CUT_MEAT_LABELS[cutMeatType] ?? cutMeatType;
}
