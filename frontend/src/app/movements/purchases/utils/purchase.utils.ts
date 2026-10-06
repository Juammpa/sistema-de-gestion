import { SpecieValue } from '../../../cut-meat/utils/specie.utils';
import { SupplierType } from '../../../supplier/interfaces/supplier.interface';

// Valores exactos del enum PurchaseType
export type PurchaseTypeValue =
  | 'HALF_PIG'
  | 'VEAL_UP_TO_110KG'
  | 'NT_111_120KG'
  | 'NT_121_130KG'
  | 'STEER_OVER_130KG';

const PURCHASE_TYPE_LABELS: Record<PurchaseTypeValue, string> = {
  HALF_PIG: 'Media cerdo',
  VEAL_UP_TO_110KG: 'Ternero hasta 110 kg',
  NT_111_120KG: 'NT 111-120 kg',
  NT_121_130KG: 'NT 121-130 kg',
  STEER_OVER_130KG: 'NOVILLO +130 kg',
};

// Opciones listas para el <select>
export const PURCHASE_TYPE_OPTIONS: { value: PurchaseTypeValue; label: string }[] = (
  Object.keys(PURCHASE_TYPE_LABELS) as PurchaseTypeValue[]
).map((value) => ({ value, label: PURCHASE_TYPE_LABELS[value] }));

// Traduce el valor recibido del backend para mostrarlo
export default function translatePurchaseType(purchaseType: string): string {
  return PURCHASE_TYPE_LABELS[purchaseType as PurchaseTypeValue] ?? purchaseType;
}

// Especie de cada tipo de compra (igual que PurchaseType en el backend)
export const PURCHASE_TYPE_SPECIE: Record<PurchaseTypeValue, SpecieValue> = {
  HALF_PIG: 'PORCINE',
  VEAL_UP_TO_110KG: 'BOVINE',
  NT_111_120KG: 'BOVINE',
  NT_121_130KG: 'BOVINE',
  STEER_OVER_130KG: 'BOVINE',
};

// Tipos de compra que puede vender un proveedor segun su tipo
export function purchaseTypeOptionsFor(supplierType: SupplierType) {
  return PURCHASE_TYPE_OPTIONS.filter(
    (option) => supplierType === 'BOTH' || PURCHASE_TYPE_SPECIE[option.value] === supplierType,
  );
}
