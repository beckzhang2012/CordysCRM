<template>
  <n-data-table
    :columns="columns"
    :data="data"
    :loading="loading"
    :pagination="pagination"
    :scroll-x="1000"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { NDataTable, NTag, NButton, useMessage } from 'naive-ui';

import { useI18n } from '@lib/shared/hooks/useI18n';
import { CustomerReminderListItem, CustomerReminderStatus } from '@lib/shared/models/customer';
import { getCustomerReminderListByCustomer, deleteCustomerReminder, dismissCustomerReminder } from '@lib/shared/api/modules/customer';
import useModal from '@/hooks/useModal';
import { hasAnyPermission } from '@/utils/permission';

const props = defineProps<{
  customerId: string;
  readonly?: boolean;
}>();

const emit = defineEmits<{
  (e: 'edit', reminder: CustomerReminderListItem): void;
  (e: 'refresh'): void;
}>();

const { t } = useI18n();
const Message = useMessage();
const { openModal } = useModal();

const loading = ref(false);
const data = ref<CustomerReminderListItem[]>([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const pagination = computed(() => ({
  page: page.value,
  pageSize: pageSize.value,
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (p: number) => {
    page.value = p;
    loadData();
  },
  onUpdatePageSize: (ps: number) => {
    pageSize.value = ps;
    page.value = 1;
    loadData();
  },
}));

const columns = computed(() => [
  {
    title: t('customer.reminderTime'),
    key: 'remindTime',
    width: 180,
    render: (row: CustomerReminderListItem) => {
      return new Date(row.remindTime).toLocaleString('zh-CN');
    },
  },
  {
    title: t('customer.reminderContent'),
    key: 'content',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('customer.reminderStatus'),
    key: 'status',
    width: 120,
    render: (row: CustomerReminderListItem) => {
      const type = row.status === CustomerReminderStatus.PENDING ? 'info' : 
                   row.status === CustomerReminderStatus.TRIGGERED ? 'success' : 'default';
      return <NTag type={type}>{t(`customer.reminderStatus.${row.status}`)}</NTag>;
    },
  },
  {
    title: t('common.createTime'),
    key: 'createTime',
    width: 180,
    render: (row: CustomerReminderListItem) => {
      return new Date(row.createTime).toLocaleString('zh-CN');
    },
  },
  {
    title: t('common.action'),
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: (row: CustomerReminderListItem) => {
      if (props.readonly || !hasAnyPermission(['CUSTOMER_MANAGEMENT:UPDATE'])) {
        return null;
      }
      return (
        <div class="flex gap-2">
          {row.status === CustomerReminderStatus.PENDING && (
            <>
              <NButton size="small" text type="primary" onClick={() => handleEdit(row)}>
                {t('common.edit')}
              </NButton>
              <NButton size="small" text type="warning" onClick={() => handleDismiss(row)}>
                {t('common.cancel')}
              </NButton>
            </>
          )}
          <NButton size="small" text type="error" onClick={() => handleDelete(row)}>
            {t('common.delete')}
          </NButton>
        </div>
      );
    },
  },
]);

function loadData() {
  if (!props.customerId) return;
  
  loading.value = true;
  getCustomerReminderListByCustomer(props.customerId)
    .then((res) => {
      data.value = res.data || [];
      total.value = res.data?.length || 0;
    })
    .catch((err) => {
      console.error(err);
      Message.error(t('common.loadFailed'));
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleEdit(row: CustomerReminderListItem) {
  emit('edit', row);
}

function handleDismiss(row: CustomerReminderListItem) {
  openModal({
    type: 'warning',
    title: t('common.confirm'),
    content: t('customer.confirmDismissReminder'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: async () => {
      try {
        await dismissCustomerReminder(row.id);
        Message.success(t('common.success'));
        loadData();
        emit('refresh');
      } catch (err) {
        console.error(err);
      }
    },
  });
}

function handleDelete(row: CustomerReminderListItem) {
  openModal({
    type: 'error',
    title: t('common.confirmDelete'),
    content: t('customer.confirmDeleteReminder'),
    positiveText: t('common.confirmDelete'),
    negativeText: t('common.cancel'),
    onPositiveClick: async () => {
      try {
        await deleteCustomerReminder(row.id);
        Message.success(t('common.deleteSuccess'));
        loadData();
        emit('refresh');
      } catch (err) {
        console.error(err);
      }
    },
  });
}

watch(() => props.customerId, () => {
  page.value = 1;
  loadData();
}, { immediate: true });

defineExpose({
  loadData,
});
</script>
