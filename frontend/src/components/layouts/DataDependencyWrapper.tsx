import type { ReactNode } from 'react';
import { Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';

interface DataDependencyWrapperProps {
  children: ReactNode;
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  fallbackPath?: string;
  loadingMessage?: string;
}

/**
 * A wrapper component that ensures data dependencies are loaded before rendering children
 * Shows loading state and handles errors appropriately
 */
export function DataDependencyWrapper({
  children,
  isLoading,
  isError,
  errorMessage = 'Failed to load required data',
  fallbackPath = '/',
  loadingMessage = 'Loading required data...',
}: DataDependencyWrapperProps) {
  const navigate = useNavigate();

  if (isLoading) {
    return (
      <div className='flex min-h-[400px] flex-col items-center justify-center p-8'>
        <Loader2 className='text-primary mb-4 h-12 w-12 animate-spin' />
        <p className='text-lg text-gray-700'>{loadingMessage}</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className='flex min-h-[400px] flex-col items-center justify-center p-8'>
        <div className='max-w-md rounded-lg border border-red-200 bg-red-50 p-6'>
          <h2 className='mb-3 text-xl font-semibold text-red-700'>Error Loading Data</h2>
          <p className='mb-4 text-gray-700'>{errorMessage}</p>
          <div className='flex gap-4'>
            <Button onClick={() => window.location.reload()} variant='outline'>
              Try Again
            </Button>
            <Button onClick={() => navigate(fallbackPath)}>Go Back</Button>
          </div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
