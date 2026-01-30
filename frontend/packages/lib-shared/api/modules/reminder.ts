import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  AddReminderUrl,
  GetReminderListUrl,
  DeleteReminderUrl,
  CancelReminderUrl,
  GetPendingReminderCountUrl,
  ProcessReminderUrl,
} from '@lib/shared/api/requrls/reminder';
import type { CommonList } from '@lib/shared/models/common';
import type { ReminderListItem, ReminderAddParams, ReminderTableParams } from '@lib/shared/models/reminder';

export default function useReminderApi(CDR: CordysAxios) {
  function addReminder(data: ReminderAddParams) {
    return CDR.post({ url: AddReminderUrl, data });
  }

  function getReminderList(data: ReminderTableParams) {
    return CDR.post<CommonList<ReminderListItem>>({ url: GetReminderListUrl, data });
  }

  function deleteReminder(id: string) {
    return CDR.delete({ url: `${DeleteReminderUrl}/${id}` });
  }

  function cancelReminder(id: string) {
    return CDR.put({ url: `${CancelReminderUrl}/${id}` });
  }

  function getPendingReminderCount() {
    return CDR.get<number>({ url: GetPendingReminderCountUrl });
  }

  function processReminder() {
    return CDR.post({ url: ProcessReminderUrl });
  }

  return {
    addReminder,
    getReminderList,
    deleteReminder,
    cancelReminder,
    getPendingReminderCount,
    processReminder,
  };
}
