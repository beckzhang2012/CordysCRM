export interface Reminder {
  id: string;
  customerId: string;
  customerName: string;
  reminderTime: string;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface ReminderFormData {
  reminderTime: string;
  content: string;
}
