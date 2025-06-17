import { SortType } from '@/types/type';
import { ChevronDown, ChevronUp } from 'lucide-react';
import { useState, useRef, useEffect, JSX } from 'react';
import Pagination from '../Pagination';

const MIN_WIDTH_EACH_COLUMN = 100;
const DEFAULT_SELECT_MODE = 'single';

/**
 * Represents the configuration for a table column.
 * @template T - The type of the data records in the table.
 */
export type ColumnType<T> = {
  /** The display title of the column. */
  title: string;

  /** The unique key for the column, used to identify it. */
  key: string | number;

  /** Whether the column is sortable. Defaults to false. */
  sortable?: boolean;

  /** The width of the column, specified as a string (e.g., '100px') or number (pixels). */
  width?: string | number;

  /** The minimum width of the column in pixels. */
  minWidth?: number;

  /** The text alignment for the column content. */
  align?: 'left' | 'center' | 'right';

  /** A custom render function to display the column's value. */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  render?: (value: any, record: T) => React.ReactNode;
};

/**
 * Props for the Table component.
 * @template T - The type of the data records in the table.
 */
interface TableProps<T> extends React.HTMLAttributes<HTMLTableElement> {
  /** Array of column configurations for the table. */
  columns: ColumnType<T>[];

  /** Array of data records to display in the table. */
  data: T[];

  highlightedRows?: T[];

  /** The key to uniquely identify each row in the data. */
  rowKey: keyof T;

  /** Whether the columns are resizable. Defaults to false. */
  resizable?: boolean;

  /** The current sort state of the table. */
  sort?: SortType<T>;

  /** Whether the table is in a loading state. Defaults to false. */
  loading?: boolean;

  /** Whether row selection is enabled. Defaults to false. */
  enableRowSelection?: boolean;
  rowSelectionMode?: 'single' | 'multiple';

  /** The key used to identify the row for selection. Defaults to 'id'. */
  countColumns?: boolean;

  /** Pagination configuration for the table. */
  pagination?: {
    /** The current page number. */
    currentPage: number;
    /** The total number of pages. */
    totalPages: number;
    /** Size button ui */
    buttonSize?: 'sm' | 'md';
    /** Callback function triggered when the page changes. */
    onPageChange: (page: number) => void;
  };
  /** Callback function triggered when the sort state changes. */
  onSortChange?: (sort: SortType<T>) => void;

  /** Callback function triggered when selected rows change. */
  onSelectColumns?: (selectedRow: T[]) => void;
  onSelectColumnsKey?: (selectedRow: T[keyof T][]) => void;

  /** Default selected row keys, used for controlled selection. */
  defaultSelectedRowKeys?: T[keyof T][];
}

/**
 * A generic table component that supports sorting, resizing, selection, and pagination.
 * @template T - The type of the data records in the table.
 * @param props - The props for configuring the table.
 * @returns A React component rendering a table with the specified features.
 */
