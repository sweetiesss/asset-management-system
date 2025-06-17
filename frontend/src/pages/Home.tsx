import { ColumnType, Table } from '@/components/table/Table';
import { DEFAULT_SORT_ORDER, PARAM_KEYS } from '@/configs/constants';
import { Assignment, ReturntState } from '@/types/assignment';
import { Check, RotateCcw, X } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';

import { useUser } from '@/context/UserContext';
import { SortType } from '@/types/type';
import { format } from 'date-fns';
import { formatDate } from '@/utils/dateUtils';
import { useState } from 'react';
import useModal from '@/hooks/useModal';
import { Modal } from '@/components/modal/Modal';
import { toast } from 'react-toastify';
import ViewAssignmentDetailModal from '@/components/modal/ViewDetailAssignmentModal';
import { useCreateReturnRequest } from '@/hooks/fetch/useAssetReturn';
import { useGetAssignments, usePutAssignmentStatus } from '@/hooks/fetch/assignment';
import { useSWRConfig } from 'swr';
const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT = 'assetName';

const ACCEPTED_STATUS = 'ACCEPTED';
const DECLINED_STATUS = 'DECLINED';

export function Home() {
  const { auth } = useUser();
  const { mutate } = useSWRConfig(); 
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get(PARAM_KEYS.PAGE) || `${DEFAULT_PAGE}`, 10);
  const size = parseInt(searchParams.get(PARAM_KEYS.SIZE) || `${DEFAULT_PAGE_SIZE}`, 10);
  const sort = searchParams.get(PARAM_KEYS.SORT) || DEFAULT_SORT;
  const sortOrder =
    (searchParams.get(PARAM_KEYS.SORT_ORDER) as 'asc' | 'desc') || DEFAULT_SORT_ORDER;

  const formattedDate = format(new Date(), 'yyyy-MM-dd');
  const {
    items: assignments,
    pagination,
    isLoading,
    mutatePage,
  } = useGetAssignments({
    page: page - 1,
    size,
    sort,
    sortOrder,
    userId: auth?.id,
    assignedDateTo: formattedDate,
  });

  const [actionSelectedAssignmentId, setActionSelectedAssignmentId] = useState<{
    id: string;
    action: 'accept' | 'decline' | 'return' | 'view';
  }>({ id: '', action: 'accept' });

  const {
    isModalVisible: actionModalVisible,
    openModal: openActionModalVisible,
    closeModal: closeActionModalVisible,
  } = useModal();

  const {
    isModalVisible: detailModalVisible,
    openModal: openDetailModalVisible,
    closeModal: closeDetailModalVisible,
  } = useModal();

  const { triggerUpdate, isUpdating } = usePutAssignmentStatus(actionSelectedAssignmentId.id);
  const { createReturnRequest } = useCreateReturnRequest();

  const doAction = async () => {
    switch (actionSelectedAssignmentId.action) {
      case 'accept':
        await triggerUpdate(ACCEPTED_STATUS);
        toast.success('Assignment accepted successfully');
        await mutatePage();
        await Promise.all([mutatePage(), mutate(['assignments', actionSelectedAssignmentId.id])]);
        closeActionModalVisible();
        break;

      case 'decline':
        await triggerUpdate(DECLINED_STATUS);
        toast.success('Assignment declined successfully');
        await Promise.all([mutatePage(), mutate(['assignments', actionSelectedAssignmentId.id])]);
        closeActionModalVisible();
        break;

      case 'return':
        try {
          await createReturnRequest(actionSelectedAssignmentId.id);
          await Promise.all([mutatePage(), mutate('personalAssignments')]);
          toast.success('Return request created successfully');
        } catch {
          toast.error('Failed to create return request');
        } finally {
          closeActionModalVisible();
        }
        break;

      case 'view':
        break;
      default:
        toast.error('Unknown action:', actionSelectedAssignmentId.action);
        return;
    }
  };

  const renderCellWithOnClick = (
    value: string | number | React.ReactNode,
    render: Assignment<'table'>
  ) => (
    <div
      className='w-full h-full cursor-pointer'
      onClick={() => {
        setActionSelectedAssignmentId({ id: render.id, action: 'view' });
        openDetailModalVisible();
      }}
    >
      {value}
    </div>
  );

  const columns: ColumnType<Assignment<'table'>>[] = [
    {
      key: 'assetCode',
      title: 'Asset Code',
      sortable: true,
      render: renderCellWithOnClick,
    },
    {
      key: 'assetName',
      title: 'Asset Name',
      sortable: true,
      render: renderCellWithOnClick,
    },

    {
      key: 'category',
      title: 'Category',
      sortable: true,
      render: (value, record) => renderCellWithOnClick(value.name, record),
    },
    {
      key: 'assignedDate',
      title: 'Assigned Date',
      sortable: true,
      render: (value, record) => renderCellWithOnClick(formatDate(value), record),
    },
    {
      key: 'status',
      title: 'State',
      sortable: true,
      render: (value, record) => renderCellWithOnClick(value.name, record),
    },
    {
      key: 'actions',
      title: 'Actions',
      width: '150px',
      render: (_, render) => {
        const isWaiting = render.status.name === 'Waiting for acceptance';
        const isReturnEnabled =
          render.status.name === 'Accepted' &&
          (render.returnState === null ||
            render.returnState === ReturntState.CANCELED.toUpperCase());

        return (
          <div className='flex items-center justify-center gap-2'>
            <button
              disabled={!isWaiting}
              className='mr-2 text-gray-600 cursor-pointer hover:text-blue-600'
              onClick={() => {
                setActionSelectedAssignmentId({
                  id: render.id,
                  action: 'accept',
                });
                openActionModalVisible();
              }}
            >
              <Check size={16} className={`${isWaiting ? 'text-green-500' : 'text-gray-500'}`} />
            </button>
            <button
              disabled={!isWaiting}
              className='mr-2 text-red-600 cursor-pointer hover:text-red-800 disabled:text-gray-400'
              onClick={() => {
                setActionSelectedAssignmentId({
                  id: render.id,
                  action: 'decline',
                });
                openActionModalVisible();
              }}
            >
              <X size={16} className={`${isWaiting ? 'text-red-500' : 'text-gray-500'}`} />
            </button>

            <button
              disabled={!isReturnEnabled}
              className={`text-blue-600 hover:text-blue-800 disabled:text-gray-400 ${
                isReturnEnabled ? 'cursor-pointer' : 'cursor-not-allowed'
              }`}
              onClick={() => {
                setActionSelectedAssignmentId({
                  id: render.id,
                  action: 'return',
                });
                openActionModalVisible();
              }}
            >
              <RotateCcw
                className={`${isReturnEnabled ? 'text-blue-500' : 'text-gray-500'}`}
                size={16}
              />
            </button>
          </div>
        );
      },
    },
  ];

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

  const handleSortChange = (sort: SortType<Assignment<'table'>>) => {
    updateParams({
      [PARAM_KEYS.SORT]: sort.key as string,
      [PARAM_KEYS.SORT_ORDER]: sort.order,
      [PARAM_KEYS.PAGE]: 1,
    });
  };

  const handlePageChange = (newPage: number) => {
    updateParams({ [PARAM_KEYS.PAGE]: newPage });
  };

  return (
    <div className='flex flex-col h-full px-5 overflow-hidden'>
      <div className='flex flex-col mb-4'>
        <div className='flex justify-between'>
          <h1 className='font-semibold text-red-500'>My Assignment</h1>
        </div>
      </div>
      <Table<Assignment<'table'>>
        data={
          assignments?.filter(
            (a) => a.status.name !== 'Completed' && a.status.name !== 'Declined'
          ) || []
        }
        columns={columns}
        rowKey='id'
        sort={{ key: sort as keyof Assignment<'table'>, order: sortOrder }}
        loading={isLoading}
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
          label:
            actionSelectedAssignmentId.action === 'accept'
              ? 'Accept'
              : actionSelectedAssignmentId.action === 'decline'
                ? 'Decline'
                : 'Yes',
          onClick: doAction,
          variant: 'destructive',
          disabled: isUpdating,
        }}
        secondaryAction={{
          label: actionSelectedAssignmentId.action === 'return' ? 'No' : 'Cancel',
          onClick: closeActionModalVisible,
          variant: 'secondary',
          disabled: isUpdating,
        }}
        maxWidth='sm:max-w-[400px]'
      >
        <div className='px-5'>
          {actionSelectedAssignmentId.action === 'accept' &&
            'Do you want to accept this assignment?'}
          {actionSelectedAssignmentId.action === 'decline' &&
            'Do you want to decline this assignment?'}
          {actionSelectedAssignmentId.action === 'return' &&
            'Do you want to create a return request for this asset?'}
        </div>
      </Modal>

      {actionSelectedAssignmentId && (
        <ViewAssignmentDetailModal
          visible={detailModalVisible}
          openModal={openDetailModalVisible}
          closeModal={() => {
            closeDetailModalVisible();
            setActionSelectedAssignmentId({ id: '', action: 'accept' });
          }}
          assignmentId={actionSelectedAssignmentId.id || ''}
        />
      )}
    </div>
  );
}
