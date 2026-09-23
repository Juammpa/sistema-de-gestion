export interface CutMeat {
  id: number;
  cutMeatName: string;
  pricePerKilo: number;
  specie: string;
  isActive: boolean;
}

export interface CutMeatRequest {
  cutMeatName: string;
  pricePerKilo: number;
  specie: string;
}
