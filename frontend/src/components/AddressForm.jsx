import { useState } from 'react';
import { Field, Spinner } from './Bits.jsx';

const EMPTY = { label: 'Casa', recipient: '', zipCode: '', street: '', streetNumber: '', complement: '', district: '', city: '', state: '', isDefault: false };

/** Formulário de endereço. Preenche rua, bairro e cidade pelo CEP (ViaCEP) quando a internet permite. */
export default function AddressForm({ initial, onSubmit, onCancel, submitLabel = 'Salvar endereço' }) {
  const [a, setA] = useState({ ...EMPTY, ...initial });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [looking, setLooking] = useState(false);
  const set = (k, v) => { setA((s) => ({ ...s, [k]: v })); setErrors((e) => ({ ...e, [k]: undefined })); };

  async function lookup(zip) {
    const digits = zip.replace(/\D/g, '');
    if (digits.length !== 8) return;
    setLooking(true);
    try {
      const r = await fetch(`https://viacep.com.br/ws/${digits}/json/`);
      const d = await r.json();
      if (!d.erro) setA((s) => ({ ...s, street: d.logradouro || s.street, district: d.bairro || s.district, city: d.localidade || s.city, state: d.uf || s.state }));
    } catch { /* sem consulta de CEP: a pessoa preenche */ } finally { setLooking(false); }
  }

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    try {
      await onSubmit({ ...a, complement: a.complement || null });
    } catch (err) {
      setErrors(err.fields || {});
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="stack fade" style={{ gap: 14 }} onSubmit={submit}>
      <div className="grid-2">
        <Field label="Nome do endereço" error={errors.label}><input className="input" value={a.label} onChange={(e) => set('label', e.target.value)} /></Field>
        <Field label="Quem vai receber" error={errors.recipient}><input className="input" value={a.recipient} onChange={(e) => set('recipient', e.target.value)} autoComplete="name" /></Field>
      </div>
      <div className="grid-3">
        <Field label="CEP" error={errors.zipCode} hint={looking ? 'Buscando...' : undefined}>
          <input className="input" inputMode="numeric" autoComplete="postal-code" value={a.zipCode}
            onChange={(e) => { const v = e.target.value.replace(/\D/g, '').slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2'); set('zipCode', v); lookup(v); }} />
        </Field>
        <div style={{ gridColumn: 'span 2' }}>
          <Field label="Rua" error={errors.street}><input className="input" value={a.street} onChange={(e) => set('street', e.target.value)} autoComplete="address-line1" /></Field>
        </div>
      </div>
      <div className="grid-3">
        <Field label="Número" error={errors.streetNumber}><input className="input" value={a.streetNumber} onChange={(e) => set('streetNumber', e.target.value)} /></Field>
        <div style={{ gridColumn: 'span 2' }}>
          <Field label="Complemento"><input className="input" value={a.complement || ''} onChange={(e) => set('complement', e.target.value)} placeholder="Apto, bloco, referência" /></Field>
        </div>
      </div>
      <div className="grid-3">
        <Field label="Bairro" error={errors.district}><input className="input" value={a.district} onChange={(e) => set('district', e.target.value)} /></Field>
        <Field label="Cidade" error={errors.city}><input className="input" value={a.city} onChange={(e) => set('city', e.target.value)} /></Field>
        <Field label="UF" error={errors.state}><input className="input" maxLength={2} value={a.state} onChange={(e) => set('state', e.target.value.toUpperCase())} /></Field>
      </div>
      <label className="check"><input type="checkbox" checked={a.isDefault} onChange={(e) => set('isDefault', e.target.checked)} /> Usar como endereço principal</label>
      <div className="row">
        <button type="submit" className="btn btn-primary" disabled={busy}>{busy && <Spinner />} {submitLabel}</button>
        {onCancel && <button type="button" className="btn btn-ghost" onClick={onCancel}>Cancelar</button>}
      </div>
    </form>
  );
}
