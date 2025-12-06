import  { createContext, useContext, useState, type ReactNode } from "react";

export interface Store {
  id: number;
  shopDomain: string;
  lastFullSyncAt?: string | null;
  lastOrderWebhookAt?: string | null;
}

export interface LoginResponse {
  userId: number;
  orgId: number;
  email: string;
  token: string;
  stores: Store[];
}

interface AuthContextValue {
  user: LoginResponse | null;
  selectedStore: Store | null;
  setUser: (u: LoginResponse | null) => void;
  setSelectedStore: (s: Store | null) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const USER_KEY = "xeno_user";
const STORE_KEY = "xeno_selected_store";

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  // Initialize from localStorage on first render
  const [user, setUserState] = useState<LoginResponse | null>(() => {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? (JSON.parse(raw) as LoginResponse) : null;
    } catch {
      return null;
    }
  });

  const [selectedStore, setSelectedStoreState] = useState<Store | null>(() => {
    try {
      const raw = localStorage.getItem(STORE_KEY);
      return raw ? (JSON.parse(raw) as Store) : null;
    } catch {
      return null;
    }
  });

  const setUser = (u: LoginResponse | null) => {
    setUserState(u);
    if (u) {
      localStorage.setItem(USER_KEY, JSON.stringify(u));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  };

  const setSelectedStore = (s: Store | null) => {
    setSelectedStoreState(s);
    if (s) {
      localStorage.setItem(STORE_KEY, JSON.stringify(s));
    } else {
      localStorage.removeItem(STORE_KEY);
    }
  };

  const logout = () => {
    setUserState(null);
    setSelectedStoreState(null);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(STORE_KEY);
  };

  const value: AuthContextValue = {
    user,
    selectedStore,
    setUser,
    setSelectedStore,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
};
