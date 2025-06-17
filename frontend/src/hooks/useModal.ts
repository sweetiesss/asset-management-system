import { useState } from 'react';

/**
 * A custom React hook for managing modal visibility state.
 * Provides functions to open, close, or set the modal's visibility.
 *
 * @example
 * const { isModalVisible, openModal, closeModal, setModal } = useModal();
 * 
**/
export default function useModal(defaultValue?: boolean) {
  const [isModalVisible, setModalVisible] = useState<boolean>(defaultValue || false);

  const openModal = () => setModalVisible(true);
  const closeModal = () => setModalVisible(false);
  const setModal = (value: boolean) => setModalVisible(value);

  return {
    isModalVisible,
    openModal,
    closeModal,
    setModal,
  };
}

export interface BaseModalProp{
  visible: boolean;
  openModal: () => void;
  closeModal: () => void;
}