import { useAuth } from '../state/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import dayjs from 'dayjs';

interface OrderSummary {
  id: number;
  orderNumber: number;
  shopifyOrderId: number;
  totalPrice: number;
  currency: string;
  createdAt: string;
}

export const OrdersPage = () => {
  const { selectedStore } = useAuth();
  const storeId = selectedStore?.id;

  const { data, isLoading } = useQuery<OrderSummary[]>({
    queryKey: ['ordersList', storeId],
    enabled: !!storeId,
    queryFn: async () => {
      const res = await api.get('/api/orders/list', { params: { storeId } });
      return res.data;
    },
  });

  return (
    <div>
      <h1 className="text-xl font-semibold text-slate-800 mb-4">Orders</h1>
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-2 text-left">Order #</th>
              <th className="px-4 py-2 text-left">Shopify ID</th>
              <th className="px-4 py-2 text-right">Total</th>
              <th className="px-4 py-2 text-left">Currency</th>
              <th className="px-4 py-2 text-left">Created at</th>
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr>
                <td colSpan={5} className="px-4 py-4 text-center text-slate-500">
                  Loading...
                </td>
              </tr>
            )}
            {!isLoading && data && data.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-4 text-center text-slate-500">
                  No orders yet.
                </td>
              </tr>
            )}
            {data?.map(order => (
              <tr key={order.id} className="border-t hover:bg-slate-50">
                <td className="px-4 py-2">#{order.orderNumber}</td>
                <td className="px-4 py-2 text-xs text-slate-500">
                  {order.shopifyOrderId}
                </td>
                <td className="px-4 py-2 text-right">
                  ₹{order.totalPrice.toFixed(2)}
                </td>
                <td className="px-4 py-2">{order.currency}</td>
                <td className="px-4 py-2 text-xs text-slate-500">
                  {dayjs(order.createdAt).format('YYYY-MM-DD HH:mm')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
