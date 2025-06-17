import { useUser } from '@/context/UserContext';
import { Navigate, Outlet } from 'react-router-dom';

export const AdminProtectedRoute = () => {
  const {isLoading, auth} = useUser();

  if (!isLoading && auth && !auth?.roles.includes('ADMIN')) {
    return <Navigate to={'/'} replace />;
  }

  return <Outlet />;
};
