<template>
  <div class="reminder-notification">
    <van-badge :content="unreadCount" :show="unreadCount > 0" :offset="[0, 0]" max="99">
      <div class="notification-icon" @click="showReminderList = true">
        <van-icon name="bell" size="20" color="var(--text-n1)" />
      </div>
    </van-badge>

    <van-popup v-model:show="showReminderList" position="top" :style="{ top: '60px', right: '16px', width: '320px' }">
      <div class="reminder-list">
        <div class="list-header">
          <div class="header-title">{{ t('reminder.reminderList') }}</div>
          <div class="header-actions">
            <van-button size="mini" type="primary" plain @click="handleMarkAllRead">
              {{ t('reminder.markAllRead') }}
            </van-button>
          </div>
        </div>

        <div class="list-content">
          <van-empty v-if="reminders.length === 0" :description="t('reminder.noReminder')" />

          <van-list v-else>
            <div v-for="reminder in reminders" :key="reminder.id" class="reminder-item">
              <div class="item-content" :class="{ unread: !reminder.isRead }" @click="handleItemClick(reminder)">
                <div class="item-header">
                  <div class="customer-name">{{ reminder.customerName }}</div>
                  <div class="reminder-time">{{ formatTime(reminder.reminderTime) }}</div>
                </div>
                <div class="item-body">{{ reminder.content }}</div>
              </div>
              <van-icon name="cross" size="16" color="var(--text-n3)" @click="handleDelete(reminder.id)" />
            </div>
          </van-list>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@lib/shared/hooks/useI18n';

  import useReminderStore from '@/store/modules/reminder';

  import type { ReminderListResponse } from '@lib/shared/models/reminder';

  const { t } = useI18n();
  const router = useRouter();
  const reminderStore = useReminderStore();

  const showReminderList = ref(false);

  const reminders = computed(() => {
    return [...reminderStore.unreadReminders, ...reminderStore.readReminders].sort((a, b) => {
      return b.reminderTime - a.reminderTime;
    });
  });

  const unreadCount = computed(() => reminderStore.unreadReminders.length);

  function formatTime(time: number) {
    const date = new Date(time);
    const now = new Date();
    const diff = date.getTime() - now.getTime();

    if (diff < 0) {
      const minutes = Math.floor(Math.abs(diff) / (1000 * 60));
      if (minutes < 60) {
        return `${minutes}${t('common.dayUnit')}${t('reminder.ago')}`;
      }
      const hours = Math.floor(minutes / 60);
      if (hours < 24) {
        return `${hours}${t('reminder.hoursAgo')}`;
      }
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hour}:${minute}`;
  }

  function handleItemClick(reminder: ReminderListResponse) {
    if (!reminder.isRead) {
      reminderStore.markAsRead(reminder.id);
    }
    showReminderList.value = false;
    router.push({
      name: 'customerDetail',
      query: { id: reminder.customerId, name: reminder.customerName },
    });
  }

  function handleDelete(id: string) {
    reminderStore.deleteReminder(id);
  }

  function handleMarkAllRead() {
    reminderStore.markAllAsRead();
  }
</script>

<style lang="less" scoped>
  .reminder-notification {
    position: fixed;
    top: 16px;
    right: 16px;
    z-index: 1000;
  }

  .notification-icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--text-n9);
    border-radius: 50%;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .reminder-list {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    max-height: 400px;
    display: flex;
    flex-direction: column;
  }

  .list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid var(--text-n8);

    .header-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--text-n1);
    }
  }

  .list-content {
    flex: 1;
    overflow-y: auto;
    max-height: 340px;
  }

  .reminder-item {
    display: flex;
    align-items: flex-start;
    padding: 12px 16px;
    border-bottom: 1px solid var(--text-n8);

    &:last-child {
      border-bottom: none;
    }
  }

  .item-content {
    flex: 1;
    cursor: pointer;

    &.unread {
      .item-header {
        .customer-name {
          font-weight: 500;
        }
      }

      &::before {
        content: '';
        display: inline-block;
        width: 8px;
        height: 8px;
        background-color: var(--primary-6);
        border-radius: 50%;
        margin-right: 8px;
      }
    }
  }

  .item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
  }

  .customer-name {
    font-size: 14px;
    color: var(--text-n1);
  }

  .reminder-time {
    font-size: 12px;
    color: var(--text-n3);
  }

  .item-body {
    font-size: 13px;
    color: var(--text-n2);
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
</style>
