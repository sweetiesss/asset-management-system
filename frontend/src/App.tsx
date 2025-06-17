import { Routes, Route } from 'react-router-dom';
import { AdminProtectedRoute } from './components/layouts/AdminProtectedRoute';
import LogIn from './pages/LogIn';
import MainLayout from './components/layouts/MainLayout';
import AuthLayout from './components/layouts/AuthLayout';
import { UserManagement } from './pages/UserManagement';
import { CreateUserPage } from './pages/CreateUserPage';
import { AssetManagement } from './pages/AssetManagement';
import { CreateAssetPage } from './pages/CreateAssetPage';
import EditUserPage from './pages/EditUserPage';
import { useUser } from './context/UserContext';
import UpdateAssetPage from './pages/EditAssetPage';
import { AssignmentManagement } from './pages/AssignmentManagement';
import  CreateAssignmentPage  from './pages/CreateAssignmentPage';
import { UpdateAssignmentPage } from './pages/EditAssignmentPage';
import Report from './pages/Report';
import { Home } from './pages/Home';
import CreateExport from './pages/DetailReport';
import { AssetReturnManagementPage } from './pages/AssetReturnManagement';

function App() {
  const { auth, isLoading } = useUser();
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path='login' element={<LogIn />} />
      </Route>
      <Route element={<MainLayout />}>
        <Route index element={<Home />} />
        {!isLoading && auth?.roles.includes('ADMIN') && (
          <Route element={<AdminProtectedRoute />}>
            <Route path='users' element={<UserManagement />} />
            <Route path='users/create' element={<CreateUserPage />} />
            <Route path='users/:userId/edit' element={<EditUserPage />} />
            <Route path='assets' element={<AssetManagement />} />
            <Route path='assets/create' element={<CreateAssetPage />} />
            <Route path='assets/:assetId/edit' element={<UpdateAssetPage />} />
            <Route path='assignments' element={<AssignmentManagement />} />
            <Route path='assignments/create' element={<CreateAssignmentPage />} />
            <Route path='assignments/:assignmentId/edit' element={<UpdateAssignmentPage />} />
            <Route path='returning-requests' element={<AssetReturnManagementPage />} />
            <Route path='reports' element={<Report />} />
            <Route path='reports/create' element={<CreateExport />} />
          </Route>
        )}
        <Route path='/*' element={<Home />} />
      </Route>
    </Routes>
  );
}

export default App;
