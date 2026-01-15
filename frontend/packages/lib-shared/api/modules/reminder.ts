import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AddReminderUrl,
  DeleteReminderUrl,
  GetReminderListUrl,
  GetReminderUrl,
  GetUnreadCountUrl,
  MarkAllAsReadUrl,
  MarkAsReadUrl,
} from '@lib/shared/api/requrls/reminder';
import type { CommonList } from '@lib/shared/models/common';
import type { Reminder, ReminderAddRequest, ReminderListResponse, ReminderTableParams } from '@lib/shared/models/reminder';

export default function useReminderApi(CDR: CordysAxios) {
  function getReminderList(data: ReminderTableParams) {
    return CDR.post<CommonList<ReminderListResponse>>({ url: GetReminderListUrl, data });
  }

  function getReminder(id: string) {
    return CDR.get<Reminder>({ url: `${GetReminderUrl}/${id}` });
  }

  function addReminder(data: ReminderAddRequest) {
    return CDR.post<Reminder>({ url: AddReminderUrl, data });
  }

  function deleteReminder(id: string) {
    return CDR.post({ url: `${DeleteReminderUrl}/${id}` });
  }

  function markAsRead(id: string) {
    return CDR.post({ url: `${MarkAsReadUrl}/${id}` });
  }

  function markAllAsRead() {
    return CDR.post({ url: MarkAllAsReadUrl });
  }

  function getUnreadCount() {
    return CDR.get<number>({ url: GetUnreadCountUrl });
  }

  return {
    getReminderList,
    getReminder,
    addReminder,
    deleteReminder,
    markAsRead,
    markAllAsRead,
    getUnreadCount,
  };
}
