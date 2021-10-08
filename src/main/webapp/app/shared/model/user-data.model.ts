export interface IUserData {
  id?: number;
  userId?: number | null;
  store?: string | null;
  token?: string | null;
  payGateID?: string | null;
  payGateSecret?: string | null;
  scriptId?: string | null;
}

export const defaultValue: Readonly<IUserData> = {};
