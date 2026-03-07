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
import { useFocusEffect } from '@react-navigation/native';
import { Category } from '../../types';
import { categoriaService } from '../../services';
import { Card } from '../../components/common/Card';
import CategoryForm from '../../components/forms/CategoryForm';

const CategoriesScreen: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showForm, setShowForm] = useState(false);

  const loadCategories = async () => {
    try {
      const categoriesData = await categoriaService.listar();
      setCategories(categoriesData.sort((a, b) => a.nome.localeCompare(b.nome)));
    } catch (error) {
      Alert.alert('Erro', 'Não foi possível carregar as categorias');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    React.useCallback(() => {
      loadCategories();
    }, [])
  );

  const onRefresh = () => {
    setRefreshing(true);
    loadCategories();
  };

  const renderCategory = ({ item }: { item: Category }) => (
    <Card style={styles.categoryCard}>
      <View style={styles.categoryHeader}>
        <Text style={styles.categoryName}>{item.nome}</Text>
        <View style={[
          styles.categoryType,
          item.tipo === 'RECEITA' ? styles.typeReceita : styles.typeDespesa
        ]}>
          <Text style={[
            styles.categoryTypeText,
            item.tipo === 'RECEITA' ? styles.typeReceitaText : styles.typeDespesaText
          ]}>
            {item.tipo}
          </Text>
        </View>
      </View>
    </Card>
  );

  const getCategoriesByType = (type: string) => {
    return categories.filter(cat => cat.tipo === type);
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#4CAF50" />
        <Text style={styles.loadingText}>Carregando categorias...</Text>
      </View>
    );
  }

  const receitas = getCategoriesByType('RECEITA');
  const despesas = getCategoriesByType('DESPESA');

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Categorias</Text>
        <Text style={styles.subtitle}>
          {categories.length} categorias cadastradas
        </Text>
      </View>

      <FlatList
        data={[
          { title: 'Receitas', data: receitas, type: 'RECEITA' },
          { title: 'Despesas', data: despesas, type: 'DESPESA' }
        ]}
        renderItem={({ item }) => (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>{item.title}</Text>
            {item.data.length === 0 ? (
              <View style={styles.emptySection}>
                <Text style={styles.emptyText}>
                  Nenhuma categoria de {item.type.toLowerCase()} encontrada
                </Text>
              </View>
            ) : (
              item.data.map((category) => (
                <Card key={category.id} style={styles.categoryCard}>
                  <View style={styles.categoryHeader}>
                    <Text style={styles.categoryName}>{category.nome}</Text>
                    <View style={[
                      styles.categoryType,
                      category.tipo === 'RECEITA' ? styles.typeReceita : styles.typeDespesa
                    ]}>
                      <Text style={[
                        styles.categoryTypeText,
                        category.tipo === 'RECEITA' ? styles.typeReceitaText : styles.typeDespesaText
                      ]}>
                        {category.tipo}
                      </Text>
                    </View>
                  </View>
                </Card>
              ))
            )}
          </View>
        )}
        keyExtractor={(item) => item.type}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>Nenhuma categoria encontrada</Text>
            <Text style={styles.emptySubtext}>
              Toque no botão + para adicionar sua primeira categoria
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

      <CategoryForm
        visible={showForm}
        onClose={() => setShowForm(false)}
        onSuccess={loadCategories}
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
  subtitle: {
    fontSize: 16,
    color: 'white',
    opacity: 0.9,
  },
  listContainer: {
    padding: 16,
  },
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 12,
  },
  emptySection: {
    backgroundColor: 'white',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
  },
  categoryCard: {
    marginBottom: 8,
    padding: 16,
  },
  categoryHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  categoryName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    flex: 1,
  },
  categoryType: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
  },
  typeReceita: {
    backgroundColor: '#e8f5e8',
  },
  typeDespesa: {
    backgroundColor: '#ffebee',
  },
  categoryTypeText: {
    fontSize: 12,
    fontWeight: 'bold',
  },
  typeReceitaText: {
    color: '#4CAF50',
  },
  typeDespesaText: {
    color: '#f44336',
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

export default CategoriesScreen;
