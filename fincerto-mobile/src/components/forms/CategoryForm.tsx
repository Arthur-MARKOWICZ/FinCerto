import React, { useState } from 'react';
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
import { CategoryType, CategoriaCadastroDto } from '../../types';
import { categoriaService, storageService } from '../../services';

interface CategoryFormProps {
  visible: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const CategoryForm: React.FC<CategoryFormProps> = ({ visible, onClose, onSuccess }) => {
  const [nome, setNome] = useState('');
  const [tipo, setTipo] = useState<CategoryType>(CategoryType.DESPESA);
  const [isLoading, setIsLoading] = useState(false);

  const handleSave = async () => {
    if (!nome) {
      Alert.alert('Erro', 'Preencha o nome da categoria');
      return;
    }

    try {
      setIsLoading(true);
      const token = await storageService.getToken();
      if (!token) {
        Alert.alert('Erro', 'Usuário não autenticado');
        return;
      }

      const dados: CategoriaCadastroDto = {
        nome,
        tipo,
        token,
      };

      await categoriaService.criar(dados);
      Alert.alert('Sucesso', 'Categoria criada com sucesso!');
      onSuccess();
      handleClose();
    } catch (error: any) {
      console.error('Error creating category:', error);
      const errorMessage = error.response?.data?.messagem || 'Erro ao criar categoria';
      Alert.alert('Erro', errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setNome('');
    setTipo(CategoryType.DESPESA);
    onClose();
  };

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
          <Text style={styles.title}>Nova Categoria</Text>
          <TouchableOpacity onPress={handleSave} disabled={isLoading}>
            <Text style={[styles.saveButton, isLoading && styles.saveButtonDisabled]}>
              {isLoading ? 'Salvando...' : 'Salvar'}
            </Text>
          </TouchableOpacity>
        </View>

        <ScrollView contentContainerStyle={styles.content}>
          <View style={styles.form}>
            <Text style={styles.label}>Nome da Categoria</Text>
            <TextInput
              style={styles.input}
              placeholder="Ex: Alimentação"
              value={nome}
              onChangeText={setNome}
              autoCapitalize="words"
            />

            <Text style={styles.label}>Tipo de Categoria</Text>
            <View style={styles.typeContainer}>
              <TouchableOpacity
                style={[
                  styles.typeButton,
                  tipo === CategoryType.DESPESA && styles.typeButtonDespesa,
                ]}
                onPress={() => setTipo(CategoryType.DESPESA)}
              >
                <Text style={[
                  styles.typeButtonText,
                  tipo === CategoryType.DESPESA && styles.typeButtonTextSelected,
                ]}>
                  Despesa
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[
                  styles.typeButton,
                  tipo === CategoryType.RECEITA && styles.typeButtonReceita,
                ]}
                onPress={() => setTipo(CategoryType.RECEITA)}
              >
                <Text style={[
                  styles.typeButtonText,
                  tipo === CategoryType.RECEITA && styles.typeButtonTextSelected,
                ]}>
                  Receita
                </Text>
              </TouchableOpacity>
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
});

export default CategoryForm;
