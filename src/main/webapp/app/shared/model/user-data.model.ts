export interface IUserData {
  id?: number;
  referId?: number | null;
  token?: string | null;
  payGateID?: string | null;
  payGateSecret?: string | null;
}

export const defaultValue: Readonly<IUserData> = {};
