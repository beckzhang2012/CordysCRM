<template>
  <CrmDrawer v-model:show="showDrawer" :title="t('customer.reminder.reminderManagement')" width="800" :footer="false">
    <template #titleRight>
      <n-button v-if="reminderStore.sortedReminders.length > 0" text type="primary" @click="handleClearAll">
        <CrmIcon class="mr-[4px]" type="iconicon_delete" :size="16" />
        {{ t('customer.reminder.clearAll') }}
      </n-button>
    </template>

    <div class="reminder-drawer-content">
      <!-- 筛选条件 -->
      <div class="mb-[16px] flex items-center gap-[12px]">
        <n-input
          v-model:value="searchKeyword"
          class="w-[240px]"
          :placeholder="t('customer.reminder.searchPlaceholder')"
          clearable
        >
          <template #prefix>
            <CrmIcon type="iconicon_search-outline_outlined" :size="16" />
          </template>
        </n-input>
        <n-select
          v-model:value="filterStatus"
          class="w-[140px]"
          :options="statusOptions"
          clearable
          :placeholder="t('customer.reminder.filterStatus')"
        />
      </div>

      <!-- 提醒列表 -->
      <CrmTable
        :data="filteredReminders"
        :columns="columns"
        :pagination="pagination"
        :loading="false"
        virtual-scroll
        :virtual-scroll-height="'calc(100vh - 220px)'"
      />

      <!-- 空状态 -->
      <div v-if="filteredReminders.length === 0" class="flex flex-col items-center justify-center py-[80px]">
        <CrmIcon type="iconicon-alarmclock" :size="64" class="text-[var(--text-n4)]" />
        <div class="mt-[16px] text-[16px] text-[var(--text-n3)]">{{ t('customer.reminder.noData') }}</div>
      </div>
    </div>
  </CrmDrawer>
</template>

<script setup lang="ts">
  import { computed, h, ref } from 'vue';
  import { NButton, NInput, NPopconfirm, NSelect, NSwitch, useDialog, useMessage } from 'naive-ui';
  import dayjs from 'dayjs';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmDrawer from '@/components/pure/crm-drawer/index.vue';
  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import CrmTable from '@/components/pure/crm-table/index.vue';
  import type { CrmDataTableColumn } from '@/components/pure/crm-table/type';

  import useReminderStore, { type ReminderItem } from '@/store/modules/reminder';

  const { t } = useI18n();
  const reminderStore = useReminderStore();
  const message = useMessage();
  const dialog = useDialog();
  const showDrawer = defineModel<boolean>('show', { required: true });

  const searchKeyword = ref('');
  const filterStatus = ref<string | null>(null);

  // 状态选项
  const statusOptions = [
    { label: t('customer.reminder.unread'), value: 'unread' },
    { label: t('customer.reminder.read'), value: 'read' },
  ];

  // 筛选后的提醒列表
  const filteredReminders = computed(() => {
    let list = reminderStore.sortedReminders;

    // 关键词筛选
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase();
      list = list.filter(
        (item) => item.customerName.toLowerCase().includes(keyword) || item.content.toLowerCase().includes(keyword)
      );
    }

    // 状态筛选
    if (filterStatus.value) {
      list = list.filter((item) => (filterStatus.value === 'unread' ? !item.isRead : item.isRead));
    }

    return list;
  });

  // 分页配置
  const pagination = computed(() => ({
    pageSize: 20,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
    showQuickJumper: true,
  }));

  // 格式化时间
  const formatTime = (timestamp: number) => {
    return dayjs(timestamp).format('YYYY-MM-DD HH:mm');
  };

  // 表格列
  const columns = computed<CrmDataTableColumn<ReminderItem>[]>(() => [
    {
      title: t('customer.reminder.customerName'),
      key: 'customerName',
      width: 150,
      ellipsis: {
        tooltip: true,
      },
    },
    {
      title: t('customer.reminder.content'),
      key: 'content',
      minWidth: 200,
      ellipsis: {
        tooltip: true,
      },
    },
    {
      title: t('customer.reminder.reminderTime'),
      key: 'reminderTime',
      width: 160,
      render: (row) => formatTime(row.reminderTime),
    },
    {
      title: t('customer.reminder.status'),
      key: 'isRead',
      width: 100,
      render: (row) => {
        return h(
          'span',
          {
            class: row.isRead ? 'text-[var(--text-n3)]' : 'text-[var(--primary-6)] font-medium',
          },
          row.isRead ? t('customer.reminder.read') : t('customer.reminder.unread')
        );
      },
    },
    {
      title: t('common.operation'),
      key: 'operation',
      width: 150,
      fixed: 'right',
      render: (row) => {
        return h('div', { class: 'flex items-center gap-[8px]' }, [
          h(
            NButton,
            {
              text: true,
              type: 'primary',
              size: 'small',
              disabled: row.isRead,
              onClick: () => reminderStore.markAsRead(row.id),
            },
            { default: () => t('customer.reminder.markRead') }
          ),
          h(
            NPopconfirm,
            {
              onPositiveClick: () => reminderStore.deleteReminder(row.id),
            },
            {
              trigger: () =>
                h(
                  NButton,
                  {
                    text: true,
                    type: 'error',
                    size: 'small',
                  },
                  { default: () => t('common.delete') }
                ),
              default: () => t('customer.reminder.deleteConfirm'),
            }
          ),
        ]);
      },
    },
  ]);

  // 清空所有提醒
  const handleClearAll = () => {
    dialog.warning({
      title: t('common.confirmDelete'),
      content: t('customer.reminder.clearAllConfirm'),
      positiveText: t('common.confirm'),
      negativeText: t('common.cancel'),
      onPositiveClick: () => {
        reminderStore.reminderList = [];
        reminderStore.saveToLocalStorage();
        message.success(t('common.deleteSuccess'));
      },
    });
  };
</script>

<style scoped>
  .reminder-drawer-content {
    padding: 16px;
  }
</style>
