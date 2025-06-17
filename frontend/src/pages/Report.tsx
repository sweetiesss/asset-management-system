import { ColumnType, Table } from '@/components/table/Table';
import { Button } from '@/components/ui/button';
import { DEFAULT_SORT_ORDER, PARAM_KEYS } from '@/configs/constants';
import { useGetReports, useGetStandardAssetReport } from '@/hooks/fetch/report';
import Report from '@/types/report';
import { SortType } from '@/types/type';
import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
// import React, { useEffect, useState } from 'react';
// import { Link } from 'react-router-dom';
// import {
//   DropdownMenu,
//   DropdownMenuContent,
//   DropdownMenuItem,
//   DropdownMenuTrigger,
// } from '@/components/ui/dropdown-menu';
// import { Triangle } from 'lucide-react';
import { toast } from 'react-toastify';
import Spinner from '@/components/Spinner';

import { downloadBuilder } from '@/utils/downloadBuilder';

const DEFAULT_PAGE = 1;
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT = 'category';
const ReportPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get(PARAM_KEYS.PAGE) || `${DEFAULT_PAGE}`, 10);
  const size = parseInt(searchParams.get(PARAM_KEYS.SIZE) || `${DEFAULT_PAGE_SIZE}`, 10);
  const sort = searchParams.get(PARAM_KEYS.SORT) || DEFAULT_SORT;
  const sortOrder =
    (searchParams.get(PARAM_KEYS.SORT_ORDER) as 'asc' | 'desc') || DEFAULT_SORT_ORDER;
  const {
    items: reports,
    pagination,
    isLoading,
  } = useGetReports({
    page: page - 1,
    size,
    sort,
    sortOrder,
  });
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

  const handleSortChange = (sort: SortType<Report>) => {
    updateParams({
      [PARAM_KEYS.SORT]: sort.key as string,
      [PARAM_KEYS.SORT_ORDER]: sort.order,
      [PARAM_KEYS.PAGE]: 1,
    });
  };

  const handlePageChange = (newPage: number) => {
    updateParams({ [PARAM_KEYS.PAGE]: newPage });
  };

  const totalPages = pagination?.totalPages || 1;
  const columns: ColumnType<Report>[] = [
    {
      key: 'category',
      title: 'Category',
      sortable: true,
      width: '150px',
    },
    {
      key: 'total',
      title: 'Total',
      sortable: true,
      width: '150px',
    },
    {
      key: 'assigned',
      title: 'Assigned',
      sortable: true,
      width: '150px',
    },
    {
      key: 'available',
      title: 'Available',
      sortable: true,
      width: '150px',
    },
    {
      key: 'notAvailable',
      title: 'Not Available',
      sortable: true,
      width: '150px',
    },
    {
      key: 'waitingForRecycling',
      title: 'Waiting for recycling',
      sortable: true,
      width: '150px',
    },
    {
      key: 'recycled',
      title: 'Recycled',
      sortable: true,
      width: '150px',
    },
  ];

  return (
    <div className='flex h-full flex-col overflow-hidden px-5'>
      <div className='mb-4 flex flex-col'>
        <h1 className='font-semibold text-red-500'>Report</h1>
      </div>

      <div className='flex justify-end pr-2 pb-2'>
        <ExportBtnWrapper />
      </div>

      <div className='min-h-0 grow overflow-y-auto'>
        <Table<Report>
          data={reports}
          columns={columns}
          loading={isLoading}
          rowKey='category'
          sort={{ key: sort as keyof Report, order: sortOrder }}
          onSortChange={handleSortChange}
          pagination={{
            currentPage: page,
            totalPages,
            onPageChange: handlePageChange,
          }}
        />
      </div>
    </div>
  );
};

const ExportBtnWrapper = () => {
  const { trigger, blobUrl, fileName, error: blobError, isMutating } = useGetStandardAssetReport();
  useEffect(() => {
    if (blobError) {
      console.error('Error fetching report file:', blobError);
      toast.error(blobError.response?.data.error.message);
    }
  }, [blobError]);

  useEffect(() => {
    if (blobUrl && fileName) {
      downloadBuilder(fileName, blobUrl);
    }
  }, [blobUrl, fileName]);

  const handleStandardExportClick = async () => {
    await trigger();
  };
  return (
    <Button onClick={handleStandardExportClick}>
      {isMutating ? (
        <span className='inline-flex items-center gap-x-2'>
          <Spinner />
          Exporting…
        </span>
      ) : (
        <span className='inline-flex items-center gap-x-2'>Export</span>
      )}
    </Button>
  );
};

// const Export: React.FC = () => {
//   const [openMenu, setOpenMenu] = useState(false);
//   const { trigger, blobUrl, fileName, error: blobError, isMutating } = useGetStandardAssetReport();
//   useEffect(() => {
//     if (blobError) {
//       console.error('Error fetching report file:', blobError);
//       toast.error(blobError.response?.data.error.message);
//     }
//   }, [blobError]);

//   useEffect(() => {
//     if (blobUrl && fileName) {
//       downloadBuilder(fileName, blobUrl);
//     }
//   }, [blobUrl, fileName]);

//   const handleStandardExportClick = async () => {
//     await trigger();
//   };

//   return (
//     <DropdownMenu onOpenChange={(open) => setOpenMenu(open)}>
//       <DropdownMenuTrigger asChild>
//         <Button variant='default' className='bg-primary text-white'>
//           <ExportButton openMenu={openMenu} isMutating={isMutating} />
//         </Button>
//       </DropdownMenuTrigger>
//       <DropdownMenuContent className='w-56'>
//         <DropdownMenuItem className='hover:bg-primary' onClick={handleStandardExportClick}>
//           Standard Export
//         </DropdownMenuItem>
//         <DropdownMenuItem>
//           <Link to='./create'>Detailed Export</Link>
//         </DropdownMenuItem>
//       </DropdownMenuContent>
//     </DropdownMenu>
//   );
// };

// const ExportButton: React.FC<{ openMenu: boolean; isMutating: boolean }> = ({
//   openMenu,
//   isMutating,
// }) => {
//   return (
//     <p className='leading-none'>
//       {isMutating ? (
//         <span className='inline-flex items-center gap-x-2'>
//           <Spinner />
//           Exporting…
//         </span>
//       ) : openMenu ? (
//         <span className='inline-flex items-center gap-x-2'>
//           Export
//           <Triangle width={2} height={2} />
//         </span>
//       ) : (
//         <span className='inline-flex items-center gap-x-2'>
//           Export
//           <Triangle className='rotate-180' width={2} height={2} />
//         </span>
//       )}
//     </p>
//   );
// };

export default ReportPage;
