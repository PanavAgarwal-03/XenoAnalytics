// src/pages/DashboardPage.tsx
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";
import { useAuth } from "../state/AuthContext";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
} from "recharts";
import dayjs from "dayjs";

interface OverviewMetrics {
  totalCustomers: number;
  totalOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
  newCustomers: number;
  returningCustomers: number;
}

interface OrdersByDatePoint {
  date: string;
  orderCount: number;
  totalRevenue: number;
}

interface TopCustomer {
  customerId: number;
  email: string | null;
  name: string | null;
  totalSpend: number;
  orderCount: number;
}

interface TopProduct {
  productId: number;
  title: string | null;
  totalRevenue: number;
  quantitySold: number;
}

export const DashboardPage = () => {
  const { selectedStore } = useAuth();

  const [dateRange, setDateRange] = useState(() => {
    const to = dayjs();
    const from = to.subtract(30, "day");
    return {
      from: from.format("YYYY-MM-DD"),
      to: to.format("YYYY-MM-DD"),
    };
  });

  if (!selectedStore) return null;
  const storeId = selectedStore.id;

  const overviewQuery = useQuery<OverviewMetrics>({
    queryKey: ["overview", storeId, dateRange],
    queryFn: async () => {
      const res = await api.get("/api/metrics/overview", {
        params: {
          storeId,
          from: dateRange.from,
          to: dateRange.to,
        },
      });
      return res.data;
    },
  });

  const ordersByDateQuery = useQuery<OrdersByDatePoint[]>({
    queryKey: ["ordersByDate", storeId, dateRange],
    queryFn: async () => {
      const res = await api.get("/api/metrics/orders-by-date", {
        params: {
          storeId,
          from: dateRange.from,
          to: dateRange.to,
        },
      });
      return res.data;
    },
  });

  const topCustomersQuery = useQuery<TopCustomer[]>({
    queryKey: ["topCustomers", storeId, dateRange],
    queryFn: async () => {
      const res = await api.get("/api/metrics/top-customers", {
        params: {
          storeId,
          from: dateRange.from,
          to: dateRange.to,
          limit: 5,
        },
      });
      return res.data;
    },
  });

  const topProductsQuery = useQuery<TopProduct[]>({
    queryKey: ["topProducts", storeId, dateRange],
    queryFn: async () => {
      const res = await api.get("/api/metrics/top-products", {
        params: {
          storeId,
          from: dateRange.from,
          to: dateRange.to,
          limit: 5,
        },
      });
      return res.data;
    },
  });

  const overview = overviewQuery.data;
  const ordersByDate = ordersByDateQuery.data || [];
  const topCustomers = topCustomersQuery.data || [];
  const topProducts = topProductsQuery.data || [];

  const setPresetRange = (days: number) => {
    const to = dayjs();
    const from = to.subtract(days, "day");
    setDateRange({
      from: from.format("YYYY-MM-DD"),
      to: to.format("YYYY-MM-DD"),
    });
  };

  return (
    <div className="space-y-6">
      {/* Header + date filters */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <h1 className="text-xl font-semibold text-slate-800">Dashboard</h1>

        <div className="flex flex-col md:flex-row md:items-center gap-2">
          {/* Date inputs */}
          <div className="flex items-center gap-2 text-xs">
            <span className="text-slate-500">From</span>
            <input
              type="date"
              value={dateRange.from}
              max={dateRange.to}
              onChange={(e) =>
                setDateRange((prev) => ({
                  ...prev,
                  from: e.target.value,
                }))
              }
              className="border border-slate-300 rounded px-2 py-1 text-xs"
            />
            <span className="text-slate-500">To</span>
            <input
              type="date"
              value={dateRange.to}
              min={dateRange.from}
              onChange={(e) =>
                setDateRange((prev) => ({
                  ...prev,
                  to: e.target.value,
                }))
              }
              className="border border-slate-300 rounded px-2 py-1 text-xs"
            />
          </div>

          {/* Quick presets */}
          <div className="flex items-center gap-1 text-xs">
            <span className="text-slate-500">Quick:</span>
            <button
              onClick={() => setPresetRange(7)}
              className="px-2 py-1 border border-slate-300 rounded hover:bg-slate-50"
            >
              7d
            </button>
            <button
              onClick={() => setPresetRange(30)}
              className="px-2 py-1 border border-slate-300 rounded hover:bg-slate-50"
            >
              30d
            </button>
            <button
              onClick={() => setPresetRange(90)}
              className="px-2 py-1 border border-slate-300 rounded hover:bg-slate-50"
            >
              90d
            </button>
          </div>
        </div>
      </div>

      {/* Overview cards */}
      <div className="grid md:grid-cols-4 gap-4">
        <StatCard
          title="Total revenue"
          value={
            overview
              ? `₹${overview.totalRevenue.toFixed(2)}`
              : overviewQuery.isLoading
              ? "Loading..."
              : "0"
          }
        />
        <StatCard
          title="Total orders"
          value={
            overview
              ? overview.totalOrders
              : overviewQuery.isLoading
              ? "Loading..."
              : 0
          }
        />
        <StatCard
          title="Total customers"
          value=
            {overview
              ? overview.totalCustomers
              : overviewQuery.isLoading
              ? "Loading..."
              : 0}
        />
        <StatCard
          title="Avg order value"
          value={
            overview
              ? `₹${overview.averageOrderValue.toFixed(2)}`
              : overviewQuery.isLoading
              ? "Loading..."
              : "0"
          }
        />
      </div>

      {/* Orders & revenue chart */}
      <div className="bg-white rounded-xl shadow p-4">
        <h2 className="text-sm font-semibold text-slate-800 mb-2">
          Orders & revenue over time
        </h2>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={ordersByDate}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis yAxisId="left" />
              <YAxis yAxisId="right" orientation="right" />
              <Tooltip />
              <Line
                type="monotone"
                dataKey="orderCount"
                stroke="#2563eb"
                yAxisId="left"
                name="Orders"
              />
              <Line
                type="monotone"
                dataKey="totalRevenue" // make sure backend field is this
                stroke="#16a34a"
                yAxisId="right"
                name="Revenue"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Top customers & products */}
      <div className="grid md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-4">
          <h2 className="text-sm font-semibold text-slate-800 mb-2">
            Top customers by spend
          </h2>
          <TopList
            loading={topCustomersQuery.isLoading}
            rows={topCustomers.map((c) => ({
              title: c.name || c.email || `Customer #${c.customerId}`,
              subtitle: `${c.orderCount} order(s)`,
              value: `₹${c.totalSpend.toFixed(2)}`,
            }))}
          />
        </div>

        <div className="bg-white rounded-xl shadow p-4">
          <h2 className="text-sm font-semibold text-slate-800 mb-2">
            Top products by revenue
          </h2>
          <TopList
            loading={topProductsQuery.isLoading}
            rows={topProducts.map((p) => ({
              title: p.title || `Product #${p.productId}`,
              subtitle: `${p.quantitySold} sold`,
              value: `₹${p.totalRevenue.toFixed(2)}`,
            }))}
          />
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ title, value }: { title: string; value: any }) => (
  <div className="bg-white rounded-xl shadow p-4">
    <div className="text-xs text-slate-500 mb-1">{title}</div>
    <div className="text-xl font-semibold text-slate-800">{value}</div>
  </div>
);

const TopList = ({
  loading,
  rows,
}: {
  loading: boolean;
  rows: { title: string; subtitle: string; value: string }[];
}) => {
  if (loading) {
    return <div className="text-sm text-slate-500">Loading...</div>;
  }
  if (!rows.length) {
    return <div className="text-sm text-slate-500">No data in this range.</div>;
  }
  return (
    <ul className="space-y-2 text-sm">
      {rows.map((r, i) => (
        <li
          key={i}
          className="flex items-center justify-between border-b last:border-b-0 pb-1"
        >
          <div>
            <div className="font-medium text-slate-800">{r.title}</div>
            <div className="text-xs text-slate-500">{r.subtitle}</div>
          </div>
          <div className="text-sm font-semibold text-slate-800">{r.value}</div>
        </li>
      ))}
    </ul>
  );
};
