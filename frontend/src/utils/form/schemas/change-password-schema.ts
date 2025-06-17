import { passwordValidator } from '@/utils/validations/validators';
import * as z from 'zod';

export type ChangePasswordRequest = {
  userId: string;
  oldPassword: string;
  newPassword: string;
};

export const changePasswordSchema = z
  .object({
    oldPassword: z.string().optional(),
    newPassword: passwordValidator,
    changePasswordRequired: z.boolean(),
  })
  .refine(
    (data) => {
      // Check if oldPassword is provided in normal change password flow
      return data.changePasswordRequired || data.oldPassword !== undefined;
    },
    {
      message: 'Please enter your old password',
      path: ['oldPassword'],
    }
  );