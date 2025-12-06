
import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../api/client";
import { useAuth } from "../state/AuthContext";

interface SyncStatus {
  totalCustomers: number;
  totalOrders: number;
  totalProducts: number;
  lastOrderWebhookAt?: string | null;
  lastFullSyncAt?: string | null;
}

export const SyncHealthPage = () => {
  const { selectedStore } = useAuth();
  const storeId = selectedStore?.id;
  const queryClient = useQueryClient();
  const [syncLoading, setSyncLoading] = useState(false);

  const syncStatusQuery = useQuery<SyncStatus>({
    queryKey: ["syncStatus", storeId],
    enabled: !!storeId,
    queryFn: async () => {
      const res = await api.get("/api/metrics/sync-status", {
        params: { storeId },
      });
      return res.data;
    },
    refetchInterval: 60000,
  });

  const handleSyncNow = async () => {
    if (!storeId) return;
    setSyncLoading(true);
    try {
      await api.post("/api/sync/now", null, { params: { storeId } });
      await queryClient.invalidateQueries({ queryKey: ["syncStatus", storeId] });
    } catch (e) {
      console.error(e);
      alert("Sync failed. Check backend logs for details.");
    } finally {
      setSyncLoading(false);
    }
  };

  const syncStatus = syncStatusQuery.data;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">
          Sync & Health
        </h1>
        <button
          onClick={handleSyncNow}
          disabled={syncLoading || !storeId}
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm px-4 py-2 rounded-lg disabled:opacity-60"
        >
          {syncLoading ? "Syncing..." : "Sync data now"}
        </button>
      </div>

      <div className="bg-white rounded-xl shadow p-4">
        {syncStatusQuery.isLoading && (
          <div className="text-sm text-slate-500">Loading sync status...</div>
        )}

        {syncStatus && (
          <div className="space-y-2 text-sm text-slate-700">
            <div>
              <span className="font-medium">Total orders:</span>{" "}
              {syncStatus.totalOrders}
            </div>
            <div>
              <span className="font-medium">Total customers:</span>{" "}
              {syncStatus.totalCustomers}
            </div>
            <div>
              <span className="font-medium">Total products:</span>{" "}
              {syncStatus.totalProducts}
            </div>
            <div className="text-xs text-slate-500 mt-2">
              Last order webhook: {syncStatus.lastOrderWebhookAt || "Never"}
            </div>
            <div className="text-xs text-slate-500">
              Last full sync: {syncStatus.lastFullSyncAt || "Never"}
            </div>
          </div>
        )}

        {!syncStatusQuery.isLoading && !syncStatus && (
          <div className="text-sm text-slate-500">
            No sync status available yet.
          </div>
        )}
      </div>
    </div>
  );
};
export default SyncHealthPage;
