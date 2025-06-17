import { UserListParams, UserService } from '@/services/UserService';
import { Response } from '@/types/dto';
import { User } from '@/types/type';
import { CreateUserFormRequest } from '@/utils/form/schemas/create-user-schema';
import { EditUserFormRequest } from '@/utils/form/schemas/edit-user-schema';
import useSWR from 'swr';
import { isUUID } from 'validator';
import { usePatch } from '.';

export function useCreateUsers() {
  const { mutate } = useSWR('users');

  const createUser = async (user: CreateUserFormRequest): Promise<Response<User>> => {
    const response = await UserService.createUser(user);
    mutate();
    return response;
  };

  return {
    createUser,
  };
}

export const useUsers = (params: UserListParams) => {
  const queryKey = ['users', params];

  const { data, error, isLoading, mutate } = useSWR(queryKey, () => UserService.getUsers(params));
  return {
    users: data?.data?.content ?? [],
    pagination: data?.data?.pageable,
    isLoading,
    isError: !!error,
    error: error?.message,
    mutate,
  };
};

export const useUser = (id?: string) => {
  const isValidId = typeof id === 'string' && isUUID(id);
  const key = isValidId ? ['users', id] as const : null;


   const {
    data: response,
    error,
    isLoading,
    isValidating,
    mutate,
  } = useSWR(
    key,
    // The fetcher receives the key tuple; we know uuid is defined when key !== null
    ([, uuid]) => UserService.getUser(uuid),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    user: response?.data ?? null,
    isLoading,
    isValidating,
    isError: !!error,
    error: error?.message,
    mutate,
  };
};

export const useUpdateUser = (id: string) => {
  const { mutate } = useSWR(['users', id]);

  const updateUser = async (user: EditUserFormRequest): Promise<Response<User>> => {
    const response = await UserService.updateUser(id, user);
    mutate();
    return response;
  };

  return {
    updateUser,
  };
}

export const usePatchUserStatus = (id: string) => {
  return usePatch<Pick<EditUserFormRequest, 'status'>, User>(UserService.updateUserStatus, id);
}