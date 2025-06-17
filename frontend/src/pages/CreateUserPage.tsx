import { useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import {
  createUserSchema,
  defaultValues,
  CreateUserFormRequest,
} from '@/utils/form/schemas/create-user-schema';
import { User, UserType } from '@/types/type';
import { InputField } from '@/components/form/InputField';
import { DateField } from '@/components/form/DateField';
import { GenderField } from '@/components/form/GenderField';
import { SelectField, SelectItem } from '@/components/form/SelectField';
import { useCreateUsers } from '@/hooks/fetch/useUsers';
import { FieldError } from '@/types/dto';
import { useNavigate } from 'react-router-dom';
import { RANGE } from '@/configs/constants';
import Spinner from '@/components/Spinner';
import capitalizeEachWord from '@/utils/capitilizeUtils';
const locations: SelectItem[] = [
  { value: 'HCM', label: 'Ho Chi Minh' },
  { value: 'HN', label: 'Ha Noi' },
  { value: 'DN', label: 'Da Nang' },
];
export function CreateUserPage() {
  const form = useForm<CreateUserFormRequest>({
    resolver: zodResolver(createUserSchema),
    defaultValues: defaultValues,
    mode: 'onChange',
  });
  const selectedUserType = form.watch('type');
  const isFormValid = form.formState.isValid;
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { createUser } = useCreateUsers();
  const navigate = useNavigate();

  const onSubmit = async (data: CreateUserFormRequest) => {
    setIsSubmitting(true);
    try {
      const response = await createUser(data);
      if (response.success) {
        const createdUser = response.data as User;
        navigate('/users', { replace: true, state: { updatedUser: createdUser } });
      } else {
        const errors = response.error?.details as FieldError[];

        errors?.forEach((error) => {
          form.setError(error.field as keyof CreateUserFormRequest, {
            type: 'manual',
            message: error.message,
          });
        });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const userTypeSelectItems: SelectItem[] = useMemo(() => {
    return Object.entries(UserType).map(([key, value]) => ({
      value,
      label: capitalizeEachWord(key),
    }));
  }, []);

  useEffect(() => {
    const subscription = form.watch((_, { name }) => {
      if (name === 'dateOfBirth' && form.getValues('joinedOn') != '') {
        form.trigger('joinedOn');
        form.setValue('joinedOn', form.getValues('joinedOn'), {
          shouldValidate: true,
          shouldDirty: true,
        });
      }
    });
    return () => subscription.unsubscribe();
  }, [form]);

  return (
    <div className='mx-auto max-w-md rounded-lg bg-white p-6 shadow-sm'>
      <h1 className='text-primary mb-5 text-2xl font-bold'>Create New User</h1>

      <span className='block w-full text-right text-sm text-red-500'>*All fields are required</span>
      <Form {...form}>
        <form className='space-y-4'>
          <InputField
            maxLength={RANGE.FIRST_NAME.MAX}
            name='firstName'
            label='First Name'
            control={form.control}
          />
          <InputField
            maxLength={RANGE.LAST_NAME.MAX}
            name='lastName'
            label='Last Name'
            control={form.control}
          />

          <DateField
            name='dateOfBirth'
            label='Date of Birth'
            control={form.control}
            max={new Date().toISOString().split('T')[0]}
          />

          <GenderField name='gender' label='Gender' control={form.control} />

          <DateField name='joinedOn' label='Joined Date' control={form.control} />

          <SelectField
            name='type'
            label='Type'
            defaultValue='STAFF'
            control={form.control}
            items={userTypeSelectItems}
          />
          {selectedUserType === 'ADMIN' && (
            <SelectField
              name='locationCode'
              label='Location'
              defaultValue='HCM'
              control={form.control}
              items={locations}
            />
          )}

          <div className='flex justify-end gap-4 pt-4'>
            <Button
              type='button'
              disabled={!isFormValid || isSubmitting}
              className='bg-primary hover:bg-primary-600 cursor-pointer text-white'
              onClick={form.handleSubmit(onSubmit)}
            >
              {isSubmitting && <Spinner />}
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
}
