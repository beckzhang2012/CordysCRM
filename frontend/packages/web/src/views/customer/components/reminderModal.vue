<template>
  <n-modal
    v-model:show="showModal"
    preset="card"
    :title="t('customer.setReminder')"
    class="w-[500px]"
    :mask-closable="false"
  >
    <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80">
      <n-form-item :label="t('customer.reminderTime')" path="reminderTime">
        <n-date-picker
          v-model:value="formData.reminderTime"
          type="datetime"
          :placeholder="t('customer.selectReminderTime')"
          clearable
          class="w-full"
          :is-date-disabled="isDateDisabled"
        />
      </n-form-item>
      <n-form-item :label="t('customer.reminderContent')" path="content">
        <n-input
          v-model:value="formData.content"
          type="textarea"
          :placeholder="t('customer.inputReminderContent')"
          :rows="3"
          maxlength="200"
          show-count
        />
      </n-form-item>
    </n-form>
    <template #footer>
      <div class="flex justify-end gap-[12px]">
        <n-button @click="handleCancel">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleConfirm">{{ t('common.confirm') }}</n-button>
      </div>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
  import { NButton, NDatePicker, NForm, NFormItem, NInput, NModal, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import { addCustomerReminder } from '@/api/modules';

  import type { FormRules } from 'naive-ui';

  const props = defineProps<{
    customerId: string;
    customerName: string;
  }>();

  const emit = defineEmits<{
    (e: 'success'): void;
  }>();

  const { t } = useI18n();
  const Message = useMessage();

  const showModal = defineModel<boolean>('show', { required: true });
  const formRef = ref<InstanceType<typeof NForm>>();
  const loading = ref(false);

  const formData = ref({
    reminderTime: null as number | null,
    content: '',
  });

  const rules: FormRules = {
    reminderTime: {
      required: true,
      type: 'number',
      message: t('customer.selectReminderTime'),
      trigger: ['blur', 'change'],
    },
    content: {
      required: true,
      message: t('customer.inputReminderContent'),
      trigger: ['blur', 'change'],
    },
  };

  function isDateDisabled(ts: number) {
    return ts < Date.now() - 86400000;
  }

  function resetForm() {
    formData.value = {
      reminderTime: null,
      content: '',
    };
    formRef.value?.restoreValidation();
  }

  function handleCancel() {
    showModal.value = false;
    resetForm();
  }

  async function handleConfirm() {
    try {
      await formRef.value?.validate();
      loading.value = true;
      await addCustomerReminder({
        customerId: props.customerId,
        customerName: props.customerName,
        reminderTime: formData.value.reminderTime!,
        content: formData.value.content,
      });
      Message.success(t('common.saveSuccess'));
      showModal.value = false;
      resetForm();
      emit('success');
    } catch (error) {
      Message.error(String(error));
    } finally {
      loading.value = false;
    }
  }

  watch(showModal, (val) => {
    if (!val) {
      resetForm();
    }
  });
</script>
