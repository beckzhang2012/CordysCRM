<template>
  <div class="reminder-notification-wrapper">
    <n-button class="p-[8px]" quaternary @click="showReminderList = true">
      <n-badge :value="pendingCount" :show="pendingCount > 0" processing>
        <CrmIcon type="iconicon-alarmclock" :size="16" />
      </n-badge>
    </n-button>
    <CrmDrawer
      v-model:show="showReminderList"
      :title="t('customer.reminderList')"
      width="600"
      :footer="false"
    >
      <div class="reminder-list">
        <div class="mb-[16px] flex items-center justify-between">
          <n-select
            v-model:value="statusFilter"
            class="w-[150px]"
            :options="statusOptions"
            @update:value="loadReminderList"
          />
          <n-button text type="primary" @click="loadReminderList">
            <template #icon>
              <CrmIcon type="iconicon_refresh" :size="16" />
            </template>
          </n-button>
        </div>
        <n-spin :show="loading">
          <n-list :bordered="false" class="reminder-list-content">
            <template #footer>
              <div v-if="reminderList.length > 0" class="flex justify-center">
                <n-pagination
                  v-model:page="page"
                  v-model:page-size="pageSize"
                  :item-count="total"
                  :page-sizes="[10, 20, 50]"
                  show-size-picker
                  @update:page="loadReminderList"
                  @update:page-size="loadReminderList"
                />
              </div>
              <n-empty v-else-if="!loading" :description="t('common.noData')" />
            </template>
            <n-list-item v-for="item in reminderList" :key="item.id">
              <div class="reminder-item-wrapper">
                <div class="reminder-item-content">
                  <div class="reminder-item-title">{{ item.customerName }}</div>
                  <div class="reminder-item-desc">
                    <div class="text-[14px] text-[var(--text-n1)]">{{ item.content }}</div>
                    <div class="mt-[8px] flex items-center gap-[16px] text-[12px] text-[var(--text-n4)]">
                      <span>
                        <CrmIcon class="mr-[4px]" type="iconicon_time" :size="14" />
                        {{ formatDate(item.reminderTime) }}
                      </span>
                      <n-tag :type="getStatusTagType(item.status)" size="small">
                        {{ getStatusText(item.status) }}
                      </n-tag>
                    </div>
                  </div>
                </div>
                <div class="reminder-item-actions">
                  <n-space>
                    <n-button
                      v-if="item.status === 'PENDING'"
                      text
                      type="warning"
                      size="small"
                      @click="handleCancelReminder(item)"
                    >
                      {{ t('customer.cancelReminder') }}
                    </n-button>
                    <n-button text type="error" size="small" @click="handleDeleteReminder(item)">
                      {{ t('common.delete') }}
                    </n-button>
                  </n-space>
                </div>
              </div>
            </n-list-item>
          </n-list>
        </n-spin>
      </div>
    </CrmDrawer>
  </div>
</template>

<script setup lang="ts">
  import { NBadge, NButton, NEmpty, NList, NListItem, NPagination, NSelect, NSpace, NSpin, NTag, useMessage } from 'naive-ui';
  import dayjs from 'dayjs';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { ReminderListItem } from '@lib/shared/models/reminder';

  import CrmDrawer from '@/components/pure/crm-drawer/index.vue';
  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';

  import { cancelReminder, deleteReminder, getPendingReminderCount, getReminderList } from '@/api/modules';
  import useModal from '@/hooks/useModal';

  const { t } = useI18n();
  const Message = useMessage();
  const { openModal } = useModal();

  const showReminderList = ref(false);
  const pendingCount = ref(0);
  const loading = ref(false);
  const reminderList = ref<ReminderListItem[]>([]);
  const page = ref(1);
  const pageSize = ref(10);
  const total = ref(0);
  const statusFilter = ref('');

  const statusOptions = [
    { label: t('common.all'), value: '' },
    { label: t('customer.reminderPending'), value: 'PENDING' },
    { label: t('customer.reminderCompleted'), value: 'COMPLETED' },
    { label: t('customer.reminderCancelled'), value: 'CANCELLED' },
  ];

  function getStatusTagType(status: string) {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'COMPLETED':
        return 'success';
      case 'CANCELLED':
        return 'default';
      default:
        return 'default';
    }
  }

  function getStatusText(status: string) {
    switch (status) {
      case 'PENDING':
        return t('customer.reminderPending');
      case 'COMPLETED':
        return t('customer.reminderCompleted');
      case 'CANCELLED':
        return t('customer.reminderCancelled');
      default:
        return status;
    }
  }

  function formatDate(timestamp: number) {
    return dayjs(timestamp).format('YYYY-MM-DD HH:mm');
  }

  async function loadPendingCount() {
    try {
      const count = await getPendingReminderCount();
      pendingCount.value = count;
    } catch (error) {
      console.error(error);
    }
  }

  async function loadReminderList() {
    try {
      loading.value = true;
      const result = await getReminderList({
        page: page.value,
        pageSize: pageSize.value,
        status: statusFilter.value,
      });
      reminderList.value = result.records;
      total.value = result.total;
    } catch (error) {
      console.error(error);
    } finally {
      loading.value = false;
    }
  }

  function handleCancelReminder(item: ReminderListItem) {
    openModal({
      type: 'warning',
      title: t('customer.cancelReminderConfirm'),
      content: t('customer.cancelReminderContent'),
      positiveText: t('common.confirm'),
      negativeText: t('common.cancel'),
      onPositiveClick: async () => {
        try {
          await cancelReminder(item.id);
          Message.success(t('common.operationSuccess'));
          loadReminderList();
          loadPendingCount();
        } catch (error) {
          console.error(error);
        }
      },
    });
  }

  function handleDeleteReminder(item: ReminderListItem) {
    openModal({
      type: 'error',
      title: t('common.deleteConfirm'),
      content: t('customer.deleteReminderContent'),
      positiveText: t('common.confirmDelete'),
      negativeText: t('common.cancel'),
      onPositiveClick: async () => {
        try {
          await deleteReminder(item.id);
          Message.success(t('common.deleteSuccess'));
          loadReminderList();
          loadPendingCount();
        } catch (error) {
          console.error(error);
        }
      },
    });
  }

  watch(
    () => showReminderList.value,
    (val) => {
      if (val) {
        loadReminderList();
      }
    }
  );

  onMounted(() => {
    loadPendingCount();
  });

  defineExpose({
    loadPendingCount,
  });
</script>

<style lang="less" scoped>
  .reminder-notification-wrapper {
    display: inline-block;
  }

  .reminder-list {
    height: 100%;
  }

  .reminder-list-content {
    height: 100%;
    overflow-y: auto;
  }

  .reminder-item-wrapper {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .reminder-item-content {
    flex: 1;
    min-width: 0;
  }

  .reminder-item-title {
    font-size: 16px;
    font-weight: 500;
    color: var(--n-text-color);
    margin-bottom: 8px;
  }

  .reminder-item-desc {
    width: 100%;
  }

  .reminder-item-actions {
    flex-shrink: 0;
  }
</style>
