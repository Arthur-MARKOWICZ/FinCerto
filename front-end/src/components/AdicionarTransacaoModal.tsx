import React, { useState, useEffect } from 'react';
import { Transacao, TransacaoTipos, TransacaoCadastroDto } from '../types/transacao';
import { Tipo, CategoriaCadastroDto } from '../types/categoria';
import { transacaoApiService } from '../services/transacaoApi';
import categoriaApiService from '../services/categoriaApi';

interface AdicionarTransacaoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onTransacaoAdicionada: () => void;
  contaNome: string;
}

const AdicionarTransacaoModal: React.FC<AdicionarTransacaoModalProps> = ({ 
  isOpen, 
  onClose, 
  onTransacaoAdicionada,
  contaNome 
}) => {
  const [formData, setFormData] = useState<TransacaoCadastroDto>({
    valor: 0,
    data: new Date().toISOString().slice(0, 16), // Formato YYYY-MM-DDTHH:mm para LocalDateTime
    descricao: '',
    tipo: TransacaoTipos.DESPESA,
    nomeConta: contaNome,
    nomeCategoria: '',
    token: localStorage.getItem('token') || '',
  });
  const [categorias, setCategorias] = useState<string[]>([]);
  const [novaCategoria, setNovaCategoria] = useState<string>('');
  const [mostrarNovaCategoria, setMostrarNovaCategoria] = useState<boolean>(false);
  const [erro, setErro] = useState<string>('');
  const [carregando, setCarregando] = useState<boolean>(false);
  const [carregandoCategorias, setCarregandoCategorias] = useState<boolean>(false);

  useEffect(() => {
    if (isOpen && formData.tipo) {
      carregarCategorias();
    }
  }, [isOpen, formData.tipo]);

  const carregarCategorias = async () => {
    try {
      setCarregandoCategorias(true);
      // Buscando categorias reais do back-end
      const categoriasReais = await categoriaApiService.listarTodas();
      
      // Filtrando categorias por tipo
      const categoriasFiltradas = categoriasReais
        .filter(cat => cat.tipo === (formData.tipo === TransacaoTipos.DESPESA ? 'DESPESA' : 'RECEITA'))
        .map(cat => cat.nome);
      
      setCategorias(categoriasFiltradas);
    } catch (error) {
      console.error('Erro ao carregar categorias:', error);
      // Fallback para categorias padrão em caso de erro
      const categoriasPadrao = formData.tipo === TransacaoTipos.DESPESA 
        ? ['Alimentação', 'Transporte', 'Moradia', 'Saúde', 'Educação', 'Lazer', 'Outros']
        : ['Salário', 'Freelance', 'Investimentos', 'Vendas', 'Outros'];
      setCategorias(categoriasPadrao);
    } finally {
      setCarregandoCategorias(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev: TransacaoCadastroDto) => ({
      ...prev,
      [name]: name === 'valor' ? parseFloat(value) || 0 : value,
      tipo: name === 'tipo' ? value as TransacaoTipos : prev.tipo,
    }));
    if (name === 'tipo') {
      setMostrarNovaCategoria(false);
      setNovaCategoria('');
      carregarCategorias();
    }
  };

  const handleCategoriaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    if (value === 'nova') {
      setMostrarNovaCategoria(true);
      setFormData((prev: TransacaoCadastroDto) => ({ ...prev, nomeCategoria: '' }));
    } else {
      setMostrarNovaCategoria(false);
      setFormData((prev: TransacaoCadastroDto) => ({ ...prev, nomeCategoria: value }));
      setNovaCategoria('');
    }
  };

  const handleCriarCategoria = async () => {
    if (!novaCategoria.trim()) return;
    
    try {
      setCarregando(true);
      const categoriaDto = {
        nome: novaCategoria.trim(),
        tipo: formData.tipo === TransacaoTipos.DESPESA ? Tipo.DESPESA : Tipo.RECEITA,
        token: localStorage.getItem('token') || ''
      };
      await categoriaApiService.cadastrar(categoriaDto);
      // Recarrega a lista de categorias após criar uma nova
      await carregarCategorias();
      setFormData((prev: TransacaoCadastroDto) => ({ ...prev, nomeCategoria: novaCategoria.trim() }));
      setNovaCategoria('');
      setMostrarNovaCategoria(false);
    } catch (error: any) {
      console.error('Erro ao criar categoria:', error);
      if (error.response?.data?.message) {
        setErro(error.response.data.message);
      } else {
        setErro('Erro ao criar categoria. Tente novamente.');
      }
    } finally {
      setCarregando(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.nomeCategoria.trim()) {
      setErro('Selecione ou crie uma categoria');
      return;
    }

    setErro('');
    setCarregando(true);

    try {
      await transacaoApiService.cadastrar(formData);
      onTransacaoAdicionada();
      onClose();
      setFormData({
        valor: 0,
        data: new Date().toISOString().split('T')[0],
        descricao: '',
        tipo: TransacaoTipos.DESPESA,
        nomeConta: contaNome,
        nomeCategoria: '',
        token: localStorage.getItem('token') || '',
      });
    } catch (error: any) {
      if (error.response?.data?.message) {
        setErro(error.response.data.message);
      } else {
        setErro('Erro ao adicionar transação. Tente novamente mais tarde.');
      }
    } finally {
      setCarregando(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">Adicionar Transação</h2>
        
        {erro && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {erro}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label htmlFor="tipo" className="block text-sm font-medium text-gray-700 mb-1">
                Tipo de Transação
              </label>
              <select
                id="tipo"
                name="tipo"
                value={formData.tipo}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={TransacaoTipos.DESPESA}>Despesa</option>
                <option value={TransacaoTipos.RECEITA}>Receita</option>
              </select>
            </div>

            <div>
              <label htmlFor="valor" className="block text-sm font-medium text-gray-700 mb-1">
                Valor (R$)
              </label>
              <input
                id="valor"
                name="valor"
                type="number"
                step="0.01"
                required
                value={formData.valor}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.00"
              />
            </div>

            <div>
              <label htmlFor="data" className="block text-sm font-medium text-gray-700 mb-1">
                Data
              </label>
              <input
                type="datetime-local"
                id="data"
                name="data"
                value={formData.data}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label htmlFor="descricao" className="block text-sm font-medium text-gray-700 mb-1">
                Descrição
              </label>
              <textarea
                id="descricao"
                name="descricao"
                required
                value={formData.descricao}
                onChange={handleChange}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Descrição da transação"
              />
            </div>

            <div>
              <label htmlFor="categoria" className="block text-sm font-medium text-gray-700 mb-1">
                Categoria
              </label>
              {carregandoCategorias ? (
                <div className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50">
                  <span className="text-gray-500">Carregando categorias...</span>
                </div>
              ) : (
                <select
                  id="categoria"
                  value={mostrarNovaCategoria ? 'nova' : formData.nomeCategoria}
                  onChange={handleCategoriaChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Selecione uma categoria</option>
                  {categorias.map((cat) => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                  <option value="nova">+ Criar nova categoria</option>
                </select>
              )}
            </div>

            {mostrarNovaCategoria && (
              <div className="space-y-2">
                <input
                  type="text"
                  value={novaCategoria}
                  onChange={(e) => setNovaCategoria(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Nome da nova categoria"
                  disabled={carregando}
                />
                <button
                  type="button"
                  onClick={handleCriarCategoria}
                  disabled={carregando || !novaCategoria.trim()}
                  className="w-full bg-green-600 hover:bg-green-700 text-white px-3 py-2 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {carregando ? 'Criando...' : 'Criar Categoria'}
                </button>
              </div>
            )}
          </div>

          <div className="flex justify-end space-x-3 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={carregando || !formData.nomeCategoria.trim()}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {carregando ? 'Adicionando...' : 'Adicionar Transação'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AdicionarTransacaoModal;
