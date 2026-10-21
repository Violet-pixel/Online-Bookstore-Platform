import { KeyRound, MapPin, Package, Pencil, Plus, Trash2, User } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router';
import AddressForm from '../components/AddressForm.jsx';
import { Field, Spinner } from '../components/Bits.jsx';
import { del, post, put } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';
import { useFetch, useTitle } from '../lib/hooks.js';
import { useToast } from '../lib/toast.jsx';

export default function Account() {
  useTitle('Minha conta');
  const { user, setUser } = useAuth();
  const toast = useToast();
  const addresses = useFetch('/api/account/addresses');
  const [profile, setProfile] = useState({ fullName: user.fullName, phone: user.phone || '' });
  const [pwd, setPwd] = useState({ currentPassword: '', newPassword: '' });
  const [errors, setErrors] = useState({});
  const [editing, setEditing] = useState(null);
  const [busy, setBusy] = useState('');

  async function saveProfile(e) {
    e.preventDefault();
    setBusy('profile');
    try {
      setUser(await put('/api/account/profile', profile));
      toast('Dados atualizados');
    } catch (err) {
      setErrors(err.fields);
      toast(err.message, { error: true });
    } finally { setBusy(''); }
  }
  async function savePassword(e) {
    e.preventDefault();
    setBusy('pwd');
    try {
      await post('/api/account/password', pwd);
      setPwd({ currentPassword: '', newPassword: '' });
      toast('Senha alterada');
    } catch (err) {
      setErrors(err.fields);
      toast(err.message, { error: true });
    } finally { setBusy(''); }
  }
  async function saveAddress(data) {
    if (editing === 'new') await post('/api/account/addresses', data);
    else await put(`/api/account/addresses/${editing}`, data);
    setEditing(null);
    addresses.reload();
    toast('Endereço salvo');
  }
  async function remove(id) {
    await del(`/api/account/addresses/${id}`);
    addresses.reload();
  }

  return (
    <div className="container page">
      <div className="account">
        <nav className="side-nav">
          <a href="#dados" className="active"><User size={16} /> Seus dados</a>
          <a href="#enderecos"><MapPin size={16} /> Endereços</a>
          <a href="#senha"><KeyRound size={16} /> Senha</a>
          <Link to="/compras"><Package size={16} /> Minhas compras</Link>
        </nav>
        <div className="stack" style={{ gap: 22 }}>
          <div><span className="eyebrow">Minha conta</span><h1 style={{ marginTop: 8 }}>Olá, {user.fullName.split(' ')[0]}</h1></div>
          <section id="dados" className="card rise">
            <h3 style={{ marginBottom: 16 }}>Seus dados</h3>
            <form className="stack" style={{ gap: 14 }} onSubmit={saveProfile}>
              <div className="grid-2">
                <Field label="Nome completo" error={errors.fullName}><input className="input" value={profile.fullName} onChange={(e) => setProfile({ ...profile, fullName: e.target.value })} /></Field>
                <Field label="Telefone" error={errors.phone}><input className="input" value={profile.phone} placeholder="(21) 90000-0000" onChange={(e) => setProfile({ ...profile, phone: e.target.value })} /></Field>
              </div>
              <Field label="E-mail" hint="O e-mail identifica a sua conta e não pode ser trocado aqui."><input className="input" value={user.email} disabled /></Field>
              <div><button type="submit" className="btn btn-dark" disabled={busy === 'profile'}>{busy === 'profile' && <Spinner />} Salvar</button></div>
            </form>
          </section>
          <section id="enderecos" className="card rise" style={{ '--i': 1 }}>
            <div className="panel-title"><h3>Endereços</h3>{editing === null && <button type="button" className="btn btn-sm" onClick={() => setEditing('new')}><Plus size={14} /> Novo endereço</button>}</div>
            <div className="stack" style={{ gap: 10 }}>
              {editing === 'new' && <AddressForm onSubmit={saveAddress} onCancel={() => setEditing(null)} />}
              {addresses.data?.map((a) => (editing === a.id ? (
                <AddressForm key={a.id} initial={a} onSubmit={saveAddress} onCancel={() => setEditing(null)} />
              ) : (
                <div key={a.id} className="addr fade">
                  <div className="stack" style={{ gap: 2 }}>
                    <b>{a.label} {a.isDefault && <span className="badge rose" style={{ marginLeft: 6 }}>principal</span>}</b>
                    <span className="muted" style={{ fontSize: 14 }}>{a.street}, {a.streetNumber}{a.complement ? ` · ${a.complement}` : ''} · {a.district} · {a.city}/{a.state}</span>
                    <span className="subtle">CEP {a.zipCode} · {a.recipient}</span>
                  </div>
                  <div className="row" style={{ gap: 4 }}>
                    <button type="button" className="btn btn-ghost btn-icon btn-sm" aria-label="Editar" onClick={() => setEditing(a.id)}><Pencil size={15} /></button>
                    <button type="button" className="btn btn-ghost btn-icon btn-sm" aria-label="Excluir" onClick={() => remove(a.id)}><Trash2 size={15} /></button>
                  </div>
                </div>
              )))}
              {addresses.data?.length === 0 && editing === null && <p className="muted">Nenhum endereço cadastrado.</p>}
            </div>
          </section>
          <section id="senha" className="card rise" style={{ '--i': 2 }}>
            <h3 style={{ marginBottom: 16 }}>Trocar senha</h3>
            <form className="stack" style={{ gap: 14 }} onSubmit={savePassword}>
              <div className="grid-2">
                <Field label="Senha atual" error={errors.currentPassword}><input type="password" className="input" autoComplete="current-password" value={pwd.currentPassword} onChange={(e) => setPwd({ ...pwd, currentPassword: e.target.value })} /></Field>
                <Field label="Nova senha" error={errors.newPassword} hint="Mínimo de 8 caracteres."><input type="password" className="input" autoComplete="new-password" value={pwd.newPassword} onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })} /></Field>
              </div>
              <div><button type="submit" className="btn btn-dark" disabled={busy === 'pwd'}>{busy === 'pwd' && <Spinner />} Alterar senha</button></div>
            </form>
          </section>
        </div>
      </div>
    </div>
  );
}
