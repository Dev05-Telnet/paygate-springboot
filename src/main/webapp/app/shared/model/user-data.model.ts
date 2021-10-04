export interface IUserData {
  id?: number;
  referId?: string | null;
  token?: string | null;
  payGateID?: string | null;
  payGateSecret?: string | null;
}

export const defaultValue: Readonly<IUserData> = {};
