import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  RefreshControl,
  ActivityIndicator,
  Alert,
  TouchableOpacity,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { Transaction, Account, Category } from '../../types';
import { transacaoService, contaService, categoriaService } from '../../services';
import { Card } from '../../components/common/Card';

const ReportsScreen: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState<'month' | 'year'>('month');

  const loadData = async () => {
    try {
      const [transactionsData, accountsData, categoriesData] = await Promise.all([
        transacaoService.listar(),
        contaService.listar(),
        categoriaService.listar(),
      ]);
      
      setTransactions(transactionsData);
      setAccounts(accountsData);
      setCategories(categoriesData);
    } catch (error) {
      Alert.alert('Erro', 'Não foi possível carregar os dados para os relatórios');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    React.useCallback(() => {
      loadData();
    }, [])
  );

  const onRefresh = () => {
    setRefreshing(true);
    loadData();
  };

  const getFilteredTransactions = () => {
    const now = new Date();
    const filtered = transactions.filter(t => {
      if (!t.date) return false;
      const transactionDate = new Date(t.date);
      
      if (selectedPeriod === 'month') {
        return transactionDate.getMonth() === now.getMonth() &&
               transactionDate.getFullYear() === now.getFullYear();
      } else {
        return transactionDate.getFullYear() === now.getFullYear();
      }
    });
    
    return filtered.sort((a, b) => new Date(b.date || '').getTime() - new Date(a.date || '').getTime());
  };

  const getTransactionsByCategory = () => {
    const filtered = getFilteredTransactions();
    const byCategory: { [key: string]: { amount: number; count: number; type: string } } = {};
    
    filtered.forEach(t => {
      const categoryName = t.categoria?.nome || 'Sem categoria';
      if (!byCategory[categoryName]) {
        byCategory[categoryName] = { amount: 0, count: 0, type: t.tipo };
      }
      byCategory[categoryName].amount += t.valor;
      byCategory[categoryName].count += 1;
    });
    
    return Object.entries(byCategory)
      .sort(([, a], [, b]) => b.amount - a.amount)
      .slice(0, 10);
  };

  const getTransactionsByAccount = () => {
    const filtered = getFilteredTransactions();
    const byAccount: { [key: string]: { amount: number; count: number } } = {};
    
    filtered.forEach(t => {
      const accountName = t.conta?.nome || 'Sem conta';
      if (!byAccount[accountName]) {
        byAccount[accountName] = { amount: 0, count: 0 };
      }
      byAccount[accountName].amount += t.valor;
      byAccount[accountName].count += 1;
    });
    
    return Object.entries(byAccount)
      .sort(([, a], [, b]) => b.amount - a.amount);
  };

  const getSummary = () => {
    const filtered = getFilteredTransactions();
    const receitas = filtered.filter(t => t.tipo === 'RECEITA').reduce((sum, t) => sum + t.valor, 0);
    const despesas = filtered.filter(t => t.tipo === 'DESPESA').reduce((sum, t) => sum + t.valor, 0);
    const saldo = receitas - despesas;
    
    return { receitas, despesas, saldo, count: filtered.length };
  };

  const renderSummaryCard = () => {
    const summary = getSummary();
    return (
      <Card style={styles.summaryCard}>
        <Text style={styles.cardTitle}>Resumo do {selectedPeriod === 'month' ? 'Mês' : 'Ano'}</Text>
        <View style={styles.summaryRow}>
          <View style={styles.summaryItem}>
            <Text style={styles.summaryLabel}>Receitas</Text>
            <Text style={styles.summaryValuePositive}>+R$ {summary.receitas.toFixed(2)}</Text>
          </View>
          <View style={styles.summaryItem}>
            <Text style={styles.summaryLabel}>Despesas</Text>
            <Text style={styles.summaryValueNegative}>-R$ {summary.despesas.toFixed(2)}</Text>
          </View>
        </View>
        <View style={styles.balanceRow}>
          <Text style={styles.balanceLabel}>Saldo</Text>
          <Text style={[
            styles.balanceValue,
            summary.saldo >= 0 ? styles.balancePositive : styles.balanceNegative
          ]}>
            R$ {summary.saldo.toFixed(2)}
          </Text>
        </View>
        <Text style={styles.transactionCount}>{summary.count} transações</Text>
      </Card>
    );
  };

  const renderTopCategories = () => {
    const byCategory = getTransactionsByCategory();
    return (
      <Card style={styles.card}>
        <Text style={styles.cardTitle}>Top Categorias</Text>
        {byCategory.length === 0 ? (
          <Text style={styles.emptyText}>Nenhuma transação no período</Text>
        ) : (
          byCategory.map(([name, data], index) => (
            <View key={name} style={styles.categoryRow}>
              <Text style={styles.categoryRank}>#{index + 1}</Text>
              <Text style={styles.categoryName}>{name}</Text>
              <Text style={[
                styles.categoryAmount,
                data.type === 'RECEITA' ? styles.amountPositive : styles.amountNegative
              ]}>
                {data.type === 'RECEITA' ? '+' : '-'}R$ {data.amount.toFixed(2)}
              </Text>
            </View>
          ))
        )}
      </Card>
    );
  };

  const renderAccountSummary = () => {
    const byAccount = getTransactionsByAccount();
    return (
      <Card style={styles.card}>
        <Text style={styles.cardTitle}>Resumo por Conta</Text>
        {byAccount.length === 0 ? (
          <Text style={styles.emptyText}>Nenhuma transação no período</Text>
        ) : (
          byAccount.map(([name, data]) => (
            <View key={name} style={styles.accountRow}>
              <Text style={styles.accountName}>{name}</Text>
              <View style={styles.accountDetails}>
                <Text style={styles.accountAmount}>R$ {data.amount.toFixed(2)}</Text>
                <Text style={styles.accountCount}>{data.count} transações</Text>
              </View>
            </View>
          ))
        )}
      </Card>
    );
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#4CAF50" />
        <Text style={styles.loadingText}>Carregando relatórios...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Relatórios</Text>
        <View style={styles.periodSelector}>
          <TouchableOpacity
            style={[
              styles.periodButton,
              selectedPeriod === 'month' && styles.periodButtonActive
            ]}
            onPress={() => setSelectedPeriod('month')}
          >
            <Text style={[
              styles.periodButtonText,
              selectedPeriod === 'month' && styles.periodButtonTextActive
            ]}>
              Mês
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.periodButton,
              selectedPeriod === 'year' && styles.periodButtonActive
            ]}
            onPress={() => setSelectedPeriod('year')}
          >
            <Text style={[
              styles.periodButtonText,
              selectedPeriod === 'year' && styles.periodButtonTextActive
            ]}>
              Ano
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      <ScrollView
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        contentContainerStyle={styles.scrollContainer}
        showsVerticalScrollIndicator={false}
      >
        {renderSummaryCard()}
        {renderTopCategories()}
        {renderAccountSummary()}
      </ScrollView>
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
  periodSelector: {
    flexDirection: 'row',
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 8,
    padding: 4,
  },
  periodButton: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 6,
    alignItems: 'center',
  },
  periodButtonActive: {
    backgroundColor: 'white',
  },
  periodButtonText: {
    fontSize: 14,
    color: 'white',
    fontWeight: '500',
  },
  periodButtonTextActive: {
    color: '#4CAF50',
    fontWeight: 'bold',
  },
  scrollContainer: {
    padding: 16,
  },
  card: {
    marginBottom: 16,
    padding: 16,
  },
  summaryCard: {
    marginBottom: 16,
    padding: 16,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  summaryItem: {
    flex: 1,
    alignItems: 'center',
  },
  summaryLabel: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  summaryValuePositive: {
    fontSize: 16,
    color: '#4CAF50',
    fontWeight: 'bold',
  },
  summaryValueNegative: {
    fontSize: 16,
    color: '#f44336',
    fontWeight: 'bold',
  },
  balanceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
    marginBottom: 8,
  },
  balanceLabel: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  balanceValue: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  balancePositive: {
    color: '#4CAF50',
  },
  balanceNegative: {
    color: '#f44336',
  },
  transactionCount: {
    fontSize: 12,
    color: '#999',
    textAlign: 'center',
  },
  categoryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  categoryRank: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#666',
    width: 30,
  },
  categoryName: {
    flex: 1,
    fontSize: 16,
    color: '#333',
    marginLeft: 12,
  },
  categoryAmount: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  amountPositive: {
    color: '#4CAF50',
  },
  amountNegative: {
    color: '#f44336',
  },
  accountRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  accountName: {
    fontSize: 16,
    color: '#333',
    flex: 1,
  },
  accountDetails: {
    alignItems: 'flex-end',
  },
  accountAmount: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  accountCount: {
    fontSize: 12,
    color: '#666',
  },
  emptyText: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
    paddingVertical: 20,
  },
});

export default ReportsScreen;
