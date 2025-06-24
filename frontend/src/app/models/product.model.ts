export interface Product {
  name: string;
  category?: string;
  description?: string;
  price: number;
  stock: number;
  created_at: string;
  updated_at?: string;
  discount?: {
    type: 'percent' | 'fixed';
    value: number;
    appliedWithCoupon?: boolean;
  };
}

