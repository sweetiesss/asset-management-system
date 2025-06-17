import { BaseModalProp } from '@/hooks/useModal';
import { Modal } from './Modal';
import Spinner from '../Spinner';
import { toast } from 'react-toastify';
import { useEffect, useState } from 'react';
import ReloadModal from './ReloadModal';
import { usePatchUserStatus } from '@/hooks/fetch/useUsers';

type DeletionProps = BaseModalProp & {
  id: string;
  onSuccess?: () => void;
};

const DeletionConfirmation: React.FC<DeletionProps> = ({
  visible,
  openModal,
  closeModal,
  id,
  onSuccess,
}) => {
  const { triggerUpdate, isUpdating, updateError } = usePatchUserStatus(id);
  const [modalIndex, setModalIndex] = useState(0);
  const handleDisableUser = async () => {
    const data = { status: 'INACTIVE' };
    await triggerUpdate(data);
    if (!updateError) {
      onSuccess?.();
      toast.success('User disabled successfully.');
      closeModal();
    }
  };

  useEffect(() => {
    if (updateError) {
      switch (updateError.code) {
        case 'USER_CAN_NOT_BE_DISABLED':
          setModalIndex(1);
          break;
        case 'USER_NOT_FOUND':
          setModalIndex(2);
          break;
        default:
          toast.error('An error occurred while disabling the user.');
      }
    }
  }, [updateError]);

  return (
    <>
      {modalIndex === 0 && (
        <AreYouSure
          visible={visible}
          openModal={openModal}
          closeModal={closeModal}
          isDisable={isUpdating}
          handleDisableUser={handleDisableUser}
        />
      )}
      {modalIndex === 1 && (
        <UserCanNotBeDisabled visible={visible} openModal={openModal} closeModal={closeModal} />
      )}
      {modalIndex === 2 && (
        <ReloadModal visible={visible} openModal={openModal} closeModal={closeModal} />
      )}
    </>
  );
};

interface AreYouSureProps extends BaseModalProp {
  isDisable: boolean;
  handleDisableUser: () => void;
}

const AreYouSure: React.FC<AreYouSureProps> = ({
  visible,
  openModal,
  closeModal,
  isDisable,
  handleDisableUser,
}) => {
  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      maxWidth='max-w-2xl'
      title='Are you sure?'
      closeBtnEnabled={false}
      primaryAction={{
        label: (
          <div className='flex items-center justify-center gap-x-0.5'>
            {' '}
            {/* Ensured centering for spinner */}
            {isDisable && <Spinner />}
            Disable
          </div>
        ),
        onClick: handleDisableUser,
        type: 'button',
        variant: 'destructive',
        disabled: isDisable,
      }}
      secondaryAction={{
        label: 'Cancel',
        onClick: closeModal,
        type: 'button',
        variant: 'outline',
        disabled: isDisable,
      }}
    >
      <div className='px-5'>Do you want to disable this user?</div>
    </Modal>
  );
};

const UserCanNotBeDisabled: React.FC<BaseModalProp> = ({ visible, openModal, closeModal }) => {
  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      title='Cannot Disable user'
      closeBtnEnabled={true}
      maxWidth='max-w-2xl'
    >
      <div className='flex items-center justify-center gap-x-0.5 p-5'>        
          There are valid assignments belonging to this user. Please close all assignments before
          disabling user.        
      </div>
    </Modal>
  );
};

export default DeletionConfirmation;