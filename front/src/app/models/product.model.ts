export interface Review {
    id: string;
    userId: string;
    userName: string;
    rating: number;
    comment: string;
    date: Date;
}

export interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    pricePerDay?: number;
    purchasePrice?: number;
    availableForRent?: boolean;
    availableForSale?: boolean;
    condition?: string;
    specifications?: string;
    weight?: number;
    imageUrl: string;
    category: string;
    categoryId?: number;
    providerId: string;
    ownerId?: number;
    stock: number;
    rating: number;
    reviews: Review[];
    warehouseId?: number;
    warehouseName?: string;
    blockedPeriods?: { startDate: string; endDate: string; type: string; reason: string }[];
    createdAt?: Date | string;
}

export interface Order {
    id: string;
    userId: string;
    items: { productId: string; quantity: number; price: number }[];
    totalPrice: number;
    orderDate: Date;
    status: 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
    deliveryAddress: string;
}
