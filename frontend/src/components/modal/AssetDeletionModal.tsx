import { BaseModalProp } from '@/hooks/useModal';
import Spinner from '@/components/Spinner';
import { Link } from 'react-router-dom';
import { Modal } from './Modal';
import { toast } from 'react-toastify';
import ReloadModal from './ReloadModal';
import { useDeleteAsset } from '@/hooks/fetch/assets';
import { useEffect, useState } from 'react';
import { KeyedMutator } from 'swr';
import { Page } from '@/types/type';
import { Asset } from '@/types/asset';

type DeletionProps = BaseModalProp & {
  id: string;
  name: string;
  cache: KeyedMutator<Page<Asset<"table">>>
};
const DeletionConfirmation: React.FC<DeletionProps> = ({ visible, openModal, closeModal, id, name, cache }) => {
  const { triggerDelete, isDeleting, deleteError } = useDeleteAsset(id);
  const [ modalIndex, setModalIndex] = useState(0);
  const handleDeleteAsset = async () => {
    await triggerDelete();
    if (!deleteError) {
      await cache((prev) => {
        if (!prev) return prev;
        const updatedAsset = prev.content.filter(asset => asset.id !== id);
        return {
          ...prev,
          content: updatedAsset
        }
      }, {
        revalidate: false,
      })
      toast.success(`Asset ${name} deleted successfully.`);
      closeModal();
    }
  };
  useEffect(() => {
    if(deleteError){
      console.error('Error deleting asset:', deleteError);
      switch(deleteError.code){
        case 'ASSET_NOT_DELETABLE':
          setModalIndex(1);
          break;
        case 'ASSET_NOT_FOUND':
          setModalIndex(2);
          break;
        default:
          setModalIndex(0);
      }
    }
  }, [deleteError])

  return (
    <>
    { modalIndex === 0 && <AreYouSure
      visible={visible}
      openModal={openModal}
      closeModal={closeModal}
      isDeleting={isDeleting}
      handleDeleteAsset={handleDeleteAsset}
    />}
    { modalIndex === 1 && <AssetNotDeletable
      visible={visible}
      openModal={openModal}
      closeModal={closeModal}
      id={id}
    />}
    { modalIndex === 2 && <ReloadModal visible={visible} openModal={openModal} closeModal={closeModal}/>}
    </>
  );
};

interface AreYouSureProps extends BaseModalProp {
  isDeleting: boolean;
  handleDeleteAsset: () => Promise<void>;
}


const AreYouSure: React.FC<AreYouSureProps> = ({visible, openModal, closeModal, isDeleting, handleDeleteAsset}) => {
  return(
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      maxWidth='max-w-2xl'
      title='Are you sure?'
      closeBtnEnabled={false}
      primaryAction={{
        label: (
          <div className='flex items-center justify-center gap-x-0.5'>
            {' '}
            {/* Ensured centering for spinner */}
            {isDeleting && <Spinner />}
            Delete
          </div>
        ),
        onClick: handleDeleteAsset,
        type: 'button',
        variant: 'destructive',
        disabled: isDeleting,
      }}
      secondaryAction={{
        label: 'Cancel',
        onClick: closeModal,
        type: 'button',
        variant: 'outline',
        disabled: isDeleting,
      }}
    >
      <div className='px-5'>Do you want to delete this asset?</div>
    </Modal>
  )
}

interface AssetNotDeletableProps extends BaseModalProp{
  id: string;
}

const AssetNotDeletable: React.FC<AssetNotDeletableProps> = ({visible, openModal, closeModal, id}) => {
  return(
    <Modal
        isOpen={visible}
        onOpen={openModal}
        onClose={closeModal}
        maxWidth='max-w-2xl'
        title='Cannot Delete Asset'
        closeBtnEnabled={true}
      >
        <div className='px-5 py-4'>
          <p>Cannot delete the asset because it belongs to one or more historical assignments.</p>
          <p>
            If the asset is not able to be used anymore, please update its state in{' '}
            <Link to={`/assets/${id}/edit`} className='text-blue-500 underline-offset-4'>
              Edit Asset page
            </Link>
          </p>
        </div>
      </Modal>
  )
}

export default DeletionConfirmation;
