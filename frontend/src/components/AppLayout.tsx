// src/components/AppLayout.tsx
import { type ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../state/AuthContext";

export const AppLayout = ({ children }: { children: ReactNode }) => {
  const { user, selectedStore, logout, setSelectedStore } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-100 flex">
      {/* Sidebar */}
      <div className="w-56 bg-slate-900 text-slate-100 flex flex-col">
        <div className="px-4 py-4 border-b border-slate-800">
          <div className="text-sm text-slate-400">XenoNovaMart</div>
          <div className="text-lg font-semibold">Insights</div>
        </div>
        <nav className="flex-1 px-2 py-4 space-y-1 text-sm">
          <NavLink
            to="/dashboard"
            className={({ isActive }) =>
              `block px-3 py-2 rounded-lg ${
                isActive ? "bg-slate-800" : "hover:bg-slate-800/60"
              }`
            }
          >
            Dashboard
          </NavLink>
          <NavLink
            to="/orders"
            className={({ isActive }) =>
              `block px-3 py-2 rounded-lg ${
                isActive ? "bg-slate-800" : "hover:bg-slate-800/60"
              }`
            }
          >
            Orders
          </NavLink>
          <NavLink
            to="/customers"
            className={({ isActive }) =>
              `block px-3 py-2 rounded-lg ${
                isActive ? "bg-slate-800" : "hover:bg-slate-800/60"
              }`
            }
          >
            Customers
          </NavLink>
          <NavLink
            to="/products"
            className={({ isActive }) =>
              `block px-3 py-2 rounded-lg ${
                isActive ? "bg-slate-800" : "hover:bg-slate-800/60"
              }`
            }
          >
            Products
          </NavLink>
          <NavLink
            to="/sync-health"
            className={({ isActive }) =>
              `block px-3 py-2 rounded-lg ${
                isActive ? "bg-slate-800" : "hover:bg-slate-800/60"
              }`
            }
          >
            Sync & Health
          </NavLink>
        </nav>
        <div className="px-4 py-3 border-t border-slate-800 text-xs space-y-1">
          <div className="truncate text-slate-300">
            {user?.email || "Not logged in"}
          </div>
          <div className="truncate text-slate-500">
            {selectedStore?.shopDomain || "No store selected"}
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="flex-1 flex flex-col">
        {/* Top bar */}
        <div className="bg-white shadow-sm px-6 py-3 flex justify-between items-center">
          <div>
            <div className="text-xs text-slate-500">Organization</div>
            <div className="text-sm font-semibold text-slate-800">
              {user?.email}
            </div>
            <div className="text-xs text-slate-500">
              Store: {selectedStore?.shopDomain || "None"}
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                setSelectedStore(null);
                navigate("/select-store");
              }}
              className="text-xs text-slate-600 border border-slate-300 px-3 py-1.5 rounded-lg hover:bg-slate-50"
            >
              Switch store
            </button>
            <button
              onClick={() => {
                logout();
                navigate("/login");
              }}
              className="text-xs text-red-600 border border-red-300 px-3 py-1.5 rounded-lg hover:bg-red-50"
            >
              Logout
            </button>
          </div>
        </div>

        {/* Page content */}
        <div className="flex-1 p-6">{children}</div>
      </div>
    </div>
  );
};
