import { Asset, Category } from './asset';
export enum AssignmentState {
  ACCEPTED = 'Accepted',
  DECLINED = 'Declined',
  WAITING_FOR_ACCEPTANCE = 'Waiting for acceptance',
}

import { User, UserTableItem } from './type';

export enum ReturntState {
  WAITING_FOR_RETURNING = 'WAITING_FOR_RETURNING',
  COMPLETED = 'COMPLETED',
  CANCELED = 'CANCELED',
}

type BaseAssignment = {
  id: string;
  user: User;
  asset: Asset<'basic'>;
  assetCode: string;
  assetName: string;
  specification: string;
  userId: string;
  createdBy: string;
  assignedDate: string;
  status: AssignmentStatus;
  note: string;
  returnState?: ReturntState;
  category?: Category;
};

export type AssignmentStatus = {
  id: string;
  name: string;
};

export interface AssignmentResponse {
  id: string;
  assetCode: string;
  assetName: string;
  assignTo: string;
  assignBy: string;
  assignedDate: string;
  state: AssignmentState;
}

type AssignmentBasic = Omit<BaseAssignment, 'assetCode' | 'assetName' | 'userId' | 'createdBy'>;

type AssignmentDetail = Omit<BaseAssignment, 'user' | 'asset'>;

type AssignmentTableItem = Pick<
  BaseAssignment,
  | 'id'
  | 'assetCode'
  | 'assetName'
  | 'userId'
  | 'createdBy'
  | 'assignedDate'
  | 'status'
  | 'returnState'
  | 'category'
>;

export type AssignmentEditView = Pick<BaseAssignment, 'id' | 'assignedDate' | 'note'> & {
  user: UserTableItem;
  asset: Asset<'table'>;
  version: number;
};

export type Assignment<Info extends 'basic' | 'table' | 'detail'> = Info extends 'basic'
  ? AssignmentBasic
  : Info extends 'table'
    ? AssignmentTableItem
    : Info extends 'detail'
      ? AssignmentDetail
      : Info extends 'full'
        ? BaseAssignment
        : never;

export class AssignmentStateHandler {
  private readonly _state: AssignmentState;
  static labelMap: Record<AssignmentState, string> = {
    [AssignmentState.ACCEPTED]: 'Accepted',
    [AssignmentState.DECLINED]: 'Declined',
    [AssignmentState.WAITING_FOR_ACCEPTANCE]: 'Waiting for acceptance',
  };

  constructor(state: AssignmentState) {
    this._state = state;
  }

  static parse(input: string): AssignmentState {
    const key = input as keyof typeof AssignmentState;
    const state = key in AssignmentState ? AssignmentState[key] : AssignmentState.DECLINED;
    return new AssignmentStateHandler(state)._state;
  }

  value(): AssignmentState {
    return this._state;
  }

  state(): AssignmentState {
    return this._state;
  }

  label(): string {
    return AssignmentStateHandler.labelMap[this._state] ?? 'Unknown State';
  }

  static get(): { value: AssignmentState; label: string }[] {
    return Object.entries(AssignmentStateHandler.labelMap).map(([value, label]) => ({
      value: value as AssignmentState,
      label,
    }));
  }

  isAccepted(): boolean {
    return this._state === AssignmentState.ACCEPTED;
  }

  isDeclined(): boolean {
    return this._state === AssignmentState.DECLINED;
  }

  isWaitingForAcceptance(): boolean {
    return this._state === AssignmentState.WAITING_FOR_ACCEPTANCE;
  }
}

export interface AssignmentListParams {
  page?: number;
  size?: number;
  search?: string;
  sort?: string;
  sortOrder?: 'asc' | 'desc';
  states?: string[];
  assignedDateFrom?: string;
  assignedDateTo?: string;
  userId?: string;
}
