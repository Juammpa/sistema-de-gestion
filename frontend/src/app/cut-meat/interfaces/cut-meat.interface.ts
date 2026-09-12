export interface CutMeat {
  id: number;
  cutMeatType: string;
  pricePerKilo: number;
}

export interface CutMeatRequest {
  cutMeatType: string;
  pricePerKilo: number;
}
