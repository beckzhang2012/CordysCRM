<template>
  <CrmDrawer
    v-model:show="visible"
    no-padding
    :title="t('customerReminder.title')"
    width="600"
    :footer="false"
  >
    <template #titleRight>
      <div class="flex items-center gap-[8px]">
        <n-button text type="primary" @click="handleMarkAllAsRead">
          <CrmIcon class="mr-[4px]" type="iconicon_browse" :size="16" />
          {{ t('customerReminder.markAsRead') }}
        </n-button>
      </div>
    </template>
    <div class="p-[24px]">
      <n-list bordered>
        <n-list-item v-for="item in reminders" :key="item.id" :hoverable="true">
          <div class="flex items-start gap-[12px]">
            <div class="flex-1">
              <div class="flex items-center justify-between mb-[8px]">
                <n-tag v-if="item.status === CustomerReminderStatusEnum.PENDING" type="warning" size="small">
                  {{ t('customerReminder.status.pending') }}
                </n-tag>
                <n-tag v-else-if="item.status === CustomerReminderStatusEnum.REMINDED" type="success" size="small">
                  {{ t('customerReminder.status.reminded') }}
                </n-tag>
                <n-tag v-else type="info" size="small">
                  {{ t('customerReminder.status.closed') }}
                </n-tag>
                <n-dropdown :options="getItemOptions(item)" trigger="click">
                  <n-button text size="small">
                    <CrmIcon type="iconicon_more_vert" :size="16" />
                  </n-button>
                </n-dropdown>
              </div>
              <div class="text-[16px] font-semibold mb-[4px]">
                {{ item.customerName }}
              </div>
              <div class="text-[14px] text-[var(--text-n3)] mb-[8px]">
                {{ item.content }}
              </div>
              <div class="text-[12px] text-[var(--text-n4)]">
                <CrmIcon type="iconicon_time" :size="14" class="mr-[4px]" />
                {{ formatTime(item.reminderTime) }}
              </div>
            </div>
          </div>
        </n-list-item>
        <n-empty v-if="reminders.length === 0" :description="t('customerReminder.noReminders')" />
      </n-list>
    </div>
  </CrmDrawer>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { NButton, NDropdown, NEmpty, NList, NTag, useMessage } from 'naive-ui';
  import dayjs from 'dayjs';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { CustomerReminderStatusEnum, type CustomerReminderListItem } from '@lib/shared/models/customer/reminder';
  import { getPendingReminders, updateCustomerReminder, deleteCustomerReminder } from '@lib/shared/api/modules/customer';

  import CrmDrawer from '@/components/pure/crm-drawer/index.vue';
  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';

  interface Props {
    show: boolean;
  }

  const props = defineProps<Props>();
  const emit = defineEmits<{
    'update:show': [value: boolean];
  }>();

  const visible = computed({
    get: () => props.show,
    set: (val) => emit('update:show', val)
  });

  const { t } = useI18n();
  const Message = useMessage();

  const reminders = ref<CustomerReminderListItem[]>([]);

  async function fetchReminders() {
    try {
      const res = await getPendingReminders();
      reminders.value = res.data.data || [];
    } catch (error) {
      console.error('Failed to fetch reminders:', error);
    }
  }

  function formatTime(timestamp: number): string {
    return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss');
  }

  function getItemOptions(item: CustomerReminderListItem) {
    const options: any[] = [
      {
        label: t('customerReminder.delete'),
        key: 'delete',
        onClick: () => handleDelete(item.id)
      }
    ];
    
    if (item.status === CustomerReminderStatusEnum.PENDING) {
      options.unshift({
        label: t('customerReminder.markAsRead'),
        key: 'markAsRead',
        onClick: () => handleMarkAsRead(item.id)
      });
    }
    
    return options;
  }

  async function handleMarkAsRead(id: string) {
    try {
      await updateCustomerReminder({
        id,
        status: CustomerReminderStatusEnum.REMINDED
      });
      Message.success(t('common.operationSuccess'));
      fetchReminders();
    } catch (error) {
      console.error('Failed to mark as read:', error);
    }
  }

  async function handleMarkAllAsRead() {
    try {
      const pendingReminders = reminders.value.filter(r => r.status === CustomerReminderStatusEnum.PENDING);
      await Promise.all(
        pendingReminders.map(r => updateCustomerReminder({
          id: r.id,
          status: CustomerReminderStatusEnum.REMINDED
        }))
      );
      Message.success(t('common.operationSuccess'));
      fetchReminders();
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  }

  async function handleDelete(id: string) {
    try {
      await deleteCustomerReminder(id);
      Message.success(t('common.deleteSuccess'));
      fetchReminders();
    } catch (error) {
      console.error('Failed to delete reminder:', error);
    }
  }

  watch(
    () => props.show,
    (newVal) => {
      if (newVal) {
        fetchReminders();
      }
    }
  );
</script>

<style lang="less" scoped>
  .n-list-item {
    border-bottom: 1px solid var(--border-color);
    
    &:last-child {
      border-bottom: none;
    }
  }
</style>
