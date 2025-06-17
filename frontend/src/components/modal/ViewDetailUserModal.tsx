import { FC } from 'react';
import { useUser } from '@/hooks/fetch/useUsers';
import { UserDetailType } from '@/types/type';
import { formatDate } from '@/utils/dateUtils';
import capitalizeEachWord from '@/utils/capitilizeUtils';
import { Modal } from './Modal';
interface ViewUserDetailModalProps {
  visible: boolean;
  openModal: () => void;
  closeModal: () => void;
  userId: string;
}

const ViewUserDetailModal: FC<ViewUserDetailModalProps> = ({
  visible,
  openModal,
  closeModal,
  userId,
}) => {
  const { user, isLoading, isError, error } = useUser(userId);
  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      title='Detailed User Information'
      maxWidth='max-w-3xl'
    >
      <div className=''>
        {isLoading && <LoadingDisplay />}
        {isError && <ErrorDisplay error={error as string} />}
        {user && <UserDetail user={user} />}
      </div>
    </Modal>
  );
};

const UserDetail: FC<{ user: UserDetailType }> = ({ user }) => (
  <div className='pr-0 pb-5 pl-5'>
    <table className='w-full table-auto border-none'>
      <tbody>
        <tr>
          <td className='px-4 py-2 font-semibold'>Staff Code</td>
          <td className='px-4 py-2'>{user.staffCode}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Full Name</td>
          <td className='px-4 py-2'>
            <div className='max-h-20 max-w-xl overflow-auto break-words whitespace-pre-wrap'>
              {user.fullName}
            </div>
          </td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Username</td>
          <td className='px-4 py-2'>{user.username}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Date of Birth</td>
          <td className='px-4 py-2'>{formatDate(user.dateOfBirth)}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Gender</td>
          <td className='px-4 py-2'>{capitalizeEachWord(user.gender)}</td>
        </tr>

        <tr>
          <td className='px-4 py-2 font-semibold'>Joined Date</td>
          <td className='px-4 py-2'>{formatDate(user.joinedOn)}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Type</td>
          <td className='px-4 py-2'>{capitalizeEachWord(user.types[0].name)}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Location</td>
          <td className='px-4 py-2'>{user.location.name}</td>
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
    <p className='text-gray-600'>Loading user...</p>
  </div>
);
export default ViewUserDetailModal;
