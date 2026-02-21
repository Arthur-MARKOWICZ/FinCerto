import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  Modal,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { TransactionType, TransacaoCadastroDto, Account, Category } from '../../types';
import { transacaoService, contaService, categoriaService, storageService } from '../../services';

interface TransactionFormProps {
  visible: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const TransactionForm: React.FC<TransactionFormProps> = ({ visible, onClose, onSuccess }) => {
  const [valor, setValor] = useState('');
  const [descricao, setDescricao] = useState('');
  const [tipo, setTipo] = useState<TransactionType>(TransactionType.DESPESA);
  const [contas, setContas] = useState<Account[]>([]);
  const [categorias, setCategorias] = useState<Category[]>([]);
  const [contaSelecionada, setContaSelecionada] = useState('');
  const [categoriaSelecionada, setCategoriaSelecionada] = useState('');
  const [data, setData] = useState(new Date().toISOString().split('T')[0]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    if (visible) {
      loadData();
    }
  }, [visible]);

  const loadData = async () => {
    try {
      const [contasData, categoriasData] = await Promise.all([
        contaService.listar(),
        categoriaService.listar(),
      ]);
      setContas(contasData);
      setCategorias(categoriasData);
      
      if (contasData.length > 0) {
        setContaSelecionada(contasData[0].nome);
      }
      
      const categoriasFiltradas = categoriasData.filter(cat => cat.tipo.toString() === tipo.toString());
      if (categoriasFiltradas.length > 0) {
        setCategoriaSelecionada(categoriasFiltradas[0].nome);
      }
    } catch (error) {
      console.error('Error loading data:', error);
      Alert.alert('Erro', 'Não foi possível carregar os dados');
    } finally {
      setLoadingData(false);
    }
  };

  useEffect(() => {
    const categoriasFiltradas = categorias.filter(cat => cat.tipo.toString() === tipo.toString());
    if (categoriasFiltradas.length > 0 && !categoriasFiltradas.find(cat => cat.nome === categoriaSelecionada)) {
      setCategoriaSelecionada(categoriasFiltradas[0].nome);
    }
  }, [tipo]);

