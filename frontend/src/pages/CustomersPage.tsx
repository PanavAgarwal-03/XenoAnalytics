import { useAuth } from '../state/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

interface CustomerSummary {
  id: number;
  shopifyCustomerId: number;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
}

export const CustomersPage = () => {
  const { selectedStore } = useAuth();
  const storeId = selectedStore?.id;

  const { data, isLoading } = useQuery<CustomerSummary[]>({
    queryKey: ['customersList', storeId],
    enabled: !!storeId,
    queryFn: async () => {
      const res = await api.get('/api/customers/list', { params: { storeId } });
      return res.data;
    },
  });

  return (
    <div>
      <h1 className="text-xl font-semibold text-slate-800 mb-4">Customers</h1>
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-2 text-left">Name</th>
              <th className="px-4 py-2 text-left">Email</th>
              <th className="px-4 py-2 text-left">Shopify Customer ID</th>
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr>
                <td colSpan={3} className="px-4 py-4 text-center text-slate-500">
                  Loading...
                </td>
              </tr>
            )}
            {!isLoading && data && data.length === 0 && (
              <tr>
                <td colSpan={3} className="px-4 py-4 text-center text-slate-500">
                  No customers yet.
                </td>
              </tr>
            )}
            {data?.map(c => (
              <tr key={c.id} className="border-t hover:bg-slate-50">
                <td className="px-4 py-2">
                  {(c.firstName || '') + ' ' + (c.lastName || '')}
                </td>
                <td className="px-4 py-2">{c.email || '-'}</td>
                <td className="px-4 py-2 text-xs text-slate-500">
                  {c.shopifyCustomerId}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
