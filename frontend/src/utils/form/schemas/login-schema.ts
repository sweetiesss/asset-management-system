import * as z from 'zod';

export type LoginFormRequest = {
  username: string;
  password: string;
};

export const defaultValues: LoginFormRequest = {
  username: '',
  password: '',
};

export const loginFormSchema = z.object({
  username: z.string().min(1, {
    message: 'Username is required',
  }),
  password: z.string().min(1, {
    message: 'Password is required',
  }),
});
