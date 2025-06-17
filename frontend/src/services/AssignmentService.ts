import { authApi } from '@/configs/axios';
import {
  Assignment,
  AssignmentEditView,
  AssignmentListParams,
  AssignmentResponse,
  AssignmentStatus,
} from '@/types/assignment';
import { ExpectedResponse, Response } from '@/types/dto';
import { handleApiError } from '@/utils/api';
import { AssignmentFormRequest } from '@/utils/form/schemas/create-assignment-schema';
import { deleteEntity, getEntities, getEntity, patchEntity, postEntity, putEntity } from '.';
const endpoint = 'assignments';

export const AssignmentService = {
  /**
   * Create a new Assignment
   * @param data Assignment data
   * @returns Promise with created Assignment or error
   */
  createAssignment: async (data: AssignmentFormRequest): Promise<Response<AssignmentResponse>> => {
    try {
      const response = await authApi.post<Response<AssignmentResponse>>('/assignments', data);
      return response.data;
    } catch (error: unknown) {
      return handleApiError<AssignmentResponse>(error);
    }
  },
  postAssignment: async (data: AssignmentFormRequest) => {
    const endpoint = 'assignments';
    return postEntity<AssignmentResponse, AssignmentFormRequest>(endpoint, data);
  },

  getAssignments: async (params: AssignmentListParams) => {
    return getEntities<Assignment<'table'>, AssignmentListParams>(endpoint, params);
  },

  // updateAssignment: async (
  //   id: string,
  //   data: AssignmentFormRequest
  // ): Promise<Response<AssignmentResponse>> => {
  //   try {
  //     const response = await authApi.patch<Response<AssignmentResponse>>(
  //       `/assignments/${id}`,
  //       data
  //     );
  //     return response.data;
  //   } catch (error: unknown) {
  //     return handleApiError<AssignmentResponse>(error);
  //   }
  // },

  updateAssignment: async (id: string, data: AssignmentFormRequest) => {
    const endpoint = `/assignments`;
    return patchEntity<AssignmentResponse, AssignmentFormRequest>(endpoint, data, id);
  },

  updateAssignmentStatus: async (
    id: string,
    status: string
  ): Promise<Response<AssignmentResponse>> => {
    try {
      const response = await authApi.put<Response<AssignmentResponse>>(
        `/assignments/${id}/status`,
        { status }
      );
      return response.data;
    } catch (error: unknown) {
      return handleApiError<AssignmentResponse>(error);
    }
  },

  getAssignmentEditView: async (id: string): Promise<Response<AssignmentEditView>> => {
    try {
      const response = await authApi.get<Response<AssignmentEditView>>(
        `/assignments/${id}/edit-view`
      );
      return response.data;
    } catch (error: unknown) {
      return handleApiError<AssignmentEditView>(error);
    }
  },

  deleteAssignment: async (id: string): Promise<ExpectedResponse<void>> => {
    return deleteEntity(endpoint, id);
  },

  getAssignmentBasic: async (id: string): Promise<ExpectedResponse<AssignmentEditView>> => {
    const endpoint = `assignments/${id}`;
    return getEntity<AssignmentEditView>(endpoint);
  },

  getAssignment: async (assignmentId: string): Promise<Response<Assignment<'detail'>>> => {
    try {
      const response = await authApi.get<Response<Assignment<'detail'>>>(
        `/assignments/${assignmentId}`
      );
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  getAssignmentStatuses: async () => {
    const endpoint = '/assignments/assignment-statuses';
    return getEntity<AssignmentStatus[]>(endpoint);
  },

  putAssignmentStatus: async(id: string, status: string) => {
    const endpoint = `/assignments/${id}/status`;
    return putEntity<AssignmentResponse, { status: string }>(endpoint, { status });    
  }
};
