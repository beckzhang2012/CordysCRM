<template>
  <n-drawer v-model:show="show" :width="500" :placement="'right'" :style="{ padding: '0' }">
    <div class="h-full flex flex-col">
      <div class="flex items-center justify-between p-[16px_20px] border-b">
        <div class="text-[16px] font-semibold">提醒列表</div>
        <n-button type="text" @click="show = false"><n-icon component="Close" :size="16" /></n-button>
      </div>
      <div class="flex-1 overflow-auto p-[16px_20px]">
        <div v-if="reminders.length === 0" class="flex flex-col items-center justify-center h-full text-center">
          <n-icon class="text-[48px] text-[rgba(0,0,0,0.2)]" component="AlarmClockOutline" />
          <div class="mt-[16px] text-[14px] text-[rgba(0,0,0,0.5)]">暂无提醒</div>
        </div>
        <div v-else class="space-y-[12px]">
          <div v-for="item in reminders" :key="item.id" class="p-[16px] border rounded-md hover:bg-[rgba(0,0,0,0.04)] cursor-pointer" @click="handleItemClick(item)">
            <div class="flex items-center justify-between mb-[8px]">
              <div class="font-semibold">{{ item.sourceName }}</div>
              <n-button type="text" size="small" @click="$event.stopPropagation(); handleDelete(item.id)">删除</n-button>
            </div>
            <div class="text-[#666] text-[14px]">{{ item.remindContent }}</div>
            <div class="text-[#999] text-[12px] mt-[8px]">{{ item.remindTime }}</div>
          </div>
        </div>
      </div>
    </div>
  </n-drawer>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { NButton, NDrawer, NIcon } from 'naive-ui';
  import { Close, AlarmClockOutline } from '@vicons/ionicons5';
  import { useMessage } from 'naive-ui';

  import { deleteReminder, markReminderAsRead } from '@/api/modules/reminder';

  const props = defineProps<{
    visible: boolean;
    reminders: any[];
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'read', id: string): void;
    (e: 'deleted'): void;
  }>();

  const Message = useMessage();
  const show = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  async function handleItemClick(item: any) {
    await markReminderAsRead(item.id);
    emit('read', item.id);
  }

  async function handleDelete(id: string) {
    await deleteReminder(id);
    emit('deleted');
    Message.success('删除成功');
  }
</script>

<style lang="less" scoped>
  .space-y-\[12px\] {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
</style>
