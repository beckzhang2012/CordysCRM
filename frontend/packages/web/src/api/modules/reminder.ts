import type { Reminder } from '@lib/shared/models/reminder';

import { Axios } from '@lib/shared/api/http';

const baseUrl = '/crm/reminder';

// 新增提醒
export const addReminder = async (params: Reminder) => {
  return await Axios.post(`${baseUrl}/add`, params);
};

// 获取提醒列表
export const getReminderList = async (params: any) => {
  return await Axios.get(`${baseUrl}/list`, { params });
};

// 删除提醒
export const deleteReminder = async (id: string) => {
  return await Axios.delete(`${baseUrl}/delete/${id}`);
};

// 获取待处理提醒
export const getPendingReminders = async () => {
  return await Axios.get(`${baseUrl}/pending`);
};

// 标记提醒为已读
export const markReminderAsRead = async (id: string) => {
  return await Axios.post(`${baseUrl}/mark-read/${id}`);
};
