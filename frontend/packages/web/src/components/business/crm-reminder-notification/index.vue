<template>
  <div class="reminder-notification-wrapper">
    <!-- 提醒图标按钮 -->
    <n-popover
      v-model:show="showPopover"
      trigger="click"
      placement="bottom-end"
      :show-arrow="false"
      content-class="reminder-popover-content"
    >
      <template #trigger>
        <n-button class="p-[8px]" quaternary @click="handleIconClick">
          <n-badge :value="unreadCount" :max="99" :show="unreadCount > 0">
            <CrmIcon type="iconicon-alarmclock" :size="16" />
          </n-badge>
        </n-button>
      </template>

      <div class="w-[360px]">
        <!-- 头部 -->
        <div class="flex items-center justify-between border-b border-[var(--border-1)] p-[12px_16px]">
          <div class="flex items-center gap-[8px]">
            <span class="text-[16px] font-medium">{{ t('customer.reminder.reminderList') }}</span>
            <n-tag v-if="unreadCount > 0" type="error" size="small">{{ unreadCount }}</n-tag>
          </div>
          <n-button v-if="reminderList.length > 0" text type="primary" size="small" @click="handleMarkAllRead">
            {{ t('customer.reminder.markAllRead') }}
          </n-button>
        </div>

        <!-- 提醒列表 -->
        <div class="max-h-[400px] overflow-y-auto">
          <div v-if="reminderList.length === 0" class="flex flex-col items-center justify-center py-[40px]">
            <CrmIcon type="iconicon-alarmclock" :size="48" class="text-[var(--text-n4)]" />
            <div class="mt-[12px] text-[14px] text-[var(--text-n3)]">{{ t('customer.reminder.noReminder') }}</div>
          </div>

          <div
            v-for="item in reminderList"
            :key="item.id"
            :class="[
              'group relative border-b border-[var(--border-1)] p-[12px_16px] hover:bg-[var(--fill-1)]',
              item.isRead ? 'opacity-60' : '',
            ]"
          >
            <!-- 未读标记 -->
            <div
              v-if="!item.isRead"
              class="absolute left-[4px] top-[50%] h-[8px] w-[8px] translate-y-[-50%] rounded-full bg-[var(--primary-6)]"
            />

            <div class="ml-[8px]">
              <!-- 客户名称和时间 -->
              <div class="mb-[8px] flex items-center justify-between">
                <span class="max-w-[180px] truncate text-[14px] font-medium text-[var(--text-1)]">
                  {{ item.customerName }}
                </span>
                <span class="text-[12px] text-[var(--text-n3)]">{{ formatTime(item.reminderTime) }}</span>
              </div>

              <!-- 提醒内容 -->
              <div class="mb-[8px] line-clamp-2 text-[13px] text-[var(--text-2)]">{{ item.content }}</div>

              <!-- 操作按钮 -->
              <div class="flex items-center justify-end gap-[8px]">
                <n-button v-if="!item.isRead" text type="primary" size="tiny" @click="handleMarkRead(item.id)">
                  {{ t('customer.reminder.markRead') }}
                </n-button>
                <n-popconfirm @positive-click="handleDelete(item.id)">
                  <template #trigger>
                    <n-button text type="error" size="tiny">
                      {{ t('common.delete') }}
                    </n-button>
                  </template>
                  {{ t('customer.reminder.deleteConfirm') }}
                </n-popconfirm>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部 -->
        <div v-if="reminderList.length > 0" class="border-t border-[var(--border-1)] p-[12px_16px]">
          <n-button text type="primary" block @click="handleViewAll">
            {{ t('customer.reminder.viewAll') }}
          </n-button>
        </div>
      </div>
    </n-popover>

    <!-- 提醒抽屉 -->
    <ReminderDrawer v-model:show="showDrawer" />
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, onUnmounted, ref } from 'vue';
  import { NBadge, NButton, NPopconfirm, NPopover, NTag, useNotification } from 'naive-ui';
  import dayjs from 'dayjs';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import ReminderDrawer from './reminderDrawer.vue';

  import useReminderStore from '@/store/modules/reminder';

  const { t } = useI18n();
  const reminderStore = useReminderStore();
  const notification = useNotification();

  const showPopover = ref(false);
  const showDrawer = ref(false);
  let checkInterval: ReturnType<typeof setInterval> | null = null;

  // 提醒列表（按时间排序，未读在前）
  const reminderList = computed(() => {
    return [...reminderStore.sortedReminders].sort((a, b) => {
      // 未读排在前面
      if (a.isRead !== b.isRead) {
        return a.isRead ? 1 : -1;
      }
      // 按提醒时间排序
      return b.reminderTime - a.reminderTime;
    });
  });

  // 未读数量
  const unreadCount = computed(() => reminderStore.unreadReminders.length);

  // 格式化时间
  const formatTime = (timestamp: number) => {
    const now = dayjs();
    const time = dayjs(timestamp);
    const diff = now.diff(time, 'minute');

    if (diff < 1) {
      return t('common.justNow');
    }
    if (diff < 60) {
      return t('common.minutesAgo', { n: diff });
    }
    if (diff < 24 * 60) {
      return t('common.hoursAgo', { n: Math.floor(diff / 60) });
    }
    if (now.isSame(time, 'year')) {
      return time.format('MM-DD HH:mm');
    }
    return time.format('YYYY-MM-DD HH:mm');
  };

  // 标记已读
  const handleMarkRead = (id: string) => {
    reminderStore.markAsRead(id);
  };

  // 标记全部已读
  const handleMarkAllRead = () => {
    reminderStore.markAllAsRead();
  };

  // 删除提醒
  const handleDelete = (id: string) => {
    reminderStore.deleteReminder(id);
  };

  // 查看全部
  const handleViewAll = () => {
    showPopover.value = false;
    showDrawer.value = true;
  };

  // 图标点击
  const handleIconClick = () => {
    // 加载本地存储的提醒
    reminderStore.loadFromLocalStorage();
  };

  // 检查并显示桌面通知
  const checkAndNotify = () => {
    const { unreadReminders } = reminderStore;
    unreadReminders.forEach((reminder) => {
      // 检查是否已经显示过通知（通过检查提醒时间是否在1分钟内）
      const now = Date.now();
      const diff = now - reminder.reminderTime;
      if (diff >= 0 && diff < 60000) {
        notification.info({
          title: t('customer.reminder.reminderTitle'),
          content: `${reminder.customerName}: ${reminder.content}`,
          duration: 5000,
          onClose: () => {
            reminderStore.markAsRead(reminder.id);
          },
        });
      }
    });
  };

  onMounted(() => {
    // 加载本地存储的提醒
    reminderStore.loadFromLocalStorage();
    // 清理过期提醒
    reminderStore.cleanExpiredReminders();
    // 每分钟检查一次提醒
    checkInterval = setInterval(checkAndNotify, 60000);
  });

  onUnmounted(() => {
    if (checkInterval) {
      clearInterval(checkInterval);
    }
  });
</script>

<style scoped>
  .reminder-notification-wrapper {
    display: inline-flex;
  }
  :deep(.reminder-popover-content) {
    padding: 0 !important;
  }
  .line-clamp-2 {
    display: box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
</style>
