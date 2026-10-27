import { lazy, Suspense, useEffect } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router';
import CartDrawer from './components/CartDrawer.jsx';
import Footer from './components/Footer.jsx';
import Header from './components/Header.jsx';
import { useAuth } from './lib/auth.jsx';
import Home from './pages/Home.jsx';
import Catalog from './pages/Catalog.jsx';
import Categories from './pages/Categories.jsx';
import Book from './pages/Book.jsx';
import Cart from './pages/Cart.jsx';
import Checkout from './pages/Checkout.jsx';
import Orders from './pages/Orders.jsx';
import Order from './pages/Order.jsx';
import Account from './pages/Account.jsx';
import Auth from './pages/Auth.jsx';
import NotFound from './pages/NotFound.jsx';
import Wishlist from './pages/Wishlist.jsx';
import Author from './pages/Author.jsx';
import Shortcuts from './components/Shortcuts.jsx';

const Admin = lazy(() => import('./pages/admin/Admin.jsx'));

function Private({ children, admin }) {
  const { user, ready, isAdmin } = useAuth();
  const location = useLocation();
  if (!ready) return <div className="container" style={{ padding: 80 }}><div className="skeleton" style={{ height: 240 }} /></div>;
  if (!user) return <Navigate to={`/entrar?voltar=${encodeURIComponent(location.pathname + location.search)}`} replace />;
  if (admin && !isAdmin) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  const { pathname } = useLocation();
  useEffect(() => { window.scrollTo({ top: 0, behavior: 'instant' }); }, [pathname]);
  const bare = pathname === '/entrar' || pathname === '/cadastro';
  return (
    <>
      <a className="skip-link" href="#conteudo">Pular para o conteúdo</a>
      <Header />
      <main id="conteudo" key={pathname.split('/').slice(0, 2).join('/')}>
        <Suspense fallback={<div className="container" style={{ padding: 60 }}><div className="skeleton" style={{ height: 300 }} /></div>}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/livros" element={<Catalog />} />
            <Route path="/categorias" element={<Categories />} />
            <Route path="/categorias/:slug" element={<Catalog mode="category" />} />
            <Route path="/catalogos/:slug" element={<Catalog mode="catalog" />} />
            <Route path="/livro/:slug" element={<Book />} />
            <Route path="/autor/:slug" element={<Author />} />
            <Route path="/quero-ler" element={<Wishlist />} />
            <Route path="/carrinho" element={<Cart />} />
            <Route path="/checkout" element={<Private><Checkout /></Private>} />
            <Route path="/compras" element={<Private><Orders /></Private>} />
            <Route path="/compras/:code" element={<Private><Order /></Private>} />
            <Route path="/conta" element={<Private><Account /></Private>} />
            <Route path="/entrar" element={<Auth mode="login" />} />
            <Route path="/cadastro" element={<Auth mode="register" />} />
            <Route path="/admin/*" element={<Private admin><Admin /></Private>} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </main>
      {!bare && <Footer />}
      <CartDrawer />
      <Shortcuts />
    </>
  );
}
