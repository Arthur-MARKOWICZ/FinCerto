import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  RefreshControl,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { Account, AccountWithBalance } from '../../types';
import { contaService } from '../../services';
import { useAuth } from '../../context/AuthContext';
import { Card } from '../../components/common/Card';

const DashboardScreen: React.FC = () => {
  const [accounts, setAccounts] = useState<AccountWithBalance[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const { logout } = useAuth();

  const loadAccountsWithBalance = async () => {
    try {
      const accountsData = await contaService.listar();
      const accountsWithBalance = await Promise.all(
        accountsData.map(async (account) => {
          if (account.id) {
            try {
              const balance = await contaService.obterSaldo(account.id);
              return { ...account, saldoAtual: balance };
            } catch (error) {
              console.error(`Erro ao carregar saldo da conta ${account.id}:`, error);
              return { ...account, saldoAtual: account.saldo || 0 };
            }
          }
          return { ...account, saldoAtual: account.saldo || 0 };
        })
      );
      setAccounts(accountsWithBalance);
    } catch (error) {
      Alert.alert('Erro', 'Não foi possível carregar as contas');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    React.useCallback(() => {
      loadAccountsWithBalance();
    }, [])
  );

  const onRefresh = () => {
    setRefreshing(true);
    loadAccountsWithBalance();
  };

  const handleLogout = async () => {
    console.log('🔥 Logout button clicked!');
    try {
      console.log('🔴 Starting logout from DashboardScreen...');
      await logout();
      console.log('✅ Logout completed successfully');
    } catch (error) {
      console.error('❌ Erro ao fazer logout:', error);
      Alert.alert('Erro', 'Não foi possível fazer logout. Tente novamente.');
    }
  };

  const renderAccount = ({ item }: { item: AccountWithBalance }) => (
    <Card style={styles.accountCard}>
      <View style={styles.accountHeader}>
        <Text style={styles.accountName}>{item.nome}</Text>
        <Text style={styles.accountType}>{item.tipos}</Text>
      </View>
      <Text style={styles.accountBalance}>
        R$ {item.saldoAtual?.toFixed(2) || '0,00'}
      </Text>
      <View style={styles.accountFooter}>
        <Text style={styles.lastUpdate}>
          Saldo atualizado
        </Text>
      </View>
    </Card>
  );

  const getTotalBalance = () => {
    return accounts.reduce((total, account) => total + (account.saldoAtual || 0), 0);
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#4CAF50" />
        <Text style={styles.loadingText}>Carregando contas...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.headerContent}>
          <Text style={styles.title}>FinCerto</Text>
          <Text style={styles.totalBalance}>
            Saldo Total: R$ {getTotalBalance().toFixed(2)}
          </Text>
        </View>
        <TouchableOpacity 
          style={styles.logoutButton}
          onPress={handleLogout}
          activeOpacity={0.7}
          testID="logout-button"
        >
          <Text style={styles.logoutButtonText}>Sair</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={accounts}
        renderItem={renderAccount}
        keyExtractor={(item) => item.id?.toString() || Math.random().toString()}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>Nenhuma conta encontrada</Text>
            <Text style={styles.emptySubtext}>
              Adicione sua primeira conta para começar
            </Text>
          </View>
        }
      />
    </View>
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
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
  },
  header: {
    backgroundColor: '#4CAF50',
    paddingHorizontal: 20,
    paddingVertical: 24,
    paddingTop: 60,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  headerContent: {
    flex: 1,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 8,
  },
  totalBalance: {
    fontSize: 18,
    color: 'white',
    opacity: 0.9,
  },
  logoutButton: {
    backgroundColor: '#d32f2f',
    borderRadius: 8,
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderWidth: 0,
    minHeight: 44,
    justifyContent: 'center',
    alignItems: 'center',
  },
  logoutButtonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 15,
  },
  listContainer: {
    padding: 16,
  },
  accountCard: {
    marginBottom: 12,
    padding: 16,
  },
  accountHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  accountName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  accountType: {
    fontSize: 12,
    color: '#666',
    backgroundColor: '#e0e0e0',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  accountBalance: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#4CAF50',
    marginBottom: 8,
  },
  accountFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  lastUpdate: {
    fontSize: 12,
    color: '#999',
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 18,
    color: '#666',
    marginBottom: 8,
  },
  emptySubtext: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
  },
});

export default DashboardScreen;
