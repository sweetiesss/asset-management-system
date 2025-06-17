import Report, { ReportEntity, ReportExportParams, ReportListParams } from '@/types/report';
import { getEntities, getEntityBlob } from '.';


const getReports = async(params: ReportListParams) =>{
  return getEntities<Report, ReportListParams>("/reports/assets", params);
}

const getStandardReportFile = async() => {
  return getEntityBlob("/reports/assets/export?exportType=standard");
}

const getCustomReport = async <T extends ReportEntity>(
  entity: T, 
  params: ReportExportParams<T>
) => {
  const pathMap: Record<ReportEntity, string> = {
    [ReportEntity.ASSETS]: "/reports/assets/export",
    [ReportEntity.USERS]: "/reports/users/export",
    [ReportEntity.ASSIGNMENTS]: "/reports/assignments/export",
  };

  const path = pathMap[entity];

  const finalParams = {
    ...params,
    exportType: 'custom'
  };
  return getEntityBlob<typeof finalParams>(path, finalParams);
};



const ReportService = {
  getReports,
  getStandardReportFile,
  getCustomReport
};

export default ReportService;
