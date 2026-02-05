<template>
  <n-notification-provider ref="notificationProviderRef">
    <div class="reminder-notification">
      <n-space>
        <n-button type="primary" :count="reminders.length" @click="showReminders">
          <template #icon>
            <n-icon :component="Alarm" />
          </template>
          提醒
        </n-button>

        <n-dropdown
          ref="dropdownRef"
          trigger="click"
          :options="notificationOptions"
          :render-trigger="() => null"
          @select="handleNotificationSelect"
        />
      </n-space>
    </div>
  </n-notification-provider>
</template>

<script setup lang="ts">
  import { h, onMounted, onUnmounted, ref } from 'vue';
  import { useOsTheme } from 'naive-ui';
  import { Alarm } from '@vicons/ionicons5';

  import type { Reminder } from '@lib/shared/models/reminder';

  import { deleteReminder, getPendingReminders, markReminderAsRead } from '@/api/modules/reminder';

  const osThemeRef = useOsTheme();
  const dropdownRef = ref<HTMLElement | null>(null);
  const reminders = ref<Reminder[]>([]);
  let checkInterval: number | null = null;

  const notificationOptions = ref<{ key: string; label: () => any; reminder: Reminder }[]>([]);

  const checkReminders = async () => {
    try {
      const response = await getPendingReminders();
      const newReminders = response.data as Reminder[];

      // 显示新的提醒通知
      newReminders.forEach((reminder) => {
        if (!reminders.value.some((r) => r.id === reminder.id)) {
          showNotification(reminder);
        }
      });

      reminders.value = newReminders;
      updateNotificationOptions();
    } catch (error) {
      console.error('获取待处理提醒失败', error);
    }
  };

  const showNotification = (reminder: Reminder) => {
    const messageApi = window.$message;
    if (messageApi) {
      messageApi.create({
        content: h('div', { style: { maxWidth: '300px' } }, [
          h('div', { style: { fontWeight: 'bold', marginBottom: '8px' } }, '跟进提醒'),
          h('p', [h('strong', reminder.sourceName)]),
          h('p', reminder.remindContent),
          h('div', { style: { color: '#666', fontSize: '12px', marginTop: '8px' } },
            new Date(reminder.remindTime).toLocaleString()),
        ]),
        duration: 0,
        theme: osThemeRef.value,
      });
    }
  };

  const showReminders = () => {
    if (dropdownRef.value) {
      (dropdownRef.value as any).setShow(true);
    }
  };

  const updateNotificationOptions = () => {
    notificationOptions.value = reminders.value.map((reminder) => ({
      key: reminder.id || '',
      label: () =>
        h('div', { style: { width: '300px' } }, [
          h('div', { style: { fontWeight: 'bold' } }, reminder.sourceName),
          h(
            'div',
            { style: { color: '#666', fontSize: '12px', marginTop: '4px' } },
            new Date(reminder.remindTime).toLocaleString()
          ),
          h('div', { style: { marginTop: '4px' } }, reminder.remindContent),
        ]),
      reminder,
    }));
  };

  const handleNotificationSelect = async (key: string) => {
    const reminder = reminders.value.find((r) => r.id === key);
    if (!reminder) return;

    try {
      await markReminderAsRead(key);
      await checkReminders();
    } catch (error) {
      console.error('处理提醒失败', error);
    }
  };

  onMounted(() => {
    // 立即检查一次
    checkReminders();

    // 每隔1分钟检查一次
    checkInterval = window.setInterval(checkReminders, 60000);
  });

  onUnmounted(() => {
    if (checkInterval) {
      clearInterval(checkInterval);
    }
  });
</script>

<style scoped>
  .reminder-notification {
    display: flex;
    align-items: center;
  }
</style>
