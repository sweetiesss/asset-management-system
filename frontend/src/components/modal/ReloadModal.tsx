import { BaseModalProp } from '@/hooks/useModal';
import { Modal } from './Modal';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface ReloadModalProps extends BaseModalProp {
  redirect?: string;
}

const ReloadModal: React.FC<ReloadModalProps> = ({ visible, openModal, closeModal, redirect }) => {
  const navigate = useNavigate();
  const [pressed, setPressed] = useState(false);
  const handleReload = () => {
    if(redirect) {
      navigate(redirect, { replace: true });
      return;
    }else{
      setPressed(true);
      window.location.reload();
    }
  };
  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      title='Reload Page'
      maxWidth='max-w-2xl'
      closeBtnEnabled={false}
      primaryAction={{
        label: 'Reload',
        onClick: handleReload,
        type: 'button',
        disabled: pressed,
      }}
    >
      <p className='px-5'>This entity has a newer version! Please reload this page!</p>
    </Modal>
  );
};

export default ReloadModal;
