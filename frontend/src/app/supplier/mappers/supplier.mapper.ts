import { ListItem } from '../../shared/interfaces/list-item.interface';
import { Supplier } from '../interfaces/supplier.interface';

export class SupplierMapper {
  // Supplier => ListItem
  static mapSupplierToListItem(supplier: Supplier): ListItem {
    return {
      id: supplier.id,
      title: `${supplier.companyName}`,
      subtitle: supplier.phoneNumber,
      initials: `${supplier.companyName[0]}`,
    };
  }

  // Supplier[] => ListItem[]
  static mapSuppliersToListItems(suppliers: Supplier[]): ListItem[] {
    return suppliers.map(this.mapSupplierToListItem);
  }
}
