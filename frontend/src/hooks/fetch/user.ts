import { UserService } from '@/services/UserService';
import { usePatch } from '.';
import { User } from '@/types/type';
import { EditUserFormRequest } from '@/utils/form/schemas/edit-user-schema';
import { FieldError } from '@/types/dto';

export const usePatchUser = (userId: string) => {
  return usePatch<EditUserFormRequest, User, FieldError[]>(UserService.patchUser, userId, [
    `/users/${userId}`,
  ]);
};
