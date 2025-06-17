import { FC, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useUser } from '@/context/UserContext';
import { UserService } from '@/services/UserService';
import { Modal } from './Modal';

interface LogoutModalProps {
  visible: boolean;
  openModal: () => void;
  closeModal: () => void;
}

export const LogoutModal: FC<LogoutModalProps> = ({ visible, openModal, closeModal }) => {
  const navigate = useNavigate();
  const userContext = useUser();
  const [isPending, setIsPending] = useState(false);

  const onLogout = async () => {
    setIsPending(true);
    const response = await UserService.logout();
    if (response.success) {
      userContext.setAuth(null);
      navigate('/login');
    } else {
      toast.error(response.error?.code);
    }
    setIsPending(false);
  };

  return (
    <Modal
      isOpen={visible}
      onOpen={openModal}
      onClose={closeModal}
      title="Are you sure?"
      primaryAction={{
        label: 'Log out',
        onClick: onLogout,
        variant: 'destructive',
        disabled: isPending,
      }}
      secondaryAction={{
        label: 'Cancel',
        onClick: closeModal,
        variant: 'secondary',
        disabled: isPending,
      }}
      maxWidth="sm:max-w-[300px]"
    >
      <span className='px-5'>Do you want to log out?</span>
    </Modal>
  );
};
