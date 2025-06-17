import { useUser } from '@/context/UserContext';
import { FC, useEffect } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { Link, useLocation } from 'react-router-dom';
import { UserDropdown } from '../dropdown/UserDropdown';
import { ChangePasswordModal } from '../modal/ChangePasswordModal';
import useModal from '@/hooks/useModal';
import { NAVIGATOR } from '@/configs/constants';

const tabs = [
  { name: 'Home', href: '/', onlyAdmin: false },
  { name: 'Manage User', href: '/users', onlyAdmin: true },
  { name: 'Manage Asset', href: '/assets', onlyAdmin: true },
  { name: 'Manage Assignment', href: '/assignments', onlyAdmin: true },
  { name: 'Request for Returning', href: '/returning-requests', onlyAdmin: true },
  { name: 'Report', href: '/reports', onlyAdmin: true },
];

const urlMatch = (pageToMatch: string, url: string) => {
  if (pageToMatch === '/') {
    return url === pageToMatch;
  }
  return url.startsWith(pageToMatch);
};

const MainLayout: FC = () => {
  const location = useLocation();
  const userContext = useUser();
  const username = userContext.auth?.username;

  const { isModalVisible, openModal, closeModal } = useModal(false);

  useEffect(() => {
    if (!userContext.isLoading && userContext.auth?.changePasswordRequired) {
      openModal();
    }
  }, [userContext.isLoading, userContext.auth?.changePasswordRequired]);

  if (!userContext.isLoading && userContext.auth === null) {
    return <Navigate to={'/login'} replace />;
  }

  return (
    <div className='flex h-screen flex-col overflow-y-auto'>
      <nav className='bg-primary front-bold top-0 z-30 flex h-15 items-center justify-between border-b-1 border-gray-400 px-4 text-white shadow-sm lg:px-6'>
        <div className='font-bold'>
          {NAVIGATOR.find((page) => page.pattern.test(location.pathname))?.label ||
            'Assets Management'}
        </div>
        <div className='flex space-x-2'>
          <div>{username}</div>
          <UserDropdown />
        </div>
      </nav>

      <div className='flex flex-1 overflow-y-auto'>
        <aside
          id='logo-sidebar'
          className={`left-0 z-40 w-64 translate-x-0 overflow-hidden border-r border-gray-200 bg-white transition-transform sm:translate-x-0 dark:border-gray-700 dark:bg-gray-800`}
          aria-label='Sidebar'
        >
          <div className='flex h-40 flex-col items-start justify-center gap-2 px-5 py-10'>
            <div className='text-primary font-bold'>Online Asset Management</div>
          </div>

          <div className='h-full bg-white px-3 pb-4 dark:bg-gray-800'>
            <ul className='space-y-1 font-medium'>
              {tabs
                .filter((tab) => userContext.auth?.roles.includes('ADMIN') || !tab.onlyAdmin)
                .map((tab) => (
                  <li key={tab.name} id={tab.name}>
                    <Link
                      to={tab.href}
                      className={`group flex items-center p-2 text-black ${
                        urlMatch(tab.href, location.pathname)
                          ? `bg-primary text-white`
                          : `bg-gray-100 hover:bg-red-300`
                      } group`}
                    >
                      <span className='ms-3 flex-1 whitespace-nowrap'>{tab.name}</span>
                    </Link>
                  </li>
                ))}
            </ul>
          </div>
        </aside>
        <div className='flex-1 overflow-y-auto p-4'>
          <Outlet />
        </div>
      </div>
      {userContext.auth?.changePasswordRequired && (
        <ChangePasswordModal
          visible={isModalVisible}
          openModal={openModal}
          closeModal={closeModal}
          firstTimeLogin
          changePasswordRequired={userContext.auth?.changePasswordRequired}
        />
      )}
    </div>
  );
};

export default MainLayout;