export const Table = <T,>(props: TableProps<T>) => {
  const [sort, setSort] = useState<SortType<T> | null>(props.sort || null);
  const [selectedRows, setSelectedRows] = useState<T[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<T[keyof T][]>([]);

  const [columnWidths, setColumnWidths] = useState<Record<string, string>>(
    props.columns.reduce(
      (acc, col) => ({
        ...acc,
        [col.key]: col.width ? String(col.width) : 'auto',
      }),
      {}
    )
  );

  const minWidthsRecord = props.columns.reduce(
    (acc, col) => {
      acc[col.key] = col.minWidth || MIN_WIDTH_EACH_COLUMN;
      return acc;
    },
    {} as Record<string, number>
  );

  const resizingRef = useRef<{ key: string | number; startX: number } | null>(null);
  const tableRef = useRef<HTMLTableElement>(null);
  const initTableWidth = tableRef.current?.offsetWidth;

  const handleSort = (key: keyof T) => {
    if (sort?.key === key) {
      setSort({
        key,
        order: sort.order === 'asc' ? 'desc' : 'asc',
      });
    } else {
      setSort({ key, order: 'desc' });
    }
  };

  /**
   * Initiates column resizing on mouse down.
   */
  const handleMouseDown = (e: React.MouseEvent, key: string | number) => {
    e.preventDefault();
    resizingRef.current = { key, startX: e.clientX };
    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  };

  /**
   * Updates column width during resizing.
   */
  const handleMouseMove = (e: MouseEvent) => {
    if (resizingRef.current && tableRef.current) {
      const { key, startX } = resizingRef.current;
      const thElement = tableRef.current.querySelector(`th[data-key="${key}"]`);
      if (thElement) {
        const rect = thElement.getBoundingClientRect();
        const newWidth = rect.width + (e.clientX - startX);

        if (
          newWidth > minWidthsRecord[key] &&
          tableRef.current.offsetWidth <= (initTableWidth || 0)
        ) {
          setColumnWidths((prev) => ({
            ...prev,
            [key]: `${newWidth}px`,
          }));
          resizingRef.current.startX = e.clientX;
        }
      }
    }
  };

  /**
   * Cleans up event listeners when resizing is complete.
   */
  const handleMouseUp = () => {
    resizingRef.current = null;
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
  };

  const addSelectedRows = (value: T) => {
    if ((props.rowSelectionMode || DEFAULT_SELECT_MODE) === 'single') {
      setSelectedRows([value]);
      setSelectedRowKeys([value[props.rowKey as keyof T]]);
      return;
    } else if (
      (props.rowSelectionMode || DEFAULT_SELECT_MODE) === 'multiple' &&
      !selectedRows.includes(value)
    ) {
      setSelectedRows((prev) => [...prev, value]);
      setSelectedRowKeys((prev) => [...prev, value[props.rowKey as keyof T]]);
      return;
    }
  };
  const removeSelectedRows = (value: T) => {
    setSelectedRows((prev) => prev.filter((item) => item !== value));
    setSelectedRowKeys((prev) => prev.filter((key) => key !== value[props.rowKey as keyof T]));
  };

  /**
   * Cleans up event listeners on component unmount.
   */
  useEffect(() => {
    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, []);

  useEffect(() => {
    if (sort) {
      props.onSortChange?.(sort || props.sort);
    }
  }, [sort]);

  useEffect(() => {
    if (props.onSelectColumns) {
      props.onSelectColumns(selectedRows);
    }
    if (props.onSelectColumnsKey) {
      props.onSelectColumnsKey(selectedRowKeys);
    }
  }, [selectedRows, selectedRowKeys]);

  return (
    <div className='flex flex-col h-full overflow-hidden'>
      <div className='flex flex-col flex-1 overflow-hidden'>
        <div className='pr-[8px]'>
          <table
            ref={tableRef}
            className='w-full min-w-full text-left border border-gray-300 rounded table-fixed'
          >
            <TableHeader
              columns={props.columns}
              sort={sort}
              onSort={handleSort}
              resizable={props.resizable}
              columnWidths={columnWidths}
              countColumns={props.countColumns || false}
              select={props.enableRowSelection || false}
              handleMouseDown={handleMouseDown}
            />
          </table>
        </div>

        <div className='flex-1 overflow-x-hidden overflow-y-scroll'>
          <table className='w-full text-left border border-gray-300 rounded table-fixed'>
            <TableBody
              columns={props.columns}
              data={props.data}
              highlightedRows={props.highlightedRows || []}
              rowKey={props.rowKey}
              loading={props.loading}
              select={props.enableRowSelection || false}
              selectMode={props.rowSelectionMode || DEFAULT_SELECT_MODE}
              columnWidths={columnWidths}
              countColumns={props.countColumns || false}
              addSelectedRows={addSelectedRows}
              removeSelectedRows={removeSelectedRows}
              defaultSelectedRowKeys={props.defaultSelectedRowKeys || []}
            />
          </table>
        </div>
      </div>

      {props.pagination && (
        <div className='flex justify-end w-full my-2'>
          <Pagination
            currentPage={props.pagination?.currentPage}
            totalPages={props.pagination?.totalPages}
            onPageChange={props.pagination.onPageChange}
            buttonSize={props.pagination?.buttonSize || 'md'}
          />
        </div>
      )}
    </div>
  );
};

interface TableBodyProps<T> {
  columns: ColumnType<T>[];
  data: T[];
  loading?: boolean;
  rowKey: keyof T;
  select: boolean;
  selectMode: 'single' | 'multiple';
  countColumns?: boolean;
  columnWidths: Record<string, string>;
  highlightedRows?: T[];
  defaultSelectedRowKeys?: T[keyof T][];
  addSelectedRows?: (value: T) => void;
  removeSelectedRows?: (value: T) => void;
}

const TableBody: <T>(props: TableBodyProps<T>) => JSX.Element = <T,>({
  columns,
  data,
  rowKey,
  loading,
  select,
  selectMode,
  columnWidths,
  countColumns = false,
  highlightedRows = [],
  defaultSelectedRowKeys = [],
  addSelectedRows,
  removeSelectedRows,
}: TableBodyProps<T>) => {
  const handleSelectRow = (e: React.ChangeEvent<HTMLInputElement>, item: T) => {
    if (e.target.checked) {
      addSelectedRows?.(item);
    } else {
      removeSelectedRows?.(item);
    }
  };
  return (
    <tbody>
      {loading && (
        <tr>
          <td colSpan={columns.length} className='p-4 text-center text-gray-500'>
            Loading
          </td>
        </tr>
      )}

      {!loading && data.length + highlightedRows.length === 0 && (
        <tr>
          <td colSpan={columns.length} className='p-4 text-center text-gray-500'>
            No results were found
          </td>
        </tr>
      )}

      {highlightedRows.length > 0 &&
        !loading &&
        highlightedRows.map((item, index) => (
          <tr key={String(item[rowKey])} className='bg-yellow-100 hover:bg-yellow-300'>
            {select && (
              <td className='box-border w-12 p-2 text-center border'>
                <input
                  type={selectMode === 'single' ? 'radio' : 'checkbox'}
                  name='select-row'
                  defaultChecked={defaultSelectedRowKeys.includes(item[rowKey as keyof T])}
                  onChange={(e) => handleSelectRow(e, item)}
                  className='cursor-pointer text-primary'
                />
              </td>
            )}

            {countColumns && (
              <td className='box-border w-12 p-2 text-center border'>{index + 1}</td>
            )}

            {columns.map((column, index) => (
              <td
                key={index}
                className={`box-border overflow-hidden border p-2 break-words text-${column.align || 'left'}`}
                style={{ width: columnWidths[column.key] || 'auto' }}
              >
                {column.render
                  ? column.render(item[column.key as keyof T], item)
                  : column.key
                    ? String(item[column.key as keyof T])
                    : ''}
              </td>
            ))}
          </tr>
        ))}

      {data.length > 0 &&
        data.map((item, index) => (
          <tr key={String(item[rowKey])} className='hover:bg-gray-50'>
            {select && (
              <td className='box-border w-12 p-2 text-center border'>
                <input
                  type={selectMode === 'single' ? 'radio' : 'checkbox'}
                  name='select-row'
                  defaultChecked={defaultSelectedRowKeys.includes(item[rowKey as keyof T])}
                  onChange={(e) => handleSelectRow(e, item)}
                  className='cursor-pointer text-primary'
                />
              </td>
            )}

            {countColumns && (
              <td className='box-border w-12 p-2 text-center border'>
                {index + 1 + highlightedRows.length}
              </td>
            )}

            {columns.map((column, index) => (
              <td
                key={index}
                className={`box-border overflow-hidden border p-2 break-words text-${column.align || 'left'}`}
                style={{ width: columnWidths[column.key] || 'auto' }}
              >
                {column.render
                  ? column.render(item[column.key as keyof T], item)
                  : column.key
                    ? String(item[column.key as keyof T])
                    : ''}
              </td>
            ))}
          </tr>
        ))}
    </tbody>
  );
};

interface TableHeaderProps<T> {
  columns: ColumnType<T>[];
  sort: SortType<T> | null;
  onSort: (key: keyof T) => void;
  resizable?: boolean;
  select: boolean;
  columnWidths: Record<string, string>;
  countColumns?: boolean;
  handleMouseDown: (e: React.MouseEvent, key: string | number) => void;
}

const TableHeader: <T>(props: TableHeaderProps<T>) => JSX.Element = <T,>({
  columns,
  sort,
  onSort,
  resizable,
  select,
  columnWidths,
  countColumns = false,
  handleMouseDown,
}: TableHeaderProps<T>) => {
  return (
    <thead className='w-full px-4 py-3 text-sm font-medium text-left text-gray-900 bg-gray-100 border-b border-gray-200 select-none'>
      <tr className='cursor-pointer'>
        {select && <th className='box-border relative w-12 p-2 border'></th>}
        {countColumns && <th className='box-border relative w-12 p-2 border'>No.</th>}
        {columns.map((column, index) => (
          <th
            key={column.key}
            data-key={column.key}
            className='box-border relative p-2 border'
            style={{ width: columnWidths[column.key] || 'auto' }}
          >
            <div
              className='flex items-center justify-between w-full h-full px-2'
              onClick={() => column.sortable && onSort(column.key as keyof T)}
            >
              <span>{column.title}</span>
              {column.sortable && (
                <div className='flex flex-col items-center'>
                  <ChevronUp
                    strokeWidth={3}
                    className={`${sort?.key === column.key && sort?.order === 'desc' ? 'text-gray-800' : 'text-gray-400'}`}
                    size={16}
                  />
                  <ChevronDown
                    strokeWidth={3}
                    className={`${sort?.key === column.key && sort?.order === 'asc' ? 'text-gray-800' : 'text-gray-400'}`}
                    size={16}
                  />
                </div>
              )}
            </div>
            {resizable && index < columns.length - 1 && (
              <div
                className='absolute top-0 right-[-2px] h-full w-1 transform-none cursor-col-resize bg-gray-400 hover:bg-gray-600'
                style={{ userSelect: 'none' }}
                onMouseDown={(e) => handleMouseDown(e, column.key)}
              />
            )}
          </th>
        ))}
      </tr>
    </thead>
  );
};
