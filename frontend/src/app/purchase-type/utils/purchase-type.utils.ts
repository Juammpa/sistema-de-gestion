import { SupplierType } from '../../supplier/interfaces/supplier.interface';
import { PurchaseType } from '../interfaces/purchase-type.interface';

// Tipos de compra activos que puede vender un proveedor segun su tipo
export function purchaseTypesFor(
  types: PurchaseType[],
  supplierType: SupplierType,
): PurchaseType[] {
  return types.filter(
    (type) => type.isActive && (supplierType === 'BOTH' || type.specie === supplierType),
  );
}
