export interface CutMeat {
  id: number;
  cutMeatName: string;
  pricePerKilo: number;
  isActive: boolean;
}

export interface CutMeatRequest {
  cutMeatName: string;
  pricePerKilo: number;
}
