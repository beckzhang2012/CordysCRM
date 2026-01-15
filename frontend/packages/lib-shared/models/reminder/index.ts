export interface Reminder {
  id?: string;
  sourceId: string;
  sourceName: string;
  remindTime: string;
  remindContent: string;
  isRead?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
