import { useUser } from '@/context/UserContext';
import { FC } from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const AuthLayout: FC = () => {
  const userContext = useUser();
  if (userContext.auth !== null) {
      return <Navigate to={"/"} replace />;
  }

  return (
    <div className='flex items-center justify-center w-full h-screen'>
      <nav className='fixed top-0 z-30 flex items-center justify-between w-full px-4 text-white border-gray-400 shadow-sm bg-primary front-bold h-15 border-b-1 lg:px-6'>
        <div>Log in</div>
      </nav>
      <Outlet />
    </div>
  );
};

export default AuthLayout;
