import { UserService } from '@/services/UserService';
import { Auth } from '@/types/type';
import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useEffect,
  useRef,
  useLayoutEffect,
} from 'react';
import { toast } from 'react-toastify';

interface UserContextType {
  // user: User | null;
  auth: Auth | null;
  isLoading: boolean;
  setAuth: (auth: Auth | null) => void;
  getAccessToken: () => string | null;
}

let getAccessToken: () => string | null = () => null;
let updateAccessToken: (accessToken: string | null) => void = () => {};

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider = ({ children }: { children: ReactNode }) => {
  // const [user, setUser] = useState<User | null>(null);
  const [auth, setAuth] = useState<Auth | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const accessTokenRef = useRef<string | null>(null);
  getAccessToken = () => accessTokenRef.current;
  updateAccessToken = (accessToken: string | null) => {
    setAuth((prev) => {
      const newAuth = accessToken
        ? ({
            id: prev?.id ?? '',
            username: prev?.username ?? '',
            roles: prev?.roles ?? [],
            changePasswordRequired: prev?.changePasswordRequired ?? false,
            accessToken,
          } as Auth)
        : null;
      return newAuth;
    });
  };

  const fetchAuth = async () => {
    try {
      const refreshResponse = await UserService.refreshAccessToken();
      if (refreshResponse.data === undefined) {
        toast.error(refreshResponse.error?.message);
        return;
      }

      accessTokenRef.current = refreshResponse.data.accessToken;
      const getUserResponse = await UserService.getMe();
      if (getUserResponse.data) {
        setAuth({
          ...getUserResponse.data,
          accessToken: accessTokenRef.current,
        });
      }
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  useLayoutEffect(() => {
    fetchAuth();
  }, []);

  useEffect(() => {
    accessTokenRef.current = auth?.accessToken ?? null;
  }, [auth]);

  return (
    <UserContext.Provider
      value={{
        auth,
        isLoading,
        setAuth,
        getAccessToken,
      }}
    >
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) throw new Error('useUserContext must be used within a UserProvider');
  return context;
};

export { getAccessToken, updateAccessToken };
