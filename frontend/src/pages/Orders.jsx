import { ChevronRight, Package } from 'lucide-react';
import { Link, useSearchParams } from 'react-router';
import BookCover from '../components/BookCover.jsx';
import { Empty, OrderBadge, Pager } from '../components/Bits.jsx';
import { qs } from '../lib/api.js';
import { date, longDay, money, PAYMENT_METHOD, plural, SHIPMENT_STATUS } from '../lib/format.js';
import { useFetch, useTitle } from '../lib/hooks.js';

const FILTERS = [['', 'Todas'], ['PENDING_PAYMENT', 'A pagar'], ['PAID', 'Em separação'], ['SHIPPED', 'A caminho'], ['DELIVERED', 'Entregues'], ['CANCELED', 'Canceladas']];

export default function Orders() {
  useTitle('Minhas compras');
  const [params, setParams] = useSearchParams();
  const status = params.get('status') || '';
  const page = Number(params.get('pagina') || 0);
  const { data, loading } = useFetch(`/api/orders${qs({ status, page, size: 10 })}`);

  return (
    <div className="container page">
      <div className="listing-head">
        <span className="eyebrow">Sua conta</span>
        <h1>Minhas compras</h1>
        <p>Acompanhe pagamentos, envios e o histórico dos seus pedidos.</p>
      </div>
      <div className="subnav">
        {FILTERS.map(([k, v]) => (
          <button key={k} type="button" className={`chip ${status === k ? 'on' : ''}`} onClick={() => setParams(k ? { status: k } : {})}>{v}</button>
        ))}
      </div>
      {loading && !data ? (
        <div className="stack">{[0, 1, 2].map((i) => <div key={i} className="skeleton" style={{ height: 104, borderRadius: 20 }} />)}</div>
      ) : data?.items.length ? (
        <div className="stack" style={{ gap: 12 }}>
          {data.items.map((o, i) => (
            <Link key={o.code} to={`/compras/${o.code}`} className="order-card rise" style={{ '--i': i }}>
              <div className="stack-covers">
                {o.preview.slice(0, 3).map((it) => <BookCover key={it.bookId} title={it.title} style={it.coverStyle} showText={false} />)}
              </div>
              <div className="stack" style={{ gap: 6, minWidth: 0 }}>
                <div className="row wrap" style={{ gap: 10 }}><b className="mono">{o.code}</b><OrderBadge status={o.status} /></div>
                <span className="muted" style={{ fontSize: 14, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {o.preview.map((it) => it.title).join(' · ')}
                </span>
                <span className="subtle">
                  {date(o.placedAt)} · {plural(o.itemCount, 'livro', 'livros')}{o.paymentMethod ? ` · ${PAYMENT_METHOD[o.paymentMethod]}` : ''}
                  {o.shipmentStatus && o.status !== 'DELIVERED' && o.status !== 'CANCELED' ? ` · ${SHIPMENT_STATUS[o.shipmentStatus]}` : ''}
                  {o.estimatedDelivery && (o.status === 'PAID' || o.status === 'SHIPPED') ? ` · chega até ${longDay(o.estimatedDelivery)}` : ''}
                </span>
              </div>
              <div className="row" style={{ gap: 14 }}><b style={{ fontSize: 17 }}>{money(o.total)}</b><ChevronRight size={18} className="muted" /></div>
            </Link>
          ))}
          <Pager page={data.page} totalPages={data.totalPages} onPage={(p) => setParams({ ...(status ? { status } : {}), pagina: p })} />
        </div>
      ) : (
        <Empty title={status ? 'Nenhum pedido neste filtro' : 'Você ainda não fez nenhuma compra'} action={<Link to="/livros" className="btn btn-dark"><Package size={16} /> Encontrar um livro</Link>}>
          Quando você comprar, os pedidos aparecem aqui com o rastreio do envio.
        </Empty>
      )}
    </div>
  );
}
