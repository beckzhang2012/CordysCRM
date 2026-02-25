import type { TableQueryParams } from './common';

export enum ReminderBusinessType {
  CUSTOMER = 'CUSTOMER',
  OPPORTUNITY = 'OPPORTUNITY',
  CLUE = 'CLUE',
}

export enum ReminderStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface ReminderItem {
  id: string;
  businessType: ReminderBusinessType;
  businessId: string;
  remindTime: number;
  content?: string;
  status: ReminderStatus;
  organizationId: string;
  owner?: string;
  createTime: number;
  updateTime: number;
  createUser: string;
  updateUser: string;
}

export interface ReminderAddParams {
  businessType: ReminderBusinessType;
  businessId: string;
  remindTime: number;
  content?: string;
}

export interface ReminderPageParams extends TableQueryParams {
  businessType?: ReminderBusinessType;
  businessId?: string;
  status?: ReminderStatus;
}