// src/services/settingsService.js
import api from './api';

const settingsService = {
  // Notification Settings
  getNotificationSettings: async () => {
    const response = await api.get('/notifications/settings');
    return response.data;
  },

  updateNotificationSettings: async (settings) => {
    const response = await api.put('/notifications/settings', settings);
    return response.data;
  },

  resetNotificationSettings: async () => {
    const response = await api.post('/notifications/settings/reset');
    return response.data;
  },

  toggleAllNotifications: async (enabled) => {
    const response = await api.put('/notifications/settings/toggle-all', null, {
      params: { enabled }
    });
    return response.data;
  },

  setDoNotDisturb: async (enabled, startHour, endHour) => {
    const response = await api.put('/notifications/settings/do-not-disturb', null, {
      params: { enabled, startHour, endHour }
    });
    return response.data;
  },

  // User Preferences
  getUserPreferences: async () => {
    const response = await api.get('/users/preferences');
    return response.data;
  },

  updateUserPreferences: async (preferences) => {
    const response = await api.put('/users/preferences', preferences);
    return response.data;
  },

  // Theme Settings
  updateTheme: async (theme) => {
    const response = await api.put('/users/theme', { theme });
    return response.data;
  },

  // Language Settings
  updateLanguage: async (language) => {
    const response = await api.put('/users/language', { language });
    return response.data;
  }
};

export default settingsService;