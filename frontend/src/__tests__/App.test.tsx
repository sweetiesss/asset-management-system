import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Outlet } from 'react-router-dom'
import App from '@/App'
import { useUser } from '@/context/UserContext'

// Mock all the page components
vi.mock('@/pages/LogIn', () => ({
  default: () => <div data-testid="login-page">Login Page</div>
}))

vi.mock('@/pages/Home', () => ({
  Home: () => <div data-testid="home-page">Home Page</div>
}))

vi.mock('@/pages/UserManagement', () => ({
  UserManagement: () => <div data-testid="user-management-page">User Management</div>
}))

vi.mock('@/pages/AssetManagement', () => ({
  AssetManagement: () => <div data-testid="asset-management-page">Asset Management</div>
}))

vi.mock('@/pages/CreateUserPage', () => ({
  CreateUserPage: () => <div data-testid="create-user-page">Create User</div>
}))

vi.mock('@/pages/EditUserPage', () => ({
  default: () => <div data-testid="edit-user-page">Edit User</div>
}))

vi.mock('@/pages/CreateAssetPage', () => ({
  CreateAssetPage: () => <div data-testid="create-asset-page">Create Asset</div>
}))

vi.mock('@/pages/EditAssetPage', () => ({
  default: () => <div data-testid="edit-asset-page">Edit Asset</div>
}))

vi.mock('@/pages/AssignmentManagement', () => ({
  AssignmentManagement: () => <div data-testid="assignment-management">Assignment Management</div>
}))

vi.mock('@/pages/CreateAssignmentPage', () => ({
  default: () => <div data-testid="create-assignment">Create Assignment</div>
}))

vi.mock('@/pages/EditAssignmentPage', () => ({
  UpdateAssignmentPage: () => <div data-testid="edit-assignment">Edit Assignment</div>
}))

vi.mock('@/pages/Report', () => ({
  default: () => <div data-testid="report-page">Report</div>
}))

vi.mock('@/pages/DetailReport', () => ({
  default: () => <div data-testid="detail-report">Detail Report</div>
}))

vi.mock('@/pages/AssetReturnManagement', () => ({
  AssetReturnManagementPage: () => <div data-testid="asset-return-management">Asset Return</div>
}))

// Mock the layout components to properly render children
vi.mock('@/components/layouts/MainLayout', () => ({
  default: () => (
    <div data-testid="main-layout">
      <Outlet />
    </div>
  )
}))

vi.mock('@/components/layouts/AuthLayout', () => ({
  default: () => (
    <div data-testid="auth-layout">
      <Outlet />
    </div>
  )
}))

vi.mock('@/components/layouts/AdminProtectedRoute', () => ({
  AdminProtectedRoute: () => (
    <div data-testid="admin-protected-route">
      <Outlet />
    </div>
  )
}))

vi.mock('@/context/UserContext')

const mockUseUser = vi.mocked(useUser)

// Custom render function that wraps with MemoryRouter
const renderApp = (initialEntries = ['/']) => {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <App />
    </MemoryRouter>
  )
}

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render without crashing', () => {
    mockUseUser.mockReturnValue({
      auth: null,
      isLoading: false,
      setAuth: vi.fn(),
      getAccessToken: vi.fn()
    })

    renderApp()
    
    expect(screen.getByTestId('main-layout')).toBeInTheDocument()
  })

  it('should show loading state when isLoading is true', () => {
    mockUseUser.mockReturnValue({
      auth: null,
      isLoading: true,
      setAuth: vi.fn(),
      getAccessToken: vi.fn()
    })

    renderApp()
    
    // When loading, admin routes should not be rendered  
    expect(screen.getByTestId('main-layout')).toBeInTheDocument()
    expect(screen.getByTestId('home-page')).toBeInTheDocument()
  })

  it('should render login page on /login route', () => {
    mockUseUser.mockReturnValue({
      auth: null,
      isLoading: false,
      setAuth: vi.fn(),
      getAccessToken: vi.fn()
    })

    renderApp(['/login'])
    
    expect(screen.getByTestId('auth-layout')).toBeInTheDocument()
    expect(screen.getByTestId('login-page')).toBeInTheDocument()
  })

  it('should render home page by default', () => {
    mockUseUser.mockReturnValue({
      auth: null,
      isLoading: false,
      setAuth: vi.fn(),
      getAccessToken: vi.fn()
    })

    renderApp()
    
    expect(screen.getByTestId('home-page')).toBeInTheDocument()
  })

  it('should navigate to users page for admin users', () => {
    mockUseUser.mockReturnValue({
      auth: {
        accessToken: 'token',
        roles: ['ADMIN'],
        id: '1',
        username: 'admin',
        changePasswordRequired: false
      },
      isLoading: false,
      setAuth: vi.fn(),
      getAccessToken: vi.fn()
    })

    renderApp(['/users'])
    
    expect(screen.getByTestId('admin-protected-route')).toBeInTheDocument()
    expect(screen.getByTestId('user-management-page')).toBeInTheDocument()
  })
})
