<template>
  <CrmModal
    v-model:show="showModal"
    :title="t('customer.reminder.setReminder')"
    :ok-loading="loading"
    @confirm="handleConfirm"
    @cancel="handleClose"
  >
    <n-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-placement="left"
      label-width="100"
      require-mark-placement="right-hanging"
    >
      <n-form-item :label="t('customer.reminder.customerName')">
        <n-input v-model:value="customerName" disabled />
      </n-form-item>
      <n-form-item :label="t('customer.reminder.reminderTime')" path="reminderTime">
        <n-date-picker
          v-model:value="formData.reminderTime"
          type="datetime"
          :placeholder="t('customer.reminder.selectTime')"
          class="w-full"
          :is-date-disabled="disablePreviousDate"
        />
      </n-form-item>
      <n-form-item :label="t('customer.reminder.content')" path="content">
        <n-input
          v-model:value="formData.content"
          type="textarea"
          :placeholder="t('customer.reminder.contentPlaceholder')"
          :rows="4"
          maxlength="500"
          show-count
        />
      </n-form-item>
    </n-form>
  </CrmModal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { type FormInst, type FormRules, NDatePicker, NForm, NFormItem, NInput, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmModal from '@/components/pure/crm-modal/index.vue';

  import useReminderStore from '@/store/modules/reminder';

  const props = defineProps<{
    customerId: string;
    customerName: string;
  }>();

  const emit = defineEmits<{
    (e: 'success'): void;
  }>();

  const { t } = useI18n();
  const reminderStore = useReminderStore();
  const message = useMessage();
  const showModal = defineModel<boolean>('show', { required: true });

  const formRef = ref<FormInst>();
  const loading = ref(false);

  const formData = ref({
    reminderTime: null as number | null,
    content: '',
  });

  const rules: FormRules = {
    reminderTime: [
      {
        required: true,
        message: t('customer.reminder.timeRequired'),
        trigger: ['blur', 'change'],
      },
    ],
    content: [
      {
        required: true,
        message: t('customer.reminder.contentRequired'),
        trigger: ['blur', 'change'],
      },
      {
        max: 500,
        message: t('customer.reminder.contentMaxLength'),
        trigger: ['blur', 'change'],
      },
    ],
  };

  // 禁用过去的日期
  const disablePreviousDate = (ts: number) => {
    return ts < Date.now() - 24 * 60 * 60 * 1000;
  };

  // 重置表单
  const resetForm = () => {
    formData.value = {
      reminderTime: null,
      content: '',
    };
  };

  // 确认
  const handleConfirm = async () => {
    await formRef.value?.validate(async (errors) => {
      if (errors) return;

      loading.value = true;
      try {
        if (formData.value.reminderTime) {
          reminderStore.addReminder(
            props.customerId,
            props.customerName,
            formData.value.reminderTime,
            formData.value.content
          );
          message.success(t('customer.reminder.setSuccess'));
          emit('success');
          showModal.value = false;
          resetForm();
        }
      } finally {
        loading.value = false;
      }
    });
  };

  // 关闭
  const handleClose = () => {
    resetForm();
  };

  watch(
    () => showModal.value,
    (val) => {
      if (val) {
        resetForm();
      }
    }
  );
</script>
