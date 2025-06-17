import { useSearchParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Table, ColumnType } from '@/components/table/Table';
import { PARAM_KEYS, DEFAULT_SORT_ORDER } from '@/configs/constants';
import { AssetReturnType, ReturnStateHandler } from '@/types/assetReturn';
import { ReturntState } from '@/types/assignment';
import { SortType } from '@/types/type';
import { formatDate } from '@/utils/dateUtils';
import FilterDropdown from '@/components/FilterDropdown';
import SearchBar from '@/components/SearchBar';
import { useGetAssetReturns } from '@/hooks/fetch/assetReturn';
import { getReturnStateLabel, getReturnStateValue } from '@/utils/stateUtils';
import { Check, X } from 'lucide-react';
import { SimpleDatePicker } from '@/components/SimpleDatePicker';
import useModal from '@/hooks/useModal';
import { useUpdateAssetReturnState } from '@/hooks/fetch/useAssetReturn';
import { toast } from 'react-toastify';
import { Modal } from '@/components/modal/Modal';
const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT = 'assetName';
const CANCELED_STATE = ReturntState.CANCELED;

export function AssetReturnManagementPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get(PARAM_KEYS.PAGE) || `${DEFAULT_PAGE}`, 10);
  const size = parseInt(searchParams.get(PARAM_KEYS.SIZE) || `${DEFAULT_PAGE_SIZE}`, 10);
  const sort = searchParams.get(PARAM_KEYS.SORT) || DEFAULT_SORT;
  const sortOrder =
    (searchParams.get(PARAM_KEYS.SORT_ORDER) as 'asc' | 'desc') || DEFAULT_SORT_ORDER;
  const search = searchParams.get(PARAM_KEYS.SEARCH) || '';
  const states = searchParams.getAll(PARAM_KEYS.STATES);
  const [returnedDate, setReturnedDate] = useState(searchParams.get(PARAM_KEYS.DATES) || '');
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

  useEffect(() => {
    if (searchParams.getAll(PARAM_KEYS.STATES).length === 0) {
      const defaultStates = [ReturntState.COMPLETED, ReturntState.WAITING_FOR_RETURNING];
      const updated = new URLSearchParams(searchParams.toString());
      defaultStates.forEach((s) => updated.append(PARAM_KEYS.STATES, s));
      updated.set(PARAM_KEYS.PAGE, '1');
      setSearchParams(updated);
    }
  }, []);

  const handleSearch = (keyword: string) => {
    updateParams({ [PARAM_KEYS.SEARCH]: keyword, [PARAM_KEYS.PAGE]: 1 });
  };

  const handleSortChange = (sort: SortType<AssetReturnType<'table'>>) => {
    updateParams({
      [PARAM_KEYS.SORT]: sort.key as string,
      [PARAM_KEYS.SORT_ORDER]: sort.order,
      [PARAM_KEYS.PAGE]: 1,
    });
  };

  const handlePageChange = (newPage: number) => {
    updateParams({ [PARAM_KEYS.PAGE]: newPage });
  };

  const handleFilterStateChange = (selectedStates: string[]) => {
    const updated = new URLSearchParams(searchParams.toString());
    updated.delete(PARAM_KEYS.STATES);

    selectedStates.forEach((s) => updated.append(PARAM_KEYS.STATES, s));
    updated.set(PARAM_KEYS.PAGE, '1');
    setSearchParams(updated);
  };

  const {
    items: assetReturns,
    pagination,
    isLoading,
    mutatePage,
  } = useGetAssetReturns({
    page: page - 1,
    size,
    search,
    sort,
    sortOrder,
    states: states.length > 0 ? states : undefined,
    returnedDateFrom: returnedDate || undefined,
    returnedDateTo: returnedDate || undefined,
  });

  const [selectedAssetReturnId, setSelectedAssetReturnId] = useState<{
    id: string;
    action: 'complete' | 'cancel';
  }>({ id: '', action: 'complete' });

  const {
    isModalVisible: actionModalVisible,
    openModal: openActionModalVisible,
    closeModal: closeActionModalVisible,
  } = useModal();

  const { updateAssetReturnState, isUpdating } = useUpdateAssetReturnState();

  const doAction = async () => {
    switch (selectedAssetReturnId.action) {
      case 'complete':
        await updateAssetReturnState({
          id: selectedAssetReturnId.id,
          state: ReturntState.COMPLETED,
        });
        toast.success('Asset return request completed successfully');
        break;
      case 'cancel':
        await updateAssetReturnState({
          id: selectedAssetReturnId.id,
          state: CANCELED_STATE,
        });
        toast.success('Asset return request canceled successfully');
        break;
      default:
        toast.error('Unknown action:', selectedAssetReturnId.action);
        return;
    }
    closeActionModalVisible();
    mutatePage();
  };

  const columns: ColumnType<AssetReturnType<'table'>>[] = [
    { key: 'assetCode', title: 'Asset Code', sortable: true },
    { key: 'assetName', title: 'Asset Name', sortable: true },
    { key: 'createdBy', title: 'Requested by', sortable: true },
    { key: 'assignedDate', title: 'Assigned Date', sortable: true, render: formatDate },
    {
      key: 'updatedBy',
      title: 'Accepted by',
      sortable: true,
      render: (_, value) => (value.updatedBy === null ? '' : value.updatedBy),
    },
    { key: 'returnedDate', title: 'Returned Date', sortable: true, render: formatDate },
    {
      key: 'state',
      title: 'State',
      sortable: true,
      render: (value) => ReturnStateHandler.parse(value).label(),
    },
    {
      key: 'actions',
      title: 'Actions',
      width: '150px',
      render: (_, render) => {
        const isCanceledEnabled = render.state === ReturntState.WAITING_FOR_RETURNING;
        return (
          <div className='flex items-center justify-center gap-2'>
            <button
              className={`cursor-pointer text-red-600 hover:text-red-800 disabled:cursor-not-allowed disabled:text-gray-400`}
              disabled={!isCanceledEnabled}
              onClick={() => {
                setSelectedAssetReturnId({
                  id: render.id,
                  action: 'complete',
                });
                openActionModalVisible();
              }}
            >
              <Check size={16} />
            </button>
            <button
              disabled={!isCanceledEnabled}
              className={`text-black-600 hover:text-black-800 cursor-pointer disabled:cursor-not-allowed disabled:text-gray-400 ${
                isCanceledEnabled ? 'cursor-pointer' : 'cursor-not-allowed'
              }`}
              onClick={() => {
                setSelectedAssetReturnId({
                  id: render.id,
                  action: 'cancel',
                });
                openActionModalVisible();
              }}
            >
              <X size={16} />
            </button>
          </div>
        );
      },
    },
  ];

  return (
    <div className='flex flex-col h-full px-5 overflow-hidden'>
      <div className='flex justify-between mb-4'>
        <h1 className='font-semibold text-red-500'>Request List</h1>
      </div>

      <div className='flex items-center justify-between mb-4'>
        <div className='flex items-center gap-4'>
          <FilterDropdown
            label='State'
            options={ReturnStateHandler.getOptions().map((opt) => opt.label)}
            selected={states.map(getReturnStateLabel)}
            onChange={(selectedLabels) => {
              const selectedValues = selectedLabels
                .map(getReturnStateValue)
                .filter(Boolean) as string[];
              handleFilterStateChange(selectedValues);
            }}
            width='w-55'
          />
          <div className='w-60'>
            <SimpleDatePicker
              label='Returned Date'
              value={returnedDate}
              onChange={(val) => {
                setReturnedDate(val);
                updateParams({ [PARAM_KEYS.DATES]: val });
              }}
            />
          </div>
        </div>
        <SearchBar
          defaultValue={search}
          onSearch={handleSearch}
          className='w-125'
          placeholder='Search request by asset code or asset name or requester’s username'
        />
      </div>

      <Table<AssetReturnType<'table'>>
        data={assetReturns}
        columns={columns}
        rowKey='id'
        sort={{ key: sort as keyof AssetReturnType<'table'>, order: sortOrder }}
        loading={isLoading}
        countColumns={true}
        onSortChange={handleSortChange}
        pagination={{
          currentPage: page,
          totalPages: pagination?.totalPages || 1,
          onPageChange: handlePageChange,
        }}
      />

      <Modal
        isOpen={actionModalVisible}
        onOpen={openActionModalVisible}
        onClose={closeActionModalVisible}
        title='Are you sure?'
        primaryAction={{
          label: 'Yes',
          onClick: doAction,
          variant: 'destructive',
          disabled: isUpdating,
        }}
        secondaryAction={{
          label: 'No',
          onClick: closeActionModalVisible,
          variant: 'secondary',
          disabled: isUpdating,
        }}
        maxWidth='sm:max-w-[400px]'
      >
        <div className='px-5'>
          {selectedAssetReturnId.action === 'complete' &&
            "Do you want to mark this retuning request as 'Completed'?"}
          {selectedAssetReturnId.action === 'cancel' &&
            'Do you want to cancel this retuning request?'}
        </div>
      </Modal>
    </div>
  );
}
