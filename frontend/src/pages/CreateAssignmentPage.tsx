import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { Label } from '@/components/ui/label';
import { DateField } from '@/components/form/DateField';
import { InputField } from '@/components/form/InputField';
import { UserSelectPopup } from '@/components/UserSelectPopup';
import { AssetSelectPopup } from '@/components/AssetSelectPopup';
import Spinner from '@/components/Spinner';
import ReloadModal from '@/components/modal/ReloadModal';

import useModal from '@/hooks/useModal';
import { usePostAssignment } from '@/hooks/fetch/assignment';
import {
  AssignmentFormRequest,
  createAssignmentSchema,
  defaultValues,
} from '@/utils/form/schemas/create-assignment-schema';
import { RANGE } from '@/configs/constants';
import { UserTableItem } from '@/types/type';
import { Asset } from '@/types/asset';

const CreateAssignmentPage = () => {
  const { triggerPost, isPosting, postError } = usePostAssignment();
  const navigate = useNavigate();
  const [scenario, setScenario] = useState(1); // 1: normal, 2: reload error
  const [modal, setModal] = useState(0);
  const { isModalVisible, openModal, closeModal } = useModal(false);
  const [lastSelectedUser, setLastSelectedUser] = useState<UserTableItem | null>(null);
  const [lastSelectedAsset, setLastSelectedAsset] = useState<Asset<'table'> | null>(null);

  const form = useForm<AssignmentFormRequest>({
    resolver: zodResolver(createAssignmentSchema),
    defaultValues,
    mode: 'onChange',
  });

  const isFormValid = form.formState.isValid;
  const { setValue } = form;

  const onSubmit = async (data: AssignmentFormRequest) => {
    const newAssignment = await triggerPost(data);
    if (newAssignment) {
      closeModal();
      setModal(0);
      navigate('/assignments', { replace: true, state: { updatedAssignment: newAssignment } });
    }
  };

  const onUserSelectChange = (newUser: UserTableItem) => {
    setLastSelectedUser(newUser);
    setValue('userId', newUser.id, { shouldValidate: true });
  };

  const onAssetSelectChange = (newAsset: Asset<'table'>) => {
    setLastSelectedAsset(newAsset);
    setValue('assetId', newAsset.id, { shouldValidate: true });
  };

  useEffect(() => {
    if (postError) {
      switch (postError.code) {
        case 'ASSET_NOT_AVAILABLE':
        case 'OPTIMISTIC_LOCKING_FAILURE':
          setModal(2);
          openModal();
          setScenario(2);
          break;
        case 'USER_IS_DISABLED':
          setModal(3);
          openModal();
          break;
        default:
          toast.error('An unexpected error occurred.');
      }
    }
  }, [postError]);

  return (
    <div className='flex items-center justify-center w-full'>
      {scenario === 1 && (
        <div className='max-w-md p-6 mx-auto overflow-scroll bg-white rounded-lg shadow-sm'>
          <h1 className='mb-5 text-2xl font-bold text-primary'>Create New Assignment</h1>
          <Form {...form}>
            <form className='space-y-4'>
              <Label>
                User<span className='block w-full text-sm text-left text-red-500'>*</span>
              </Label>
              <UserSelectPopup
                selectedUser={lastSelectedUser}
                onUserSelectChange={onUserSelectChange}
              />

              <Label>
                Asset<span className='block w-full text-sm text-left text-red-500'>*</span>
              </Label>
              <AssetSelectPopup
                selectedAsset={lastSelectedAsset}
                onAssetSelectChange={onAssetSelectChange}
              />

              <DateField
                name='assignedDate'
                label='Assigned Date'
                control={form.control}
                min={new Date().toISOString().split('T')[0]}
                required
              />

              <InputField
                name='note'
                label='Note'
                control={form.control}
                maxLength={RANGE.NOTE.MAX}
                rows={4}
                multiline
              />

              <div className='flex justify-end gap-4 pt-4'>
                <Button
                  type='button'
                  disabled={!isFormValid || isPosting}
                  className='text-white cursor-pointer bg-primary hover:bg-primary-600'
                  onClick={form.handleSubmit(onSubmit)}
                >
                  {isPosting && <Spinner />}
                  <span>Save</span>
                </Button>
                <Button type='button' variant='outline' onClick={() => navigate(-1)}>
                  Cancel
                </Button>
              </div>
            </form>
          </Form>
        </div>
      )}

      {scenario === 2 && <UnavailableToCreate />}

      {isModalVisible && modal === 2 && (
        <ReloadModal visible={isModalVisible} openModal={openModal} closeModal={closeModal} />
      )}

      {isModalVisible && modal === 3 && (
        <ReloadModal
          visible={isModalVisible}
          openModal={openModal}
          closeModal={closeModal}
          redirect='/assignments'
        />
      )}
    </div>
  );
};

export default CreateAssignmentPage;

const UnavailableToCreate = () => (
  <div className='max-w-md p-6 mx-auto bg-white rounded-lg shadow-sm'>
    <h1 className='mb-5 text-2xl font-bold text-primary'>Asset Not Available for Assignment</h1>
    <p className='text-gray-600'>
      The asset you selected is either not available or currently being modified.
    </p>
    <Link to='/assignments' className='text-blue-600 underline'>
      Return to Assignment Management
    </Link>
  </div>
);
