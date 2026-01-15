import { defineStore } from 'pinia';

import type { ReminderListResponse } from '@lib/shared/models/reminder';
import { getReminderList, addReminder, deleteReminder, markAsRead, markAllAsRead } from '@/api/modules';

interface ReminderState {
  reminders: ReminderListResponse[];
  timer: number | null;
  unreadCount: number;
}

const useReminderStore = defineStore('reminder', {
  state: (): ReminderState => ({
    reminders: [],
    timer: null,
    unreadCount: 0,
  }),
  getters: {
    unreadReminders(state: ReminderState) {
      return state.reminders.filter((r) => !r.isRead);
    },
    readReminders(state: ReminderState) {
      return state.reminders.filter((r) => r.isRead);
    },
  },
  actions: {
    async initReminders() {
      await this.loadReminders();
    },

    async loadReminders(customerId?: string) {
      try {
        const res = await getReminderList({ customerId, current: 1, pageSize: 100 });
        this.reminders = res.list;
        this.scheduleReminders();
      } catch (error) {
        console.error('Failed to load reminders:', error);
      }
    },

    async addReminder(data: { customerId: string; reminderTime: number; content: string }) {
      try {
        await addReminder(data);
        await this.loadReminders();
      } catch (error) {
        console.error('Failed to add reminder:', error);
        throw error;
      }
    },

    async deleteReminder(id: string) {
      try {
        await deleteReminder(id);
        const index = this.reminders.findIndex((r) => r.id === id);
        if (index !== -1) {
          this.reminders.splice(index, 1);
        }
      } catch (error) {
        console.error('Failed to delete reminder:', error);
        throw error;
      }
    },

    async markAsRead(id: string) {
      try {
        await markAsRead(id);
        const reminder = this.reminders.find((r) => r.id === id);
        if (reminder) {
          reminder.isRead = true;
        }
      } catch (error) {
        console.error('Failed to mark reminder as read:', error);
        throw error;
      }
    },

    async markAllAsRead() {
      try {
        await markAllAsRead();
        this.reminders.forEach((r) => {
          r.isRead = true;
        });
      } catch (error) {
        console.error('Failed to mark all reminders as read:', error);
        throw error;
      }
    },

    getRemindersByCustomerId(customerId: string) {
      return this.reminders.filter((r) => r.customerId === customerId);
    },

    scheduleReminders() {
      const now = Date.now();
      this.reminders.forEach((reminder) => {
        const reminderTime = reminder.reminderTime;
        const delay = reminderTime - now;

        if (delay > 0 && !reminder.isRead) {
          setTimeout(() => {
            this.triggerReminder(reminder);
          }, delay);
        }
      });
    },

    triggerReminder(reminder: ReminderListResponse) {
      reminder.isRead = false;
    },

    clearExpiredReminders() {
      const now = Date.now();
      this.reminders = this.reminders.filter((r) => {
        const reminderTime = r.reminderTime;
        return reminderTime > now - 24 * 60 * 60 * 1000;
      });
    },
  },
});

export default useReminderStore;
