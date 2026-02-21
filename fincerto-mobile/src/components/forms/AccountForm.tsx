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
import { AccountType, ContaCadastroDto } from '../../types';
import { contaService, storageService } from '../../services';

interface AccountFormProps {
  visible: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const AccountForm: React.FC<AccountFormProps> = ({ visible, onClose, onSuccess }) => {
  const [nome, setNome] = useState('');
  const [tipo, setTipo] = useState<AccountType>(AccountType.CORRENTE);
  const [saldoInicial, setSaldoInicial] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSave = async () => {
    if (!nome || !saldoInicial) {
      Alert.alert('Erro', 'Preencha todos os campos');
      return;
    }

    const saldo = parseFloat(saldoInicial);
    if (isNaN(saldo) || saldo < 0) {
      Alert.alert('Erro', 'Saldo inicial deve ser um número válido');
      return;
    }

    try {
      setIsLoading(true);
      const token = await storageService.getToken();
      console.log('📝 Token retrieved from storage:', token ? 'exists' : 'null');
      
      if (!token) {
        Alert.alert('Erro', 'Usuário não autenticado');
        return;
      }

      const dados: ContaCadastroDto = {
        nome,
        tipos: tipo,
        saldoInicial: saldo,
        token,
      };

      console.log('📤 Sending account data:', { nome, tipos: tipo, saldoInicial: saldo, token: token.substring(0, 20) + '...' });
      await contaService.criar(dados);
      Alert.alert('Sucesso', 'Conta criada com sucesso!');
      onSuccess();
      handleClose();
    } catch (error: any) {
      console.error('Error creating account:', error);
      const errorMessage = error.response?.data?.messagem || 'Erro ao criar conta';
      Alert.alert('Erro', errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setNome('');
    setTipo(AccountType.CORRENTE);
    setSaldoInicial('');
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
          <Text style={styles.title}>Nova Conta</Text>
          <TouchableOpacity onPress={handleSave} disabled={isLoading}>
            <Text style={[styles.saveButton, isLoading && styles.saveButtonDisabled]}>
              {isLoading ? 'Salvando...' : 'Salvar'}
            </Text>
          </TouchableOpacity>
        </View>

        <ScrollView contentContainerStyle={styles.content}>
          <View style={styles.form}>
            <Text style={styles.label}>Nome da Conta</Text>
            <TextInput
              style={styles.input}
              placeholder="Ex: Conta Corrente"
              value={nome}
              onChangeText={setNome}
              autoCapitalize="words"
            />

            <Text style={styles.label}>Tipo de Conta</Text>
            <View style={styles.typeContainer}>
              {Object.values(AccountType).map((type) => (
                <TouchableOpacity
                  key={type}
                  style={[
                    styles.typeButton,
                    tipo === type && styles.typeButtonSelected,
                  ]}
                  onPress={() => setTipo(type)}
                >
                  <Text style={[
                    styles.typeButtonText,
                    tipo === type && styles.typeButtonTextSelected,
                  ]}>
                    {type}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={styles.label}>Saldo Inicial</Text>
            <TextInput
              style={styles.input}
              placeholder="0,00"
              value={saldoInicial}
              onChangeText={setSaldoInicial}
              keyboardType="numeric"
            />
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
    paddingHorizontal: 8,
    backgroundColor: 'white',
    borderWidth: 1,
    borderColor: '#ddd',
    marginRight: 8,
    borderRadius: 8,
    alignItems: 'center',
  },
  typeButtonSelected: {
    backgroundColor: '#4CAF50',
    borderColor: '#4CAF50',
  },
  typeButtonText: {
    fontSize: 12,
    color: '#666',
  },
  typeButtonTextSelected: {
    color: 'white',
    fontWeight: 'bold',
  },
});

export default AccountForm;
