<template>
  <CrmModal
    v-model:show="showModal"
    preset="card"
    size="large"
    :title="t('reminder.reminderList')"
    :positive-text="t('common.close')"
    :show-negative-button="false"
  >
    <n-spin :show="loading">
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :pagination="{
          page: pageNum,
          pageSize: pageSize,
          itemCount: total,
          showSizePicker: true,
          pageSizes: [10, 20, 50],
        }"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-spin>
  </CrmModal>
</template>

<script setup lang="ts">
import { h, ref, onMounted } from 'vue';
import { NButton, NDataTable, NSpace, NSpin, NTag, useMessage } from 'naive-ui';
import dayjs from 'dayjs';

import { ReminderBusinessType, ReminderItem, ReminderStatus } from '@lib/shared/models/reminder';

import CrmModal from '@/components/pure/crm-modal/index.vue';
import { completeReminder, cancelReminder, deleteReminder, getReminderPage } from '@/api/modules';
import { useI18n } from '@lib/shared/hooks/useI18n';

const { t } = useI18n();
const Message = useMessage();

const props = defineProps<{
  businessType?: ReminderBusinessType;
  businessId?: string;
}>();

const showModal = defineModel<boolean>('show', {
  required: true,
  default: false,
});

const loading = ref(false);
const list = ref<ReminderItem[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const statusMap: Record<ReminderStatus, { type: 'success' | 'info' | 'error'; text: string }> = {
  [ReminderStatus.PENDING]: { type: 'info', text: t('reminder.pending') },
  [ReminderStatus.COMPLETED]: { type: 'success', text: t('reminder.completed') },
  [ReminderStatus.CANCELLED]: { type: 'error', text: t('reminder.cancelled') },
};

const columns = [
  {
    title: t('reminder.remindTime'),
    key: 'remindTime',
    width: 180,
    render: (row: ReminderItem) => {
      return dayjs(row.remindTime).format('YYYY-MM-DD HH:mm');
    },
  },
  {
    title: t('reminder.remindContent'),
    key: 'content',
    render: (row: ReminderItem) => {
      return row.content || '-';
    },
  },
  {
    title: t('common.status'),
    key: 'status',
    width: 100,
    render: (row: ReminderItem) => {
      const status = statusMap[row.status];
      return h(NTag, { type: status.type }, () => status.text);
    },
  },
  {
    title: t('common.operation'),
    key: 'action',
    width: 200,
    fixed: 'right' as const,
    render: (row: ReminderItem) => {
      if (row.status !== ReminderStatus.PENDING) {
        return h(
          NButton,
          { size: 'small', type: 'error', text: true, onClick: () => handleDelete(row.id) },
          () => t('reminder.delete'),
        );
      }
      return h(NSpace, null, () => [
        h(
          NButton,
          { size: 'small', type: 'primary', text: true, onClick: () => handleComplete(row.id) },
          () => t('reminder.complete'),
        ),
        h(
          NButton,
          { size: 'small', type: 'warning', text: true, onClick: () => handleCancel(row.id) },
          () => t('reminder.cancel'),
        ),
        h(
          NButton,
          { size: 'small', type: 'error', text: true, onClick: () => handleDelete(row.id) },
          () => t('reminder.delete'),
        ),
      ]);
    },
  },
];

async function fetchList() {
  loading.value = true;
  try {
    const result = await getReminderPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      businessType: props.businessType,
      businessId: props.businessId,
    });
    list.value = result.list || [];
    total.value = result.total || 0;
  } finally {
    loading.value = false;
  }
}

function handlePageChange(page: number) {
  pageNum.value = page;
  fetchList();
}

function handlePageSizeChange(size: number) {
  pageSize.value = size;
  pageNum.value = 1;
  fetchList();
}

async function handleComplete(id: string) {
  try {
    await completeReminder(id);
    Message.success(t('common.success'));
    fetchList();
  } catch (e) {
    console.error(e);
  }
}

async function handleCancel(id: string) {
  try {
    await cancelReminder(id);
    Message.success(t('common.success'));
    fetchList();
  } catch (e) {
    console.error(e);
  }
}

async function handleDelete(id: string) {
  try {
    await deleteReminder(id);
    Message.success(t('common.success'));
    fetchList();
  } catch (e) {
    console.error(e);
  }
}

onMounted(() => {
  fetchList();
});

watch(
  () => showModal.value,
  (val) => {
    if (val) {
      pageNum.value = 1;
      fetchList();
    }
  },
);
</script>
