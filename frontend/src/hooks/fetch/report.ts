import Report, { ReportEntity, ReportExportParams, ReportListParams } from "@/types/report"
import {  useGetBlobWithTrigger, useGetPage } from "."
import ReportService from "@/services/ReportService"
import { ErrResponse } from "@/types/dto"
import { AxiosError } from "axios"

export const useGetReports = (params: ReportListParams) => {
    return useGetPage<Report, ReportListParams>("report",ReportService.getReports,params)
}

export const useGetStandardAssetReport = () => {
    const key = ['standard-report'];
    const fetcher = () => ReportService.getStandardReportFile();
    return useGetBlobWithTrigger<never,AxiosError<ErrResponse>>(key, fetcher);
}

export const useGetCustomReport = <T extends ReportEntity>(entity: T) => {
  const key = ['custom-report', entity.toLowerCase()];
  type QueryParams = ReportExportParams<T>;

  const fetcher = (params: QueryParams) =>
    ReportService.getCustomReport(entity, params);

  return useGetBlobWithTrigger<QueryParams, AxiosError<ErrResponse>>(
    key,
    fetcher
  );
};
