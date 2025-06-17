import FilterDropdown from '@/components/FilterDropdown';
import SearchBar from '@/components/SearchBar';
import { Button } from '@/components/ui/button';
import { SortType, ToastType, User, UserTableItem } from '@/types/type';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useUsers } from '@/hooks/fetch/useUsers';
import { PARAM_KEYS, ROLE_OPTIONS, DEFAULT_SORT_ORDER } from '@/configs/constants';
import { ColumnType, Table } from '@/components/table/Table';
import capitalizeEachWord from '@/utils/capitilizeUtils';
import { Pencil, X } from 'lucide-react';
import ViewUserDetailModal from '@/components/modal/ViewDetailUserModal';
import useModal from '@/hooks/useModal';
import { formatDate } from '@/utils/dateUtils';
import { useLocation } from 'react-router-dom';
import { convertUserToUserTableItem } from '@/utils/mapperUtils';
import DeletionConfirmation from '@/components/modal/UserDeletionModal';

const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT = 'fullName';

export function UserManagement() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const location = useLocation();
  const [highlightedUser, setHighlightedUser] = useState<UserTableItem | null>(null);

  const page = parseInt(searchParams.get(PARAM_KEYS.PAGE) || `${DEFAULT_PAGE}`, 10);
  const size = parseInt(searchParams.get(PARAM_KEYS.SIZE) || `${DEFAULT_PAGE_SIZE}`, 10);
  const sort = searchParams.get(PARAM_KEYS.SORT) || DEFAULT_SORT;
  const sortOrder =
    (searchParams.get(PARAM_KEYS.SORT_ORDER) as 'asc' | 'desc') || DEFAULT_SORT_ORDER;
  const search = searchParams.get(PARAM_KEYS.SEARCH) || '';
  const roles = searchParams.getAll(PARAM_KEYS.ROLES);
  const { isModalVisible, openModal, closeModal } = useModal();
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const {
    isModalVisible: isDeletionModalVisible,
    openModal: openDeletionModal,
    closeModal: closeDeletionModal,
  } = useModal(false);

  const updateParams = (newParams: Record<string, string | number | undefined>) => {
    const updated = new URLSearchParams(searchParams.toString());

    Object.entries(newParams).forEach(([key, value]) => {
      if (value === undefined || value === '') {
        updated.delete(key);
      } else {
        updated.set(key, String(value));
      }
    });

    setSearchParams(updated);
  };

  const { users, pagination, isLoading, mutate} = useUsers({
    page: page - 1,
    size,
    search,
    sort,
    sortOrder,
    roles: roles.length > 0 ? roles : undefined,
  });

  const totalPages = pagination?.totalPages || 1;

  const navigateToCreateUser = () => {
    navigate('/users/create');
  };

  const handlePageChange = (newPage: number) => {
    updateParams({ [PARAM_KEYS.PAGE]: newPage });
    setHighlightedUser(null);
  };

  const handleSearch = (keyword: string) => {
    updateParams({ [PARAM_KEYS.SEARCH]: keyword, [PARAM_KEYS.PAGE]: 1 });
    setHighlightedUser(null);
  };

  const handleFilterChange = (selectedRoles: string[]) => {
    const updated = new URLSearchParams(searchParams.toString());
    updated.delete(PARAM_KEYS.ROLES);

    if (selectedRoles.length > 0) {
      selectedRoles.forEach((role) => {
        updated.append(PARAM_KEYS.ROLES, role);
      });
    }

    updated.set(PARAM_KEYS.PAGE, '1');
    setSearchParams(updated);
    setHighlightedUser(null);
  };

  const handleSortChange = (sort: SortType<UserTableItem>) => {
    updateParams({
      [PARAM_KEYS.SORT]: sort.key as string,
      [PARAM_KEYS.SORT_ORDER]: sort.order,
      [PARAM_KEYS.PAGE]: 1,
    });
    setHighlightedUser(null);
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const renderCellWithOnClick = (value: any, render: UserTableItem) => (
    <div
      className='w-full h-full cursor-pointer'
      onClick={() => {
        setSelectedUserId(render.id);
        openModal();
      }}
    >
      {value}
    </div>
  );

  // Show toast message if present in search params
  useEffect(() => {
    const message = searchParams.get(PARAM_KEYS.MESSAGE);
    const messageType = searchParams.get(PARAM_KEYS.MESSAGE_TYPE) || 'info';

    if (message) {
      toast(message, {
        type: messageType as ToastType,
      });
    }
  }, []);

  // Highlight user if updated user is present in location state
  useEffect(() => {
    const userEdited = location.state?.updatedUser || null;
    if (userEdited && typeof userEdited === 'object' && 'id' in userEdited) {
      setHighlightedUser(convertUserToUserTableItem(userEdited as User));
    }

    if (location.state?.updatedUser) {
      window.history.replaceState({}, '', location.pathname);
    }
  }, [location.state]);

  const columns: ColumnType<UserTableItem>[] = [
    {
      key: 'staffCode',
      title: 'Staff Code',
      sortable: true,
      width: '150px',
      render: renderCellWithOnClick,
    },
    {
      key: 'fullName',
      title: 'Full Name',
      sortable: true,
      minWidth: 150,
      render: renderCellWithOnClick,
    },
    {
      key: 'username',
      title: 'Username',
      sortable: true,
      render: renderCellWithOnClick,
    },
    {
      key: 'joinedDate',
      title: 'Joined Date',
      sortable: true,
      render: (value) => formatDate(value),
    },
    {
      key: 'type',
      title: 'Type',
      width: '150px',
      sortable: true,
      render: (_, record) => capitalizeEachWord(record.type[0].name),
    },
    {
      key: 'actions',
      title: 'Actions',
      width: '100px',
      render: (_, record) => (
        <div className='flex items-center justify-center gap-2'>
          <button className='mr-2 text-gray-600 cursor-pointer hover:text-blue-600'>
            <Link to={`/users/${record.id}/edit`}>
              <Pencil size={16} />
            </Link>
          </button>
          <button className='text-red-600 cursor-pointer hover:text-red-800'>
            <X
              size={16}
              onClick={() => {
                setSelectedUserId(record.id);
                openDeletionModal();
              }}
            />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className='flex flex-col h-full px-5 overflow-hidden'>
      <div className='flex flex-col mb-4'>
        <div>
          <h1 className='font-semibold text-red-500'>User List</h1>
        </div>

        <div className='flex items-center justify-between gap-4 mt-4'>
          <FilterDropdown
            label='Type'
            options={ROLE_OPTIONS}
            selected={roles}
            onChange={handleFilterChange}
            width='w-32'
          />

          <div className='flex items-center gap-4'>
            <div className='h-full w-75'>
              <SearchBar
                defaultValue={search}
                onSearch={handleSearch}
                placeholder='Search by Staff Code or Full Name'
              />
            </div>
            <Button
              onClick={navigateToCreateUser}
              className='px-6 text-white bg-red-600 shrink-0 hover:bg-red-700'
            >
              Create new user
            </Button>
          </div>
        </div>
      </div>

      <Table<UserTableItem>
        data={users.filter((user) => user.id !== highlightedUser?.id)}
        highlightedRows={highlightedUser ? [highlightedUser] : []}
        columns={columns}
        rowKey='id'
        sort={{ key: sort as keyof UserTableItem, order: sortOrder }}
        loading={isLoading}
        onSortChange={handleSortChange}
        pagination={{
          currentPage: page,
          totalPages,
          onPageChange: handlePageChange,
        }}
      />

      {selectedUserId && (
        <ViewUserDetailModal
          visible={isModalVisible}
          openModal={openModal}
          closeModal={() => {
            closeModal();
            setSelectedUserId(null);
          }}
          userId={selectedUserId || ''}
        />
      )}
      
      {isDeletionModalVisible && selectedUserId && (
        <DeletionConfirmation id={selectedUserId} onSuccess={mutate} visible={isDeletionModalVisible} openModal={openDeletionModal} closeModal={() => {
          setSelectedUserId(null);
          setHighlightedUser(null);
          closeDeletionModal();
        }} />
      )}
    </div>
  );
}
