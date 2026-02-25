<template>
  <n-drawer v-model:show="visible" :width="400" placement="right">
    <n-drawer-content :title="t('customer.reminderList')" closable>
      <div class="flex flex-col gap-[12px]">
        <div v-if="loading" class="flex justify-center py-[20px]">
          <n-spin size="medium" />
        </div>
        <template v-else-if="reminderList.length > 0">
          <div
            v-for="item in reminderList"
            :key="item.id"
            class="rounded-[8px] border border-[var(--n-border-color)] bg-[var(--n-color)] p-[12px]"
          >
            <div class="mb-[8px] flex items-center justify-between">
              <span class="font-medium text-[var(--text-n1)]">{{ item.customerName }}</span>
              <n-button text type="error" size="small" @click="handleDelete(item.id)">
                {{ t('common.delete') }}
              </n-button>
            </div>
            <div class="mb-[4px] text-[12px] text-[var(--text-n4)]">
              {{ t('customer.reminderTime') }}: {{ formatTime(item.reminderTime) }}
            </div>
            <div class="text-[14px] text-[var(--text-n2)]">{{ item.content }}</div>
            <div class="mt-[8px] flex items-center justify-between">
              <n-tag :type="item.status === 'PENDING' ? 'warning' : 'success'" size="small">
                {{ item.status === 'PENDING' ? t('customer.reminderPending') : t('customer.reminderNotified') }}
              </n-tag>
              <span class="text-[12px] text-[var(--text-n4)]">
                {{ formatTime(item.createTime) }}
              </span>
            </div>
          </div>
        </template>
        <n-empty v-else :description="t('customer.noReminder')" />
      </div>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
  import { NButton, NDrawer, NDrawerContent, NEmpty, NSpin, NTag, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import { deleteCustomerReminder, getCustomerReminderList } from '@/api/modules';

  interface ReminderItem {
    id: string;
    customerId: string;
    customerName: string;
    reminderTime: number;
    content: string;
    status: string;
    createTime: number;
  }

  const visible = defineModel<boolean>('visible', { required: true });

  const { t } = useI18n();
  const Message = useMessage();

  const loading = ref(false);
  const reminderList = ref<ReminderItem[]>([]);

  function formatTime(timestamp: number) {
    if (!timestamp) return '-';
    const date = new Date(timestamp);
    return date.toLocaleString();
  }

  async function loadList() {
    try {
      loading.value = true;
      const res = await getCustomerReminderList();
      reminderList.value = res || [];
    } catch (error) {
      Message.error(String(error));
    } finally {
      loading.value = false;
    }
  }

  async function handleDelete(id: string) {
    try {
      await deleteCustomerReminder(id);
      Message.success(t('common.deleteSuccess'));
      await loadList();
    } catch (error) {
      Message.error(String(error));
    }
  }

  watch(visible, (val) => {
    if (val) {
      loadList();
    }
  });
</script>