  const handleSave = async () => {
    if (!valor || !descricao || !contaSelecionada || !categoriaSelecionada) {
      Alert.alert('Erro', 'Preencha todos os campos');
      return;
    }

    const valorNum = parseFloat(valor.replace(',', '.'));
    if (isNaN(valorNum) || valorNum <= 0) {
      Alert.alert('Erro', 'Valor deve ser um número positivo');
      return;
    }

    try {
      setIsLoading(true);
      const token = await storageService.getToken();
      if (!token) {
        Alert.alert('Erro', 'Usuário não autenticado');
        return;
      }

      const dados: TransacaoCadastroDto = {
        valor: valorNum,
        data,
        descricao,
        tipo,
        nomeConta: contaSelecionada,
        nomeCategoria: categoriaSelecionada,
        token,
      };

      await transacaoService.criar(dados);
      Alert.alert('Sucesso', 'Transação criada com sucesso!');
      onSuccess();
      handleClose();
    } catch (error: any) {
      console.error('Error creating transaction:', error);
      const errorMessage = error.response?.data?.messagem || 'Erro ao criar transação';
      Alert.alert('Erro', errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setValor('');
    setDescricao('');
    setTipo(TransactionType.DESPESA);
    setContaSelecionada('');
    setCategoriaSelecionada('');
    setData(new Date().toISOString().split('T')[0]);
    setLoadingData(true);
    onClose();
  };

  if (loadingData) {
    return (
      <Modal visible={visible} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.loadingContainer}>
          <Text style={styles.loadingText}>Carregando...</Text>
        </View>
      </Modal>
    );
  }

  return (
    <Modal
      visible={visible}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={handleClose}
    >
      <KeyboardAvoidingView 
        style={styles.container}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <View style={styles.header}>
          <TouchableOpacity onPress={handleClose}>
            <Text style={styles.cancelButton}>Cancelar</Text>
          </TouchableOpacity>
          <Text style={styles.title}>Nova Transação</Text>
          <TouchableOpacity onPress={handleSave} disabled={isLoading}>
            <Text style={[styles.saveButton, isLoading && styles.saveButtonDisabled]}>
              {isLoading ? 'Salvando...' : 'Salvar'}
            </Text>
          </TouchableOpacity>
        </View>

        <ScrollView contentContainerStyle={styles.content}>
          <View style={styles.form}>
            <Text style={styles.label}>Tipo</Text>
            <View style={styles.typeContainer}>
              <TouchableOpacity
                style={[
                  styles.typeButton,
                  tipo === TransactionType.DESPESA && styles.typeButtonDespesa,
                ]}
                onPress={() => setTipo(TransactionType.DESPESA)}
              >
                <Text style={[
                  styles.typeButtonText,
                  tipo === TransactionType.DESPESA && styles.typeButtonTextSelected,
                ]}>
                  Despesa
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[
                  styles.typeButton,
                  tipo === TransactionType.RECEITA && styles.typeButtonReceita,
                ]}
                onPress={() => setTipo(TransactionType.RECEITA)}
              >
                <Text style={[
                  styles.typeButtonText,
                  tipo === TransactionType.RECEITA && styles.typeButtonTextSelected,
                ]}>
                  Receita
                </Text>
              </TouchableOpacity>
            </View>

            <Text style={styles.label}>Valor (R$)</Text>
            <TextInput
              style={styles.input}
              placeholder="0,00"
              value={valor}
              onChangeText={setValor}
              keyboardType="numeric"
            />

            <Text style={styles.label}>Descrição</Text>
            <TextInput
              style={styles.input}
              placeholder="Ex: Compra no supermercado"
              value={descricao}
              onChangeText={setDescricao}
              autoCapitalize="words"
            />

            <Text style={styles.label}>Data</Text>
            <TextInput
              style={styles.input}
              value={data}
              onChangeText={setData}
              placeholder="YYYY-MM-DD"
            />

            <Text style={styles.label}>Conta</Text>
            <View style={styles.pickerContainer}>
              {contas.map((conta) => (
                <TouchableOpacity
                  key={conta.id}
                  style={[
                    styles.optionButton,
                    contaSelecionada === conta.nome && styles.optionButtonSelected,
                  ]}
                  onPress={() => setContaSelecionada(conta.nome)}
                >
                  <Text style={[
                    styles.optionButtonText,
                    contaSelecionada === conta.nome && styles.optionButtonTextSelected,
                  ]}>
                    {conta.nome}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={styles.label}>Categoria</Text>
            <View style={styles.pickerContainer}>
              {categorias
                .filter(cat => cat.tipo.toString() === tipo.toString())
                .map((categoria) => (
                  <TouchableOpacity
                    key={categoria.id}
                    style={[
                      styles.optionButton,
                      categoriaSelecionada === categoria.nome && styles.optionButtonSelected,
                    ]}
                    onPress={() => setCategoriaSelecionada(categoria.nome)}
                  >
                    <Text style={[
                      styles.optionButtonText,
                      categoriaSelecionada === categoria.nome && styles.optionButtonTextSelected,
                    ]}>
                      {categoria.nome}
                    </Text>
                  </TouchableOpacity>
                ))}
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </Modal>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 16,
    color: '#666',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: 'white',
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  cancelButton: {
    fontSize: 16,
    color: '#666',
  },
  saveButton: {
    fontSize: 16,
    color: '#4CAF50',
    fontWeight: 'bold',
  },
  saveButtonDisabled: {
    color: '#ccc',
  },
  content: {
    padding: 20,
  },
  form: {
    width: '100%',
  },
  label: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  input: {
    backgroundColor: 'white',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 20,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  typeContainer: {
    flexDirection: 'row',
    marginBottom: 20,
  },
  typeButton: {
    flex: 1,
    paddingVertical: 12,
    backgroundColor: 'white',
    borderWidth: 1,
    borderColor: '#ddd',
    marginRight: 8,
    borderRadius: 8,
    alignItems: 'center',
  },
  typeButtonDespesa: {
    backgroundColor: '#f44336',
    borderColor: '#f44336',
  },
  typeButtonReceita: {
    backgroundColor: '#4CAF50',
    borderColor: '#4CAF50',
  },
  typeButtonText: {
    fontSize: 14,
    color: '#666',
  },
  typeButtonTextSelected: {
    color: 'white',
    fontWeight: 'bold',
  },
  pickerContainer: {
    marginBottom: 20,
  },
  optionButton: {
    backgroundColor: 'white',
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    marginBottom: 8,
  },
  optionButtonSelected: {
    backgroundColor: '#4CAF50',
    borderColor: '#4CAF50',
  },
  optionButtonText: {
    fontSize: 16,
    color: '#333',
  },
  optionButtonTextSelected: {
    color: 'white',
    fontWeight: 'bold',
  },
});

export default TransactionForm;
