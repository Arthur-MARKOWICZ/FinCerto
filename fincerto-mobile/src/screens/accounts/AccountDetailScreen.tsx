import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { RouteProp } from '@react-navigation/native';
import { AccountsStackParamList } from '../../navigation/TabNavigator';

type AccountDetailScreenRouteProp = RouteProp<AccountsStackParamList, 'AccountDetail'>;

interface Props {
  route: AccountDetailScreenRouteProp;
}

const AccountDetailScreen: React.FC<Props> = ({ route }) => {
  const { accountId } = route.params;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Detalhes da Conta</Text>
      <Text style={styles.subtitle}>Conta ID: {accountId}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
  },
});

export default AccountDetailScreen;
