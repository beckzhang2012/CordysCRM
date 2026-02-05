<template>
  <div class="reminder-list">
    <n-card title="提醒列表" :bordered="false" style="width: 100%">
      <n-data-table
        :columns="columns"
        :data="reminders"
        :pagination="pagination"
        row-key="id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      >
        <template #cell(remindTime)="{ row }">
          {{ formatDate(row.remindTime) }}
        </template>

        <template #cell(isRead)="{ row }">
          <n-tag :type="row.isRead ? 'success' : 'warning'">
            {{ row.isRead ? '已读' : '未读' }}
          </n-tag>
        </template>

        <template #cell(action)="{ row }">
          <n-space>
            <n-button size="small" type="primary" :disabled="row.isRead" @click="handleMarkAsRead(row.id)">
              {{ row.isRead ? '已读' : '标记已读' }}
            </n-button>
            <n-popconfirm placement="bottom" @positive-click="handleDelete(row.id)">
              <template #trigger>
                <n-button size="small" type="error"> 删除 </n-button>
              </template>
              确定要删除该提醒吗？
            </n-popconfirm>
          </n-space>
        </template>
      </n-data-table>
    </n-card>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue';

  import type { Reminder } from '@lib/shared/models/reminder';

  import { deleteReminder, getReminderList, markReminderAsRead } from '@/api/modules/reminder';

  import type { DataTableColumns, PaginationProps } from 'naive-ui';

  const reminders = ref<Reminder[]>([]);

  const pagination = reactive<PaginationProps>({
    page: 1,
    pageSize: 10,
    pageCount: 1,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
    showQuickJumper: true,
  });

  const columns = [
    { title: '源名称', key: 'sourceName' },
    { title: '提醒时间', key: 'remindTime' },
    { title: '提醒内容', key: 'content' },
    { title: '状态', key: 'isRead' },
    { title: '操作', key: 'action' },
  ] as DataTableColumns;

  const formatDate = (date: string | Date) => {
    return new Date(date).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  const fetchReminders = async () => {
    try {
      const response = await getReminderList({
        page: pagination.page,
        pageSize: pagination.pageSize,
      });
      const { data } = response;
      reminders.value = data.records;
      pagination.pageCount = data.pages;
    } catch (error) {
      console.error('获取提醒列表失败', error);
    }
  };

  const handlePageChange = (page: number) => {
    pagination.page = page;
    fetchReminders();
  };

  const handlePageSizeChange = (pageSize: number) => {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    fetchReminders();
  };

  const handleMarkAsRead = async (id: string) => {
    try {
      await markReminderAsRead(id);
      fetchReminders();
    } catch (error) {
      console.error('标记已读失败', error);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteReminder(id);
      fetchReminders();
    } catch (error) {
      console.error('删除提醒失败', error);
    }
  };

  onMounted(() => {
    fetchReminders();
  });
</script>

<style scoped>
  .reminder-list {
    padding: 20px;
  }
</style>
