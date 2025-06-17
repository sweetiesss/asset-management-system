import { Asset } from './asset';
import { Assignment, ReturntState } from './assignment';

type BaseAssetReturn = {
  id: string;
  returnedDate: string;
  state: ReturntState;
  assignent: Assignment<'basic'>;
  asset: Asset<'basic'>;
  assetCode: string;
  assetName: string;
  createdBy: string;
  assignedDate: string;
  updatedBy: string;
};

type AssetReturnBasic = Omit<
  BaseAssetReturn,
  'asset' | 'assetCode' | 'assetName' | 'updatedBy' | 'createdBy' | 'assignedDate'
>;

type AssetReturnTableItem = Pick<
  BaseAssetReturn,
  | 'id'
  | 'assetCode'
  | 'assetName'
  | 'createdBy'
  | 'assignedDate'
  | 'updatedBy'
  | 'returnedDate'
  | 'state'
>;

export class ReturnStateHandler {
  static labelMap: Record<ReturntState, string> = {
    [ReturntState.WAITING_FOR_RETURNING]: 'Waiting for returning',
    [ReturntState.COMPLETED]: 'Completed',
    [ReturntState.CANCELED]: 'Canceled',
  };

  constructor(private readonly state: ReturntState) {}

  static parse(input: string): ReturnStateHandler {
    const key = input as ReturntState;
    const state = key in ReturntState ? key : ReturntState.CANCELED;
    return new ReturnStateHandler(state);
  }

  value(): ReturntState {
    return this.state;
  }

  label(): string {
    return ReturnStateHandler.labelMap[this.state] ?? 'Unknown';
  }

  static getOptions(): { value: ReturntState; label: string }[] {
    return Object.entries(this.labelMap)
      .filter(([value]) => value !== ReturntState.CANCELED)
      .map(([value, label]) => ({
        value: value as ReturntState,
        label,
      }));
  }
}

export interface AssetReturnListParams {
  page?: number;
  size?: number;
  search?: string;
  sort?: string;
  sortOrder?: 'asc' | 'desc';
  states?: string[];
  returnedDateFrom?: string;
  returnedDateTo?: string;
}

export type AssetReturnType<Info extends 'basic' | 'table'> = Info extends 'basic'
  ? AssetReturnBasic
  : Info extends 'table'
    ? AssetReturnTableItem
    : Info extends 'full'
      ? BaseAssetReturn
      : never;
