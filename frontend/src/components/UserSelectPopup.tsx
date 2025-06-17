import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import SearchBar from './SearchBar';
import { ColumnType, Table } from './table/Table';
import capitalizeEachWord from '@/utils/capitilizeUtils';
import { SortType, UserTableItem } from '@/types/type';
import { useUsers } from '@/hooks/fetch/useUsers';
import { useEffect, useState } from 'react';
import { Button } from './ui/button';

const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 5;
const DEFAULT_SORT = 'fullName';

type UserSelectPopupProps = {
  /**
   * The currently selected user.
   * If null, no user is selected.
   */
  selectedUser?: UserTableItem | null;

  /**
   * Callback function to handle user selection changes.
   * It will be called with the newly selected user when the user confirms their selection.
   */
  onUserSelectChange?: (user: UserTableItem) => void;
};

export const UserSelectPopup = ({ selectedUser, onUserSelectChange }: UserSelectPopupProps) => {
  const [page, setPage] = useState(DEFAULT_PAGE);
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [search, setSearch] = useState('');
  const [isOpenPopup, setIsOpenPopup] = useState(false);

  const [confirmSelectedUser, setConfirmSelectedUser] = useState<UserTableItem | null>(null);
  const [lastSelectedUser, setLastSelectedUser] = useState<UserTableItem | null>(selectedUser || null);

  const { users, isLoading, pagination } = useUsers({
    page: page - 1,
    size: DEFAULT_PAGE_SIZE,
    search,
    sort,
    sortOrder,
  });

  const handleSortChange = (sort: SortType<UserTableItem>) => {
    setSort(sort.key as string);
    setSortOrder(sort.order);
    setPage(DEFAULT_PAGE);
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(DEFAULT_PAGE);
  };

  const handleUserSelect = (items: UserTableItem[]) => {
    setLastSelectedUser(items[0] || selectedUser);
  };

  useEffect(() => {
    if (confirmSelectedUser) {
      onUserSelectChange?.(confirmSelectedUser);
    }
  }, [confirmSelectedUser]);

  useEffect(() => {
    if (selectedUser) {
      setConfirmSelectedUser(selectedUser);
      setLastSelectedUser(selectedUser);
    }
  }, [selectedUser]);

  const columns: ColumnType<UserTableItem>[] = [
    {
      key: 'staffCode',
      title: 'Staff Code',
      sortable: true,
      width: '150px',
    },
    {
      key: 'fullName',
      title: 'Full Name',
      sortable: true,
      minWidth: 150,
    },
    {
      key: 'type',
      title: 'Type',
      width: '150px',
      sortable: true,
      render: (_, record) => capitalizeEachWord(record.type[0].name),
    },
  ];
  return (
    <Popover open={isOpenPopup} onOpenChange={setIsOpenPopup}>
      <PopoverTrigger className='w-full'>
        <div className='w-full'>
          <SearchBar readOnly defaultValue={confirmSelectedUser?.fullName || selectedUser?.fullName} onSearch={() => {}} />
        </div>
      </PopoverTrigger>
      <PopoverContent
        className='flex h-full max-h-[600px] max-w-[700px] flex-col overflow-hidden bg-white px-5'
        side='bottom'
        align='start'
      >
        <div className='flex h-full flex-col overflow-hidden px-5'>
          <div className='mb-4 flex flex-col'>
            <div className='mt-4 flex items-center justify-between gap-4'>
              <h2 className='text-primary text-base font-semibold'>Select User</h2>
              <SearchBar defaultValue={search} onSearch={handleSearchChange} />
            </div>
          </div>

          <Table
            columns={columns}
            data={users}
            rowKey={'id'}
            enableRowSelection
            rowSelectionMode='single'
            sort={{
              key: sort as keyof UserTableItem,
              order: sortOrder,
            }}
            pagination={{
              currentPage: page,
              totalPages: pagination?.totalPages || 0,
              onPageChange: handlePageChange,
              buttonSize: 'sm',
            }}
            onSortChange={handleSortChange}
            onSelectColumns={handleUserSelect}
            defaultSelectedRowKeys={confirmSelectedUser ? [confirmSelectedUser.id] : []}
          ></Table>
          <div className='mt-1 flex justify-end'>
            <Button
              disabled={isLoading}
              onClick={() => {
                setConfirmSelectedUser(lastSelectedUser);
                setIsOpenPopup(false);
              }}
            >
              Save
            </Button>
            <Button variant='outline' className='ml-2' onClick={() => setIsOpenPopup(false)}>
              Cancel
            </Button>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};
