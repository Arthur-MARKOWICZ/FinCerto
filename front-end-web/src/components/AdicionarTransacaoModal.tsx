import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Transacao, TransacaoTipos, TransacaoCadastroDto } from '../types/transacao';
import { Tipo, CategoriaCadastroDto } from '../types/categoria';
import { transacaoApiService } from '../services/transacaoApi';
import categoriaApiService from '../services/categoriaApi';
import { useAuth } from '../hooks/useAuth';

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
  const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = useForm<TransacaoCadastroDto>({
    defaultValues: {
      valor: 0,
      data: new Date().toISOString().slice(0, 16), 
      descricao: '',
      tipo: TransacaoTipos.DESPESA,
      nomeConta: contaNome,
      nomeCategoria: ''
    }
  });
  const { token } = useAuth();
  
  const [categorias, setCategorias] = useState<string[]>([]);
  const [novaCategoria, setNovaCategoria] = useState<string>('');
  const [mostrarNovaCategoria, setMostrarNovaCategoria] = useState<boolean>(false);
  const [erro, setErro] = useState<string>('');
  const [carregando, setCarregando] = useState<boolean>(false);
  const [carregandoCategorias, setCarregandoCategorias] = useState<boolean>(false);

  const tipoAtual = watch('tipo');
  const nomeCategoriaAtual = watch('nomeCategoria');

  useEffect(() => {
    if (isOpen && tipoAtual) {
      carregarCategorias();
    }
  }, [isOpen, tipoAtual]);

  const carregarCategorias = async () => {
    try {
      setCarregandoCategorias(true);
      let todasCategorias: any[] = [];
      let pagina = 0;
      let temMaisPaginas = true;
      
      while (temMaisPaginas) {
        const response = await categoriaApiService.obterPorUsuario(pagina, 50);
        const categoriasDaPagina = response.content || [];
        todasCategorias = [...todasCategorias, ...categoriasDaPagina];
        temMaisPaginas = response.totalPages > pagina + 1 && categoriasDaPagina.length > 0;
        pagina++;
        if (pagina >= 10) break;
      }
      
      const categoriasFiltradas = todasCategorias
        .filter((cat: any) => {
          const tipoEsperado = tipoAtual === TransacaoTipos.DESPESA ? 'DESPESA' : 'RECEITA';
          return cat.tipo === tipoEsperado;
        })
        .map((cat: any) => cat.nome);
      
      setCategorias(categoriasFiltradas);
    } catch (error) {
      console.error('Erro ao carregar categorias:', error);
    } finally {
      setCarregandoCategorias(false);
    }
  };

  const handleCategoriaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    if (value === 'nova') {
      setMostrarNovaCategoria(true);
      setValue('nomeCategoria', '');
    } else {
      setMostrarNovaCategoria(false);
      setValue('nomeCategoria', value);
      setNovaCategoria('');
    }
  };

  const handleCriarCategoria = async () => {
    if (!novaCategoria.trim()) return;
    
    try {
      setCarregando(true);
      const categoriaDto = {
        nome: novaCategoria.trim(),
        tipo: tipoAtual === TransacaoTipos.DESPESA ? Tipo.DESPESA : Tipo.RECEITA,
        token: token || ''
      };
      await categoriaApiService.cadastrar(categoriaDto);
      await carregarCategorias();
      setValue('nomeCategoria', novaCategoria.trim());
      setNovaCategoria('');
      setMostrarNovaCategoria(false);
    } catch (error: any) {
      if (error.response?.data?.message) {
        setErro(error.response.data.message);
      } else {
        setErro('Erro ao criar categoria. Tente novamente.');
      }
    } finally {
      setCarregando(false);
    }
  };

  const onSubmit = async (data: TransacaoCadastroDto) => {
    if (!data.nomeCategoria?.trim()) {
      setErro('Selecione ou crie uma categoria');
      return;
    }

    setErro('');
    setCarregando(true);

    try {
      await transacaoApiService.cadastrar({ ...data, valor: Number(data.valor), token: token || '', nomeConta: contaNome });
      onTransacaoAdicionada();
      reset();
      onClose();
      setValue('data', new Date().toISOString().slice(0, 16));
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

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            <div>
              <label htmlFor="tipo" className="block text-sm font-medium text-gray-700 mb-1">
                Tipo de Transação
              </label>
              <select
                id="tipo"
                {...register('tipo', { required: 'Tipo é obrigatório' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                onChange={(e) => {
                  setValue('tipo', e.target.value as TransacaoTipos);
                  setMostrarNovaCategoria(false);
                  setNovaCategoria('');
                }}
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
                type="number"
                step="0.01"
                {...register('valor', { required: 'Valor é obrigatório', min: { value: 0.01, message: 'Deve ser maior que zero' } })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.00"
              />
              {errors.valor && <span className="text-red-500 text-xs">{errors.valor.message as string}</span>}
            </div>

            <div>
              <label htmlFor="data" className="block text-sm font-medium text-gray-700 mb-1">
                Data
              </label>
              <input
                type="datetime-local"
                id="data"
                {...register('data', { required: 'Data é obrigatória' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              {errors.data && <span className="text-red-500 text-xs">{errors.data.message as string}</span>}
            </div>

            <div>
              <label htmlFor="descricao" className="block text-sm font-medium text-gray-700 mb-1">
                Descrição
              </label>
              <textarea
                id="descricao"
                rows={3}
                {...register('descricao', { required: 'Descrição é obrigatória' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Descrição da transação"
              />
              {errors.descricao && <span className="text-red-500 text-xs">{errors.descricao.message as string}</span>}
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
                  value={mostrarNovaCategoria ? 'nova' : nomeCategoriaAtual}
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
              disabled={carregando || !nomeCategoriaAtual?.trim()}
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
