import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AddReminderUrl,
  GetReminderPageUrl,
  GetPendingRemindersUrl,
  CompleteReminderUrl,
  CancelReminderUrl,
  DeleteReminderUrl,
} from '@lib/shared/api/requrls/reminder';
import type { CommonList } from '@lib/shared/models/common';
import type { ReminderAddParams, ReminderItem, ReminderPageParams } from '@lib/shared/models/reminder';

export default function useReminderApi(CDR: CordysAxios) {
  function addReminder(data: ReminderAddParams) {
    return CDR.post<ReminderItem>({ url: AddReminderUrl, data });
  }

  function getReminderPage(data: ReminderPageParams) {
    return CDR.post<CommonList<ReminderItem>>({ url: GetReminderPageUrl, data });
  }

  function getPendingReminders() {
    return CDR.get<ReminderItem[]>({ url: GetPendingRemindersUrl });
  }

  function completeReminder(id: string) {
    return CDR.get({ url: `${CompleteReminderUrl}/${id}` });
  }

  function cancelReminder(id: string) {
    return CDR.get({ url: `${CancelReminderUrl}/${id}` });
  }

  function deleteReminder(id: string) {
    return CDR.get({ url: `${DeleteReminderUrl}/${id}` });
  }

  return {
    addReminder,
    getReminderPage,
    getPendingReminders,
    completeReminder,
    cancelReminder,
    deleteReminder,
  };
}