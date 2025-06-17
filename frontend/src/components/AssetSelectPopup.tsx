import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import SearchBar from './SearchBar';
import { ColumnType, Table } from './table/Table';
import { SortType } from '@/types/type';
import { useEffect, useState } from 'react';
import { Button } from './ui/button';

import { Asset, AssetState } from '@/types/asset';
import { useGetAssets } from '@/hooks/fetch/assets';

const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 5;
const DEFAULT_SORT = 'name';
const DEFAULT_STATES: AssetState[] = [AssetState.AVAILABLE];

type AssetSelectPopupProps = {
  /**
   * The currently selected asset.
   * If null, no asset is selected.
   */
  selectedAsset?: Asset<'table'> | null;

  /**
   * Callback function to handle asset selection changes.
   * It will be called with the newly selected asset when the asset confirms their selection.
   */
  onAssetSelectChange?: (asset: Asset<'table'>) => void;
};

export const AssetSelectPopup = ({ selectedAsset, onAssetSelectChange }: AssetSelectPopupProps) => {
  const [page, setPage] = useState(DEFAULT_PAGE);
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [search, setSearch] = useState('');
  const [isOpenPopup, setIsOpenPopup] = useState(false);

  const [confirmSelectedAsset, setConfirmSelectedAsset] = useState<Asset<'table'> | null>(
    selectedAsset || null
  );
  const [lastSelectedAsset, setLastSelectedAsset] = useState<Asset<'table'> | null>(null);

  const { items: assets, pagination, isLoading } = useGetAssets({
      page: page - 1,
      size: DEFAULT_PAGE_SIZE,
      search,
      sort,
      sortOrder,
      states: DEFAULT_STATES,
    });
  const handleSortChange = (sort: SortType<Asset<"table">>) => {
    setSort(sort.key as string);
    setSortOrder(sort.order);
    setPage(DEFAULT_PAGE);
  };

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(DEFAULT_PAGE);
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  const handleAssetSelect = (items: Asset<'table'>[]) => {
    setLastSelectedAsset(items[0] || lastSelectedAsset);
  };

  useEffect(() => {
    if (confirmSelectedAsset) {
      onAssetSelectChange?.(confirmSelectedAsset);
    }
  }, [confirmSelectedAsset]);

  useEffect(() => {
    if (selectedAsset) {
      setConfirmSelectedAsset(selectedAsset);
      setLastSelectedAsset(selectedAsset);
    }
  }, [selectedAsset]);

  const columns: ColumnType<Asset<'table'>>[] = [
    {
      key: 'code',
      title: 'Asset Code',
      sortable: true,
      width: '150px',
    },
    {
      key: 'name',
      title: 'Asset Name',
      sortable: true,
      minWidth: 150,
    },
    {
      key: 'categoryName',
      title: 'Category',
      sortable: true,
    },
  ];

  return (
    <Popover open={isOpenPopup} onOpenChange={setIsOpenPopup}>
      <PopoverTrigger className='w-full'>
        <div className='w-full'>
          <SearchBar readOnly defaultValue={confirmSelectedAsset?.name || lastSelectedAsset?.name} onSearch={() => {}} />
        </div>
      </PopoverTrigger>
      <PopoverContent
        className='flex h-full max-h-[600px] max-w-[700px] flex-col overflow-hidden bg-white px-5 py-0'
        side='bottom'
        align='start'
      >
        <div className='flex h-full flex-col overflow-hidden px-5'>
          <div className='mb-4 flex flex-col'>
            <div className='mt-4 flex items-center justify-between gap-4'>
              <h2 className='text-primary text-base font-semibold'>Select Asset</h2>
              <SearchBar defaultValue={search} onSearch={handleSearchChange} />
            </div>
          </div>

          <Table
            columns={columns}
            data={assets}
            rowKey={'id'}
            enableRowSelection
            rowSelectionMode='single'
            sort={{
              key: sort as keyof Asset<'table'>,
              order: sortOrder,
            }}
            pagination={{
              currentPage: page,
              totalPages: pagination?.totalPages || 0,
              onPageChange: handlePageChange,
              buttonSize: 'sm'
            }}
            onSortChange={handleSortChange}
            onSelectColumns={handleAssetSelect}
            defaultSelectedRowKeys={confirmSelectedAsset ? [confirmSelectedAsset.id] : []}
          ></Table>
          <div className='my-4 flex justify-end'>
            <Button
              disabled={isLoading}
              onClick={() => {
                setConfirmSelectedAsset(lastSelectedAsset);
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
