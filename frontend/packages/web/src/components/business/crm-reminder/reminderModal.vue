<template>
  <n-modal
    v-model:show="showModal"
    :title="t('customer.reminder.setReminder')"
    preset="card"
    class="w-[480px]"
    :bordered="false"
  >
    <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80">
      <n-form-item :label="t('customer.reminder.remindTime')" path="remindTime">
        <n-date-picker
          v-model:value="formData.remindTime"
          type="datetime"
          :placeholder="t('customer.reminder.selectTime')"
          class="w-full"
          :is-date-disabled="disablePastDates"
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
    <template #footer>
      <div class="flex justify-end gap-[12px]">
        <n-button @click="handleCancel">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleConfirm">
          {{ t('common.confirm') }}
        </n-button>
      </div>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { NButton, NDatePicker, NForm, NFormItem, NInput, NModal, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import useReminderStore from '@/store/modules/reminder';

  import type { FormInst, FormRules } from 'naive-ui';

  const props = defineProps<{
    show: boolean;
    customerId: string;
    customerName: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:show', value: boolean): void;
    (e: 'success'): void;
  }>();

  const { t } = useI18n();
  const message = useMessage();
  const reminderStore = useReminderStore();
  const formRef = ref<FormInst>();
  const loading = ref(false);

  const showModal = computed({
    get: () => props.show,
    set: (val) => emit('update:show', val),
  });

  const formData = ref({
    remindTime: null as number | null,
    content: '',
  });

  const rules: FormRules = {
    remindTime: [
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

  function disablePastDates(ts: number) {
    return ts < Date.now() - 24 * 60 * 60 * 1000;
  }

  function resetForm() {
    formData.value = {
      remindTime: null,
      content: '',
    };
  }

  function handleCancel() {
    showModal.value = false;
    resetForm();
  }

  async function handleConfirm() {
    try {
      await formRef.value?.validate();

      if (!formData.value.remindTime) {
        message.error(t('customer.reminder.timeRequired'));
        return;
      }

      loading.value = true;

      reminderStore.addReminder({
        customerId: props.customerId,
        customerName: props.customerName,
        remindTime: formData.value.remindTime,
        content: formData.value.content,
      });

      message.success(t('customer.reminder.setSuccess'));
      emit('success');
      showModal.value = false;
      resetForm();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.show,
    (val) => {
      if (val) {
        resetForm();
      }
    }
  );
</script>
