import { LayoutDashboard, LogOut, Moon, Package, Search, ShoppingBag, Sun, User } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router';
import { useAuth } from '../lib/auth.jsx';
import { useCart } from '../lib/cart.jsx';
import { useScrolled, useTheme } from '../lib/hooks.js';
import Logo from './Logo.jsx';

export default function Header() {
  const scrolled = useScrolled();
  const { user, isAdmin, logout } = useAuth();
  const { cart, setOpen, bump } = useCart();
  const [theme, toggleTheme] = useTheme();
  const [menu, setMenu] = useState(false);
  const [q, setQ] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const input = useRef(null);
  const menuRef = useRef(null);
  const [bumping, setBumping] = useState(false);

  useEffect(() => {
    if (!bump) return undefined;
    setBumping(true);
    const t = setTimeout(() => setBumping(false), 500);
    return () => clearTimeout(t);
  }, [bump]);

  useEffect(() => setMenu(false), [location.pathname]);
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (location.pathname === '/livros') setQ(params.get('q') || '');
  }, [location]);

  useEffect(() => {
    const onKey = (e) => {
      if (e.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) {
        e.preventDefault();
        input.current?.focus();
      }
    };
    const onClick = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenu(false);
    document.addEventListener('keydown', onKey);
    document.addEventListener('mousedown', onClick);
    return () => {
      document.removeEventListener('keydown', onKey);
      document.removeEventListener('mousedown', onClick);
    };
  }, []);

  function submit(e) {
    e.preventDefault();
    navigate(`/livros${q.trim() ? `?q=${encodeURIComponent(q.trim())}` : ''}`);
    input.current?.blur();
  }

  return (
    <header className={`header ${scrolled ? 'scrolled' : ''}`}>
      <div className="container header-inner">
        <Link to="/" className="brand" aria-label="Prelo, página inicial"><Logo /></Link>
        <form className="search" onSubmit={submit} role="search">
          <Search className="icon" />
          <input ref={input} value={q} onChange={(e) => setQ(e.target.value)} placeholder="Título, autor ou ISBN"
            aria-label="Buscar livros" />
          <kbd>/</kbd>
        </form>
        <nav className="nav">
          <NavLink to="/categorias" className="nav-link desktop-only">Categorias</NavLink>
          {user && <NavLink to="/compras" className="nav-link"><Package size={18} /><span className="label">Compras</span></NavLink>}
          <button type="button" className="nav-link btn-ghost" style={{ border: 0, background: 'none' }} onClick={toggleTheme}
            aria-label={theme === 'dark' ? 'Usar tema claro' : 'Usar tema escuro'}>
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </button>
          {user ? (
            <div className="menu" ref={menuRef}>
              <button type="button" className="nav-link" style={{ border: 0, background: 'none' }} onClick={() => setMenu((m) => !m)}
                aria-expanded={menu} aria-haspopup="menu">
                <User size={18} /><span className="label">{user.fullName.split(' ')[0]}</span>
              </button>
              {menu && (
                <div className="menu-pop" role="menu">
                  <div className="who"><b>{user.fullName}</b><div className="subtle">{user.email}</div></div>
                  <Link to="/compras" role="menuitem"><Package size={16} /> Minhas compras</Link>
                  <Link to="/conta" role="menuitem"><User size={16} /> Minha conta</Link>
                  {isAdmin && <Link to="/admin" role="menuitem"><LayoutDashboard size={16} /> Administração</Link>}
                  <button type="button" onClick={() => logout().then(() => navigate('/'))} role="menuitem"><LogOut size={16} /> Sair</button>
                </div>
              )}
            </div>
          ) : (
            <NavLink to="/entrar" className="nav-link"><User size={18} /><span className="label">Entrar</span></NavLink>
          )}
          <button type="button" className="nav-link cart-btn" style={{ border: 0, background: 'none' }} onClick={() => setOpen(true)}
            aria-label={`Carrinho com ${cart.itemCount} itens`}>
            <ShoppingBag size={19} />
            {cart.itemCount > 0 && <span className={`cart-count ${bumping ? 'bump' : ''}`}>{cart.itemCount}</span>}
          </button>
        </nav>
      </div>
    </header>
  );
}
