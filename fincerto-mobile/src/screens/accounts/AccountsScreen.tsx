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
  SafeAreaView,
} from 'react-native';
import { useFocusEffect, useNavigation, NavigationProp } from '@react-navigation/native';
import { Account, AccountWithBalance } from '../../types';
import { contaService } from '../../services';
import { AccountsStackParamList } from '../../navigation/TabNavigator';
import { Card } from '../../components/common/Card';
import AccountForm from '../../components/forms/AccountForm';

const AccountsScreen: React.FC = () => {
  const navigation = useNavigation<NavigationProp<AccountsStackParamList>>();

  const [accounts, setAccounts] = useState<AccountWithBalance[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showForm, setShowForm] = useState(false);

  const loadAccountsWithBalance = async () => {
    console.log('🔄 loadAccountsWithBalance called');
    try {
      const accountsData = await contaService.listar();
      console.log('📦 contas retornadas:', accountsData.length);
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
      console.error('❌ Erro em loadAccountsWithBalance:', error);
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

  const renderAccount = ({ item }: { item: AccountWithBalance }) => (
    <TouchableOpacity
      onPress={() => navigation.navigate('AccountDetail', { account: item })}
    >
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
    </TouchableOpacity>
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
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Contas</Text>
        <Text style={styles.totalBalance}>
          Total: R$ {getTotalBalance().toFixed(2)}
        </Text>
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
              Toque no botão + para adicionar sua primeira conta
            </Text>
          </View>
        }
      />

      <TouchableOpacity 
        style={styles.fab} 
        onPress={() => setShowForm(true)}
      >
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>

      <AccountForm
        visible={showForm}
        onClose={() => setShowForm(false)}
        onSuccess={loadAccountsWithBalance}
      />
    </SafeAreaView>
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
  fab: {
    position: 'absolute',
    right: 20,
    bottom: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: '#4CAF50',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 5,
  },
  fabText: {
    fontSize: 24,
    color: 'white',
    fontWeight: 'bold',
  },
});

export default AccountsScreen;
