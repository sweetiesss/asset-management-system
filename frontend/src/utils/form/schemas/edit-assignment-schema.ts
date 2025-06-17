import { z } from 'zod';
import { MESSAGE, RANGE } from '@/configs/constants';
import { uuidValidator } from '@/utils/validations/validators';
import { isBefore } from 'date-fns';
import { isTodayOrFuture } from '@/utils/dateUtils';

export type AssignmentUpdateFormRequest = {
  userId: string;
  assetId: string;
  assignedDate: string;
  note: string;
  version: number;
};

export const defaultValues: AssignmentUpdateFormRequest = {
  userId: '',
  assetId: '',
  assignedDate: '',
  note: '',
  version: 0,
};

export const buildAssignDateValidator = (oldDate: Date) =>
  z.string().refine(
    (val) => {
      const date = new Date(val);
      return !isBefore(date, oldDate) || isTodayOrFuture(date);
    },
    {
      message: MESSAGE.ASSIGNED_DATE.BEFORE_OLD_DATE,
    }
  );
export const buildUpdateAssignmentSchema = (oldAssignedDate: Date) =>
  z.object({
    userId: uuidValidator,
    assetId: uuidValidator,
    assignedDate: buildAssignDateValidator(oldAssignedDate),
    note: z.string().max(RANGE.NOTE.MAX, { message: MESSAGE.NOTE.TOO_LONG }),
    version: z.number().int().min(0),
  });
