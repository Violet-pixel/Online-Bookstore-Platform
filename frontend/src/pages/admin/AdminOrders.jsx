import { Search } from 'lucide-react';
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { OrderBadge, Pager } from '../../components/Bits.jsx';
import { qs } from '../../lib/api.js';
import { dateTime, money, PAYMENT_METHOD, SHIPMENT_STATUS } from '../../lib/format.js';
import { useFetch } from '../../lib/hooks.js';

const FILTERS = [['', 'Todos'], ['PENDING_PAYMENT', 'A pagar'], ['PAID', 'Para enviar'], ['SHIPPED', 'A caminho'], ['DELIVERED', 'Entregues'], ['CANCELED', 'Cancelados']];

export default function AdminOrders() {
  const [params, setParams] = useSearchParams();
  const status = params.get('status') || '';
  const q = params.get('q') || '';
  const page = Number(params.get('pagina') || 0);
  const [term, setTerm] = useState(q);
  const { data } = useFetch(`/api/admin/orders${qs({ status, q, page, size: 15 })}`);
  const set = (next) => setParams(Object.fromEntries(Object.entries({ status, q, ...next }).filter(([, v]) => v !== '' && v !== undefined)));
  return (
    <div className="stack fade" style={{ gap: 18 }}>
      <div className="row between wrap"><h1>Pedidos e envios</h1>
        <form className="search" style={{ maxWidth: 320 }} onSubmit={(e) => { e.preventDefault(); set({ q: term, pagina: '' }); }}>
          <Search className="icon" /><input value={term} onChange={(e) => setTerm(e.target.value)} placeholder="Código, cliente ou e-mail" />
        </form>
      </div>
      <div className="subnav" style={{ padding: 0 }}>
        {FILTERS.map(([k, v]) => <button key={k} type="button" className={`chip ${status === k ? 'on' : ''}`} onClick={() => set({ status: k, pagina: '' })}>{v}</button>)}
      </div>
      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrap">
          <table className="table">
            <thead><tr><th>Pedido</th><th>Cliente</th><th>Feito em</th><th>Status</th><th>Envio</th><th>Pagamento</th><th style={{ textAlign: 'right' }}>Total</th></tr></thead>
            <tbody>
              {data?.items.map((o) => (
                <tr key={o.code}>
                  <td><Link to={`/admin/pedidos/${o.code}`} className="mono" style={{ fontWeight: 700, color: 'var(--link)' }}>{o.code}</Link></td>
                  <td>{o.customerName}</td>
                  <td className="subtle">{dateTime(o.placedAt)}</td>
                  <td><OrderBadge status={o.status} /></td>
                  <td className="subtle">{o.shipmentStatus ? SHIPMENT_STATUS[o.shipmentStatus] : '—'}</td>
                  <td className="subtle">{o.paymentMethod ? PAYMENT_METHOD[o.paymentMethod] : '—'}</td>
                  <td style={{ textAlign: 'right', fontWeight: 700 }}>{money(o.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {data && data.items.length === 0 && <p className="muted" style={{ padding: 20 }}>Nenhum pedido encontrado.</p>}
      </div>
      {data && <Pager page={data.page} totalPages={data.totalPages} onPage={(p) => set({ pagina: p })} />}
    </div>
  );
}
