import { ArrowRight, Eye, EyeOff } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';
import BookCover from '../components/BookCover.jsx';
import { Field, Spinner } from '../components/Bits.jsx';
import { useAuth } from '../lib/auth.jsx';
import { useTitle } from '../lib/hooks.js';

export default function Auth({ mode }) {
  const register = mode === 'register';
  useTitle(register ? 'Criar conta' : 'Entrar');
  const auth = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const back = params.get('voltar') || '/';
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [show, setShow] = useState(false);
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState(null);
  const [busy, setBusy] = useState(false);
  const set = (k, v) => { setForm((f) => ({ ...f, [k]: v })); setErrors((e) => ({ ...e, [k]: undefined })); };

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setMessage(null);
    try {
      const user = register ? await auth.register(form) : await auth.login(form.email, form.password);
      navigate(back === '/' && user.role === 'ADMIN' ? '/admin' : back, { replace: true });
    } catch (err) {
      setErrors(err.fields || {});
      setMessage(err.message);
    } finally {
      setBusy(false);
    }
  }
  const demo = (email) => setForm({ ...form, email, password: 'Prelo@2026' });

  return (
    <div className="auth page" style={{ paddingBottom: 0 }}>
      <div className="auth-art">
        <div className="floaty" style={{ top: 70, left: 60, rotate: '-8deg' }}><BookCover title="O Último Bibliotecário" authors="Sofia Carvalho" style={7} /></div>
        <div className="floaty" style={{ top: 150, left: 250, width: 170, animationDelay: '-2s' }}><BookCover title="Dom Casmurro" authors="Machado de Assis" style={2} /></div>
        <div className="floaty" style={{ top: 60, left: 440, rotate: '9deg', animationDelay: '-4s' }}><BookCover title="Orgulho e Preconceito" authors="Jane Austen" style={3} /></div>
        <span className="eyebrow" style={{ color: 'var(--butter)' }}>Prelo</span>
        <h2 style={{ marginTop: 12 }}>{register ? 'Sua estante começa com uma conta.' : 'Que bom ver você de volta.'}</h2>
        <p style={{ opacity: .75, marginTop: 12, maxWidth: 420 }}>Acompanhe pedidos, salve endereços e pague com Pix, boleto ou cartão em até 6x.</p>
      </div>
      <div className="auth-form">
        <form onSubmit={submit} className="rise">
          <div><h1 style={{ fontSize: 36 }}>{register ? 'Criar conta' : 'Entrar'}</h1>
            <p className="muted" style={{ marginTop: 6 }}>{register ? 'Leva menos de um minuto.' : 'Use seu e-mail e senha.'}</p></div>
          {message && <div className="alert red fade">{message}</div>}
          {register && (
            <Field label="Nome completo" error={errors.fullName}>
              <input className="input" autoComplete="name" value={form.fullName} onChange={(e) => set('fullName', e.target.value)} />
            </Field>
          )}
          <Field label="E-mail" error={errors.email}>
            <input className="input" type="email" autoComplete="email" value={form.email} onChange={(e) => set('email', e.target.value)} />
          </Field>
          <Field label="Senha" error={errors.password} hint={register ? 'Mínimo de 8 caracteres.' : undefined}>
            <div style={{ position: 'relative' }}>
              <input className="input" type={show ? 'text' : 'password'} autoComplete={register ? 'new-password' : 'current-password'}
                value={form.password} onChange={(e) => set('password', e.target.value)} style={{ paddingRight: 46 }} />
              <button type="button" className="btn btn-ghost btn-icon btn-sm" style={{ position: 'absolute', right: 6, top: 6 }}
                onClick={() => setShow((s) => !s)} aria-label={show ? 'Esconder senha' : 'Mostrar senha'}>{show ? <EyeOff size={16} /> : <Eye size={16} />}</button>
            </div>
          </Field>
          <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={busy}>
            {busy ? <Spinner /> : null} {register ? 'Criar conta' : 'Entrar'} <ArrowRight size={18} />
          </button>
          <p className="muted" style={{ textAlign: 'center', fontSize: 14 }}>
            {register ? <>Já tem conta? <Link to={`/entrar?voltar=${encodeURIComponent(back)}`} style={{ fontWeight: 800, color: 'var(--accent-strong)' }}>Entrar</Link></>
              : <>Primeira vez aqui? <Link to={`/cadastro?voltar=${encodeURIComponent(back)}`} style={{ fontWeight: 800, color: 'var(--accent-strong)' }}>Criar conta</Link></>}
          </p>
          {!register && (
            <div className="demo-box">
              Contas de demonstração (senha <b>Prelo@2026</b>):{' '}
              <button type="button" onClick={() => demo('leitora@prelo.dev')}>leitora@prelo.dev</button> ·{' '}
              <button type="button" onClick={() => demo('admin@prelo.dev')}>admin@prelo.dev</button>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}
