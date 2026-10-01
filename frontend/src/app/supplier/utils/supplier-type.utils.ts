import { SupplierType } from '../interfaces/supplier.interface';

export const SUPPLIER_TYPE_INFO: Record<
  SupplierType,
  { label: string; icon: string; badge: string }
> = {
  BOVINE: {
    label: 'Bovinos',
    icon: 'fa-solid fa-cow',
    badge: 'border-amber-500/30 bg-amber-500/10 text-amber-300',
  },
  PORCINE: {
    label: 'Porcinos',
    icon: 'fa-solid fa-bacon',
    badge: 'border-pink-400/30 bg-pink-400/10 text-pink-300',
  },
  BOTH: {
    label: 'Ambos',
    icon: 'fa-solid fa-layer-group',
    badge: 'border-blue-500/30 bg-blue-500/10 text-blue-300',
  },
};

// Opciones listas para el formulario
export const SUPPLIER_TYPE_OPTIONS = (Object.keys(SUPPLIER_TYPE_INFO) as SupplierType[]).map(
  (value) => ({ value, ...SUPPLIER_TYPE_INFO[value] }),
);
