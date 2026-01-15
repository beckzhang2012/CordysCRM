<template>
  <n-notification-provider ref="notificationProviderRef">
    <div class="reminder-notification">
      <n-space>
        <n-button type="primary" @click="showReminders" :count="reminders.length">
          <template #icon>
            <n-icon :component="Bell" />
          </template>
          提醒
        </n-button>

        <n-dropdown
          trigger="click"
          @select="handleNotificationSelect"
          :options="notificationOptions"
          :render-trigger="() => null"
          ref="dropdownRef"
        />
      </n-space>
    </div>
  </n-notification-provider>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useOsTheme } from 'naive-ui';
import type { DropdownInst } from 'naive-ui';
import { Bell } from '@vicons/ionicons5';
import { getPendingReminders, markReminderAsRead, deleteReminder } from '@/api/modules/reminder';
import type { Reminder } from '@lib/shared/models/reminder';

const osThemeRef = useOsTheme();
const dropdownRef = ref<DropdownInst | null>(null);
const reminders = ref<Reminder[]>([]);
let checkInterval: number | null = null;

const notificationOptions = ref<{ key: string; label: string; reminder: Reminder }[]>([]);

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
  window.$message?.create({
    content: (
      <div style="max-width: 300px;">
        <div style="font-weight: bold; margin-bottom: 8px;">跟进提醒</div>
        <p><strong>${reminder.sourceName}</strong></p>
        <p>${reminder.remindContent}</p>
        <div style="color: #666; font-size: 12px; margin-top: 8px;">
          ${new Date(reminder.remindTime).toLocaleString()}
        </div>
      </div>
    
        <div style="max-width: 300px;">
          <p><strong>${reminder.sourceName}</strong></p>
          <p>${reminder.content}</p>
        </div>
      `,
      duration: 0,
      theme: osThemeRef.value,
    });
};

const showReminders = () => {
  if (dropdownRef.value) {
    dropdownRef.value.setShow(true);
  }
};

const updateNotificationOptions = () => {
  notificationOptions.value = reminders.value.map((reminder) => ({
    key: reminder.id || '',
    label: `
      <div style="width: 300px;">
        <div style="font-weight: bold;">${reminder.sourceName}</div>
        <div style="color: #666; font-size: 12px; margin-top: 4px;">
          ${new Date(reminder.remindTime).toLocaleString()}
        </div>
        <div style="margin-top: 4px;">${reminder.remindContent}</div>
      </div>
    `,
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