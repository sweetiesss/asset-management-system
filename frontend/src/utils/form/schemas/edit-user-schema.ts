import { User } from '@/types/type';
import {
  firstNameValidator,
  lastNameValidator,
  genderStringValidator,
  dobValidator,
  joinedAtValidator,
  userTypeValidator,
} from '@/utils/validations/validators';
import * as z from 'zod';
import { MESSAGE } from '@/configs/constants';

export type EditUserFormRequest = Pick<
  User,
  'firstName' | 'lastName' | 'dateOfBirth' | 'joinedOn' | 'type' | 'version'
> & {
  status?: string;
  locationCode?: string;
  gender: string;
};

export const editUserSchema = z
  .object({
    firstName: firstNameValidator,
    lastName: lastNameValidator,
    gender: genderStringValidator,
    dateOfBirth: dobValidator,
    joinedOn: joinedAtValidator,
    type: userTypeValidator,
    locationCode: z.string().optional(),
    version: z.number().int().min(0),
    status: z.string().optional(),
  })
  .refine(
    (data) => {
      if (data.dateOfBirth === '' || data.joinedOn === '') {
        return true;
      }
      const dob = new Date(data.dateOfBirth);
      const joinedAt = new Date(data.joinedOn);
      return joinedAt > dob;
    },
    {
      message: MESSAGE.JOINED_AT.JOINED_BEFORE_DOB,
      path: ['joinedOn'],
    }
  );
