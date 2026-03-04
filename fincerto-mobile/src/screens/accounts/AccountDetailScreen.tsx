import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  ActivityIndicator,
} from 'react-native';
import { RouteProp } from '@react-navigation/native';
import { AccountsStackParamList } from '../../navigation/TabNavigator';
import { AccountWithBalance } from '../../types/account';
import { Transaction } from '../../types';
import { transacaoService, contaService } from '../../services';
import { Card } from '../../components/common/Card';

// route prop now contains entire account object
type AccountDetailScreenRouteProp = RouteProp<AccountsStackParamList, 'AccountDetail'>;

interface Props {
  route: AccountDetailScreenRouteProp;
}

const AccountDetailScreen: React.FC<Props> = ({ route }) => {
  const { account } = route.params;
  const [saldo, setSaldo] = useState<number | undefined>(account.saldoAtual);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  const loadTransactions = async () => {
    if (!account.id) return;
    setLoading(true);
    try {
      const all = await transacaoService.listarPorConta(account.id);
      const sorted = all.sort((a, b) =>
        new Date(b.date || '').getTime() - new Date(a.date || '').getTime()
      );
      setTransactions(sorted.slice(0, 5));
    } catch (error) {
      console.error('Erro ao carregar transações da conta:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTransactions();
    // also refresh balance in case it was stale
    if (account.id) {
      contaService.obterSaldo(account.id).then(setSaldo).catch(console.error);
    }
  }, [account.id]);

  const renderTransaction = ({ item }: { item: Transaction }) => (
    <Card style={styles.transactionCard}>
      <View style={styles.transactionHeader}>
        <Text style={styles.transactionDescription}>{item.descricao}</Text>
        <Text style={[
          styles.transactionValue,
          item.tipo === 'RECEITA' ? styles.valuePositive : styles.valueNegative
        ]}>
          {item.tipo === 'RECEITA' ? '+' : '-'} R$ {item.valor.toFixed(2)}
        </Text>
      </View>
      <View style={styles.transactionFooter}>
        <Text style={styles.transactionCategory}>{item.categoria?.nome}</Text>
        <Text style={styles.transactionDate}>
          {item.date ? new Date(item.date).toLocaleDateString() : ''}
        </Text>
      </View>
    </Card>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>{account.nome}</Text>
        <Text style={styles.type}>{account.tipos}</Text>
        {saldo !== undefined && (
          <Text style={styles.balance}>Saldo: R$ {saldo.toFixed(2)}</Text>
        )}
      </View>
      {loading ? (
        <ActivityIndicator size="large" color="#4CAF50" />
      ) : (
        <FlatList
          data={transactions}
          renderItem={renderTransaction}
          keyExtractor={(item) => item.id?.toString() || Math.random().toString()}
          ListEmptyComponent={<Text style={styles.emptyText}>Nenhuma transação recente</Text>}
          contentContainerStyle={styles.listContainer}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    padding: 20,
    alignItems: 'center',
    backgroundColor: '#4CAF50',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 4,
  },
  type: {
    fontSize: 14,
    color: 'white',
    marginBottom: 4,
  },
  balance: {
    fontSize: 18,
    color: 'white',
    fontWeight: 'bold',
  },
  listContainer: {
    padding: 16,
  },
  transactionCard: {
    marginBottom: 12,
    padding: 16,
  },
  transactionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  transactionDescription: {
    fontSize: 16,
    color: '#333',
  },
  transactionValue: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  valuePositive: {
    color: '#4CAF50',
  },
  valueNegative: {
    color: '#e53935',
  },
  transactionFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  transactionCategory: {
    fontSize: 12,
    color: '#666',
  },
  transactionDate: {
    fontSize: 12,
    color: '#999',
  },
  emptyText: {
    padding: 20,
    color: '#666',
  },
});

export default AccountDetailScreen;
