import { MESSAGE, RANGE } from '@/configs/constants';
import { assignDateValidator, uuidValidator } from '@/utils/validations/validators';
import { z } from 'zod';

export type AssignmentFormRequest = {
  userId: string;
  assetId: string;
  assignedDate: string;
  note: string;
};

export const defaultValues: AssignmentFormRequest = {
  userId: '',
  assetId: '',
  assignedDate: new Date().toISOString().split('T')[0],
  note: '',
};
export const createAssignmentSchema = z.object({
  userId: uuidValidator,
  assetId: uuidValidator,
  assignedDate: assignDateValidator,
  note: z.string().max(RANGE.NOTE.MAX, { message: MESSAGE.NOTE.TOO_LONG }),
});
