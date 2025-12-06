import { useNavigate } from 'react-router-dom';
import { useAuth } from '../state/AuthContext';

export const StoreSelectPage = () => {
  const { user, selectedStore, setSelectedStore } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const handleSelect = (storeId: number) => {
    const store = user.stores.find(s => s.id === storeId) || null;
    setSelectedStore(store);
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen bg-slate-100 flex justify-center py-10">
      <div className="w-full max-w-3xl bg-white rounded-xl shadow-lg p-8">
        <h1 className="text-2xl font-semibold text-slate-800 mb-2">
          Choose a store
        </h1>
        <p className="text-sm text-slate-500 mb-6">
          Logged in as <span className="font-medium">{user.email}</span>.
          Your organization has {user.stores.length} store
          {user.stores.length !== 1 ? 's' : ''}.
        </p>

        {user.stores.length === 0 && (
          <div className="text-sm text-slate-600">
            No stores found for this organization.
          </div>
        )}

        <div className="grid md:grid-cols-2 gap-4">
          {user.stores.map(store => (
            <button
              key={store.id}
              onClick={() => handleSelect(store.id)}
              className={`text-left border rounded-xl px-4 py-3 hover:shadow-md transition bg-slate-50 ${
                selectedStore?.id === store.id ? 'border-blue-500' : 'border-slate-200'
              }`}
            >
              <div className="font-medium text-slate-800">
                {store.shopDomain}
              </div>
              <div className="text-xs text-slate-500 mt-1">
                Last full sync:{' '}
                {store.lastFullSyncAt ? store.lastFullSyncAt : 'Never'}
              </div>
              <div className="text-xs text-slate-500">
                Last order webhook:{' '}
                {store.lastOrderWebhookAt ? store.lastOrderWebhookAt : 'Never'}
              </div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
