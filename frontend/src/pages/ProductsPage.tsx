import { useAuth } from '../state/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

interface ProductSummary {
  id: number;
  shopifyProductId: number;
  title: string | null;
  productType: string | null;
  vendor: string | null;
  status: string | null;
  minPrice: number | null;
  maxPrice: number | null;
  totalVariants: number | null;
}

export const ProductsPage = () => {
  const { selectedStore } = useAuth();
  const storeId = selectedStore?.id;

  const { data, isLoading } = useQuery<ProductSummary[]>({
    queryKey: ['productsList', storeId],
    enabled: !!storeId,
    queryFn: async () => {
      const res = await api.get('/api/products/list', { params: { storeId } });
      return res.data;
    },
  });

  return (
    <div>
      <h1 className="text-xl font-semibold text-slate-800 mb-4">Products</h1>
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-2 text-left">Title</th>
              <th className="px-4 py-2 text-left">Type</th>
              <th className="px-4 py-2 text-left">Vendor</th>
              <th className="px-4 py-2 text-right">Price range</th>
              <th className="px-4 py-2 text-right">Variants</th>
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
                  No products yet.
                </td>
              </tr>
            )}
            {data?.map(p => (
              <tr key={p.id} className="border-t hover:bg-slate-50">
                <td className="px-4 py-2">{p.title || '-'}</td>
                <td className="px-4 py-2 text-xs text-slate-500">
                  {p.productType || '-'}
                </td>
                <td className="px-4 py-2 text-xs text-slate-500">
                  {p.vendor || '-'}
                </td>
                <td className="px-4 py-2 text-right">
                  {p.minPrice != null && p.maxPrice != null
                    ? `₹${p.minPrice.toFixed(2)} – ₹${p.maxPrice.toFixed(2)}`
                    : '-'}
                </td>
                <td className="px-4 py-2 text-right">
                  {p.totalVariants ?? '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
