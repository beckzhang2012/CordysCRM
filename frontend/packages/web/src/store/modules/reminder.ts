import { defineStore } from 'pinia';

export interface ReminderItem {
  id: string;
  customerId: string;
  customerName: string;
  remindTime: number;
  content: string;
  createTime: number;
  isRead: boolean;
}

export interface ReminderState {
  reminderList: ReminderItem[];
}

const STORAGE_KEY = 'crm_customer_reminders';

const useReminderStore = defineStore('reminder', {
  state: (): ReminderState => ({
    reminderList: [],
  }),
  getters: {
    getUnreadCount(state: ReminderState): number {
      const now = Date.now();
      return state.reminderList.filter((item) => !item.isRead && item.remindTime <= now).length;
    },
    getUnreadReminders(state: ReminderState): ReminderItem[] {
      const now = Date.now();
      return state.reminderList
        .filter((item) => !item.isRead && item.remindTime <= now)
        .sort((a, b) => b.remindTime - a.remindTime);
    },
    getCustomerReminders: (state: ReminderState) => (customerId: string) => {
      return state.reminderList
        .filter((item) => item.customerId === customerId)
        .sort((a, b) => b.remindTime - a.remindTime);
    },
  },
  actions: {
    initReminderList() {
      try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored) {
          this.reminderList = JSON.parse(stored);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error('Failed to load reminders:', error);
        this.reminderList = [];
      }
    },
    saveToStorage() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.reminderList));
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error('Failed to save reminders:', error);
      }
    },
    addReminder(reminder: Omit<ReminderItem, 'id' | 'createTime' | 'isRead'>) {
      const newReminder: ReminderItem = {
        ...reminder,
        id: `reminder_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        createTime: Date.now(),
        isRead: false,
      };
      this.reminderList.push(newReminder);
      this.saveToStorage();
      return newReminder;
    },
    deleteReminder(id: string) {
      const index = this.reminderList.findIndex((item) => item.id === id);
      if (index > -1) {
        this.reminderList.splice(index, 1);
        this.saveToStorage();
      }
    },
    markAsRead(id: string) {
      const reminder = this.reminderList.find((item) => item.id === id);
      if (reminder) {
        reminder.isRead = true;
        this.saveToStorage();
      }
    },
    markAllAsRead() {
      this.reminderList.forEach((item) => {
        if (item.remindTime <= Date.now()) {
          item.isRead = true;
        }
      });
      this.saveToStorage();
    },
    clearExpiredReminders() {
      const thirtyDaysAgo = Date.now() - 30 * 24 * 60 * 60 * 1000;
      this.reminderList = this.reminderList.filter((item) => item.createTime > thirtyDaysAgo);
      this.saveToStorage();
    },
  },
});

export default useReminderStore;
