import type { TableQueryParams } from './common';

export interface Reminder {
  id: string;
  customerId: string;
  customerName: string;
  reminderTime: number;
  content: string;
  isRead: boolean;
  organizationId: string;
  creatorId: string;
  creatorName: string;
  createTime: number;
  updateTime: number;
}

export interface ReminderListResponse {
  id: string;
  customerId: string;
  customerName: string;
  reminderTime: number;
  content: string;
  isRead: boolean;
  creatorName: string;
  createTime: number;
}

export interface ReminderTableParams extends TableQueryParams {
  customerId?: string;
}

export interface ReminderAddRequest {
  customerId: string;
  reminderTime: number;
  content: string;
}
