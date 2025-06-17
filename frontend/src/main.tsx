import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { ToastContainer } from 'react-toastify';
import { BrowserRouter } from 'react-router-dom';
import { UserProvider } from './context/UserContext.tsx';
import { TIME } from '@/configs/constants.ts';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <UserProvider>
        <LocalizationProvider dateAdapter={AdapterDayjs}>
          <App />
        </LocalizationProvider>
      </UserProvider>
      <ToastContainer autoClose={TIME.TOAST_DURATION} />
    </BrowserRouter>
  </StrictMode>
);
