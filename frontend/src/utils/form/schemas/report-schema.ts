import { MESSAGE } from '@/configs/constants';
import { ReportEntity } from '@/types/report';
import { today, yesterday } from '@/utils/dateUtils';
import {
  reportFileNameValidator,
  reportStartDateValidator,
  reportEndDateValidator
} from '@/utils/validations/validators';
import z from 'zod';

const baseSchema = z.object({
  fileName: reportFileNameValidator,
  startDate: reportStartDateValidator,
  endDate: reportEndDateValidator
});

const assetSchema = baseSchema.extend({
  reportEntity: z.literal(ReportEntity.ASSETS),
  categoryIds: z.array(z.number()),
  states: z.array(z.string())
})

const userSchema = baseSchema.extend({
  reportEntity: z.literal(ReportEntity.USERS),
  types: z.array(z.string()),
});

const assignmentSchema = baseSchema.extend({
  reportEntity: z.literal(ReportEntity.ASSIGNMENTS),
  statusIds: z.array(z.number()),
});

export const createReportSchema = z.discriminatedUnion('reportEntity', [
  assetSchema,
  userSchema,
  assignmentSchema,
]).superRefine((data, ctx) => {
  if (data.endDate <= data.startDate) {
    ctx.addIssue({
      path: ['endDate'],
      code: z.ZodIssueCode.custom,
      message: MESSAGE.REPORT_END_DATE.BEFORE_START_DATE,
    });
  }
});

export type CreateReportFormRequest = z.infer<typeof createReportSchema>;

export const defaultValues: CreateReportFormRequest = {
  fileName: '',
  reportEntity: ReportEntity.ASSETS,
  startDate: yesterday(),
  endDate: today(),
  categoryIds: [],
  states: [],
};
