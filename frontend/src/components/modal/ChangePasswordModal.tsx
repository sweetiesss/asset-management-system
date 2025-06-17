import { FC, useCallback, useEffect, useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { Form, FormControl, FormField, FormLabel, FormMessage } from '@/components/ui/form';
import * as z from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Input } from '@/components/ui/input';
import { usePassword } from '@/hooks/fetch/usePassword';
import {
  ChangePasswordRequest,
  changePasswordSchema,
} from '@/utils/form/schemas/change-password-schema';
import { ERROR } from '@/configs/constants';
import { useUser } from '@/context/UserContext';
import { Modal } from './Modal';

interface ChangePasswordModalProps {
  visible: boolean;
  openModal: () => void;
  closeModal: () => void;
  firstTimeLogin?: boolean; // Optional prop to indicate if it's the first time login
  changePasswordRequired: boolean;
}

export const ChangePasswordModal: FC<ChangePasswordModalProps> = ({
  visible,
  openModal,
  closeModal,
  firstTimeLogin = false,
  changePasswordRequired,
}) => {
  const userContext = useUser();
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showOldPassword, setShowOldPassword] = useState(false);

  const {
    updatePassword,
    error,
    errorCode,
    resetErrors,
    isSuccess: updateSuccess,
    isFetching,
  } = usePassword();
  const form = useForm<z.infer<typeof changePasswordSchema>>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      oldPassword: changePasswordRequired ? undefined : '', // Set oldPassword undefined to pass the validation of useForm
      newPassword: '',
      changePasswordRequired,
    },
    mode: 'onChange',
  });

  const onSubmit = useCallback(
    (data: z.infer<typeof changePasswordSchema>) => {
      updatePassword({
        userId: userContext.auth?.id,
        newPassword: data.newPassword,
        oldPassword: data.changePasswordRequired ? '' : data.oldPassword, // The backend expects an notnull value for oldPassword
      } as ChangePasswordRequest);
    },
    [updatePassword, userContext.auth?.id]
  );

  const [disable, setDisable] = useState(false);

  useEffect(() => {
    setDisable(
      form.getValues().newPassword.length === 0 ||
        (!form.getValues().changePasswordRequired && form.getValues().oldPassword?.length === 0)
    );
  }, [form.formState.isValidating]);

  return (
    <>
      {!updateSuccess && (
        <Modal
          isOpen={visible}
          onOpen={openModal}
          onClose={closeModal}
          title='Change Password'
          primaryAction={
            updateSuccess
              ? undefined
              : {
                  label: 'Save',
                  variant: 'destructive',
                  onClick: form.handleSubmit(onSubmit),
                  type: 'submit',
                  disabled: disable || isFetching,
                }
          }
          secondaryAction={
            firstTimeLogin
              ? undefined
              : {
                  label: 'Cancel',
                  onClick: closeModal,
                  variant: 'secondary',
                  disabled: isFetching,
                }
          }
          closeBtnEnabled={firstTimeLogin}
          maxWidth='sm:max-w-[500px]'
        >
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)}>
              <table className='w-full table-auto'>
                <tbody>
                  <FormField
                    control={form.control}
                    name='oldPassword'
                    render={({ field }) => (
                      <>
                        {changePasswordRequired ? (
                          <tr>
                            <td colSpan={2}>
                              <div className='w-full px-8 text-black'>
                                This is the first time you logged in.
                                <br />
                                You have to change your password to continue.
                              </div>
                            </td>
                          </tr>
                        ) : (
                          <tr>
                            <td className='w-1/3 pl-8 align-middle'>
                              <FormLabel className='text-black'>Old Password</FormLabel>
                            </td>
                            <td className='relative w-2/3 pr-8'>
                              <FormControl>
                                <Input
                                  id='old-password'
                                  required
                                  placeholder='Enter your password here'
                                  {...field}
                                  type={showOldPassword ? 'text' : 'password'}
                                  onChange={(e) => {
                                    field.onChange(e);
                                    resetErrors();
                                  }}
                                  className='w-full pr-10 native-hide-eye'
                                />
                              </FormControl>
                              <button
                                onClick={() => setShowOldPassword((prev) => !prev)}
                                type='button'
                                className='absolute -translate-y-1/2 top-1/2 right-10'
                              >
                                {!showOldPassword ? (
                                  <Eye className='w-6 h-6' />
                                ) : (
                                  <EyeOff className='w-6 h-6' />
                                )}
                              </button>
                            </td>
                          </tr>
                        )}
                        <tr className='w-full'>
                          <td></td>
                          <td className='flex'>
                            <FormMessage />
                            {!changePasswordRequired &&
                              errorCode === ERROR.PASSWORD_DOES_NOT_MATCH.CODE && (
                                <div className='text-sm text-destructive'>{error}</div>
                              )}
                            <span className='invisible'>&nbsp;</span>
                          </td>
                        </tr>
                      </>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name='newPassword'
                    render={({ field }) => (
                      <>
                        <tr>
                          <td className='w-1/3 pl-8 align-middle'>
                            <FormLabel>New Password</FormLabel>
                          </td>
                          <td className='relative w-2/3 pr-8'>
                            <FormControl>
                              <Input
                                id='new-password'
                                required
                                placeholder='Enter your password here'
                                {...field}
                                type={showNewPassword ? 'text' : 'password'}
                                onChange={(e) => {
                                  field.onChange(e);
                                  resetErrors();
                                }}
                                className='w-full pr-10 native-hide-eye'
                              />
                            </FormControl>
                            <button
                              onClick={() => setShowNewPassword((prev) => !prev)}
                              type='button'
                              className='absolute -translate-y-1/2 top-1/2 right-10'
                            >
                              {!showNewPassword ? (
                                <Eye className='w-6 h-6' />
                              ) : (
                                <EyeOff className='w-6 h-6' />
                              )}
                            </button>
                          </td>
                        </tr>
                        <tr>
                          <td></td>
                          <td className='flex'>
                            <FormMessage />
                            {changePasswordRequired && error && (
                              <div className='text-sm text-destructive'>{error}</div>
                            )}
                            {!changePasswordRequired &&
                              errorCode === ERROR.NEW_PASSWORD_MUST_BE_DIFFERENT.CODE && (
                                <div className='text-sm text-destructive'>{error}</div>
                              )}
                            <span className='invisible'>&nbsp;</span>
                          </td>
                        </tr>
                      </>
                    )}
                  />
                </tbody>
              </table>
            </form>
          </Form>
        </Modal>
      )}

      {updateSuccess && (
        <Modal
          isOpen={visible}
          onOpen={openModal}
          onClose={closeModal}
          title='Change Password'
          secondaryAction={{
                  label: 'Close',
                  onClick: closeModal,
                  variant: 'secondary',
                }
          }
          maxWidth='sm:max-w-[500px]'
        >
          <div className='py-5 text-base text-center'>
            Your password has been changed successfully!
          </div>
        </Modal>
      )}
    </>
  );
};
