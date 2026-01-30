<template>
  <div class="reminder-notification-container">
    <n-badge :value="activeRemindersCount" :max="99" :show="activeRemindersCount > 0">
      <n-button quaternary circle @click="showReminderDrawer = true">
        <template #icon>
          <CrmIcon type="iconicon-alarmclock" :size="16" />
        </template>
      </n-button>
    </n-badge>
    
    <!-- 提醒列表抽屉 -->
    <n-drawer v-model:show="showReminderDrawer" :width="400" placement="right">
      <n-drawer-content :title="t('customer.reminderList')" closable>
        <div class="reminder-list">
          <div v-if="reminders.length === 0" class="empty-reminder">
            <n-empty :description="t('customer.noReminders')" />
          </div>
          <div v-else>
            <n-tabs v-model:value="activeTab" type="line">
              <n-tab-pane name="upcoming" :tab="t('customer.upcomingReminders')">
                <div v-if="upcomingReminders.length === 0" class="empty-reminder">
                  <n-empty :description="t('customer.noUpcomingReminders')" size="small" />
                </div>
                <div v-else class="reminder-items">
                  <div
                    v-for="reminder in upcomingReminders"
                    :key="reminder.id"
                    class="reminder-item"
                  >
                    <div class="reminder-header">
                      <span class="customer-name">{{ reminder.customerName }}</span>
                      <n-button size="tiny" quaternary @click="deleteReminder(reminder.id)">
                        <template #icon>
                          <CrmIcon type="iconicon_delete" :size="14" />
                        </template>
                      </n-button>
                    </div>
                    <div class="reminder-content">{{ reminder.reminderContent }}</div>
                    <div class="reminder-time">
                      <CrmIcon type="iconicon_time" :size="12" />
                      {{ formatDateTime(reminder.reminderTime) }}
                    </div>
                  </div>
                </div>
              </n-tab-pane>
              <n-tab-pane name="all" :tab="t('customer.allReminders')">
                <div v-if="reminders.length === 0" class="empty-reminder">
                  <n-empty :description="t('customer.noReminders')" size="small" />
                </div>
                <div v-else class="reminder-items">
                  <div
                    v-for="reminder in reminders"
                    :key="reminder.id"
                    class="reminder-item"
                    :class="{ 'reminder-item-inactive': !reminder.isActive }"
                  >
                    <div class="reminder-header">
                      <span class="customer-name">{{ reminder.customerName }}</span>
                      <div class="reminder-actions">
                        <n-button
                          v-if="reminder.isActive"
                          size="tiny"
                          quaternary
                          @click="markAsProcessed(reminder.id)"
                        >
                          {{ t('customer.markAsProcessed') }}
                        </n-button>
                        <n-button size="tiny" quaternary @click="deleteReminder(reminder.id)">
                          <template #icon>
                            <CrmIcon type="iconicon_delete" :size="14" />
                          </template>
                        </n-button>
                      </div>
                    </div>
                    <div class="reminder-content">{{ reminder.reminderContent }}</div>
                    <div class="reminder-time">
                      <CrmIcon type="iconicon_time" :size="12" />
                      {{ formatDateTime(reminder.reminderTime) }}
                      <n-tag v-if="!reminder.isActive" size="tiny" type="success">
                        {{ t('customer.processed') }}
                      </n-tag>
                      <n-tag v-else-if="isExpired(reminder.reminderTime)" size="tiny" type="warning">
                        {{ t('customer.expired') }}
                      </n-tag>
                    </div>
                  </div>
                </div>
              </n-tab-pane>
            </n-tabs>
          </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
  import { NBadge, NButton, NDrawer, NDrawerContent, NEmpty, NTabPane, NTabs, NTag, useMessage } from 'naive-ui';
  import { computed, onMounted, onUnmounted, ref } from 'vue';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { ReminderItem } from '@/utils/reminderService';
  import { reminderService } from '@/utils/reminderService';
  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';

  const { t } = useI18n();
  const Message = useMessage();

  const showReminderDrawer = ref(false);
  const activeTab = ref('upcoming');
  const reminders = ref<ReminderItem[]>([]);

  // 活跃提醒数量
  const activeRemindersCount = computed(() => {
    return reminders.value.filter(r => r.isActive).length;
  });

  // 即将到来的提醒
  const upcomingReminders = computed(() => {
    const now = Date.now();
    return reminders.value
      .filter(r => r.isActive && r.reminderTime > now)
      .sort((a, b) => a.reminderTime - b.reminderTime);
  });

  // 格式化日期时间
  function formatDateTime(timestamp: number): string {
    const date = new Date(timestamp);
    return date.toLocaleString();
  }

  // 检查是否过期
  function isExpired(timestamp: number): boolean {
    return timestamp < Date.now();
  }

  // 删除提醒
  function deleteReminder(id: string) {
    if (reminderService.deleteReminder(id)) {
      Message.success(t('customer.reminderDeleteSuccess'));
      loadReminders();
    } else {
      Message.error(t('customer.reminderDeleteFailed'));
    }
  }

  // 标记为已处理
  function markAsProcessed(id: string) {
    if (reminderService.markAsProcessed(id)) {
      Message.success(t('customer.reminderMarkedAsProcessed'));
      loadReminders();
    } else {
      Message.error(t('customer.reminderMarkFailed'));
    }
  }

  // 加载提醒列表
  function loadReminders() {
    reminders.value = reminderService.getReminders();
  }

  // 监听提醒变化
  function handleReminderChange(updatedReminders: ReminderItem[]) {
    reminders.value = updatedReminders;
  }

  // 监听提醒事件
  function handleReminderEvent(event: CustomEvent) {
    const { reminders: eventReminders } = event.detail;
    if (eventReminders && eventReminders.length > 0) {
      Message.info(t('customer.reminderNotification', { count: eventReminders.length }));
      loadReminders();
    }
  }

  onMounted(() => {
    // 加载提醒列表
    loadReminders();
    
    // 添加监听器
    reminderService.addListener(handleReminderChange);
    
    // 监听提醒事件
    window.addEventListener('customer-reminder', handleReminderEvent as EventListener);
    
    // 启动提醒检查
    reminderService.startChecking();
    
    // 请求通知权限
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  });

  onUnmounted(() => {
    // 移除监听器
    reminderService.removeListener(handleReminderChange);
    
    // 移除事件监听
    window.removeEventListener('customer-reminder', handleReminderEvent as EventListener);
    
    // 停止提醒检查
    reminderService.stopChecking();
  });
</script>

<style lang="less" scoped>
  .reminder-notification-container {
    position: relative;
  }
  
  .reminder-list {
    .empty-reminder {
      padding: 20px 0;
      text-align: center;
    }
    
    .reminder-items {
      .reminder-item {
        padding: 12px;
        border-bottom: 1px solid var(--n-divider-color);
        
        &:last-child {
          border-bottom: none;
        }
        
        &.reminder-item-inactive {
          opacity: 0.6;
        }
        
        .reminder-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          .customer-name {
            font-weight: 500;
            color: var(--n-text-color);
          }
          
          .reminder-actions {
            display: flex;
            gap: 4px;
          }
        }
        
        .reminder-content {
          margin-bottom: 8px;
          color: var(--n-text-color-2);
          font-size: 14px;
        }
        
        .reminder-time {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: var(--n-text-color-3);
        }
      }
    }
  }
</style>