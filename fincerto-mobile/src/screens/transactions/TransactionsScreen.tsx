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
import { Transaction } from '../../types';
import { transacaoService } from '../../services';
import { Card } from '../../components/common/Card';
import TransactionForm from '../../components/forms/TransactionForm';

const TransactionsScreen: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showForm, setShowForm] = useState(false);

  const loadTransactions = async () => {
    try {
      const transactionsData = await transacaoService.listar();
      setTransactions(transactionsData.sort((a, b) => 
        new Date(b.date || '').getTime() - new Date(a.date || '').getTime()
      ));
    } catch (error) {
      Alert.alert('Erro', 'Não foi possível carregar as transações');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    React.useCallback(() => {
      loadTransactions();
    }, [])
  );

  const onRefresh = () => {
    setRefreshing(true);
    loadTransactions();
  };

  const getTotalByType = (type: string) => {
    return transactions
      .filter(t => t.tipo === type)
      .reduce((total, t) => total + t.valor, 0);
  };

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

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#4CAF50" />
        <Text style={styles.loadingText}>Carregando transações...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Transações</Text>
        <View style={styles.summary}>
          <View style={styles.summaryItem}>
            <Text style={styles.summaryLabel}>Receitas</Text>
            <Text style={styles.summaryValuePositive}>
              +R$ {getTotalByType('RECEITA').toFixed(2)}
            </Text>
          </View>
          <View style={styles.summaryItem}>
            <Text style={styles.summaryLabel}>Despesas</Text>
            <Text style={styles.summaryValueNegative}>
              -R$ {getTotalByType('DESPESA').toFixed(2)}
            </Text>
          </View>
        </View>
      </View>

      <FlatList
        data={transactions}
        renderItem={renderTransaction}
        keyExtractor={(item) => item.id?.toString() || Math.random().toString()}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>Nenhuma transação encontrada</Text>
            <Text style={styles.emptySubtext}>
              Toque no botão + para adicionar sua primeira transação
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

      <TransactionForm
        visible={showForm}
        onClose={() => setShowForm(false)}
        onSuccess={loadTransactions}
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
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 16,
  },
  summary: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  summaryItem: {
    flex: 1,
    alignItems: 'center',
  },
  summaryLabel: {
    fontSize: 14,
    color: 'white',
    opacity: 0.8,
    marginBottom: 4,
  },
  summaryValuePositive: {
    fontSize: 18,
    color: 'white',
    fontWeight: 'bold',
  },
  summaryValueNegative: {
    fontSize: 18,
    color: '#ffcdd2',
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
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  transactionDescription: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    flex: 1,
    marginRight: 12,
  },
  transactionValue: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  valuePositive: {
    color: '#4CAF50',
  },
  valueNegative: {
    color: '#f44336',
  },
  transactionFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  transactionCategory: {
    fontSize: 14,
    color: '#666',
    backgroundColor: '#e0e0e0',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  transactionDate: {
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

export default TransactionsScreen;
