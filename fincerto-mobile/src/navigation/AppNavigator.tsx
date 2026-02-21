import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useAuth } from '../context/AuthContext';
import AuthNavigator from './AuthNavigator';
import TabNavigator from './TabNavigator';
import LoadingScreen from '../screens/common/LoadingScreen';

export type RootStackParamList = {
  Auth: undefined;
  Main: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const AppNavigator: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  console.log('=== AppNavigator Debug ===');
  console.log('isLoading:', isLoading);
  console.log('isAuthenticated:', isAuthenticated);
  console.log('Should show:', isAuthenticated ? 'Main (Dashboard)' : 'Auth (Login)');

  if (isLoading) {
    console.log('🔄 Showing LoadingScreen');
    return <LoadingScreen />;
  }

  console.log('🚀 Rendering navigator for:', isAuthenticated ? 'Main' : 'Auth');

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {!isAuthenticated ? (
          <Stack.Screen 
            name="Auth" 
            component={AuthNavigator}
            options={{ animationEnabled: false }}
          />
        ) : (
          <Stack.Screen 
            name="Main" 
            component={TabNavigator}
            options={{ animationEnabled: false }}
          />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default AppNavigator;
