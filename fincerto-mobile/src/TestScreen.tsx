import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

const TestScreen: React.FC = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Test Screen - App está funcionando!</Text>
      <Text style={styles.subtext}>Se você vê isso, o React Native está funcionando</Text>
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
  text: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
  },
  subtext: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
  },
});

export default TestScreen;
