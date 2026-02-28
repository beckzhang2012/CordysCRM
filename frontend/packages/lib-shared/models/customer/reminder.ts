import type { TableQueryParams } from '../common';

export enum CustomerReminderStatusEnum {
  PENDING = 0,
  REMINDED = 1,
  CLOSED = 2
}

export interface SaveCustomerReminderParams {
  customerId: string;
  customerName: string;
  reminderTime: number;
  content: string;
}

export interface UpdateCustomerReminderParams {
  id: string;
  status: CustomerReminderStatusEnum;
}

export interface CustomerReminderTableParams extends TableQueryParams {
  customerId?: string;
  status?: CustomerReminderStatusEnum;
}

export interface CustomerReminderListItem {
  id: string;
  customerId: string;
  customerName: string;
  userId: string;
  userName: string;
  reminderTime: number;
  content: string;
  status: CustomerReminderStatusEnum;
  createTime: number;
  updateTime: number;
  createdBy: string;
  updatedBy: string;
}

export interface CustomerReminderCountParams {
  userId?: string;
}

export interface CustomerReminderCountResponse {
  count: number;
}
