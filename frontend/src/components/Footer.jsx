import { Link } from 'react-router';
import Logo from './Logo.jsx';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-grid">
        <div className="footer-brand">
          <Link to="/" className="brand"><Logo /></Link>
          <p>Livraria online com entrega em todo o Brasil. Projeto de demonstração: os pagamentos rodam em modo sandbox.</p>
          <p className="subtle">Copyright (©) 2026 Clara Portes. Licença MIT.</p>
        </div>
        <div>
          <h4>Explorar</h4>
          <Link to="/livros">Todos os livros</Link>
          <Link to="/categorias">Categorias e catálogos</Link>
          <Link to="/catalogos/lancamentos">Lançamentos</Link>
          <Link to="/catalogos/mais-vendidos">Mais vendidos</Link>
        </div>
        <div>
          <h4>Sua conta</h4>
          <Link to="/quero-ler">Quero ler</Link>
          <Link to="/compras">Minhas compras</Link>
          <Link to="/carrinho">Carrinho</Link>
          <Link to="/conta">Endereços e senha</Link>
        </div>
        <div>
          <h4>Ajuda</h4>
          <p>Frete grátis acima de R$ 199 na entrega econômica.</p>
          <p>Pix, boleto ou cartão em até 6x sem juros.</p>
          <p>Aperte <kbd>?</kbd> para ver os atalhos de teclado.</p>
          <a href="/api/docs">Documentação da API</a>
        </div>
      </div>
    </footer>
  );
}
