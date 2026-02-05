export enum ReminderStatus {
  UNREAD = 'UNREAD',
  READ = 'READ'
}

export interface Reminder {
  id?: string;
  sourceId: string;
  sourceName: string;
  remindTime: string;
  remindContent: string;
  isRead?: boolean;
  status?: ReminderStatus;
  createdAt?: string;
  updatedAt?: string;
}
