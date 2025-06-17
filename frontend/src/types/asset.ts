import { Location } from './type';

export enum AssetState {
  AVAILABLE = 'AVAILABLE',
  NOT_AVAILABLE = 'NOT_AVAILABLE',
  WAITING_FOR_RECYCLING = 'WAITING_FOR_RECYCLING',
  RECYCLED = 'RECYCLED',
  ASSIGNED = 'ASSIGNED',
}

export class AssetStateHandler {
  static labelMap: Record<AssetState, string> = {
    [AssetState.AVAILABLE]: 'Available',
    [AssetState.NOT_AVAILABLE]: 'Not available',
    [AssetState.WAITING_FOR_RECYCLING]: 'Waiting for recycling',
    [AssetState.RECYCLED]: 'Recycled',
    [AssetState.ASSIGNED]: 'Assigned',
  };
  private readonly _state: AssetState;

  public constructor(state: AssetState) {
    this._state = state;
  }

  static parse(input: string): AssetStateHandler {
    const key = input as keyof typeof AssetState;
    const state = key in AssetState ? (AssetState[key] as AssetState) : AssetState.NOT_AVAILABLE;
    return new AssetStateHandler(state);
  }

  value(): AssetState {
    return this._state;
  }

  state(): AssetState {
    return this.value();
  }

  label(): string {
    return AssetStateHandler.labelMap[this._state] ?? 'Unknown State';
  }

  /**
   * Build dropdown options.
   * @param purpose Gets the options based on the purpose of use.
   *   - 'create': `AVAILABLE`, `NOT_AVAILABLE`
   *   - 'edit': `AVAILABLE`, `NOT_AVAILABLE`, `WAITING_FOR_RECYCLING`, `RECYCLED`
   *   - 'all': `AVAILABLE`, `NOT_AVAILABLE`, `WAITING_FOR_RECYCLING`, `RECYCLED`, `ASSIGNED`
   */
  static getOptions(purpose: 'create' | 'edit' | 'all'): { value: AssetState; label: string }[] {
    let arr: AssetState[];

    switch (purpose) {
      case 'create':
        arr = [AssetState.AVAILABLE, AssetState.NOT_AVAILABLE];
        break;
      case 'edit':
        arr = [
          AssetState.AVAILABLE,
          AssetState.NOT_AVAILABLE,
          AssetState.WAITING_FOR_RECYCLING,
          AssetState.RECYCLED,
        ];
        break;
      case 'all':
        arr = Object.values(AssetState);
        break;
      default:
        arr = [AssetState.NOT_AVAILABLE];
    }

    return arr.map((v) => ({ value: v, label: this.labelMap[v] }));
  }

  isAvailable(): boolean {
    return this._state === AssetState.AVAILABLE;
  }
  isNotAvailable(): boolean {
    return this._state === AssetState.NOT_AVAILABLE;
  }
  isWaitingForRecycling(): boolean {
    return this._state === AssetState.WAITING_FOR_RECYCLING;
  }
  isRecycled(): boolean {
    return this._state === AssetState.RECYCLED;
  }
  isAssigned(): boolean {
    return this._state === AssetState.ASSIGNED;
  }
}

type BaseAsset = {
  id: string;
  code: string;
  name: string;
  category: Category;
  categoryId: number;
  categoryName: string;
  specification: string;
  installedDate: string;
  state: AssetState;
  location: Location;
  version: number;
};

type AssetBasic = Omit<BaseAsset, 'categoryName' | 'categoryId'>;

type AssetTableItem = Pick<BaseAsset, 'id' | 'code' | 'name' | 'categoryName' | 'state'>;

type AssetDetail = Omit<BaseAsset, 'categoryName' | 'categoryId'>;

export interface AssetHistory {
  id: string;
  assignedDate: string;
  assignedTo: string;
  assignedBy: string;
  returnedDate: string;
}

export interface Category {
  id: string;
  name: string;
  prefix: string;
}

export type Asset<Info extends 'basic' | 'table' | 'detail' | 'full'> = Info extends 'basic'
  ? AssetBasic
  : Info extends 'table'
    ? AssetTableItem
    : Info extends 'detail'
      ? AssetDetail
      : Info extends 'full'
        ? BaseAsset
        : never;

export interface AssetListParams {
  page?: number;
  size?: number;
  search?: string;
  sort?: string;
  sortOrder?: 'asc' | 'desc';
  categories?: string[];
  states?: string[];
}

export interface AssetReturn {
  id: string;
  assignmentId: string;
  state: 'WAITING_FOR_RETURNING' | 'COMPLETED' | 'CANCELED';
  returnedDate: string;
}
