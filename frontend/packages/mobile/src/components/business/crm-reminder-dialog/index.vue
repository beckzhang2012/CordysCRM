<template>
  <van-popup v-model:show="show" round position="bottom" :style="{ height: '60%' }">
    <div class="reminder-dialog">
      <div class="dialog-header">
        <div class="header-title">{{ t('reminder.setReminder') }}</div>
        <van-icon name="cross" size="20" @click="handleClose" />
      </div>

      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field
            v-model="formData.reminderTime"
            name="reminderTime"
            is-link
            readonly
            :label="t('reminder.reminderTime')"
            :placeholder="t('reminder.selectTime')"
            @click="showTimePicker = true"
          />

          <van-field
            v-model="formData.content"
            name="content"
            type="textarea"
            rows="3"
            autosize
            :label="t('reminder.reminderContent')"
            :placeholder="t('reminder.inputContent')"
            :rules="[{ required: true, message: t('reminder.contentRequired') }]"
          />
        </van-cell-group>

        <div class="dialog-footer">
          <van-button round block type="primary" native-type="submit">
            {{ t('common.confirm') }}
          </van-button>
        </div>
      </van-form>
    </div>

    <van-popup v-model:show="showTimePicker" position="bottom">
      <van-picker-group
        :title="t('reminder.selectTime')"
        :tabs="[t('formCreate.pickDate'), t('formCreate.pickTime')]"
        :next-step-text="t('formCreate.next')"
        @confirm="onTimeConfirm"
        @cancel="showTimePicker = false"
      >
        <van-date-picker v-model="currentDate" :columns-type="['year', 'month', 'day']" />
        <van-time-picker v-model="currentTime" :columns-type="['hour', 'minute']" />
      </van-picker-group>
    </van-popup>
  </van-popup>
</template>

<script setup lang="ts">
  import { useI18n } from '@lib/shared/hooks/useI18n';

  import useReminderStore from '@/store/modules/reminder';

  import type { ReminderFormData } from '@lib/shared/types/reminder';

  const props = defineProps<{
    customerId: string;
    customerName: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:show', value: boolean): void;
  }>();

  const { t } = useI18n();
  const reminderStore = useReminderStore();

  const show = defineModel<boolean>('show', { default: false });
  const showTimePicker = ref(false);

  const formData = ref<ReminderFormData>({
    reminderTime: '',
    content: '',
  });

  const currentDate = ref<string[]>([]);
  const currentTime = ref<string[]>([]);

  watch(
    () => show.value,
    (val) => {
      if (val) {
        resetForm();
      }
    }
  );

  function resetForm() {
    formData.value = {
      reminderTime: '',
      content: '',
    };
    const now = new Date();
    currentDate.value = [now.getFullYear().toString(), (now.getMonth() + 1).toString(), now.getDate().toString()];
    currentTime.value = [now.getHours().toString(), now.getMinutes().toString()];
  }

  function onTimeConfirm() {
    showTimePicker.value = false;
    const dateStr = currentDate.value.join('-');
    const timeStr = currentTime.value.join(':');
    formData.value.reminderTime = `${dateStr} ${timeStr}`;
  }

  function handleSubmit() {
    const reminderTime = new Date(formData.value.reminderTime).getTime();
    const now = Date.now();

    if (reminderTime <= now) {
      return;
    }

    reminderStore.addReminder({
      customerId: props.customerId,
      reminderTime,
      content: formData.value.content,
    });
    show.value = false;
  }

  function handleClose() {
    show.value = false;
  }
</script>

<style lang="less" scoped>
  .reminder-dialog {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--text-n8);

    .header-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--text-n1);
    }
  }

  .dialog-footer {
    margin-top: auto;
    padding: 16px;
  }
</style>
