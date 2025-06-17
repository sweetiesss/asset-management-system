import { AssetSelectPopup } from '@/components/AssetSelectPopup';
import Spinner from '@/components/Spinner';
import { UserSelectPopup } from '@/components/UserSelectPopup';
import { DateField } from '@/components/form/DateField';
import { InputField } from '@/components/form/InputField';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { Label } from '@/components/ui/label';
import { RANGE } from '@/configs/constants';
import { Asset } from '@/types/asset';
import { UserTableItem } from '@/types/type';
import {
  AssignmentUpdateFormRequest,
  buildUpdateAssignmentSchema,
} from '@/utils/form/schemas/edit-assignment-schema';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, useParams } from 'react-router-dom';
import { AssignmentEditView } from '@/types/assignment';
import { toast } from 'react-toastify';
import { AssignmentFormRequest } from '@/utils/form/schemas/create-assignment-schema';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Portal } from '@radix-ui/react-portal';
import useModal from '@/hooks/useModal';
import { useGetAssignmentBasic, useUpdateAssignment } from '@/hooks/fetch/assignment';
import ReloadModal from '@/components/modal/ReloadModal';

export const UpdateAssignmentPage = () => {
  const { assignmentId } = useParams();
  const { data: assignment, isLoading } = useGetAssignmentBasic(assignmentId!);

  return (
    <div>
      {isLoading && <Loading />}
      {assignment && <UpdateAssignmentForm assignment={assignment!} />}
    </div>
  );
};

interface UpdateAssignmentFormProps {
  assignment: AssignmentEditView;
}

const UpdateAssignmentForm: React.FC<UpdateAssignmentFormProps> = ({ assignment }) => {
  const navigate = useNavigate();
  const [lastSelectedUser, setLastSelectedUser] = useState<UserTableItem | null>();
  const [lastSelectedAsset, setLastSelectedAsset] = useState<Asset<'table'> | null>();
  const { triggerUpdate, updateError, isUpdating } = useUpdateAssignment(assignment.id);
  const [modal, setModal] = useState(0);
  const { isModalVisible, openModal, closeModal } = useModal(false);

  const schema = buildUpdateAssignmentSchema(new Date(assignment.assignedDate));
  const form = useForm<AssignmentUpdateFormRequest>({
    resolver: zodResolver(schema),
    defaultValues: {
      userId: assignment.user.id,
      assetId: assignment.asset.id,
      assignedDate: assignment.assignedDate,
      note: assignment.note,
      version: assignment.version,
    },
    mode: 'onChange',
  });
  const { setValue } = form;

  useEffect(() => {
    if (assignment) {
      setLastSelectedUser(assignment.user as UserTableItem);
      setLastSelectedAsset(assignment.asset as Asset<'table'>);
    }
  }, [assignment]);

  const isFormValid = form.formState.isValid;
  const onSubmit = async (data: AssignmentFormRequest) => {
    const response = await triggerUpdate(data);
    if (response) {
      toast.success('Assignment updated successfully');
      closeModal();
      setModal(0);
      navigate('/assignments', { replace: true, state: { updatedAssignment: response } });
      return;
    }
  };

  useEffect(() => {
    if (updateError) {
      console.error('Update error:', updateError);
      switch (updateError.code) {
        case 'ASSIGNMENT_NOT_FOUND':
          setModal(2);
          break;
        case 'ASSIGNMENT_NOT_EDITABLE':
          setModal(2);
          break;
        case 'ASSIGNMENT_BEING_MODIFIED':
          setModal(2);
          break;
        case 'ASSET_NOT_AVAILABLE':
          setModal(2);
          break;
        case 'USER_IS_DISABLED':
          setModal(3);
          break;
        default:
          toast.error('An error occurred while updating the assignment.');
      }
    }
  }, [updateError]);

  const onUserSelectChange = (newUser: UserTableItem) => {
    setLastSelectedUser(newUser);
    setValue('userId', newUser.id, { shouldValidate: true });
  };
  const onAssetSelectChange = (newAsset: Asset<'table'>) => {
    setLastSelectedAsset(newAsset);
    setValue('assetId', newAsset.id, { shouldValidate: true });
  };
  const currentDate = new Date();
  const invalidDate =
    new Date(assignment.assignedDate) <= currentDate
      ? new Date(assignment.assignedDate)
      : currentDate;

  return (
    <>
      <div className='max-w-md p-6 mx-auto overflow-scroll bg-white rounded-lg shadow-sm'>
        <h1 className='mb-5 text-2xl font-bold text-primary'>Edit Assignment</h1>
        <Form {...form}>
          <form className='space-y-4'>
            <Label>User</Label>

            <UserSelectPopup
              selectedUser={lastSelectedUser}
              onUserSelectChange={onUserSelectChange}
            />

            <Label>Asset</Label>

            <AssetSelectPopup
              selectedAsset={lastSelectedAsset}
              onAssetSelectChange={onAssetSelectChange}
            />

            <DateField
              name='assignedDate'
              label='Assigned Date'
              control={form.control}
              min={invalidDate.toISOString().split('T')[0]}
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
                disabled={!isFormValid || isUpdating}
                className='text-white cursor-pointer bg-primary hover:bg-primary-600'
                onClick={() => {
                  setModal(1);
                  openModal();
                }}
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
      {isModalVisible && modal === 1  && (
        <Dialog open={isModalVisible} onOpenChange={openModal}>
          <Portal>
            <div className='fixed inset-0 z-40 bg-black/50' onClick={closeModal} />
            <DialogContent className='fixed z-50 p-6 -translate-x-1/2 -translate-y-1/2 bg-white rounded-lg top-1/2 left-1/2 w-96'>
              <DialogHeader>
                <DialogTitle>Are you sure?</DialogTitle>
              </DialogHeader>
              <p className='mb-4'>Your changes will be applied to the assignment.</p>
              <div className='flex justify-end gap-3'>
                <Button onClick={closeModal} variant='outline'>
                  Cancel
                </Button>
                <Button onClick={form.handleSubmit(onSubmit)} disabled={isUpdating}>
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
      {isModalVisible && modal === 2 && (
        <ReloadModal visible={isModalVisible} openModal={openModal} closeModal={closeModal} />
      )}
      {isModalVisible && modal === 3 && (
        <ReloadModal visible={isModalVisible} openModal={openModal} closeModal={closeModal} redirect='/assignments' />
      )}
    </>
  );
};

const Loading = () => (
  <div className='max-w-md p-6 mx-auto bg-white rounded-lg shadow-sm min-h-100'>
    <h1 className='mb-5 text-2xl font-bold text-primary'>Loading...</h1>
  </div>
);