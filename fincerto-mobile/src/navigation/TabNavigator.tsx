import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import DashboardScreen from '../screens/dashboard/DashboardScreen';
import AccountsScreen from '../screens/accounts/AccountsScreen';
import TransactionsScreen from '../screens/transactions/TransactionsScreen';
import CategoriesScreen from '../screens/categories/CategoriesScreen';
import ReportsScreen from '../screens/reports/ReportsScreen';
import AccountDetailScreen from '../screens/accounts/AccountDetailScreen';

export type MainTabParamList = {
  Dashboard: undefined;
  Accounts: undefined;
  Transactions: undefined;
  Categories: undefined;
  Reports: undefined;
};

export type AccountsStackParamList = {
  AccountsList: undefined;
  AccountDetail: { accountId: number };
};

const Tab = createBottomTabNavigator<MainTabParamList>();
const AccountsStack = createNativeStackNavigator<AccountsStackParamList>();

const AccountsStackNavigator: React.FC = () => (
  <AccountsStack.Navigator>
    <AccountsStack.Screen 
      name="AccountsList" 
      component={AccountsScreen} 
      options={{ title: 'Contas' }}
    />
    <AccountsStack.Screen 
      name="AccountDetail" 
      component={AccountDetailScreen} 
      options={{ title: 'Detalhes da Conta' }}
    />
  </AccountsStack.Navigator>
);

const TabNavigator: React.FC = () => {
  return (
    <Tab.Navigator
      screenOptions={{
        tabBarActiveTintColor: '#4CAF50',
        tabBarInactiveTintColor: '#999',
        headerShown: false,
      }}
    >
      <Tab.Screen 
        name="Dashboard" 
        component={DashboardScreen} 
        options={{ 
          tabBarLabel: 'Início',
          // TODO: Add icons
        }} 
      />
      <Tab.Screen 
        name="Accounts" 
        component={AccountsStackNavigator} 
        options={{ 
          tabBarLabel: 'Contas',
          // TODO: Add icons
        }} 
      />
      <Tab.Screen 
        name="Transactions" 
        component={TransactionsScreen} 
        options={{ 
          tabBarLabel: 'Transações',
          // TODO: Add icons
        }} 
      />
      <Tab.Screen 
        name="Categories" 
        component={CategoriesScreen} 
        options={{ 
          tabBarLabel: 'Categorias',
          // TODO: Add icons
        }} 
      />
      <Tab.Screen 
        name="Reports" 
        component={ReportsScreen} 
        options={{ 
          tabBarLabel: 'Relatórios',
          // TODO: Add icons
        }} 
      />
    </Tab.Navigator>
  );
};

export default TabNavigator;
