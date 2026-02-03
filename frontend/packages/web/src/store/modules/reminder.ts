import { defineStore } from 'pinia';
import dayjs from 'dayjs';

export interface ReminderItem {
  id: string;
  customerId: string;
  customerName: string;
  reminderTime: number;
  content: string;
  createTime: number;
  isRead: boolean;
}

interface ReminderState {
  reminderList: ReminderItem[];
}

const generateId = () => {
  return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

const useReminderStore = defineStore('reminder', {
  state: (): ReminderState => ({
    reminderList: [],
  }),
  getters: {
    // 获取未读提醒
    unreadReminders: (state): ReminderItem[] => {
      const now = Date.now();
      return state.reminderList.filter((item) => !item.isRead && item.reminderTime <= now);
    },
    // 获取所有提醒（按时间排序）
    sortedReminders: (state): ReminderItem[] => {
      return [...state.reminderList].sort((a, b) => b.createTime - a.createTime);
    },
    // 获取即将到期的提醒（未来24小时内）
    upcomingReminders: (state): ReminderItem[] => {
      const now = Date.now();
      const tomorrow = now + 24 * 60 * 60 * 1000;
      return state.reminderList.filter(
        (item) => !item.isRead && item.reminderTime > now && item.reminderTime <= tomorrow
      );
    },
    // 是否有未读提醒
    hasUnreadReminder(): boolean {
      return this.unreadReminders.length > 0;
    },
    // 获取指定客户的提醒
    getCustomerReminders: (state) => (customerId: string) => {
      return state.reminderList
        .filter((item) => item.customerId === customerId)
        .sort((a, b) => b.createTime - a.createTime);
    },
  },
  actions: {
    // 添加提醒
    addReminder(customerId: string, customerName: string, reminderTime: number, content: string) {
      const newReminder: ReminderItem = {
        id: generateId(),
        customerId,
        customerName,
        reminderTime,
        content,
        createTime: Date.now(),
        isRead: false,
      };
      this.reminderList.push(newReminder);
      this.saveToLocalStorage();
      return newReminder;
    },
    // 删除提醒
    deleteReminder(id: string) {
      const index = this.reminderList.findIndex((item) => item.id === id);
      if (index > -1) {
        this.reminderList.splice(index, 1);
        this.saveToLocalStorage();
      }
    },
    // 标记提醒为已读
    markAsRead(id: string) {
      const reminder = this.reminderList.find((item) => item.id === id);
      if (reminder) {
        reminder.isRead = true;
        this.saveToLocalStorage();
      }
    },
    // 标记所有提醒为已读
    markAllAsRead() {
      this.reminderList.forEach((item) => {
        item.isRead = true;
      });
      this.saveToLocalStorage();
    },
    // 从localStorage加载
    loadFromLocalStorage() {
      try {
        const stored = localStorage.getItem('cordys_crm_reminders');
        if (stored) {
          this.reminderList = JSON.parse(stored);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error('Failed to load reminders from localStorage:', error);
      }
    },
    // 保存到localStorage
    saveToLocalStorage() {
      try {
        localStorage.setItem('cordys_crm_reminders', JSON.stringify(this.reminderList));
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error('Failed to save reminders to localStorage:', error);
      }
    },
    // 清理已过期的已读提醒（超过7天的）
    cleanExpiredReminders() {
      const sevenDaysAgo = Date.now() - 7 * 24 * 60 * 60 * 1000;
      this.reminderList = this.reminderList.filter((item) => !item.isRead || item.reminderTime > sevenDaysAgo);
      this.saveToLocalStorage();
    },
  },
  persist: true,
});

export default useReminderStore;
