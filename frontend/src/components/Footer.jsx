import { Link } from 'react-router';
import Logo from './Logo.jsx';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-grid">
        <div className="stack" style={{ gap: 14 }}>
          <Link to="/" className="brand" style={{ fontSize: 22 }}><Logo /></Link>
          <p style={{ maxWidth: 320 }}>Livros escolhidos com cuidado, entregues em todo o Brasil. Projeto de demonstração: pagamentos em modo sandbox.</p>
          <p className="subtle">Copyright (©) 2026 Clara Portes · Licença MIT</p>
        </div>
        <div>
          <h4>Explorar</h4>
          <Link to="/livros">Todos os livros</Link>
          <Link to="/categorias">Categorias</Link>
          <Link to="/catalogos/lancamentos">Lançamentos</Link>
          <Link to="/catalogos/mais-vendidos">Mais vendidos</Link>
        </div>
        <div>
          <h4>Sua conta</h4>
          <Link to="/compras">Minhas compras</Link>
          <Link to="/carrinho">Carrinho</Link>
          <Link to="/conta">Endereços</Link>
        </div>
        <div>
          <h4>Ajuda</h4>
          <span style={{ display: 'block', padding: '3px 0' }}>Frete grátis acima de R$ 199</span>
          <span style={{ display: 'block', padding: '3px 0' }}>Pix, boleto e cartão em até 6x</span>
          <a href="/api/docs">API (Swagger)</a>
        </div>
      </div>
    </footer>
  );
}
