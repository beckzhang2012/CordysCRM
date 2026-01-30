import type { TableQueryParams } from './common';

export interface ReminderListItem {
  id: string;
  customerId: string;
  customerName: string;
  reminderTime: number;
  content: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  receiver: string;
  createTime: number;
  updateTime: number;
}

export interface ReminderAddParams {
  customerId: string;
  customerName: string;
  reminderTime: number;
  content: string;
  receiver: string;
}

export interface ReminderTableParams extends TableQueryParams {
  customerId?: string;
  status?: string;
}
