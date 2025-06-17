import { z } from 'zod';
import {
  firstNameValidator,
  lastNameValidator,
  genderStringValidator,
  dobValidator,
  joinedAtValidator,
  userTypeValidator,
} from "../../validations/validators"
import { User, UserType } from "../../../types/type"
import { MESSAGE } from '@/configs/constants';
export type CreateUserFormRequest = Pick<
  User,
  'firstName' | 'lastName' | 'dateOfBirth' | 'joinedOn' | 'type'
> & {
  locationCode?: string;
  gender: string;
};

export const defaultValues: CreateUserFormRequest = {
  firstName: '',
  lastName: '',
  gender: '',
  dateOfBirth: '',
  joinedOn: '',
  type: UserType.STAFF,
  locationCode: 'HCM'
};



export const createUserSchema = z
  .object({
    firstName: firstNameValidator,
    lastName: lastNameValidator,
    gender: genderStringValidator,
    dateOfBirth: dobValidator,
    joinedOn: joinedAtValidator,
    type: userTypeValidator,
    locationCode: z.string().optional(),
  })
  .refine(
    (data) => {
      if (!data.dateOfBirth || !data.joinedOn) return true;
      const dob = new Date(data.dateOfBirth);
      const joinedAt = new Date(data.joinedOn);
      return joinedAt > dob;
    },
    {
      message: MESSAGE.JOINED_AT.JOINED_BEFORE_DOB,
      path: ['joinedOn'],
    }
  )
  .refine(
    (data) => {
      if (data.type === UserType.ADMIN) {
        return !!data.locationCode;
      }
      return true;
    },
    {
      message: MESSAGE.LOCATION.REQUIRED_FOR_ADMIN,
      path: ['locationCode'],
    }
  );
