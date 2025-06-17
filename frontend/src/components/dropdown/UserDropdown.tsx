import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import useModal from "@/hooks/useModal";
import { ChevronDown } from "lucide-react";
import { FC } from "react";
import { ChangePasswordModal } from "../modal/ChangePasswordModal";
import { LogoutModal } from "../modal/LogoutModal";

export const UserDropdown : FC = () => {

  const { isModalVisible: changePasswordModalVisible, openModal: openChangePasswordModal, closeModal: closeChangePasswordModal } = useModal();
  const { isModalVisible: logoutModalVisible, openModal: openLogoutModal, closeModal: closeLogoutModal } = useModal();
  
  return (
    <DropdownMenu>
      <DropdownMenuTrigger>
        <ChevronDown />
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuItem onClick={openChangePasswordModal}>
          Change password
        </DropdownMenuItem>
        <DropdownMenuItem onClick={openLogoutModal}>
          Log out
        </DropdownMenuItem>
      </DropdownMenuContent>
      {/**Need isModalVisible to force re-render to reset the state inside modal */}
      {changePasswordModalVisible && <ChangePasswordModal visible={changePasswordModalVisible} openModal={openChangePasswordModal} closeModal={closeChangePasswordModal} changePasswordRequired={false} /> } 
      <LogoutModal visible={logoutModalVisible} openModal={openLogoutModal} closeModal={closeLogoutModal} />
    </DropdownMenu>
  );
}
