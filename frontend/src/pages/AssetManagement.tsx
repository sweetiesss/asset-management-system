import FilterDropdown from '@/components/FilterDropdown';
import SearchBar from '@/components/SearchBar';
import { ColumnType, Table } from '@/components/table/Table';
import { Button } from '@/components/ui/button';
import { DEFAULT_SORT_ORDER, PARAM_KEYS } from '@/configs/constants';
import { useCategories } from '@/hooks/fetch/useCategories';
import { Asset, AssetState, AssetStateHandler, Category } from '@/types/asset';
import { SortType } from '@/types/type';
import { getAssetStateLabel, getAssetStateValue } from '@/utils/stateUtils';
import { Pencil, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useGetAssets } from '@/hooks/fetch/assets';
const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT = 'name';
import ViewAssetDetailModal from '@/components/modal/ViewDetailAssetModal';
import useModal from '@/hooks/useModal';
import DeletionConfirmation from '@/components/modal/AssetDeletionModal';

export function AssetManagement() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [highlightedAsset, setHighlightedAsset] = useState<Asset<'table'> | null>(null);
  const page = parseInt(searchParams.get(PARAM_KEYS.PAGE) || `${DEFAULT_PAGE}`, 10);
  const size = parseInt(searchParams.get(PARAM_KEYS.SIZE) || `${DEFAULT_PAGE_SIZE}`, 10);
  const sort = DEFAULT_SORT;
  const sortOrder =
    (searchParams.get(PARAM_KEYS.SORT_ORDER) as 'asc' | 'desc') || DEFAULT_SORT_ORDER;
  const search = searchParams.get(PARAM_KEYS.SEARCH) || '';
  const { isModalVisible, openModal, closeModal } = useModal(false);
  const {
    isModalVisible: isDeletionModalVisible,
    openModal: openDeletionModal,
    closeModal: closeDeletionModal,
  } = useModal(false);

  const [selectedAssetId, setSelectedAssetId] = useState<string | null>(null);

  const states = searchParams.getAll(PARAM_KEYS.STATES);
  const categoriesDropdown = searchParams.getAll(PARAM_KEYS.CATEGORIES);

  const navigateToCreateAsset = () => {
    navigate('/assets/create');
  };

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
    setHighlightedAsset(null);
  };

  const { data: categories } = useCategories();
  const {
    items: assets,
    pagination,
    isLoading,
    mutatePage,
  } = useGetAssets({
    page: page - 1,
    size,
    search,
    sort,
    sortOrder,
    states: states.length > 0 ? states : undefined,
    categories: categoriesDropdown.length > 0 ? categoriesDropdown : undefined,
  });
  const renderCellWithOnClick = (
    value: string | number | React.ReactNode,
    render: Asset<'table'>
  ) => (
    <div
      className='h-full w-full cursor-pointer'
      onClick={() => {
        setSelectedAssetId(render.id);
        openModal();
      }}
    >
      {value}
    </div>
  );

  const totalPages = pagination?.totalPages || 1;

  const columns: ColumnType<Asset<'table'>>[] = [
    {
      key: 'code',
      title: 'Asset Code',
      sortable: true,
      width: '150px',
      render: renderCellWithOnClick,
    },
    {
      key: 'name',
      title: 'Asset Name',
      sortable: true,
      minWidth: 150,
      render: renderCellWithOnClick,
    },
    {
      key: 'categoryName',
      title: 'Category',
      sortable: true,
      render: renderCellWithOnClick,
    },
    {
      key: 'state',
      title: 'State',
      sortable: true,
      width: '300px',
      render: (value) => AssetStateHandler.parse(value).label(),
    },
    {
      key: 'actions',
      title: 'Actions',
      width: '150px',
      render: (_, record) => {
        const isAssigned = record.state === AssetState.ASSIGNED;
        return (
          <div className='flex items-center justify-center gap-2'>
            {isAssigned ? (
              <button className='mr-2 cursor-not-allowed text-gray-400' disabled>
                <Pencil size={16} />
              </button>
            ) : (
              <button className='mr-2 cursor-pointer text-gray-600 hover:text-blue-600'>
                <Link to={`/assets/${record.id}/edit`}>
                  <Pencil size={16} />
                </Link>
              </button>
            )}
            <button
              className={`cursor-pointer text-red-600 hover:text-red-800 disabled:text-gray-400`}
              disabled={isAssigned}
            >
              <X
                size={16}
                onClick={() => {
                  setSelectedAssetId(record.id);
                  openDeletionModal();
                }}
              />
            </button>
          </div>
        );
      },
    },
  ];

  const handlePageChange = (newPage: number) => {
    updateParams({ [PARAM_KEYS.PAGE]: newPage });
    setHighlightedAsset(null);
  };

  const handleSortChange = (sort: SortType<Asset<'table'>>) => {
    updateParams({
      [PARAM_KEYS.SORT]: sort.key as string,
      [PARAM_KEYS.SORT_ORDER]: sort.order,
      [PARAM_KEYS.PAGE]: 1,
    });
    setHighlightedAsset(null);
  };

  const handleSearch = (keyword: string) => {
    updateParams({ [PARAM_KEYS.SEARCH]: keyword, [PARAM_KEYS.PAGE]: 1 });
    setHighlightedAsset(null);
  };

  const handleFilterStateChange = (selectedStates: string[]) => {
    const updated = new URLSearchParams(searchParams.toString());
    updated.delete(PARAM_KEYS.STATES);

    if (selectedStates.length > 0) {
      selectedStates.forEach((state) => {
        updated.append(PARAM_KEYS.STATES, state);
      });
    }

    updated.set(PARAM_KEYS.PAGE, '1');
    setSearchParams(updated);
    setHighlightedAsset(null);
  };

  const handleFilterCategoryChange = (selectedCategory: string[]) => {
    const updated = new URLSearchParams(searchParams.toString());
    updated.delete(PARAM_KEYS.CATEGORIES);

    if (selectedCategory.length > 0) {
      selectedCategory.forEach((category) => {
        updated.append(PARAM_KEYS.CATEGORIES, category);
      });
    }

    updated.set(PARAM_KEYS.PAGE, '1');
    setSearchParams(updated);
    setHighlightedAsset(null);
  };

  useEffect(() => {
    const assetEdited = location.state?.updatedAsset || null;
    if (assetEdited && typeof assetEdited === 'object' && 'id' in assetEdited) {
      setHighlightedAsset({
        id: assetEdited.id,
        name: assetEdited.name,
        code: assetEdited.code,
        categoryName: assetEdited.category.name,
        state: assetEdited.state,
      });
    }

    if (location.state?.updatedUser) {
      window.history.replaceState({}, '', location.pathname);
    }
  }, [location.state, location.pathname]);

  useEffect(() => {
    const hasStatesFilter = searchParams.getAll(PARAM_KEYS.STATES).length > 0;

    if (!hasStatesFilter) {
      const defaultStates = [AssetState.AVAILABLE, AssetState.NOT_AVAILABLE, AssetState.ASSIGNED];
      const updated = new URLSearchParams(searchParams.toString());
      defaultStates.forEach((state) => updated.append(PARAM_KEYS.STATES, state));
      updated.set(PARAM_KEYS.PAGE, '1');

      setSearchParams(updated);
    }
  }, [searchParams, setSearchParams]);

  return (
    <div className='flex h-full flex-col overflow-hidden px-5'>
      <div className='mb-4 flex flex-col'>
        <div>
          <h1 className='font-semibold text-red-500'>Asset List</h1>
        </div>

        <div className='mt-4 flex items-center justify-between gap-4'>
          <div className='flex items-center gap-4'>
            <FilterDropdown
              label='State'
              options={AssetStateHandler.getOptions('all').map((opt) => opt.label)}
              selected={states.map(getAssetStateLabel)}
              onChange={(selectedLabels) => {
                const selectedValues = selectedLabels
                  .map(getAssetStateValue)
                  .filter(Boolean) as string[];
                handleFilterStateChange(selectedValues);
              }}
              width='w-74'
            />

            <FilterDropdown
              label='Category'
              options={categories ? categories.map((c: Category) => c.name) : []}
              selected={categoriesDropdown}
              onChange={handleFilterCategoryChange}
              width='w-60'
            />
          </div>

          <div className='flex items-center gap-4'>
            <div className='h-full w-75'>
              <SearchBar
                defaultValue={search}
                onSearch={handleSearch}
                placeholder='Search by Asset Code or Asset Name'
              />
            </div>
            <Button
              onClick={navigateToCreateAsset}
              className='shrink-0 bg-red-600 px-6 text-white hover:bg-red-700'
            >
              Create new asset
            </Button>
          </div>
        </div>
      </div>

      <Table<Asset<'table'>>
        data={assets.filter((asset) => asset.id !== highlightedAsset?.id)}
        columns={columns}
        highlightedRows={highlightedAsset ? [highlightedAsset] : []}
        rowKey={'id' as keyof Asset<'table'>}
        sort={{ key: sort as keyof Asset<'table'>, order: sortOrder }}
        loading={isLoading}
        onSortChange={handleSortChange}
        pagination={{
          currentPage: page,
          totalPages,
          onPageChange: handlePageChange,
        }}
      />

      {isModalVisible && selectedAssetId && (
        <ViewAssetDetailModal
          visible={isModalVisible}
          openModal={openModal}
          closeModal={() => {
            closeModal();
            setSelectedAssetId(null);
          }}
          assetId={selectedAssetId || ''}
        />
      )}
      {isDeletionModalVisible && selectedAssetId && (
        <DeletionConfirmation
          id={selectedAssetId}
          cache={mutatePage}
          name={assets.find((asset) => asset.id === selectedAssetId)?.name || ''}
          visible={isDeletionModalVisible}
          openModal={openDeletionModal}
          closeModal={() => {
            setSelectedAssetId(null);
            setHighlightedAsset(null);
            closeDeletionModal();
          }}
        />
      )}
    </div>
  );
}
