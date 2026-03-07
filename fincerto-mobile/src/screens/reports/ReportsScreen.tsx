import React, { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  RefreshControl,
  ActivityIndicator,
  Alert,
  TouchableOpacity,
  Platform,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { Category } from '../../types';
import { categoriaService, relatorioService } from '../../services';
import { Buffer } from 'buffer';
import { Picker } from '@react-native-picker/picker';
import { Card } from '../../components/common/Card';

// Módulos nativos apenas para mobile
const RNFS = Platform.OS === 'web' ? null : require('react-native-fs');
const FileViewer = Platform.OS === 'web' ? null : require('react-native-file-viewer');

const ReportsScreen: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [downloadLoading, setDownloadLoading] = useState(false);


  useFocusEffect(
    React.useCallback(() => {
      // fetch categories if needed for report parameters
      categoriaService.listar().then(setCategories).catch(() => {});
      setLoading(false);
    }, [])
  );

  const onRefresh = () => {
    setRefreshing(true);
    categoriaService.listar().then(setCategories).catch(() => {}).finally(() => setRefreshing(false));
  };

  // warn user if there are no categories to filter by
  React.useEffect(() => {
    if (!loading && categories.length === 0) {
      Alert.alert('Nenhuma categoria', 'Não há categorias cadastradas. Relatórios por categoria podem ser gerados para todas as categorias ou após criar alguma.');
    }
    // if we obtained categories and user hasn't picked one yet, keep '' (Todas)
    if (categories.length > 0 && selectedCategoryId === '') {
      // nothing to change, still '' represents all
    }
  }, [loading, categories]);








  const formatDate = (d: Date) => {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  };

  const saveAndOpenArrayBuffer = async (arrayBuffer: any, filename: string) => {
    if (Platform.OS === 'web') {
      Alert.alert('Não suportado', 'Downloads não estão disponíveis no web. Use a aplicação mobile para baixar relatórios.');
      return;
    }

    try {
      const uint8 = new Uint8Array(arrayBuffer);
      const base64 = Buffer.from(uint8).toString('base64');
      const tmpDir = RNFS.TemporaryDirectoryPath || RNFS.CachesDirectoryPath || RNFS.DocumentDirectoryPath;
      const path = `${tmpDir}/${filename}`;
      await RNFS.writeFile(path, base64, 'base64');
      await FileViewer.open(path, { showOpenWithDialog: true });
    } catch (err) {
      throw err;
    }
  };


  // download optionally filtered by categoryId (string or undefined)
  const downloadPorCategoria = async (categoriaId?: string) => {
    setDownloadLoading(true);
    try {
      const arrayBuffer = await relatorioService.obterRelatorioPorCategoria({ formato: 'pdf', categoriaId });
      const filename = `relatorio_categoria_${categoriaId || 'geral'}.pdf`;
      await saveAndOpenArrayBuffer(arrayBuffer, filename);
    } catch (error: any) {
      console.error('downloadPorCategoria error', error);
      const msg = error.response?.data?.message || error.message || '';
      Alert.alert('Erro', `Não foi possível baixar/abrir o relatório por categoria. ${msg}`);
    } finally {
      setDownloadLoading(false);
    }
  };

  const handleDownloadMensal = async () => {
    setDownloadLoading(true);
    try {
      const ano = new Date().getFullYear();
      const arrayBuffer = await relatorioService.obterRelatorioMensal(ano, 'pdf');
      const filename = `relatorio_mensal_${ano}.pdf`;
      await saveAndOpenArrayBuffer(arrayBuffer, filename);
    } catch (error: any) {
      console.error('handleDownloadMensal error', error);
      const msg = error.response?.data?.message || error.message || '';
      Alert.alert('Erro', `Não foi possível baixar/abrir o relatório mensal. ${msg}`);
    } finally {
      setDownloadLoading(false);
    }
  };

  const handleDownloadPeriodo = async () => {
    setDownloadLoading(true);
    try {
      const now = new Date();
      const inicio = new Date(now);
      inicio.setDate(now.getDate() - 30);
      const dataInicio = formatDate(inicio);
      const dataFim = formatDate(now);
      const arrayBuffer = await relatorioService.obterRelatorioPeriodo(dataInicio, dataFim, 'pdf');
      const filename = `relatorio_periodo_${dataInicio}_to_${dataFim}.pdf`;
      await saveAndOpenArrayBuffer(arrayBuffer, filename);
    } catch (error: any) {
      console.error('handleDownloadPeriodo error', error);
      const msg = error.response?.data?.message || error.message || '';
      Alert.alert('Erro', `Não foi possível baixar/abrir o relatório do período. ${msg}`);
    } finally {
      setDownloadLoading(false);
    }
  };

  const requestOnlyPorCategoria = async (categoriaId?: string) => {
    try {
      await relatorioService.obterRelatorioPorCategoria({ formato: 'pdf', categoriaId });
      Alert.alert('Sucesso', 'Relatório solicitado com sucesso (recebido)');
    } catch (error: any) {
      console.error('requestOnlyPorCategoria error', error);
      const msg = error.response?.data?.message || error.message || '';
      Alert.alert('Erro', `Falha ao solicitar o relatório. ${msg}`);
    }
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
      </View>

      <ScrollView
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        contentContainerStyle={styles.scrollContainer}
        showsVerticalScrollIndicator={false}
      >
        <Card style={styles.card}>
          <Text style={styles.cardTitle}>Exportar / Baixar Relatórios</Text>
          <View style={{ marginBottom: 8 }}>
            <Text style={{ marginBottom: 4 }}>Categoria</Text>
            <Picker
              selectedValue={selectedCategoryId}
              onValueChange={(v: React.SetStateAction<string>) => setSelectedCategoryId(v)}
              style={{ height: 40, width: '100%' }}
            >
              <Picker.Item label="Todas" value="" />
              {categories.map(c => (
                <Picker.Item key={c.id} label={c.nome} value={c.id || ''} />
              ))}
            </Picker>
          </View>
          <View style={{ marginBottom: 8 }}>
            <TouchableOpacity
              style={[styles.downloadButton]}
              onPress={() => downloadPorCategoria(selectedCategoryId || undefined)}
              disabled={downloadLoading}
            >
              <Text style={styles.downloadButtonText}>{downloadLoading ? 'Processando...' : 'Baixar relatório por categoria (PDF)'}</Text>
            </TouchableOpacity>
          </View>
          <View style={{ marginBottom: 8 }}>
            <TouchableOpacity
              style={[styles.downloadButton]}
              onPress={handleDownloadMensal}
              disabled={downloadLoading}
            >
              <Text style={styles.downloadButtonText}>{downloadLoading ? 'Processando...' : 'Baixar resumo mensal (PDF)'}</Text>
            </TouchableOpacity>
          </View>
          <View style={{ marginBottom: 8 }}>
            <TouchableOpacity
              style={[styles.downloadButton]}
              onPress={handleDownloadPeriodo}
              disabled={downloadLoading}
            >
              <Text style={styles.downloadButtonText}>{downloadLoading ? 'Processando...' : 'Baixar relatório período (últimos 30 dias) (PDF)'}</Text>
            </TouchableOpacity>
          </View>
          <View>
            <TouchableOpacity
              style={[styles.requestButton]}
              onPress={() => requestOnlyPorCategoria(selectedCategoryId || undefined)}
              disabled={downloadLoading}
            >
              <Text style={styles.requestButtonText}>Solicitar relatório (sem salvar/abrir)</Text>
            </TouchableOpacity>
          </View>
        </Card>
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
  scrollContainer: {
    padding: 16,
  },
  card: {
    marginBottom: 16,
    padding: 16,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
  },
  emptyText: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
    paddingVertical: 20,
  },
  downloadButton: {
    backgroundColor: '#4CAF50',
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderRadius: 6,
    alignItems: 'center',
  },
  downloadButtonText: {
    color: 'white',
    fontWeight: '600',
  },
  requestButton: {
    backgroundColor: '#2196F3',
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderRadius: 6,
    alignItems: 'center',
  },
  requestButtonText: {
    color: 'white',
    fontWeight: '600',
  },
});

export default ReportsScreen;
