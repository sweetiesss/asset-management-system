import FilterDropdown from '@/components/FilterDropdown';
import SearchBar from '@/components/SearchBar';
import { ColumnType, Table } from '@/components/table/Table';
import { Button } from '@/components/ui/button';
import { DEFAULT_SORT_ORDER, PARAM_KEYS } from '@/configs/constants';
import { Assignment, AssignmentState, ReturntState } from '@/types/assignment';
import { SortType } from '@/types/type';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Pencil, X, RotateCcw } from 'lucide-react';
import { SimpleDatePicker } from '@/components/SimpleDatePicker';
import DeletionConfirmation from '@/components/modal/AssignmentDeletionModal';
import { formatDate } from '@/utils/dateUtils';
import ViewAssignmentDetailModal from '@/components/modal/ViewDetailAssignmentModal';
import useModal from '@/hooks/useModal';
import { Modal } from '@/components/modal/Modal';
import { useCreateReturnRequest } from '@/hooks/fetch/useAssetReturn';
import { toast } from 'react-toastify';
import { useGetAssignments } from '@/hooks/fetch/assignment';
const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT = 'assetName';

export function AssignmentManagement() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get(PARAM_KEYS.PAGE) || `${DEFAULT_PAGE}`, 10);
  const size = parseInt(searchParams.get(PARAM_KEYS.SIZE) || `${DEFAULT_PAGE_SIZE}`, 10);
  const sort = searchParams.get(PARAM_KEYS.SORT) || DEFAULT_SORT;
  const sortOrder =
    (searchParams.get(PARAM_KEYS.SORT_ORDER) as 'asc' | 'desc') || DEFAULT_SORT_ORDER;
  const search = searchParams.get(PARAM_KEYS.SEARCH) || '';
  const states = searchParams.getAll(PARAM_KEYS.STATES);
  const [assignedDate, setAssignedDate] = useState(searchParams.get(PARAM_KEYS.DATES) || '');
  const [highlightedAssignment, setHighlightedAssignment] = useState<Assignment<'table'> | null>(
    null
  );
  const { isModalVisible, openModal, closeModal } = useModal(false);
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<string | null>(null);

  const { createReturnRequest, isLoading: isCreatingReturn } = useCreateReturnRequest();

  const {
    isModalVisible: isDeletionModalVisible,
    openModal: openDeletionModal,
    closeModal: closeDeletionModal,
  } = useModal(false);

  const {
    isModalVisible: isReturnModalVisible,
    openModal: openReturnModal,
    closeModal: closeReturnModal,
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
    setHighlightedAssignment(null);
  };

  const {
    items: assignments,
    pagination,
    isLoading,
    mutatePage,
  } = useGetAssignments({
    page: page - 1,
    size,
    search,
    sort,
    sortOrder,
    states: states.length > 0 ? states : undefined,
    assignedDateFrom: assignedDate || undefined,
    assignedDateTo: assignedDate || undefined,
  });

  const doCreateReturnRequest = async () => {
    if (selectedAssignmentId) {
      try {
        await createReturnRequest(selectedAssignmentId);
        toast.success('Return request created successfully');
        await mutatePage();
      } catch {
        toast.error('Failed to create return request');
      } finally {
        closeReturnModal();
      }
    }
  };

  const handleSearch = (keyword: string) => {
    updateParams({ [PARAM_KEYS.SEARCH]: keyword, [PARAM_KEYS.PAGE]: 1 });
    setHighlightedAssignment(null);
  };

  const handleSortChange = (sort: SortType<Assignment<'table'>>) => {
    updateParams({
      [PARAM_KEYS.SORT]: sort.key as string,
      [PARAM_KEYS.SORT_ORDER]: sort.order,
      [PARAM_KEYS.PAGE]: 1,
    });
    setHighlightedAssignment(null);
  };

  const handlePageChange = (newPage: number) => {
    updateParams({ [PARAM_KEYS.PAGE]: newPage });
    setHighlightedAssignment(null);
  };

  const handleFilterStateChange = (selectedStates: string[]) => {
    const updated = new URLSearchParams(searchParams.toString());
    updated.delete(PARAM_KEYS.STATES);

    selectedStates.forEach((s) => updated.append(PARAM_KEYS.STATES, s));
    updated.set(PARAM_KEYS.PAGE, '1');
    setSearchParams(updated);
    setHighlightedAssignment(null);
  };

  useEffect(() => {
    if (searchParams.getAll(PARAM_KEYS.STATES).length === 0) {
      const defaultStates = [
        AssignmentState.ACCEPTED,
        AssignmentState.DECLINED,
        AssignmentState.WAITING_FOR_ACCEPTANCE,
      ];
      const updated = new URLSearchParams(searchParams.toString());
      defaultStates.forEach((s) => updated.append(PARAM_KEYS.STATES, s));
      updated.set(PARAM_KEYS.PAGE, '1');
      setSearchParams(updated);
      setHighlightedAssignment(null);
    }
  }, []);

  const renderCellWithOnClick = (
    value: string | number | React.ReactNode,
    render: Assignment<'table'>
  ) => (
    <div
      className='w-full h-full cursor-pointer'
      onClick={() => {
        setSelectedAssignmentId(render.id);
        openModal();
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
      key: 'userId',
      title: 'Assigned To',
      sortable: true,
      render: renderCellWithOnClick,
    },
    {
      key: 'createdBy',
      title: 'Assigned By',
      sortable: true,
      render: renderCellWithOnClick,
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
      render: (_, record) => {
        const isNotWaitingForAcceptance =
          record.status.name !== AssignmentState.WAITING_FOR_ACCEPTANCE;

        const isReturnEnabled =
          record.status.name === 'Accepted' &&
          (record.returnState === null ||
            record.returnState === ReturntState.CANCELED.toUpperCase());
        return (
          <div className='flex items-center justify-center gap-2'>
            {isNotWaitingForAcceptance ? (
              <button className='mr-2 text-gray-400 cursor-not-allowed' disabled>
                <Pencil size={16} />
              </button>
            ) : (
              <button className='mr-2 text-gray-600 cursor-pointer hover:text-blue-600'>
                <Link to={`/assignments/${record.id}/edit`}>
                  <Pencil size={16} />
                </Link>
              </button>
            )}

            <button
              className={`cursor-pointer text-red-600 hover:text-red-800 disabled:cursor-not-allowed disabled:text-gray-400`}
              disabled={isNotWaitingForAcceptance}
            >
              <X
                size={16}
                onClick={() => {
                  setSelectedAssignmentId(record.id);
                  openDeletionModal();
                  setHighlightedAssignment(null);
                }}
              />
            </button>
            <button
              disabled={!isReturnEnabled}
              className={`text-blue-600 hover:text-blue-800 disabled:text-gray-400 ${
                isReturnEnabled ? 'cursor-pointer' : 'cursor-not-allowed'
              }`}
              onClick={() => {
                setSelectedAssignmentId(record.id);
                openReturnModal();
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

  const navigateToCreateAssignment = () => {
    navigate('/assignments/create');
  };

  useEffect(() => {
    const assignmentEdited = location.state?.updatedAssignment || null;
    if (assignmentEdited && typeof assignmentEdited === 'object' && 'id' in assignmentEdited) {
      setHighlightedAssignment({
        id: assignmentEdited.id,
        assetCode: assignmentEdited.assetCode,
        assetName: assignmentEdited.assetName,
        userId: assignmentEdited.assignTo,
        createdBy: assignmentEdited.assignBy,
        assignedDate: assignmentEdited.assignedDate,
        status: assignmentEdited.state,
      });
    }

    if (location.state?.updatedUser) {
      window.history.replaceState({}, '', location.pathname);
    }
  }, [location.pathname, location.state]);

  return (
    <div className='flex flex-col h-full px-5 overflow-hidden'>
      <div className='flex flex-col mb-4'>
        <div className='flex justify-between'>
          <h1 className='font-semibold text-red-500'>Assignment List</h1>
        </div>

        <div className='flex items-center justify-between mt-4'>
          <div className='flex items-center gap-4'>
            <FilterDropdown
              label='State'
              options={Object.values(AssignmentState)}
              selected={states}
              onChange={(newStates) => handleFilterStateChange(newStates)}
              width='w-55'
            />
            <div className='w-60'>
              <SimpleDatePicker
                label='Assigned Date'
                value={assignedDate}
                onChange={(val) => {
                  setAssignedDate(val);
                  updateParams({ [PARAM_KEYS.DATES]: val });
                }}
              />
            </div>
          </div>

          <div className='flex items-center gap-4'>
            <SearchBar
              defaultValue={search}
              onSearch={handleSearch}
              className='w-115'
              placeholder='Search by asset code or asset name or assignee’s username'
            />

            <Button
              onClick={navigateToCreateAssignment}
              className='px-6 text-white bg-red-600 hover:bg-red-700'
            >
              Create Assignment
            </Button>
          </div>
        </div>
      </div>

      <Table<Assignment<'table'>>
        data={assignments.filter((assignment) => assignment.id !== highlightedAssignment?.id)}
        columns={columns}
        highlightedRows={highlightedAssignment ? [highlightedAssignment] : []}
        rowKey='id'
        sort={{ key: sort as keyof Assignment<'table'>, order: sortOrder }}
        loading={isLoading}
        onSortChange={handleSortChange}
        countColumns={true}
        pagination={{
          currentPage: page,
          totalPages: pagination?.totalPages || 1,
          onPageChange: handlePageChange,
        }}
      />
      {openDeletionModal && selectedAssignmentId && (
        <DeletionConfirmation
          id={selectedAssignmentId}
          visible={isDeletionModalVisible}
          cache={mutatePage}
          openModal={openDeletionModal}
          closeModal={() => {
            closeDeletionModal();
            setSelectedAssignmentId(null);
          }}
        />
      )}

      {selectedAssignmentId && (
        <ViewAssignmentDetailModal
          visible={isModalVisible}
          openModal={openModal}
          closeModal={() => {
            closeModal();
            setSelectedAssignmentId(null);
          }}
          assignmentId={selectedAssignmentId || ''}
        />
      )}

      <Modal
        isOpen={isReturnModalVisible}
        onOpen={openReturnModal}
        onClose={closeReturnModal}
        title='Are you sure?'
        primaryAction={{
          label: 'Return',
          onClick: doCreateReturnRequest,
          variant: 'destructive',
          disabled: isCreatingReturn,
        }}
        secondaryAction={{
          label: 'Cancel',
          onClick: closeReturnModal,
          variant: 'secondary',
          disabled: isCreatingReturn,
        }}
        maxWidth='sm:max-w-[400px]'
      >
        <div className='px-5'>Do you want to create a return request for this asset?</div>
      </Modal>
    </div>
  );
}
