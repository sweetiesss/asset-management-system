import React, { useState } from 'react';
import { User, UserTableItem } from '../../types/type';
import { ChevronDown, Pencil, X, ChevronUp } from 'lucide-react';
import useModal from '@/hooks/useModal';
import ViewUserDetailModal from '../modal/ViewDetailUserModal';
import { Link } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { formatDate } from '@/utils/dateUtils';
import capitalizeEachWord from '@/utils/capitilizeUtils';
interface UserTableProps {
  users: UserTableItem[];
  onSort: (field: string) => void;
  sortField: string;
  sortOrder: string;
}

type UpdatedUserType = {
  updatedUser: User;
} | null;

const prepareUsers = (
  users: UserTableItem[],
  editedUserWrapper?: UpdatedUserType
): UserTableItem[] => {
  if (editedUserWrapper && editedUserWrapper?.updatedUser) {
    const editedUser: User = editedUserWrapper.updatedUser;
    const filteredUsers = users.filter((user) => user.username !== editedUser.username);
    return [
      {
        id: editedUser.id,
        staffCode: editedUser.staffCode,
        fullName: `${editedUser.firstName} ${editedUser.lastName}`,
        username: editedUser.username,
        joinedDate: editedUser.joinedOn,
        type: editedUser.roles,
      },
      ...filteredUsers,
    ];
  }
  return users;
};

const UserTableManagement: React.FC<UserTableProps> = ({ users, onSort, sortField, sortOrder }) => {
  const location = useLocation();
  const editedUserWrapper: UpdatedUserType = location.state;
  users = prepareUsers(users, editedUserWrapper);

  const renderSortIcon = (field: string) => {
    if (sortField !== field) return <ChevronDown size={16} className='opacity-30' />;
    return sortOrder === 'asc' ? <ChevronDown size={16} /> : <ChevronUp size={16} />;
  };

  const { isModalVisible, openModal, closeModal } = useModal(false);
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);

  const handleCellClick = (userId: string) => {
    setSelectedUserId(userId);
    openModal();
  };

  React.useEffect(() => {
    window.history.replaceState({}, '', location.pathname);
  }, [location.pathname]);

  return (
    <div className='relative overflow-x-auto'>
      <table className='min-w-full text-left border border-gray-300'>
        <thead className='bg-gray-100'>
          <tr className='cursor-pointer hover:bg-gray-300'>
            <th className='p-2 border' onClick={() => onSort('staffCode')}>
              <div className='flex items-center justify-between'>
                <span>Staff Code</span>
                {renderSortIcon('staffCode')}
              </div>
            </th>
            <th className='p-2 border' onClick={() => onSort('fullName')}>
              <div className='flex items-center justify-between'>
                <span>Full Name</span>
                {renderSortIcon('fullName')}
              </div>
            </th>
            <th className='p-2 border' onClick={() => onSort('username')}>
              <div className='flex items-center justify-between'>
                <span>Username</span>
                {renderSortIcon('username')}
              </div>
            </th>
            <th className='p-2 border' onClick={() => onSort('joinedDate')}>
              <div className='flex items-center justify-between'>
                <span>Joined Date</span>
                {renderSortIcon('joinedDate')}
              </div>
            </th>
            <th className='p-2 border' onClick={() => onSort('type')}>
              <div className='flex items-center justify-between'>
                <span>Type</span>
                {renderSortIcon('type')}
              </div>
            </th>
            <th className='p-2 text-center border'>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr
              key={user.id}
              className={`hover:bg-gray-50 ${location.state && (location.state as UpdatedUserType)?.updatedUser.id == user.id ? 'bg-yellow-300 hover:bg-yellow-500' : ''}`}
            >
              <td className='p-2 border cursor-pointer' onClick={() => handleCellClick(user.id)}>
                {user.staffCode}
              </td>
              <td className='p-2 border cursor-pointer' onClick={() => handleCellClick(user.id)}>
                {user.fullName}
              </td>
              <td className='p-2 border cursor-pointer' onClick={() => handleCellClick(user.id)}>
                {user.username}
              </td>
              <td className='p-2 border cursor-pointer'>{formatDate(user.joinedDate)}</td>
              <td className='p-2 border cursor-pointer'>{capitalizeEachWord(user.type[0].name)}</td>
              <td className='p-2 text-center border'>
                <button className='mr-2 text-gray-600 hover:text-blue-600'>
                  <Link to={`/users/${user.id}/edit`}>
                    <Pencil size={16} />
                  </Link>
                </button>
                <button className='text-red-600 hover:text-red-800'>
                  <X size={16} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedUserId && (
        <ViewUserDetailModal
          visible={isModalVisible}
          openModal={openModal}
          closeModal={() => {
            closeModal();
            setSelectedUserId(null);
          }}
          userId={selectedUserId}
        />
      )}
    </div>
  );
};

export default UserTableManagement;
