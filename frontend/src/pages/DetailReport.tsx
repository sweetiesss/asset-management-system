import { useGetCategories } from '@/hooks/fetch/categories';
import { AssetStateHandler } from '@/types/asset';
import React, { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';

import { useForm, UseFormReturn } from 'react-hook-form';
import { Form } from '@/components/ui/form';
import { EntityField } from '@/components/form/EntityField';
import { ReportEntity } from '@/types/report';
import { InputField } from '@/components/form/InputField';
import { toast } from 'react-toastify';
import { DateField } from '@/components/form/DateField';
import {
  CreateReportFormRequest,
  createReportSchema,
  defaultValues as defaultReportValues,
} from '@/utils/form/schemas/report-schema';
import SelectMultipleDropdown from '@/components/form/SelectMultiple';
import { Button } from '@/components/ui/button';
import { useGetCustomReport } from '@/hooks/fetch/report';
import Spinner from '@/components/Spinner';
import { useNavigate } from 'react-router-dom';
import { downloadBuilder } from '@/utils/downloadBuilder';
import { today, yesterday } from '@/utils/dateUtils';
import { UserTypes } from '@/types/user';
import { useGetAssignmentStatuses } from '@/hooks/fetch/assignment';

const CreateExport = () => {
  const navigate = useNavigate();
  const form = useForm<CreateReportFormRequest>({
    resolver: zodResolver(createReportSchema),
    defaultValues: defaultReportValues,
    mode: 'onChange',
  });

  const isFormValid = form.formState.isValid;
  const selectedEntity = form.watch('reportEntity');
  const { trigger, isMutating, blobUrl, fileName } = useGetCustomReport(selectedEntity);

  const onSubmit = async (data: CreateReportFormRequest) => {
    switch (data.reportEntity) {
      case ReportEntity.ASSETS: {
        await trigger({
          fileName: data.fileName,
          categoryIds: data.categoryIds,
          states: data.states,
          startDate: data.startDate,
          endDate: data.endDate,
        });
        break;
      }
      case ReportEntity.USERS: {
        await trigger({
          fileName: data.fileName,
          startDate: data.startDate,
          endDate: data.endDate,
          types: data.types,
        });
        break;
      }
      case ReportEntity.ASSIGNMENTS: {
        await trigger({
          fileName: data.fileName,
          startDate: data.startDate,
          endDate: data.endDate,
          statusIds: data.statusIds,
        });
        break;
      }
    }
  };

  useEffect(() => {
    if (blobUrl && fileName) {
      downloadBuilder(fileName, blobUrl);
    }
  }, [blobUrl, fileName]);

  return (
    <div color='max-w-md p-6 mx-auto bg-white rounded-lg shadow-sm'>
      <h1 className='text-primary mb-5 text-2xl font-bold'>Create Detailed Report</h1>
      <span className='block w-full text-right text-sm text-red-500'>*All fields are required</span>
      <Form {...form}>
        <form className='space-y-4'>
          <InputField name='fileName' label='Report Name' control={form.control} />
          <EntityField name='reportEntity' label='Report Type' control={form.control} inline={false}/>
          {selectedEntity === ReportEntity.ASSETS && <ExportAssetForm form={form} />}
          {selectedEntity === ReportEntity.USERS && <ExportUserForm form={form} />}
          {selectedEntity === ReportEntity.ASSIGNMENTS && <ExportAssignmentForm form={form} />}
          <DateField name='startDate' label='Start Date' control={form.control} max={yesterday()} />
          <DateField name='endDate' label='End Date' control={form.control} max={today()} />
          <div className='flex justify-end space-x-2 mt-4'>
            <Button
              type='button'
              disabled={!isFormValid}
              className='bg-primary hover:bg-primary-600 cursor-pointer text-white'
              onClick={form.handleSubmit(onSubmit)}
            >
              {isMutating && <Spinner />}
              <span>Save</span>
            </Button>
            <Button type='button' variant='outline' onClick={() => navigate(-1)}>
              Cancel
            </Button>
          </div>
        </form>
      </Form>
    </div>
  );
};

const ExportAssetForm: React.FC<{ form: UseFormReturn<CreateReportFormRequest> }> = ({ form }) => {
  const { data: categories, error } = useGetCategories();

  useEffect(() => {
    if (error) {
      toast.error(error.message);
    }
  }, [error]);

  return (
    <div className='space-y-4'>
      <div className='flex justify-around'>
        <SelectMultipleDropdown
          name='categoryIds'
          control={form.control}
          label='Category'
          options={categories ? categories.map((c) => ({ value: c.id, label: c.name })) : []}
        />
        <SelectMultipleDropdown
          name='states'
          control={form.control}
          label='State'
          options={AssetStateHandler.getOptions('all')}
        />
      </div>
    </div>
  );
};

const ExportUserForm: React.FC<{ form: UseFormReturn<CreateReportFormRequest> }> = ({ form }) => {
  return (
    <SelectMultipleDropdown name='types' control={form.control} label='Type' options={UserTypes} />
  );
};

const ExportAssignmentForm: React.FC<{ form: UseFormReturn<CreateReportFormRequest> }> = ({
  form,
}) => {
  const { data: statuses, error } = useGetAssignmentStatuses();

  useEffect(() => {
    if (error) {
      toast.error(error.message);
    }
  }, [error]);
  return (
    <SelectMultipleDropdown
      name='statusIds'
      control={form.control}
      label='State'
      options={statuses ? statuses.map((s) => ({ value: s.id, label: s.name })) : []}
    />
  );
};

export default CreateExport;
