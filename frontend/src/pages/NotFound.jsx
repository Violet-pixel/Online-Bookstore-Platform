import { Link } from 'react-router';
import { Empty } from '../components/Bits.jsx';
import { useTitle } from '../lib/hooks.js';

export default function NotFound() {
  useTitle('Página não encontrada');
  return (
    <div className="container page">
      <Empty title="Essa página saiu de catálogo" action={<Link className="btn btn-dark" to="/">Voltar para a vitrine</Link>}>
        O endereço pode ter mudado ou o livro não está mais à venda.
      </Empty>
    </div>
  );
}
