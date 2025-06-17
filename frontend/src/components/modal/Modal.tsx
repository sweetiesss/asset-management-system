import { FC, ReactNode, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog';
import { Button } from '../ui/button';
import { X } from 'lucide-react';

interface ModalProps {
  isOpen: boolean;
  onOpen: () => void;
  onClose: () => void;
  title: string;
  children: ReactNode;
  closeBtnEnabled?: boolean;
  primaryAction?: {
    label: string | ReactNode;
    onClick?: () => void;
    type?: 'button' | 'submit' | 'reset';
    variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link';
    disabled?: boolean;
  };
  secondaryAction?: {
    label: string | ReactNode;
    onClick?: () => void;
    type?: 'button' | 'submit' | 'reset';
    variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link';
    disabled?: boolean;
  };
  maxWidth?: string;
}

export const Modal: FC<ModalProps> = ({
  isOpen,
  onOpen,
  onClose,
  title,
  children,
  closeBtnEnabled = false,
  primaryAction,
  secondaryAction,
  maxWidth = '',
}) => {
  useEffect(() => {
    if(!closeBtnEnabled) return;
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    if (isOpen) {
      window.addEventListener('keydown', handleEsc);
    }

    return () => {
      window.removeEventListener('keydown', handleEsc);
    };
  }, [isOpen, onClose]);

  return (
    <Dialog open={isOpen} onOpenChange={onOpen}>
      <DialogContent className={`${maxWidth}`}>
        <DialogHeader className={`text-primary rounded-t-lg bg-gray-300 p-5 text-lg font-medium`}>
          <DialogTitle className='flex items-center justify-between text-2xl font-medium'>
            <p>{title}</p>
            {!closeBtnEnabled && (
              <CloseModalBtn
                closeModal={onClose}
                className='transition-colors duration-200 hover:text-red-500'
              />
            )}
          </DialogTitle>
        </DialogHeader>
        <div className={`${maxWidth}`}>{children}</div>
        {(primaryAction || secondaryAction) && (
          <DialogFooter className='px-4 py-2'>
            <div className='flex justify-end w-full gap-2'>
              {primaryAction && (
                <Button
                  variant={primaryAction.variant || 'default'}
                  className='border-2'
                  onClick={primaryAction?.onClick}
                  disabled={primaryAction.disabled}
                  type={primaryAction.type || 'button'}
                >
                  {primaryAction.label}
                </Button>
              )}
              {secondaryAction && (
                <Button
                  variant={secondaryAction.variant || 'secondary'}
                  className='border-2'
                  onClick={secondaryAction?.onClick}
                  type={secondaryAction.type || 'button'}
                  disabled={secondaryAction.disabled}
                >
                  {secondaryAction.label}
                </Button>
              )}
            </div>
          </DialogFooter>
        )}
      </DialogContent>
    </Dialog>
  );
};

const CloseModalBtn: FC<{ closeModal: () => void; className?: string }> = ({
  closeModal,
  className,
}) => (
  <button
    className={`border-primary flex cursor-pointer items-center justify-center rounded border-2 p-1 ${className}`}
    onClick={closeModal}
    aria-label='Close modal'
  >
    <X width={20} height={20} className='font-extrabold text-primary' />
  </button>
);
