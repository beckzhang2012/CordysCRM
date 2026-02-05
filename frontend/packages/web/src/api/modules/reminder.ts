import type { Reminder } from '@lib/shared/models/reminder';

import createAxios from '@lib/shared/api/http';

const Axios = createAxios({});

const baseUrl = '/crm/reminder';

// 新增提醒
export const addReminder = async (params: Reminder) => {
  return await Axios.post({ url: `${baseUrl}/add`, data: params });
};

// 获取提醒列表
export const getReminderList = async (params: any) => {
  return await Axios.get({ url: `${baseUrl}/list`, params });
};

// 删除提醒
export const deleteReminder = async (id: string) => {
  return await Axios.delete({ url: `${baseUrl}/delete/${id}` });
};

// 获取待处理提醒
export const getPendingReminders = async () => {
  return await Axios.get({ url: `${baseUrl}/pending` });
};

// 标记提醒为已读
export const markReminderAsRead = async (id: string) => {
  return await Axios.post({ url: `${baseUrl}/mark-read/${id}` });
};
