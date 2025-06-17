import { FC, useEffect, useState } from 'react';
import { useAssetHistory } from '@/hooks/fetch/useAssets';
import { type AssetHistory } from '@/types/asset';
import { formatDate } from '@/utils/dateUtils';
import Pagination from '../Pagination';
import { Asset, AssetStateHandler } from '@/types/asset';
import { Modal } from './Modal';
import { useGetAssetDetail } from '@/hooks/fetch/assets';
import { toast } from 'react-toastify';
import TruncateWithTooltip from '@/utils/truncate-with-toolip';

interface ViewAssetDetailModalProps {
  visible: boolean;
  openModal: () => void;
  closeModal: () => void;
  assetId: string;
}
const ViewAssetDetailModal: FC<ViewAssetDetailModalProps> = ({
  visible,
  openModal,
  closeModal,
  assetId,
}) => {
  const { data: asset, error } = useGetAssetDetail(assetId)
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 5;
  const { history, pagination } = useAssetHistory(assetId, { page: currentPage, size: pageSize });
  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage - 1);
  };
  const [modalIndex, setModalIndex] = useState(0); // 0: loading, 1: detail, 2: error
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        closeModal();
      }
    };
    if (visible) {
      window.addEventListener('keydown', handleEsc);
    }
    return () => {
      window.removeEventListener('keydown', handleEsc);
    };
  }, [visible, closeModal]);

  useEffect(() => {
    if (error) {
      closeModal();
      toast.error(`${error.message}`)
    } else if (asset) {
      setModalIndex(1);
    } else {
      setModalIndex(0);
    }
  }, [asset, error]);

  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      title='Detailed Asset Information'
      maxWidth='max-w-3xl'
    >
      <div className='w-full'>
        {modalIndex === 0 && <LoadingDisplay />}
        {modalIndex === 1 && asset && history && (
          <AssetDetail
            asset={asset}
            history={history}
            currentPage={currentPage}
            totalPages={pagination?.totalPages ?? 1}
            onPageChange={handlePageChange}
          />
        )}
      </div>
    </Modal>
  );
};

const AssetDetail: FC<{
  asset: Asset<'detail'>;
  history: AssetHistory[];
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}> = ({ asset, history, currentPage, totalPages, onPageChange }) => (
  <div className='pb-5 pl-5 pr-0'>
    <table className='w-full border-none table-auto'>
      <tbody>
        <tr>
          <td className='w-[20%] px-4 py-2 font-semibold'>Asset Code</td>
          <td className='w-[80%] px-4 py-2'>{asset.code}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold align-top'>Asset Name</td>
          <td className='px-4 py-2 align-top'>
            <div className='max-w-xl overflow-auto break-words whitespace-pre-wrap max-h-20'>
              {asset.name}
            </div>
          </td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Category</td>
          <td className='px-4 py-2'>
            <div className='max-w-xl overflow-auto break-words whitespace-pre-wrap max-h-20'>
              {asset.category.name}
            </div>
          </td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Installed Date</td>
          <td className='px-4 py-2'>{formatDate(asset.installedDate)}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>State</td>
          <td className='px-4 py-2'>{AssetStateHandler.parse(asset.state).label()}</td>
        </tr>
        <tr>
          <td className='px-4 py-2 font-semibold'>Location</td>
          <td className='px-4 py-2'>{asset.location.name}</td>
        </tr>
        <td className='px-4 py-2 font-semibold align-top'>Specification</td>
        <td className='px-4 py-2 align-top'>
          <div className='max-w-xl overflow-auto break-words whitespace-pre-wrap max-h-20'>
            {asset.specification}
          </div>
        </td>
        <tr>
          <td className='px-4 py-2 font-semibold align-top'>History</td>
          <div className='max-w-xl overflow-auto break-words whitespace-pre-wrap max-h-60'>
            <td className='px-4 py-2 whitespace-nowrap'>
              {history.length > 0 ? (
                <div className='overflow-auto max-h-40'>
                  <table className='min-w-full text-sm border-separate border-spacing-x-2'>
                    <thead className='bg-inherit'>
                      <tr>
                        <th className='px-2 py-1 font-semibold text-left border-2 border-x-transparent border-t-transparent border-b-black'>
                          Date
                        </th>
                        <th className='px-2 py-1 font-semibold text-left border-2 border-x-transparent border-t-transparent border-b-black'>
                          Assigned to
                        </th>
                        <th className='px-2 py-1 font-semibold text-left border-2 border-x-transparent border-t-transparent border-b-black'>
                          Assigned by
                        </th>
                        <th className='px-2 py-1 font-semibold text-left border-2 border-x-transparent border-t-transparent border-b-black'>
                          Returned Date
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {history.map((entry, index) => (
                        <tr key={index}>
                          <td className='px-2 py-1 border-2 border-x-transparent border-t-transparent border-b-gray-300'>
                            {entry?.assignedDate ? formatDate(entry.assignedDate) : ''}
                          </td>
                          <td className='px-2 py-1 border-2 border-x-transparent border-t-transparent border-b-gray-300'>
                            <div className='overflow-hidden break-all whitespace-pre-wrap line-clamp-2 text-ellipsis max-h-20'>
                              <TruncateWithTooltip text={entry.assignedTo}>
                                <span className='block'>{entry.assignedTo}</span>
                              </TruncateWithTooltip>
                            </div>
                          </td>
                          <td className='px-2 py-1 border-2 border-x-transparent border-t-transparent border-b-gray-300'>
                            <div className='overflow-auto break-all whitespace-pre-wrap max-h-20'>
                            <TruncateWithTooltip text={entry.assignedBy}>
                                <span className='block'>{entry.assignedBy}</span>
                              </TruncateWithTooltip>
                            </div>
                          </td>
                          <td className='px-2 py-1 border-2 border-x-transparent border-t-transparent border-b-gray-300'>
                            {entry?.returnedDate ? formatDate(entry.returnedDate) : ''}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <span className='italic text-gray-500'>No history found</span>
              )}
            </td>
          </div>
        </tr>
      </tbody>
    </table>
    <Pagination currentPage={currentPage + 1} totalPages={totalPages} onPageChange={onPageChange} />    
  </div>
);
const LoadingDisplay: FC = () => (
  <div className='flex items-center justify-center w-full h-full p-5'>
    <p className='text-gray-600'>Loading asset...</p>
  </div>
);

export default ViewAssetDetailModal;
