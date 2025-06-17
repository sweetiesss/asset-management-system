import React, { useEffect, useState } from 'react';
import { BaseModalProp } from '@/hooks/useModal';
import Spinner from '@/components/Spinner';
import { Modal } from './Modal';
import { useDeleteAssignment } from '@/hooks/fetch/assignment';
import { toast } from 'react-toastify';
import ReloadModal from './ReloadModal';
import { Link } from 'react-router-dom';
import { KeyedMutator } from 'swr';
import { Page } from '@/types/type';
import { Assignment } from '@/types/assignment';

type DeletionProps = BaseModalProp & {
  id: string;
  cache: KeyedMutator<Page<Assignment<'table'>>>;
};

const DeletionConfirmation: React.FC<DeletionProps> = ({
  visible,
  openModal,
  closeModal,
  id,
  cache,
}) => {
  const { triggerDelete, isDeleting, deleteError } = useDeleteAssignment(id);
  const [modalIndex, setModalIndex] = useState(0);
  const handleDeleteAssignment = async () => {
    await triggerDelete();
    if (!deleteError) {
      await cache();
      toast.success('Assignment deleted successfully.');
      closeModal();
    }
  };

  useEffect(() => {
    if (deleteError) {
      switch (deleteError.code) {
        case 'ASSIGNMENT_NOT_DELETABLE':
          setModalIndex(1);
          break;
        case 'ASSIGNMENT_NOT_FOUND':
          setModalIndex(2);
          break;
        default:
          toast.error('An error occurred while deleting the assignment.');
      }
    }
  }, [deleteError]);

  return (
    <>
      {modalIndex === 0 && (
        <AreYouSure
          visible={visible}
          openModal={openModal}
          closeModal={closeModal}
          isDeleting={isDeleting}
          handleDeleteAssignment={handleDeleteAssignment}
        />
      )}
      {modalIndex === 1 && (
        <AssignmentNotDeletable visible={visible} openModal={openModal} closeModal={closeModal} />
      )}
      {modalIndex === 2 && (
        <ReloadModal visible={visible} openModal={openModal} closeModal={closeModal} />
      )}
    </>
  );
};

interface AreYouSureProps extends BaseModalProp {
  isDeleting: boolean;
  handleDeleteAssignment: () => Promise<void>;
}

const AreYouSure: React.FC<AreYouSureProps> = ({
  visible,
  openModal,
  closeModal,
  isDeleting,
  handleDeleteAssignment,
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
            {isDeleting && <Spinner />}
            Delete
          </div>
        ),
        onClick: handleDeleteAssignment,
        type: 'button',
        variant: 'destructive',
        disabled: isDeleting,
      }}
      secondaryAction={{
        label: 'Cancel',
        onClick: closeModal,
        type: 'button',
        variant: 'outline',
        disabled: isDeleting,
      }}
    >
      <div className='px-5'>Do you want to delete this assignment?</div>
    </Modal>
  );
};

const AssignmentNotDeletable: React.FC<BaseModalProp> = ({ visible, openModal, closeModal }) => {
  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      title='Cannot Delete Assignment'
      closeBtnEnabled={true}
    >
      <div>
        <p>The assignment cannot be deleted because it has already been accepted or declined.</p>
        <p>
          <Link to={`/assignments`} className='text-blue-500 underline-offset-4'>
            Assignment page
          </Link>
        </p>
      </div>
    </Modal>
  );
};

export default DeletionConfirmation;
