import { BookOpen, FolderTree, LayoutDashboard, Package } from 'lucide-react';
import { NavLink, Route, Routes } from 'react-router';
import { useTitle } from '../../lib/hooks.js';
import Dashboard from './Dashboard.jsx';
import AdminOrders from './AdminOrders.jsx';
import AdminOrder from './AdminOrder.jsx';
import AdminBooks from './AdminBooks.jsx';
import AdminBookForm from './AdminBookForm.jsx';
import AdminTaxonomy from './AdminTaxonomy.jsx';

export default function Admin() {
  useTitle('Administração');
  return (
    <div className="container page">
      <div className="admin">
        <nav className="side-nav">
          <span className="eyebrow" style={{ padding: '0 12px 10px' }}>Administração</span>
          <NavLink to="/admin" end><LayoutDashboard size={16} /> Painel</NavLink>
          <NavLink to="/admin/pedidos"><Package size={16} /> Pedidos e envios</NavLink>
          <NavLink to="/admin/livros"><BookOpen size={16} /> Livros e estoque</NavLink>
          <NavLink to="/admin/catalogo"><FolderTree size={16} /> Categorias e catálogos</NavLink>
        </nav>
        <div style={{ minWidth: 0 }}>
          <Routes>
            <Route index element={<Dashboard />} />
            <Route path="pedidos" element={<AdminOrders />} />
            <Route path="pedidos/:code" element={<AdminOrder />} />
            <Route path="livros" element={<AdminBooks />} />
            <Route path="livros/:id" element={<AdminBookForm />} />
            <Route path="catalogo" element={<AdminTaxonomy />} />
          </Routes>
        </div>
      </div>
    </div>
  );
}
