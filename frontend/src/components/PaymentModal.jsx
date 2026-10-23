import { Barcode, Check, Copy, CreditCard, FlaskConical, Lock, QrCode, ShieldCheck } from 'lucide-react';
import QRCode from 'qrcode';
import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { get, post } from '../lib/api.js';
import { date, money } from '../lib/format.js';
import { useCountdown } from '../lib/hooks.js';
import { Field, Spinner } from './Bits.jsx';
import { Modal } from './Overlay.jsx';

const TABS = [
  { id: 'CREDIT_CARD', label: 'Cartão', icon: CreditCard },
  { id: 'PIX', label: 'Pix', icon: QrCode },
  { id: 'BOLETO', label: 'Boleto', icon: Barcode },
];

function brandOf(n) {
  if (/^(4011|4312|4389|4514|4576|5041|5066|5067|509|6277|6362|6363|650|6516|6550)/.test(n)) return 'ELO';
  if (/^(606282|3841)/.test(n)) return 'HIPERCARD';
  if (/^4/.test(n)) return 'VISA';
  if (/^(5[1-5]|2[2-7])/.test(n)) return 'MASTERCARD';
  if (/^3[47]/.test(n)) return 'AMEX';
  return '';
}
const uuid = () => (crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`);

function Tabs({ value, onChange }) {
  const ref = useRef(null);
  const [ink, setInk] = useState({ x: 0, w: 0 });
  useLayoutEffect(() => {
    const el = ref.current?.querySelector('button.on');
    if (el) setInk({ x: el.offsetLeft - 4, w: el.offsetWidth });
  }, [value]);
  return (
    <div className="tabs" ref={ref} role="tablist">
      <span className="ink" style={{ width: ink.w, transform: `translateX(${ink.x}px)`, left: 4 }} />
      {TABS.map((t) => (
        <button key={t.id} type="button" role="tab" aria-selected={value === t.id} className={value === t.id ? 'on' : ''} onClick={() => onChange(t.id)}>
          <t.icon size={17} /> {t.label}
        </button>
      ))}
    </div>
  );
}

function Countdown({ until }) {
  const left = useCountdown(until);
  const total = 30 * 60 * 1000;
  const r = 9;
  const c = 2 * Math.PI * r;
  const m = Math.floor(left / 60000);
  const s = Math.floor((left % 60000) / 1000);
  return (
    <span className="countdown" aria-live="off">
      <svg viewBox="0 0 22 22"><circle className="bg" cx="11" cy="11" r={r} /><circle className="fg" cx="11" cy="11" r={r} strokeDasharray={c} strokeDashoffset={c * (1 - Math.min(1, left / total))} /></svg>
      {left > 0 ? `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}` : 'expirado'}
    </span>
  );
}

function CopyField({ value }) {
  const [ok, setOk] = useState(false);
  return (
    <div className="copy-field">
      <input className="input" readOnly value={value} onFocus={(e) => e.target.select()} />
      <button type="button" className={`btn ${ok ? 'btn-gold' : 'btn-dark'}`} onClick={() => navigator.clipboard?.writeText(value).then(() => { setOk(true); setTimeout(() => setOk(false), 1800); })}>
        {ok ? <Check size={16} /> : <Copy size={16} />} {ok ? 'Copiado' : 'Copiar'}
      </button>
    </div>
  );
}

function Success({ payment, onDone }) {
  const pieces = useMemo(() => Array.from({ length: 26 }, (_, i) => ({
    left: `${(i * 37) % 100}%`, delay: `${(i % 7) * 60}ms`, bg: ['#0A84B8', '#C8236A', '#F2C230', '#1B1B1D'][i % 4], rot: (i * 47) % 180,
  })), []);
  return (
    <div className="stack" style={{ alignItems: 'center', textAlign: 'center', gap: 14, position: 'relative', padding: '10px 0' }}>
      <div className="confetti">{pieces.map((p, i) => <i key={i} style={{ left: p.left, animationDelay: p.delay, background: p.bg, transform: `rotate(${p.rot}deg)` }} />)}</div>
      <svg className="success-mark" viewBox="0 0 86 86"><circle cx="43" cy="43" r="40" /><path d="M26 44 l11 11 l23 -24" /></svg>
      <h3 style={{ fontSize: 24 }}>Pagamento aprovado!</h3>
      <p className="muted" style={{ maxWidth: 380 }}>
        Recebemos {money(payment.amount)}{payment.cardLast4 ? ` no cartão final ${payment.cardLast4}` : ''}. Seus livros já estão sendo separados.
      </p>
      <button type="button" className="btn btn-primary btn-lg" onClick={onDone}>Acompanhar pedido</button>
    </div>
  );
}

export default function PaymentModal({ order, open, onClose, onChange }) {
  const [tab, setTab] = useState('CREDIT_CARD');
  const [methods, setMethods] = useState(null);
  const [installments, setInstallments] = useState([]);
  const [card, setCard] = useState({ number: '', holderName: '', exp: '', cvv: '', installments: 1 });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [failure, setFailure] = useState(null);
  const [pending, setPending] = useState({});
  const [qr, setQr] = useState(null);
  const [paid, setPaid] = useState(null);

  const orderRef = useRef(order);
  orderRef.current = order;
  const code = order?.code;
  useEffect(() => {
    const order = orderRef.current;
    if (!open || !order) return;
    setPaid(null);
    setFailure(null);
    get('/api/payments/methods').then(setMethods).catch(() => {});
    get(`/api/orders/${order.code}/installments`).then(setInstallments).catch(() => {});
    const existing = {};
    (order.payments || []).filter((p) => p.status === 'PENDING').forEach((p) => { existing[p.method] = p; });
    setPending(existing);
    if (existing.PIX) setTab('PIX');
    else if (existing.BOLETO) setTab('BOLETO');
  }, [open, code]);

  const pix = pending.PIX;
  useEffect(() => {
    if (pix?.pixPayload) QRCode.toDataURL(pix.pixPayload, { margin: 0, width: 360, color: { dark: '#1B1B1D', light: '#FFFFFF' } }).then(setQr);
    else setQr(null);
  }, [pix?.pixPayload]);

  // Pix: consulta o status a cada 3 s enquanto a janela estiver aberta
  useEffect(() => {
    const ref = pending[tab]?.reference;
    if (!open || !ref || tab === 'CREDIT_CARD') return undefined;
    const t = setInterval(async () => {
      try {
        const p = await get(`/api/payments/${ref}`);
        if (p.status === 'APPROVED') {
          setPaid(p);
          onChange?.();
        } else if (p.status !== 'PENDING') setPending((s) => ({ ...s, [tab]: p }));
      } catch { /* segue tentando */ }
    }, 3000);
    return () => clearInterval(t);
  }, [open, tab, pending, onChange]);

  const digits = card.number.replace(/\D/g, '');
  const brand = brandOf(digits);
  const shown = (digits + '•'.repeat(Math.max(0, 16 - digits.length))).replace(/(.{4})/g, '$1 ').trim();

  function set(k, v) {
    setErrors((e) => ({ ...e, [k]: undefined }));
    if (k === 'number') v = v.replace(/\D/g, '').slice(0, 19).replace(/(\d{4})(?=\d)/g, '$1 ');
    if (k === 'exp') v = v.replace(/\D/g, '').slice(0, 4).replace(/^(\d{2})(\d)/, '$1/$2');
    if (k === 'cvv') v = v.replace(/\D/g, '').slice(0, 4);
    if (k === 'holderName') v = v.toUpperCase();
    setCard((c) => ({ ...c, [k]: v }));
  }

  async function payCard(e) {
    e.preventDefault();
    const [mm, yy] = card.exp.split('/');
    const errs = {};
    if (digits.length < 13) errs.number = 'Número incompleto.';
    if (!card.holderName.trim()) errs.holderName = 'Como está impresso no cartão.';
    if (!mm || !yy || Number(mm) < 1 || Number(mm) > 12) errs.exp = 'Use MM/AA.';
    if (card.cvv.length < 3) errs.cvv = '3 ou 4 dígitos.';
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true);
    setFailure(null);
    try {
      const token = await post('/api/payments/card-tokens', {
        number: digits, holderName: card.holderName.trim(), expMonth: Number(mm), expYear: 2000 + Number(yy), cvv: card.cvv,
      });
      const p = await post(`/api/orders/${order.code}/payments`,
        { method: 'CREDIT_CARD', cardToken: token.token, installments: Number(card.installments) }, { 'Idempotency-Key': uuid() });
      if (p.status === 'APPROVED') {
        setPaid(p);
        onChange?.();
      } else {
        setFailure(p.failureReason || 'Pagamento recusado.');
        onChange?.();
      }
    } catch (err) {
      setErrors(err.fields || {});
      setFailure(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function generate(method) {
    setBusy(true);
    setFailure(null);
    try {
      const p = await post(`/api/orders/${order.code}/payments`, { method }, { 'Idempotency-Key': uuid() });
      setPending((s) => ({ ...s, [method]: p }));
      onChange?.();
    } catch (err) {
      setFailure(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function simulate(ref) {
    setBusy(true);
    try {
      const p = await post(`/api/sandbox/payments/${ref}/confirm`);
      setPaid(p);
      onChange?.();
    } catch (err) {
      setFailure(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (!order) return null;
  const sandbox = methods?.sandbox;
  const current = pending[tab];

  return (
    <Modal open={open} onClose={onClose} title={paid ? 'Tudo certo' : 'Pagamento'}>
      {(close) => (paid ? <Success payment={paid} onDone={close} /> : (
        <div className="stack" style={{ gap: 18 }}>
          <div className="pay-amount">
            <div><div className="subtle">Pedido {order.code}</div><span className="muted">Total a pagar</span></div>
            <b>{money(order.total)}</b>
          </div>
          <Tabs value={tab} onChange={(t) => { setTab(t); setFailure(null); }} />
          {failure && <div className="alert red fade">{failure}</div>}

          {tab === 'CREDIT_CARD' && (
            <form className="stack fade" style={{ gap: 14 }} onSubmit={payCard} key="card">
              <div className="card-preview" style={{ transform: card.cvv ? 'rotateX(4deg)' : undefined }}>
                <div className="row between"><span className="chipc" /><span className="brand">{brand || 'CARTÃO'}</span></div>
                <div className="num">{shown}</div>
                <div className="row between">
                  <div><div className="lbl">Nome</div><div style={{ fontSize: 13, fontWeight: 700 }}>{card.holderName || 'SEU NOME'}</div></div>
                  <div><div className="lbl">Validade</div><div style={{ fontSize: 13, fontWeight: 700 }}>{card.exp || 'MM/AA'}</div></div>
                </div>
              </div>
              <Field label="Número do cartão" error={errors.number}>
                <input className={`input ${errors.number ? 'bad' : ''}`} inputMode="numeric" autoComplete="cc-number" placeholder="0000 0000 0000 0000"
                  value={card.number} onChange={(e) => set('number', e.target.value)} />
              </Field>
              <Field label="Nome impresso no cartão" error={errors.holderName}>
                <input className={`input ${errors.holderName ? 'bad' : ''}`} autoComplete="cc-name" value={card.holderName} onChange={(e) => set('holderName', e.target.value)} />
              </Field>
              <div className="grid-3">
                <Field label="Validade" error={errors.exp}>
                  <input className={`input ${errors.exp ? 'bad' : ''}`} inputMode="numeric" autoComplete="cc-exp" placeholder="MM/AA" value={card.exp} onChange={(e) => set('exp', e.target.value)} />
                </Field>
                <Field label="CVV" error={errors.cvv}>
                  <input className={`input ${errors.cvv ? 'bad' : ''}`} inputMode="numeric" autoComplete="cc-csc" placeholder="123" value={card.cvv} onChange={(e) => set('cvv', e.target.value)} />
                </Field>
                <Field label="Parcelas">
                  <select className="select" value={card.installments} onChange={(e) => set('installments', e.target.value)}>
                    {(installments.length ? installments : [{ count: 1, value: order.total }]).map((i) => (
                      <option key={i.count} value={i.count}>{i.count}x {money(i.value)}</option>
                    ))}
                  </select>
                </Field>
              </div>
              {sandbox && (
                <div className="sandbox-note"><FlaskConical size={16} />
                  <div>Modo sandbox. Cartões de teste:{' '}
                    {methods.testCards.map((t) => (
                      <button key={t} type="button" className="btn btn-ghost btn-sm" style={{ height: 26, padding: '0 8px' }}
                        onClick={() => { set('number', t.split(' · ')[0]); setCard((c) => ({ ...c, holderName: c.holderName || 'MARINA DUARTE', exp: c.exp || '12/31', cvv: c.cvv || '123' })); }}>
                        {t}
                      </button>
                    ))}
                  </div>
                </div>
              )}
              <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={busy}>
                {busy ? <Spinner /> : <Lock size={16} />} Pagar {money(order.total)}
              </button>
              <p className="subtle row" style={{ justifyContent: 'center', gap: 6 }}><ShieldCheck size={14} /> O número do cartão não é guardado: vira um token de uso único.</p>
            </form>
          )}

          {tab === 'PIX' && (
            <div className="stack fade" style={{ gap: 16 }} key="pix">
              {current?.pixPayload ? (
                <>
                  <div className="pix-box">
                    <div className="qr">{qr ? <img src={qr} alt="QR Code Pix" /> : <div className="skeleton" style={{ aspectRatio: 1 }} />}</div>
                    <div className="stack" style={{ gap: 10 }}>
                      <b style={{ fontSize: 17 }}>Escaneie o QR Code no app do seu banco</b>
                      <p className="muted" style={{ fontSize: 14 }}>Ou copie o código abaixo e use o Pix copia e cola. A confirmação aparece aqui sozinha.</p>
                      <div className="row"><span className="badge gold"><span className="pulse" /> Aguardando pagamento</span><Countdown until={current.expiresAt} /></div>
                    </div>
                  </div>
                  <CopyField value={current.pixPayload} />
                  {sandbox && (
                    <button type="button" className="btn btn-gold btn-block" disabled={busy} onClick={() => simulate(current.reference)}>
                      {busy ? <Spinner /> : <FlaskConical size={16} />} Simular pagamento no banco (sandbox)
                    </button>
                  )}
                </>
              ) : (
                <>
                  <p className="muted">O QR Code vale por 30 minutos e a aprovação é imediata.</p>
                  <button type="button" className="btn btn-primary btn-lg btn-block" disabled={busy} onClick={() => generate('PIX')}>
                    {busy ? <Spinner /> : <QrCode size={18} />} Gerar QR Code Pix
                  </button>
                </>
              )}
            </div>
          )}

          {tab === 'BOLETO' && (
            <div className="stack fade" style={{ gap: 16 }} key="boleto">
              {current?.boletoLine ? (
                <>
                  <div className="alert gold"><Barcode size={18} /><div>Boleto gerado. Vence em <b>{date(current.expiresAt ? new Date(new Date(current.expiresAt).getTime() - 86400000) : null)}</b>; a compensação leva até 2 dias úteis.</div></div>
                  <Field label="Linha digitável"><CopyField value={current.boletoLine} /></Field>
                  <div className="row wrap">
                    <button type="button" className="btn" onClick={close}>Pagar depois</button>
                    {sandbox && (
                      <button type="button" className="btn btn-gold grow" disabled={busy} onClick={() => simulate(current.reference)}>
                        {busy ? <Spinner /> : <FlaskConical size={16} />} Simular compensação (sandbox)
                      </button>
                    )}
                  </div>
                </>
              ) : (
                <>
                  <p className="muted">O pedido fica reservado até o vencimento. Os livros são enviados depois da compensação.</p>
                  <button type="button" className="btn btn-primary btn-lg btn-block" disabled={busy} onClick={() => generate('BOLETO')}>
                    {busy ? <Spinner /> : <Barcode size={18} />} Gerar boleto
                  </button>
                </>
              )}
            </div>
          )}
        </div>
      ))}
    </Modal>
  );
}
