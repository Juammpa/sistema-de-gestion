import { SpecieValue } from '../../cut-meat/utils/specie.utils';

export interface PurchaseType {
  id: number;
  name: string;
  specie: SpecieValue;
  isActive: boolean;
}
