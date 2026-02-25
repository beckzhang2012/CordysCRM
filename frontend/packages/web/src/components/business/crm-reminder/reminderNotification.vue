<template>
  <div class="reminder-notification-container">
    <n-popover
      v-model:show="showPopover"
      trigger="click"
      placement="bottom-end"
      :show-arrow="false"
      content-class="reminder-popover"
    >
      <template #trigger>
        <n-button class="p-[8px]" quaternary>
          <n-badge :value="unreadCount" :show="unreadCount > 0">
            <CrmIcon type="iconicon-alarmclock" :size="16" />
          </n-badge>
        </n-button>
      </template>
      <div class="w-[360px]">
        <div class="flex items-center justify-between border-b border-[var(--text-n8)] px-[16px] py-[12px]">
          <span class="font-medium text-[var(--text-n1)]">{{ t('customer.reminder.reminderList') }}</span>
          <n-button v-if="unreadReminders.length > 0" text type="primary" size="small" @click="handleMarkAllRead">
            {{ t('customer.reminder.markAllRead') }}
          </n-button>
        </div>
        <div class="max-h-[400px] overflow-y-auto">
          <div v-if="unreadReminders.length === 0" class="flex flex-col items-center justify-center py-[32px]">
            <n-empty :description="t('customer.reminder.noReminder')" />
          </div>
          <div
            v-for="item in unreadReminders"
            v-else
            :key="item.id"
            class="group relative border-b border-[var(--text-n8)] px-[16px] py-[12px] hover:bg-[var(--text-n9)]"
          >
            <div class="mb-[8px] flex items-center justify-between">
              <span class="font-medium text-[var(--text-n1)]">{{ item.customerName }}</span>
              <span class="text-[12px] text-[var(--text-n3)]">{{ formatTime(item.remindTime) }}</span>
            </div>
            <div class="mb-[8px] line-clamp-2 text-[14px] text-[var(--text-n2)]">{{ item.content }}</div>
            <div class="flex items-center justify-end gap-[8px]">
              <n-button text size="tiny" @click="handleViewCustomer(item.customerId)">
                {{ t('common.view') }}
              </n-button>
              <n-button text size="tiny" type="primary" @click="handleMarkRead(item.id)">
                {{ t('common.markAsRead') }}
              </n-button>
              <n-button text size="tiny" type="error" @click="handleDelete(item.id)">
                {{ t('common.delete') }}
              </n-button>
            </div>
          </div>
        </div>
        <div v-if="unreadReminders.length > 0" class="border-t border-[var(--text-n8)] px-[16px] py-[12px] text-center">
          <n-button text type="primary" @click="handleViewAll">
            {{ t('customer.reminder.viewAll') }}
          </n-button>
        </div>
      </div>
    </n-popover>

    <!-- 提醒列表抽屉 -->
    <n-drawer v-model:show="showDrawer" :width="600" :title="t('customer.reminder.allReminders')">
      <n-drawer-content>
        <div v-if="allReminders.length === 0" class="flex flex-col items-center justify-center py-[64px]">
          <n-empty :description="t('customer.reminder.noReminder')" />
        </div>
        <div v-else class="flex flex-col gap-[12px]">
          <div
            v-for="item in allReminders"
            :key="item.id"
            class="rounded-[8px] border border-[var(--text-n8)] p-[16px]"
            :class="{ 'bg-[var(--primary-1)]': !item.isRead && item.remindTime <= Date.now() }"
          >
            <div class="mb-[8px] flex items-center justify-between">
              <div class="flex items-center gap-[8px]">
                <span class="font-medium text-[var(--text-n1)]">{{ item.customerName }}</span>
                <n-tag v-if="!item.isRead && item.remindTime <= Date.now()" size="small" type="warning">
                  {{ t('customer.reminder.pending') }}
                </n-tag>
                <n-tag v-else-if="item.isRead" size="small" type="success">
                  {{ t('customer.reminder.read') }}
                </n-tag>
              </div>
              <span class="text-[12px] text-[var(--text-n3)]">{{ formatTime(item.remindTime) }}</span>
            </div>
            <div class="mb-[12px] text-[14px] text-[var(--text-n2)]">{{ item.content }}</div>
            <div class="flex items-center justify-end gap-[12px]">
              <n-button text size="small" @click="handleViewCustomer(item.customerId)">
                {{ t('common.view') }}
              </n-button>
              <n-button
                v-if="!item.isRead && item.remindTime <= Date.now()"
                text
                size="small"
                type="primary"
                @click="handleMarkRead(item.id)"
              >
                {{ t('common.markAsRead') }}
              </n-button>
              <n-button text size="small" type="error" @click="handleDelete(item.id)">
                {{ t('common.delete') }}
              </n-button>
            </div>
          </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import { NBadge, NButton, NDrawer, NDrawerContent, NEmpty, NPopover, NTag, useDialog, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';

  import useReminderStore from '@/store/modules/reminder';

  const { t } = useI18n();
  const message = useMessage();
  const dialog = useDialog();
  const router = useRouter();
  const reminderStore = useReminderStore();

  const showPopover = ref(false);
  const showDrawer = ref(false);

  const unreadCount = computed(() => reminderStore.getUnreadCount);
  const unreadReminders = computed(() => reminderStore.getUnreadReminders);
  const allReminders = computed(() => reminderStore.reminderList);

  function formatTime(timestamp: number): string {
    const date = new Date(timestamp);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    if (isToday) {
      return `${t('customer.reminder.today')} ${date.getHours().toString().padStart(2, '0')}:${date
        .getMinutes()
        .toString()
        .padStart(2, '0')}`;
    }

    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date
      .getDate()
      .toString()
      .padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date
      .getMinutes()
      .toString()
      .padStart(2, '0')}`;
  }

  function handleMarkRead(id: string) {
    reminderStore.markAsRead(id);
    message.success(t('customer.reminder.markReadSuccess'));
  }

  function handleMarkAllRead() {
    reminderStore.markAllAsRead();
    message.success(t('customer.reminder.markAllReadSuccess'));
  }

  function handleDelete(id: string) {
    dialog.warning({
      title: t('common.confirmDelete'),
      content: t('customer.reminder.deleteConfirm'),
      positiveText: t('common.confirm'),
      negativeText: t('common.cancel'),
      onPositiveClick: () => {
        reminderStore.deleteReminder(id);
        message.success(t('common.deleteSuccess'));
      },
    });
  }

  function handleViewCustomer(customerId: string) {
    showPopover.value = false;
    showDrawer.value = false;
    router.push({
      name: 'customer',
      query: { customerId },
    });
  }

  function handleViewAll() {
    showPopover.value = false;
    showDrawer.value = true;
  }

  // 定时检查提醒
  let checkInterval: ReturnType<typeof setInterval> | null = null;

  function startReminderCheck() {
    checkInterval = setInterval(() => {
      const count = reminderStore.getUnreadCount;
      if (count > 0) {
        // 触发重新计算，让徽章显示最新数量
      }
    }, 60000); // 每分钟检查一次
  }

  function stopReminderCheck() {
    if (checkInterval) {
      clearInterval(checkInterval);
      checkInterval = null;
    }
  }

  onMounted(() => {
    reminderStore.initReminderList();
    reminderStore.clearExpiredReminders();
    startReminderCheck();
  });

  onUnmounted(() => {
    stopReminderCheck();
  });
</script>

<style scoped>
  .reminder-notification-container {
    display: inline-flex;
  }
  :deep(.reminder-popover) {
    padding: 0 !important;
  }
  .line-clamp-2 {
    display: box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
</style>
