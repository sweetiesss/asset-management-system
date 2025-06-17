import { isWeekend } from 'date-fns';
import { calculateAge, isDate, isDateInFuture, isTodayOrFuture } from '@/utils/dateUtils';
import * as z from 'zod';
import { Gender, UserType } from '@/types/type';
import { RANGE, MESSAGE } from '@/configs/constants';
import { ReportEntity } from '@/types/report';

export const firstNameValidator: z.ZodString = z
  .string()
  .min(1, { message: '' })
  .regex(/^[a-zA-Z\s]+$/, { message: MESSAGE.FIRST_NAME.INVALID_FORMAT });

export const lastNameValidator: z.ZodString = z
  .string({})
  .min(1, { message: '' })
  .regex(/^[a-zA-Z\s]+$/, { message: MESSAGE.LAST_NAME.INVALID_FORMAT });

export const genderValidator = z.nativeEnum(Gender);

export const genderStringValidator = z.string().refine((val) => [Gender.MALE.toString(), Gender.FEMALE.toString()].includes(val));

export const userTypeValidator = z.nativeEnum(UserType);

export const dobValidator = z
  .string()
  .min(1, { message: MESSAGE.DOB.INVALID })
  .refine((val) => isDate(new Date(val)), { message: MESSAGE.DOB.INVALID })
  .refine((val) => !isDateInFuture(new Date(val)), { message: MESSAGE.DOB.FUTURE })
  .refine((val) => calculateAge(new Date(val)) > RANGE.DOB.MIN_AGE, {
    message: MESSAGE.DOB.UNDERAGE,
  });

export const joinedAtValidator = z
  .string()
  .min(1, { message: MESSAGE.JOINED_AT.INVALID })
  .refine((val) => isDate(new Date(val)), { message: MESSAGE.JOINED_AT.INVALID })
  .refine((val) => !isWeekend(new Date(val)), { message: MESSAGE.JOINED_AT.WEEKEND });

export const passwordValidator = z
  .string()
  .min(RANGE.PASSWORD.MIN, { message: MESSAGE.PASSWORD.WEAK })
  .max(RANGE.PASSWORD.MAX, { message: MESSAGE.PASSWORD.WEAK })
  .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/, {
    message: MESSAGE.PASSWORD.WEAK,
  });

export const assetNameValidator = z
  .string()
  .min(RANGE.ASSET_NAME.MIN, { message: MESSAGE.ASSET_NAME.TOO_SHORT })
  .max(RANGE.ASSET_NAME.MAX, { message: MESSAGE.ASSET_NAME.TOO_LONG })
  .regex(/^[a-zA-Z0-9\s]+$/, { message: MESSAGE.ASSET_NAME.INVALID_FORMAT });

export const categoryNameValidator = z
  .string()
  .min(RANGE.CATEGORY_NAME.MIN, { message: MESSAGE.CATEGORY_NAME.TOO_SHORT })
  .max(RANGE.CATEGORY_NAME.MAX, { message: MESSAGE.CATEGORY_NAME.TOO_LONG })
  .regex(/^[a-zA-Z0-9\s]+$/, { message: MESSAGE.CATEGORY_NAME.INVALID_FORMAT });

export const categoryPrefixValidator = z
  .string()
  .length(RANGE.CATEGORY_PREFIX.LENGTH, { message: MESSAGE.CATEGORY_PREFIX.LENGTH })
  .regex(/^[A-Z]+$/, { message: MESSAGE.CATEGORY_PREFIX.INVALID_FORMAT });

export const assignDateValidator = z
  .string()
  .refine((val) => isDate(new Date(val)), { message: MESSAGE.ASSIGNED_DATE.INVALID })
  .refine((val) => isTodayOrFuture(new Date(val)), { message: MESSAGE.ASSIGNED_DATE.FUTURE });

export const uuidValidator = z
  .string({
    required_error: MESSAGE.UUID.REQUIRED,
    invalid_type_error: MESSAGE.UUID.INVALID_TYPE,
  })
  .uuid({ message: MESSAGE.UUID.INVALID_FORMAT });

export const installedDateValidator = z
  .string()
  .min(1, { message: MESSAGE.DOB.INVALID })
  .refine((val) => isDate(new Date(val)), { message: MESSAGE.DOB.INVALID });

export const reportFileNameValidator = z.string().min(1, { message: MESSAGE.REPORT_FILENAME.INVALID }).max(100, { message: MESSAGE.REPORT_FILENAME.TOO_LONG })

export const reportEntityValidator = z.string().refine((val) =>
    Object.values(ReportEntity)
      .map((v) => v.toString())
      .includes(val)
  )
export const reportStartDateValidator = z
  .string()
  .min(1, { message: MESSAGE.REPORT_START_DATE.INVALID })
  .refine((val) => isDate(new Date(val)), { message: MESSAGE.REPORT_START_DATE.INVALID })
  .refine((val) => !isDateInFuture(new Date(val)), { message: MESSAGE.REPORT_START_DATE.FUTURE });

export const reportEndDateValidator = z
  .string()
  .min(1, { message: MESSAGE.REPORT_END_DATE.INVALID })
  .refine((val) => isDate(new Date(val)), { message: MESSAGE.REPORT_END_DATE.INVALID })
  .refine((val) => !isDateInFuture(new Date(val)), { message: MESSAGE.REPORT_END_DATE.FUTURE })

