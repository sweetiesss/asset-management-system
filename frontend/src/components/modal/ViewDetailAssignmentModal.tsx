import { FC } from 'react';
import { formatDate } from '@/utils/dateUtils';
interface ViewAssignmentDetailModalProps {
  visible: boolean;
  openModal: () => void;
  closeModal: () => void;
  assignmentId: string;
}

import { useEffect } from 'react';
import { useAssignmentDetail } from '@/hooks/fetch/useAssignment';
import { Assignment } from '@/types/assignment';
import { Modal } from './Modal';

const ViewAssignmentDetailModal: FC<ViewAssignmentDetailModalProps> = ({
  visible,
  openModal,
  closeModal,
  assignmentId,
}) => {
  const { assignment, isLoading, isError, error } = useAssignmentDetail(assignmentId);
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        closeModal();
      }
    };

    if (visible) {
      window.addEventListener('keydown', handleEsc);
    }

    return () => {
      window.removeEventListener('keydown', handleEsc);
    };
  }, [visible, closeModal]);

  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      title='Detailed Assignment Information'
      maxWidth='max-w-3xl'
    >
      <div className=''>
        {isLoading && <LoadingDisplay />}
        {isError && <ErrorDisplay error={error as string} />}
        {assignment && <AssignmentDetail assignment={assignment} />}
      </div>
    </Modal>
  );
};

const AssignmentDetail: FC<{ assignment: Assignment<'detail'> }> = ({ assignment }) => (
  <div className='pr-0 pb-5 pl-5'>
    <table className='w-full table-auto border-none'>
      <tbody>
        <tr>
          <td className='w-[40%] px-4 py-2 font-semibold'>Asset Code</td>
          <td className='w-[60%] px-4 py-2'>{assignment.assetCode}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Asset Name</td>
          <td className='px-4 py-2'>
            <div className='max-h-20 max-w-xl overflow-auto break-words whitespace-pre-wrap'>
              {assignment.assetName}
            </div>
          </td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Specification</td>
          <td className='px-4 py-2'>
            <div className='max-h-20 max-w-xl overflow-auto break-words whitespace-pre-wrap'>
              {assignment.specification}
            </div>
          </td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Assigned to</td>
          <td className='px-4 py-2'>{assignment.userId}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Assigned by</td>
          <td className='px-4 py-2'>{assignment.createdBy}</td>
        </tr>

        <tr>
          <td className='px-4 py-2 font-semibold'>Assigned Date</td>
          <td className='px-4 py-2'>{formatDate(assignment.assignedDate)}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>State</td>
          <td className='px-4 py-2'>{assignment.status.name}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Note</td>
          <td className='px-4 py-2'>
            <div className='max-h-20 max-w-xl overflow-auto break-words whitespace-pre-wrap'>
              {assignment.note}
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
);

const ErrorDisplay: FC<{ error: string }> = ({ error }) => (
  <div className='flex h-full w-full items-center justify-center p-5'>
    <p className='text-red-600'>{error}</p>
  </div>
);

const LoadingDisplay: FC = () => (
  <div className='flex h-full w-full items-center justify-center p-5'>
    <p className='text-gray-600'>Loading assignment...</p>
  </div>
);

export default ViewAssignmentDetailModal;
