interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  buttonSize?: 'sm' | 'md';
  className?: string;
}

const btnHeightSize =  {
  "sm": "h-8",
  "md": "h-10",
}

const btnWidthSize =  {
  "sm": "w-8",
  "md": "w-10",
}

const Pagination = ({ currentPage, totalPages, buttonSize = 'md', onPageChange, className = '' }: PaginationProps) => {
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;

    if (totalPages <= maxPagesToShow) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);

      let startPage = Math.max(2, currentPage - 1);
      let endPage = Math.min(totalPages - 1, currentPage + 1);

      if (currentPage <= 3) {
        endPage = Math.min(totalPages - 1, 4);
      }

      if (currentPage >= totalPages - 2) {
        startPage = Math.max(2, totalPages - 3);
      }

      if (startPage > 2) {
        pages.push('ellipsis-start');
      }

      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      if (endPage < totalPages - 1) {
        pages.push('ellipsis-end');
      }

      pages.push(totalPages);
    }

    return pages;
  };

  const handlePageChange = (page: number) => {
    if (page !== currentPage && page >= 1 && page <= totalPages) {
      onPageChange(page);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  return (
    <div className={`my-2 flex items-center justify-center space-x-2 ${className}`}>
      {/* Previous button */}
      <button
        onClick={() => handlePageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className={`flex w-auto items-center justify-center rounded-md border px-4 font-medium ${btnHeightSize[buttonSize]} ${
          currentPage === 1
            ? 'cursor-not-allowed border-gray-200 text-gray-400'
            : 'border-gray-300 text-red-600 hover:bg-gray-100'
        } `}
        aria-label='Previous page'
      >
        Previous
      </button>

      {/* Page numbers */}
      {getPageNumbers().map((page, index) => {
        if (page === 'ellipsis-start' || page === 'ellipsis-end') {
          return (
            <span key={`${page}-${index}`} className={`flex items-center justify-center ${btnHeightSize[buttonSize]} ${btnWidthSize[buttonSize]}`}>
              ...
            </span>
          );
        }

        return (
          <button
            key={`page-${page}`}
            onClick={() => handlePageChange(page as number)}
            className={`${btnHeightSize[buttonSize]} ${btnWidthSize[buttonSize]} rounded-md font-medium ${currentPage === page ? 'bg-red-600 text-white' : 'border border-gray-300 text-black hover:bg-gray-100'} `}
            aria-label={`Page ${page}`}
            aria-current={currentPage === page ? 'page' : undefined}
          >
            {page}
          </button>
        );
      })}

      <button
        onClick={() => handlePageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className={`flex ${btnHeightSize[buttonSize]} w-auto items-center justify-center rounded-md border px-4 font-medium ${
          currentPage === totalPages
            ? 'cursor-not-allowed border-gray-200 text-gray-400'
            : 'border-gray-300 text-red-600 hover:bg-gray-100'
        } `}
        aria-label='Next page'
      >
        Next
      </button>
    </div>
  );
};

export default Pagination;
