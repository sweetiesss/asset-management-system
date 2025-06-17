import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Form, FormControl, FormField, FormLabel, FormMessage } from '@/components/ui/form';
import { Eye, EyeOff } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { UserService } from '@/services/UserService';
import { useUser } from '@/context/UserContext';
import { useNavigate } from 'react-router-dom';
import {
  defaultValues,
  LoginFormRequest,
  loginFormSchema,
} from '@/utils/form/schemas/login-schema';
import { zodResolver } from '@hookform/resolvers/zod';
import { toast } from 'react-toastify';
import Spinner from '@/components/Spinner';

const LogIn = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError] = useState<string | null>(null);
  const { setAuth } = useUser();
  const navigate = useNavigate();

  const form = useForm<LoginFormRequest>({
    resolver: zodResolver(loginFormSchema),
    defaultValues: defaultValues,
    mode: 'onChange',
  });
  const isFormValid = form.formState.isValid;

  async function onSubmit(values: { username: string; password: string }) {
    setLoginLoading(true);
    const response = await UserService.login(values.username, values.password);

    if (response.error) {
      console.error(response.error);
      if (response.error.code === 'BAD_CREDENTIALS') {
        toast.error('Either username or password is incorrect');
        setLoginLoading(false);
        return;
      }
      toast.error(response.error.message);
      setLoginLoading(false);
      return;
    }
    setLoginLoading(false);
    setAuth(response.data ?? null);
    navigate('/');
  }

  return (
    <div className='flex h-full w-full flex-col items-center justify-center overflow-hidden'>
      <div className='min-w-128'>
        <div className='rounded-t-2xl border-2 border-black bg-gray-200 px-8 py-4'>
          <p className='text-primary text-3xl font-bold'>Log in</p>
        </div>
        <div className='rounded-b-2xl border-2 border-t-0 border-black px-8 py-6'>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)}>
              <table className='w-full table-auto'>
                <tbody>
                  <FormField
                    control={form.control}
                    name='username'
                    render={({ field }) => (
                      <>
                        <tr className='w-full'>
                          <td className='w-1/5 py-2 align-middle'>
                            <FormLabel className='text-black'>Username</FormLabel>
                          </td>
                          <td className='py-2 align-middle'>
                            <FormControl>
                              <Input
                                id='username'
                                placeholder='Enter your username here'
                                {...field}
                                type='text'
                                className='w-full'
                              />
                            </FormControl>
                          </td>
                        </tr>
                        <tr className='w-full'>
                          <td></td>
                          <td className='flex'>
                            <FormMessage>{loginError}</FormMessage>
                            <span className='invisible'>&nbsp;</span>
                          </td>
                        </tr>
                      </>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name='password'
                    render={({ field }) => (
                      <>
                        <tr>
                          <td className='py-2 align-middle'>
                            <FormLabel>Password</FormLabel>
                          </td>
                          <td className='relative py-2'>
                            <FormControl>
                              <Input
                                id='password'
                                placeholder='Enter your password here'
                                {...field}
                                type={showPassword ? 'text' : 'password'}
                                className='native-hide-eye w-full'
                              />
                            </FormControl>
                            <button
                              onClick={() => setShowPassword((prev) => !prev)}
                              type='button'
                              className='bg-white absolute top-1/2 right-2 -translate-y-1/2 px-1'
                            >
                              {!showPassword ? (
                                <Eye className='h-6 w-6' />
                              ) : (
                                <EyeOff className='h-6 w-6' />
                              )}
                            </button>
                          </td>
                        </tr>
                        <tr>
                          <td></td>
                          <td className='flex'>
                            <FormMessage>{loginError}</FormMessage>
                            <span className='invisible'>&nbsp;</span>
                          </td>
                        </tr>
                      </>
                    )}
                  />

                  <tr>
                    <td colSpan={2} className='text-right'>
                      <Button type='submit' disabled={!isFormValid}>
                        {loginLoading && (
                          <div>
                           <Spinner/>
                          </div>
                        )}
                        <span>Login</span>
                      </Button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </form>
          </Form>
        </div>
      </div>
    </div>
  );
};

export default LogIn;

