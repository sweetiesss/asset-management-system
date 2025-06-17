// Test utilities and helpers
import { render, RenderOptions } from '@testing-library/react'
import { ReactElement } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { UserProvider } from '@/context/UserContext'

// Custom render function that includes providers
const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
  return (
    <BrowserRouter>
      <UserProvider>
        {children}
      </UserProvider>
    </BrowserRouter>
  )
}

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>,
) => render(ui, { wrapper: AllTheProviders, ...options })

export * from '@testing-library/react'
export { customRender as render }

// Mock data helpers
export const mockUser = {
  id: '1',
  staffCode: 'SD0001',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@nashtech.vn',
  roles: ['ADMIN'],
  location: 'HCM',
  type: 'ADMIN',
  gender: 'MALE',
  joinedDate: '2023-01-01',
  status: 'ACTIVE'
}

export const mockAuth = {
  accessToken: 'mock-access-token',
  refreshToken: 'mock-refresh-token',
  user: mockUser,
  roles: ['ADMIN']
}

export const mockAsset = {
  id: '1',
  assetCode: 'LA000001',
  assetName: 'Laptop Dell',
  category: 'Laptop',
  specification: 'Dell Latitude 5520',
  installedDate: '2023-01-01',
  state: 'AVAILABLE',
  location: 'HCM'
}

export const mockAssignment = {
  id: '1',
  assetCode: 'LA000001',
  assetName: 'Laptop Dell',
  assignedTo: 'SD0001',
  assignedBy: 'SD0002',
  assignedDate: '2023-01-01',
  state: 'ACCEPTED',
  note: 'Test assignment'
}
