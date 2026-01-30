interface ReminderItem {
  id: string;
  customerId: string;
  customerName: string;
  reminderTime: number;
  reminderContent: string;
  createdAt: number;
  isActive: boolean;
}

class ReminderService {
  private storageKey = 'customerReminders';
  private checkInterval: NodeJS.Timeout | null = null;
  private listeners: Array<(reminders: ReminderItem[]) => void> = [];

  // 获取所有提醒
  getReminders(): ReminderItem[] {
    try {
      const reminders = localStorage.getItem(this.storageKey);
      return reminders ? JSON.parse(reminders) : [];
    } catch {
      return [];
    }
  }

  // 添加提醒
  addReminder(reminder: Omit<ReminderItem, 'id' | 'createdAt'>): ReminderItem {
    const newReminder: ReminderItem = {
      ...reminder,
      id: `reminder_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      createdAt: Date.now(),
    };

    const reminders = this.getReminders();
    reminders.push(newReminder);
    this.saveReminders(reminders);
    this.notifyListeners();
    return newReminder;
  }

  // 更新提醒
  updateReminder(id: string, updates: Partial<ReminderItem>): boolean {
    const reminders = this.getReminders();
    const index = reminders.findIndex(r => r.id === id);
    
    if (index !== -1) {
      reminders[index] = { ...reminders[index], ...updates };
      this.saveReminders(reminders);
      this.notifyListeners();
      return true;
    }
    return false;
  }

  // 删除提醒
  deleteReminder(id: string): boolean {
    const reminders = this.getReminders();
    const filteredReminders = reminders.filter(r => r.id !== id);
    
    if (filteredReminders.length !== reminders.length) {
      this.saveReminders(filteredReminders);
      this.notifyListeners();
      return true;
    }
    return false;
  }

  // 获取特定客户的提醒
  getCustomerReminders(customerId: string): ReminderItem[] {
    return this.getReminders().filter(r => r.customerId === customerId);
  }

  // 获取即将到期的提醒（未来5分钟内）
  getUpcomingReminders(): ReminderItem[] {
    const now = Date.now();
    const fiveMinutesLater = now + 5 * 60 * 1000;
    
    return this.getReminders().filter(r => 
      r.isActive && 
      r.reminderTime >= now && 
      r.reminderTime <= fiveMinutesLater
    );
  }

  // 获取已过期但未处理的提醒
  getExpiredReminders(): ReminderItem[] {
    const now = Date.now();
    
    return this.getReminders().filter(r => 
      r.isActive && 
      r.reminderTime < now
    );
  }

  // 标记提醒为已处理
  markAsProcessed(id: string): boolean {
    return this.updateReminder(id, { isActive: false });
  }

  // 保存提醒到本地存储
  private saveReminders(reminders: ReminderItem[]): void {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(reminders));
    } catch {
      // 忽略存储写入失败
    }
  }

  // 通知监听器
  private notifyListeners(): void {
    const reminders = this.getReminders();
    this.listeners.forEach(listener => listener(reminders));
  }

  // 添加监听器
  addListener(listener: (reminders: ReminderItem[]) => void): void {
    this.listeners.push(listener);
  }

  // 移除监听器
  removeListener(listener: (reminders: ReminderItem[]) => void): void {
    const index = this.listeners.indexOf(listener);
    if (index !== -1) {
      this.listeners.splice(index, 1);
    }
  }

  // 开始检查提醒（每分钟检查一次）
  startChecking(): void {
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
    }
    
    this.checkInterval = setInterval(() => {
      this.checkReminders();
    }, 60 * 1000); // 每分钟检查一次
    
    // 立即检查一次
    this.checkReminders();
  }

  // 停止检查提醒
  stopChecking(): void {
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
      this.checkInterval = null;
    }
  }

  // 检查提醒并触发通知
  private checkReminders(): void {
    const expiredReminders = this.getExpiredReminders();
    
    if (expiredReminders.length > 0) {
      // 标记为已处理
      expiredReminders.forEach(reminder => {
        this.markAsProcessed(reminder.id);
      });
      
      // 触发通知
      this.triggerNotifications(expiredReminders);
    }
  }

  // 触发通知
  private triggerNotifications(reminders: ReminderItem[]): void {
    // 这里可以集成项目中的通知系统
    // 目前先使用浏览器通知
    if (Notification.permission === 'granted') {
      reminders.forEach(reminder => {
        // eslint-disable-next-line no-new -- 浏览器通知仅需触发显示，无需持有引用
        new Notification('客户跟进提醒', {
          body: `${reminder.customerName}: ${reminder.reminderContent}`,
          icon: '/favicon.ico'
        });
      });
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          reminders.forEach(reminder => {
            // eslint-disable-next-line no-new -- 浏览器通知仅需触发显示，无需持有引用
            new Notification('客户跟进提醒', {
              body: `${reminder.customerName}: ${reminder.reminderContent}`,
              icon: '/favicon.ico'
            });
          });
        }
      });
    }
    
    // 也可以通过事件总线发送通知
    window.dispatchEvent(new CustomEvent('customer-reminder', {
      detail: { reminders }
    }));
  }

  // 清理过期超过7天的已处理提醒
  cleanupOldReminders(): void {
    const now = Date.now();
    const sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;
    
    const reminders = this.getReminders();
    const filteredReminders = reminders.filter(r => 
      r.isActive || r.createdAt > sevenDaysAgo
    );
    
    if (filteredReminders.length !== reminders.length) {
      this.saveReminders(filteredReminders);
      this.notifyListeners();
    }
  }
}

export const reminderService = new ReminderService();
export type { ReminderItem };