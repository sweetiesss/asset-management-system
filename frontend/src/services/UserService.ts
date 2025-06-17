import { api, authApi } from '@/configs/axios';
import { Response } from '@/types/dto';
import { CurrentUserInfo, RefreshAccessToken, UserDetailType } from '@/types/type';
import { User, UserListResponse } from '@/types/type';
import { Auth } from '@/types/type';
import { handleApiError } from '@/utils/api';
import { CreateUserFormRequest } from '@/utils/form/schemas/create-user-schema';
import { EditUserFormRequest } from '@/utils/form/schemas/edit-user-schema';
import { patchEntity } from '.';

export interface UserListParams {
  page?: number;
  size?: number;
  search?: string;
  sort?: string;
  sortOrder?: 'asc' | 'desc';
  roles?: string[];
}

export const UserService = {
  getMe: async (): Promise<Response<CurrentUserInfo>> => {
    try {
      const response = await authApi.get<Response<CurrentUserInfo>>('/users/me');
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  getUsers: async (params: UserListParams): Promise<Response<UserListResponse>> => {
    try {
      const response = await authApi.get('/users', { params });
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  createUser: async (data: CreateUserFormRequest): Promise<Response<User>> => {
    try {
      const response = await authApi.post<Response<User>>('/users', data);
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  login: async (username: string, password: string): Promise<Response<Auth>> => {
    try {
      const response = await api.post<Response<Auth>>('/auth/login', { username, password });
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  logout: async (): Promise<Response<void>> => {
    try {
      const response = await api.post<Response<void>>('/auth/logout');
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  refreshAccessToken: async (): Promise<Response<RefreshAccessToken>> => {
    const response = await api.post<Response<RefreshAccessToken>>('/auth/token/refresh', {
      withCredentials: true,
    });
    return response.data;
  },

  getUser: async (userId: string): Promise<Response<UserDetailType>> => {
    try {
      const response = await authApi.get<Response<UserDetailType>>(`/users/${userId}`);
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  updateUser: async (userId: string, data: EditUserFormRequest): Promise<Response<User>> => {
    try {
      const response = await authApi.patch<Response<User>>(`/users/${userId}`, data);
      return response.data;
    } catch (error: unknown) {
      return handleApiError(error);
    }
  },

  patchUser: async (userId: string, data: EditUserFormRequest) => {
    const endpoint = `/users`;
    return patchEntity<User, EditUserFormRequest>(endpoint, data, userId);
  },

  updateUserStatus: async (id: string, data: Pick<EditUserFormRequest, 'status'>) => {
    const endpoint = "users";
    const postData: Pick<EditUserFormRequest, 'status'> = {
      status: data.status ?? '',      
    };
    return patchEntity<User, Pick<EditUserFormRequest, 'status'>>(endpoint, postData, id);
  },
};
