import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { AuthStackParamList } from '../../navigation/AuthNavigator';
import { usuarioService } from '../../services';
import { UsuarioCadastroDto } from '../../types';

type CadastroScreenNavigationProp = NativeStackNavigationProp<AuthStackParamList, 'Cadastro'>;

interface Props {
  navigation: CadastroScreenNavigationProp;
}

const CadastroScreen: React.FC<Props> = ({ navigation }) => {
  console.log('📱 CadastroScreen renderizado');
  
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [mensagem, setMensagem] = useState('');
  const [tipoMensagem, setTipoMensagem] = useState<'sucesso' | 'erro' | ''>('');

  const handleCadastro = async () => {
    console.log('🔵 Botão de cadastro pressionado');
    console.log('📋 Dados do formulário:', { nome, email, senhaLength: senha.length });
    
    // Limpa mensagens anteriores
    setMensagem('');
    setTipoMensagem('');
    
    if (!nome || !email || !senha || !confirmarSenha) {
      console.log('❌ Validação falhou: campos vazios');
      setMensagem('Preencha todos os campos');
      setTipoMensagem('erro');
      return;
    }

    if (senha !== confirmarSenha) {
      console.log('❌ Validação falhou: senhas não coincidem');
      setMensagem('As senhas não coincidem');
      setTipoMensagem('erro');
      return;
    }

    if (senha.length < 6) {
      console.log('❌ Validação falhou: senha muito curta');
      setMensagem('A senha deve ter pelo menos 6 caracteres');
      setTipoMensagem('erro');
      return;
    }

    try {
      console.log('🚀 Iniciando requisição de cadastro...');
      setIsLoading(true);
      const dados: UsuarioCadastroDto = { nome, email, senha };
      console.log('📤 Enviando dados:', dados);
      
      await usuarioService.cadastrar(dados);
      
      console.log('✅ Cadastro realizado com sucesso!');
      setMensagem('Cadastro realizado com sucesso! Redirecionando...');
      setTipoMensagem('sucesso');
      
      // Tenta mostrar o Alert também
      Alert.alert(
        'Sucesso!', 
        'Cadastro realizado com sucesso! Você será redirecionado para a tela de login.', 
        [
          { 
            text: 'OK', 
            onPress: () => {
              console.log('🔄 Navegando para tela de Login');
              navigation.navigate('Login');
            }
          }
        ]
      );
      
      // Redireciona automaticamente após 2 segundos
      setTimeout(() => {
        navigation.navigate('Login');
      }, 2000);
      
    } catch (error: any) {
      console.error('❌ Erro no cadastro:', error);
      
      let mensagemErro = 'Falha no cadastro. Tente novamente.';
      
      if (error.response) {
        console.log('📋 Status do erro:', error.response.status);
        console.log('📋 Dados do erro:', error.response.data);
        
        if (error.response.status === 409) {
          mensagemErro = 'Este e-mail já está cadastrado. Tente fazer login.';
        } else if (error.response.status === 400) {
          mensagemErro = error.response.data?.message || 'Dados inválidos. Verifique as informações.';
        } else if (error.response.status === 500) {
          mensagemErro = 'Erro no servidor. Tente novamente mais tarde.';
        }
      } else if (error.request) {
        mensagemErro = 'Não foi possível conectar ao servidor. Verifique sua conexão.';
      }
      
      setMensagem(mensagemErro);
      setTipoMensagem('erro');
      
      // Tenta mostrar o Alert também
      Alert.alert('Erro no Cadastro', mensagemErro);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView 
      style={styles.container} 
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <View style={styles.content}>
          <Text style={styles.title}>Criar Conta</Text>
          <Text style={styles.subtitle}>Junte-se ao FinCerto</Text>

          {/* Mensagem de feedback */}
          {mensagem ? (
            <View style={[
              styles.messageContainer, 
              tipoMensagem === 'sucesso' ? styles.successMessage : styles.errorMessage
            ]}>
              <Text style={[
                styles.messageText,
                tipoMensagem === 'sucesso' ? styles.successText : styles.errorText
              ]}>
                {mensagem}
              </Text>
            </View>
          ) : null}

          <View style={styles.form}>
            <TextInput
              style={styles.input}
              placeholder="Nome completo"
              value={nome}
              onChangeText={setNome}
              autoCapitalize="words"
            />

            <TextInput
              style={styles.input}
              placeholder="E-mail"
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
            />

            <TextInput
              style={styles.input}
              placeholder="Senha"
              value={senha}
              onChangeText={setSenha}
              secureTextEntry
            />

            <TextInput
              style={styles.input}
              placeholder="Confirmar senha"
              value={confirmarSenha}
              onChangeText={setConfirmarSenha}
              secureTextEntry
            />

            <TouchableOpacity
              style={[styles.button, isLoading && styles.buttonDisabled]}
              onPress={() => {
                console.log('🔘 Botão de cadastro clicado!');
                handleCadastro();
              }}
              disabled={isLoading}
            >
              <Text style={styles.buttonText}>
                {isLoading ? 'Cadastrando...' : 'Cadastrar'}
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.linkButton}
              onPress={() => navigation.navigate('Login')}
            >
              <Text style={styles.linkText}>
                Já tem uma conta? Faça login
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollContainer: {
    flexGrow: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#4CAF50',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 48,
  },
  form: {
    width: '100%',
  },
  input: {
    backgroundColor: 'white',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 16,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  button: {
    backgroundColor: '#4CAF50',
    paddingVertical: 14,
    borderRadius: 8,
    alignItems: 'center',
    marginBottom: 16,
  },
  buttonDisabled: {
    backgroundColor: '#a5d6a7',
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  linkButton: {
    alignItems: 'center',
  },
  linkText: {
    color: '#4CAF50',
    fontSize: 14,
  },
  messageContainer: {
    padding: 12,
    borderRadius: 8,
    marginBottom: 16,
  },
  successMessage: {
    backgroundColor: '#E8F5E8',
    borderWidth: 1,
    borderColor: '#4CAF50',
  },
  errorMessage: {
    backgroundColor: '#FFEBEE',
    borderWidth: 1,
    borderColor: '#F44336',
  },
  messageText: {
    fontSize: 14,
    textAlign: 'center',
  },
  successText: {
    color: '#2E7D32',
  },
  errorText: {
    color: '#C62828',
  },
});

export default CadastroScreen;
