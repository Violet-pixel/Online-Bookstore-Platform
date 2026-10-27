import { Bookmark, LayoutDashboard, LogOut, Moon, Package, ShoppingBag, Sun, User } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router';
import { useAuth } from '../lib/auth.jsx';
import { useCart } from '../lib/cart.jsx';
import { useFetch, useScrolled, useTheme } from '../lib/hooks.js';
import { useLibrary } from '../lib/library.jsx';
import Logo from './Logo.jsx';
import SearchBox from './SearchBox.jsx';

export default function Header() {
  const scrolled = useScrolled();
  const { user, isAdmin, logout } = useAuth();
  const { cart, setOpen, bump } = useCart();
  const { wish, news } = useLibrary();
  const [theme, toggleTheme] = useTheme();
  const [menu, setMenu] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const menuRef = useRef(null);
  const [bumping, setBumping] = useState(false);
  const categories = useFetch('/api/categories');
  const admin = location.pathname.startsWith('/admin');

  useEffect(() => {
    if (!bump) return undefined;
    setBumping(true);
    const t = setTimeout(() => setBumping(false), 500);
    return () => clearTimeout(t);
  }, [bump]);

  useEffect(() => { setMenu(false); }, [location.pathname]);
  useEffect(() => {
    const onClick = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenu(false);
    const onKey = (e) => e.key === 'Escape' && setMenu(false);
    document.addEventListener('mousedown', onClick);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onClick);
      document.removeEventListener('keydown', onKey);
    };
  }, []);

  return (
    <header className={`header ${scrolled ? 'scrolled' : ''}`}>
      <div className="container header-inner">
        <Link to="/" className="brand" aria-label="Prelo, página inicial"><Logo /></Link>
        <SearchBox />
        <nav className="nav" aria-label="Sua conta">
          <NavLink to="/quero-ler" className="nav-link" title="Quero ler">
            <Bookmark size={18} /><span className="label">Quero ler</span>
            {wish.length > 0 && <span className={`count ${news ? 'news' : ''}`} aria-label={news ? `${news} novidades` : `${wish.length} livros`}>{news || wish.length}</span>}
          </NavLink>
          {user ? (
            <div className="menu" ref={menuRef}>
              <button type="button" className="nav-link" onClick={() => setMenu((m) => !m)} aria-expanded={menu} aria-haspopup="menu">
                <User size={18} /><span className="label">{user.fullName.split(' ')[0]}</span>
              </button>
              {menu && (
                <div className="menu-pop" role="menu">
                  <div className="who"><b>{user.fullName}</b><div className="subtle">{user.email}</div></div>
                  <Link to="/compras" role="menuitem"><Package size={16} /> Minhas compras</Link>
                  <Link to="/conta" role="menuitem"><User size={16} /> Minha conta</Link>
                  {isAdmin && <Link to="/admin" role="menuitem"><LayoutDashboard size={16} /> Administração</Link>}
                  <button type="button" role="menuitem" onClick={toggleTheme}>{theme === 'dark' ? <Sun size={16} /> : <Moon size={16} />} {theme === 'dark' ? 'Tema claro' : 'Tema escuro'}</button>
                  <button type="button" onClick={() => logout().then(() => navigate('/'))} role="menuitem"><LogOut size={16} /> Sair</button>
                </div>
              )}
            </div>
          ) : (
            <>
              <button type="button" className="nav-link icon-only" onClick={toggleTheme} aria-label={theme === 'dark' ? 'Usar tema claro' : 'Usar tema escuro'}>
                {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
              </button>
              <NavLink to="/entrar" className="nav-link"><User size={18} /><span className="label">Entrar</span></NavLink>
            </>
          )}
          <button type="button" className="nav-link cart-btn" onClick={() => setOpen(true)} aria-label={`Carrinho com ${cart.itemCount} itens`}>
            <ShoppingBag size={19} />
            {cart.itemCount > 0 && <span className={`count solid ${bumping ? 'bump' : ''}`}>{cart.itemCount}</span>}
          </button>
        </nav>
      </div>
      {!admin && (
        <nav className="shelves-nav" aria-label="Estantes">
          <div className="container shelves-inner">
            <NavLink to="/livros" end>Todos os livros</NavLink>
            <NavLink to="/catalogos/lancamentos">Lançamentos</NavLink>
            <NavLink to="/catalogos/mais-vendidos">Mais vendidos</NavLink>
            <span className="sep" aria-hidden="true" />
            {categories.data?.map((c) => (
              <NavLink key={c.slug} to={`/categorias/${c.slug}`}><i style={{ background: c.accent }} />{c.name}</NavLink>
            ))}
          </div>
        </nav>
      )}
    </header>
  );
}
