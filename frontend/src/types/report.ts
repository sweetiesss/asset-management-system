export default interface Report{
  id: number;
  category: string;
  total: number;
  assigned: number;
  available: number;
  notAvailable: number;
  waitingForRecycling: number;
  recycled: number;
}

export interface ReportListParams {
  page?: number;
  size?: number;
  sort?: string;
  sortOrder?: 'asc' | 'desc';
}

export enum ReportEntity {
  ASSETS = 'assets',
  USERS = 'users',
  ASSIGNMENTS = 'assignments',
}



export type ReportExportParams<T extends ReportEntity = ReportEntity.ASSETS> = T extends ReportEntity.ASSETS
  ? {
      fileName: string;
      categoryIds: number[];
      states: string[];
      startDate: string;
      endDate: string;
    }
  : T extends ReportEntity.USERS
    ? {
        fileName: string;
        types: string[];
        startDate: string;
        endDate: string;
      }
    : T extends ReportEntity.ASSIGNMENTS
      ? {
          fileName: string;
          statusIds: number[];
          startDate: string;
          endDate: string;
        }
      : never;
