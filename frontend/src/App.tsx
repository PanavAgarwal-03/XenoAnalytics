import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import { LoginPage } from "./pages/LoginPage";
import { StoreSelectPage } from "./pages/StoreSelectPage";
import { DashboardPage } from "./pages/DashboardPage";
import { OrdersPage } from "./pages/OrdersPage";
import { CustomersPage } from "./pages/CustomersPage";
import { ProductsPage } from "./pages/ProductsPage";
import { SyncHealthPage } from "./pages/SyncHealthPage";
import { useAuth } from "./state/AuthContext";
import { AppLayout } from "./components/AppLayout";
import type { JSX } from "react";

function RequireAuth({ children }: { children: JSX.Element }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}

function RequireStore({ children }: { children: JSX.Element }) {
  const { selectedStore } = useAuth();
  const location = useLocation();

  if (!selectedStore) {
    return <Navigate to="/select-store" state={{ from: location }} replace />;
  }
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/select-store"
        element={
          <RequireAuth>
            <StoreSelectPage />
          </RequireAuth>
        }
      />
      <Route
        path="/dashboard"
        element={
          <RequireAuth>
            <RequireStore>
              <AppLayout>
                <DashboardPage />
              </AppLayout>
            </RequireStore>
          </RequireAuth>
        }
      />
      <Route
        path="/orders"
        element={
          <RequireAuth>
            <RequireStore>
              <AppLayout>
                <OrdersPage />
              </AppLayout>
            </RequireStore>
          </RequireAuth>
        }
      />
      <Route
        path="/customers"
        element={
          <RequireAuth>
            <RequireStore>
              <AppLayout>
                <CustomersPage />
              </AppLayout>
            </RequireStore>
          </RequireAuth>
        }
      />
      <Route
        path="/products"
        element={
          <RequireAuth>
            <RequireStore>
              <AppLayout>
                <ProductsPage />
              </AppLayout>
            </RequireStore>
          </RequireAuth>
        }
      />
      <Route
        path="/sync-health"
        element={
          <RequireAuth>
            <RequireStore>
              <AppLayout>
                <SyncHealthPage />
              </AppLayout>
            </RequireStore>
          </RequireAuth>
        }
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
