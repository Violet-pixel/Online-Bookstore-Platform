import { Link } from 'react-router';
import BookCover from '../../components/BookCover.jsx';
import { money, ORDER_STATUS } from '../../lib/format.js';
import { useFetch } from '../../lib/hooks.js';

export default function Dashboard() {
  const { data } = useFetch('/api/admin/dashboard');
  if (!data) return <div className="kpis">{[0, 1, 2, 3].map((i) => <div key={i} className="skeleton" style={{ height: 110 }} />)}</div>;
  const maxStatus = Math.max(1, ...Object.values(data.ordersByStatus));
  return (
    <div className="stack fade" style={{ gap: 22 }}>
      <div><h1>Painel</h1><p className="muted" style={{ marginTop: 6 }}>Como a livraria está indo nos últimos 30 dias.</p></div>
      <div className="kpis">
        <div className="kpi"><span>Receita em 30 dias</span><b>{money(data.revenueLast30)}</b><small>ticket médio {money(data.averageTicket)}</small></div>
        <div className="kpi"><span>Pedidos em 30 dias</span><b>{data.ordersLast30}</b><small>{data.ordersToday} hoje</small></div>
        <div className="kpi"><span>Para enviar</span><b>{data.toShip}</b><small>{data.inTransit} a caminho</small></div>
        <div className="kpi"><span>Aguardando pagamento</span><b>{data.awaitingPayment}</b><small>{data.customers} contas, {data.activeBooks} livros à venda</small></div>
      </div>
      <div className="order-grid">
        <section className="card">
          <div className="panel-title"><h3>Pedidos por status</h3><Link to="/admin/pedidos" className="btn btn-ghost btn-sm">Ver pedidos</Link></div>
          <div className="bars">
            {Object.entries(data.ordersByStatus).map(([s, n], i) => (
              <Link key={s} to={`/admin/pedidos?status=${s}`} className="b">
                <span>{ORDER_STATUS[s]?.label}</span>
                <span className="track"><i style={{ width: `${(n / maxStatus) * 100}%`, animationDelay: `${i * 80}ms`, background: s === 'CANCELED' ? 'var(--stone)' : s === 'DELIVERED' ? 'var(--success)' : undefined }} /></span>
                <b>{n}</b>
              </Link>
            ))}
          </div>
        </section>
        <section className="card">
          <div className="panel-title"><h3>Estoque baixo</h3><Link to="/admin/livros" className="btn btn-ghost btn-sm">Repor</Link></div>
          {data.lowStock.length === 0 ? <p className="muted">Tudo abastecido.</p> : data.lowStock.map((b) => (
            <Link key={b.id} to={`/admin/livros/${b.id}`} className="sum-row" style={{ padding: '8px 0' }}>
              <span>{b.title}</span><span className={`badge ${b.stockQuantity === 0 ? 'red' : 'gold'}`}>{b.stockQuantity === 0 ? 'esgotado' : `${b.stockQuantity} un.`}</span>
            </Link>
          ))}
        </section>
      </div>
      <section className="card">
        <h3 style={{ marginBottom: 16 }}>Mais vendidos</h3>
        <div className="row wrap" style={{ gap: 18, alignItems: 'flex-start' }}>
          {data.topSellers.map((b, i) => (
            <Link key={b.id} to={`/livro/${b.slug}`} className="stack" style={{ width: 110, gap: 8 }}>
              <BookCover title={b.title} style={b.coverStyle} />
              <span style={{ fontSize: 13.5, fontWeight: 600, lineHeight: 1.3 }}>{i + 1}. {b.title}</span>
              <span className="subtle">{b.soldCount.toLocaleString('pt-BR')} vendidos</span>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
