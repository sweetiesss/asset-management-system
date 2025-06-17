import { AssignmentEditView, AssignmentResponse } from './../../types/assignment';
import { AssignmentListParams } from '@/types/assignment';
import { useDelete, useGetPage, useGet, usePatch, usePost, usePut } from '.';
import { AssignmentService } from '@/services/AssignmentService';
import { AssignmentFormRequest } from '@/utils/form/schemas/create-assignment-schema';
import { FieldError } from '@/types/dto';

export const useDeleteAssignment = (id: string) => {
  return useDelete(AssignmentService.deleteAssignment, id);
};

export const useGetAssignments = (params: AssignmentListParams) => {
  return useGetPage('assignments', AssignmentService.getAssignments, params);
};

export const useGetAssignmentBasic = (id: string) => {
  const endpoint = `${id}/edit-view`;
  return useGet<AssignmentEditView>(endpoint, AssignmentService.getAssignmentBasic);
};

export const useUpdateAssignment = (id: string) => {
  return usePatch<AssignmentFormRequest, AssignmentResponse>(
    AssignmentService.updateAssignment,
    id,
    [`${id}/edit-view`, 'assignments']
  );
};

export const useGetAssignmentStatuses = () => {
  return useGet('assignment-statuses', AssignmentService.getAssignmentStatuses);
};
export const usePostAssignment = () => {
  const endpoint = 'assignments';
  return usePost<string, AssignmentFormRequest, AssignmentResponse, FieldError[]>(
    endpoint,
    AssignmentService.postAssignment
  );
};

export const usePutAssignmentStatus = (id: string) => {
  const key = ["/assignments", id]
  return usePut<string,AssignmentResponse>(AssignmentService.putAssignmentStatus, id, key)
}
