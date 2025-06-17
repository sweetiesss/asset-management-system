import { Suspense, useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { UserDetailType, UserType } from '@/types/type';
import { InputField } from '@/components/form/InputField';
import { DateField } from '@/components/form/DateField';
import { GenderField } from '@/components/form/GenderField';
import { SelectField, SelectItem } from '@/components/form/SelectField';
import { useUser } from '@/hooks/fetch/useUsers';
import { useNavigate, useParams } from 'react-router-dom';
import { RANGE } from '@/configs/constants';
import { EditUserFormRequest, editUserSchema } from '@/utils/form/schemas/edit-user-schema';
import Spinner from '@/components/Spinner';
import { toast } from 'react-toastify';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Portal } from '@radix-ui/react-portal';
import useModal from '@/hooks/useModal';
import capitalizeEachWord from '@/utils/capitilizeUtils';
import { usePatchUser } from '@/hooks/fetch/user';
import ReloadModal from '@/components/modal/ReloadModal';

const EditUserPage = () => {
  const { userId } = useParams();
  const { user, isLoading, isError, error } = useUser(userId);

  if (isLoading) {
    return <Loading />;
  }
  if (isError) {
    return <ErrorFallback error={error} />;
  }
  return (
    <Suspense fallback={<Loading />}>
      {user && userId && <EditUser userId={userId} user={user} />}
    </Suspense>
  );
};
const locations: SelectItem[] = [
  { value: 'HCM', label: 'Ho Chi Minh' },
  { value: 'HN', label: 'Ha Noi' },
  { value: 'DN', label: 'Da Nang' },
];
const EditUser = ({ userId, user }: { userId: string; user: UserDetailType }) => {
  // const { updateUser } = useUpdateUser(userId);
  const form = useForm<EditUserFormRequest>({
    resolver: zodResolver(editUserSchema),
    defaultValues: {
      firstName: user.firstName,
      lastName: user.lastName,
      dateOfBirth: user.dateOfBirth,
      joinedOn: user.joinedOn,
      gender: user.gender,
      type: user.types[0].name === 'STAFF' ? UserType.STAFF : UserType.ADMIN,
      locationCode: user.location.code,
      version: user.version,
    },
    mode: 'onChange',
  });

  const { isModalVisible, openModal, closeModal } = useModal(false);
  const [modal, setModal] = useState(0);

  const { triggerUpdate, updateError, isUpdating } = usePatchUser(userId);
  const navigate = useNavigate();
  const selectedUserType = form.watch('type');
  const isFormValid = form.formState.isValid;

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

  useEffect(() => {
    if (selectedUserType === UserType.STAFF) {
      form.setValue('locationCode', undefined);
    }
  }, [selectedUserType, form]);

  const onSubmit = async (data: EditUserFormRequest) => {
    const updatedUser = await triggerUpdate(data);
    if (updatedUser) {
      toast.success(`User ${data.firstName} ${data.lastName} updated successfully.`);
      closeModal();
      setModal(0);
      navigate('/users', { replace: true, state: { updatedUser: updatedUser } });
      return;
    }
  };

  useEffect(() => {
    if (updateError) {
      switch (updateError.code) {
        case 'USER_NOT_FOUND':
          setModal(2);
          break;
        case 'USER_NOT_EDITABLE':
          setModal(2);
          break;
        case 'USER_BEING_MODIFIED':
          setModal(2);
          break;
        case 'USER_IS_DISABLED':
          setModal(1);
          break;
        default:
          toast.error('An error occurred while updating the user.');
      }
    }
  }, [updateError]);

  const userTypeSelectItems: SelectItem[] = useMemo(() => {
    return Object.entries(UserType).map(([key, value]) => ({
      value,
      label: capitalizeEachWord(key),
    }));
  }, []);

  return (
    <>
      <div className='max-w-md p-6 mx-auto bg-white rounded-lg shadow-sm'>
        <h1 className='mb-5 text-2xl font-bold text-primary'>Edit User</h1>
        <Form {...form}>
          <form className='space-y-4'>
            <InputField
              maxLength={RANGE.FIRST_NAME.MAX}
              label='First Name'
              name='firstName'
              control={form.control}
              disabled
            />
            <InputField
              maxLength={RANGE.LAST_NAME.MAX}
              name='lastName'
              label='Last Name'
              control={form.control}
              disabled
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
              control={form.control}
              items={userTypeSelectItems}
            />

            <SelectField
              name='locationCode'
              label='Location'
              control={form.control}
              items={locations}
            />

            <div className='flex justify-end gap-4 pt-4'>
              <Button
                type='button'
                disabled={!isFormValid || isUpdating}
                className='text-white bg-primary'
                onClick={() => openModal()}
              >
                {isUpdating && <Spinner />}
                <span>Save</span>
              </Button>
              <Button type='button' variant='outline' onClick={() => navigate(-1)}>
                Cancel
              </Button>
            </div>
          </form>
        </Form>
      </div>

      {/* Confirmation Modal */}
      {isModalVisible && (
        <Dialog open={isModalVisible} onOpenChange={openModal}>
          <Portal>
            <div className='fixed inset-0 z-40 bg-black/50' onClick={closeModal} />
            <DialogContent className='fixed z-50 p-6 -translate-x-1/2 -translate-y-1/2 bg-white rounded-lg top-1/2 left-1/2 w-96'>
              <DialogHeader>
                <DialogTitle>Are you sure?</DialogTitle>
              </DialogHeader>
              <p className='mb-4'>Your changes will be applied to the user.</p>
              <div className='flex justify-end gap-3'>
                <Button onClick={closeModal} variant='outline'>
                  Cancel
                </Button>
                <Button onClick={form.handleSubmit(onSubmit)} disabled={isUpdating} type='button'>
                  {isUpdating ? (
                    <span className='flex gap-1'>
                      <Spinner /> Confirm
                    </span>
                  ) : (
                    'Confirm'
                  )}
                </Button>
              </div>
            </DialogContent>
          </Portal>
        </Dialog>
      )}

      {isModalVisible && modal === 1 && (
        <ReloadModal visible={isModalVisible} openModal={openModal} closeModal={closeModal} redirect='/users' />
      )}
      {isModalVisible && modal === 2 && (
        <ReloadModal visible={isModalVisible} openModal={openModal} closeModal={closeModal} />
      )}
    </>
  );
};

const Loading = () => (
  <div className='max-w-md p-6 mx-auto bg-white rounded-lg shadow-sm min-h-100'>
    <h1 className='mb-5 text-2xl font-bold text-primary'>Loading...</h1>
  </div>
);

const ErrorFallback = ({ error }: { error: string }) => (
  <div className='max-w-md p-6 mx-auto bg-white rounded-lg shadow-sm'>
    <h1 className='mb-5 text-2xl font-bold text-primary'>{error}</h1>
  </div>
);

export default EditUserPage;
