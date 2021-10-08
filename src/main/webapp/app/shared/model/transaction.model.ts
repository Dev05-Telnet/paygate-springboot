export interface ITransaction {
  id?: number;
  orderId?: number | null;
  payRequestId?: string | null;
}

export const defaultValue: Readonly<ITransaction> = {};
