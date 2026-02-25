<template>
  <CrmModal
    v-model:show="showModal"
    size="small"
    :title="t('reminder.setReminder')"
    :ok-loading="loading"
    :positive-text="t('common.confirm')"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <n-form ref="formRef" :model="form" label-placement="left" require-mark-placement="left" label-width="100">
      <n-form-item path="remindTime" :label="t('reminder.remindTime')" required>
        <n-date-picker
          v-model:value="form.remindTime"
          type="datetime"
          clearable
          :placeholder="t('common.pleaseSelect')"
        />
      </n-form-item>
      <n-form-item path="content" :label="t('reminder.remindContent')">
        <n-input
          v-model:value="form.content"
          type="textarea"
          :placeholder="t('common.pleaseInput')"
          :rows="3"
        />
      </n-form-item>
    </n-form>
  </CrmModal>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { NDatePicker, NForm, NFormItem, NInput, useMessage } from 'naive-ui';

import { ReminderAddParams, ReminderBusinessType } from '@lib/shared/models/reminder';

import CrmModal from '@/components/pure/crm-modal/index.vue';
import { addReminder } from '@/api/modules';
import { useI18n } from '@lib/shared/hooks/useI18n';

const { t } = useI18n();
const Message = useMessage();

const props = defineProps<{
  businessType: ReminderBusinessType;
  businessId: string;
}>();

const showModal = defineModel<boolean>('show', {
  required: true,
  default: false,
});

const emit = defineEmits<{
  (e: 'success'): void;
}>();

const form = ref<ReminderAddParams>({
  businessType: props.businessType,
  businessId: props.businessId,
  remindTime: null,
  content: '',
});

const loading = ref(false);
const formRef = ref<InstanceType<typeof NForm>>();

function handleCancel() {
  form.value = {
    businessType: props.businessType,
    businessId: props.businessId,
    remindTime: null,
    content: '',
  };
}

async function handleConfirm() {
  if (!form.value.remindTime) {
    Message.warning(t('reminder.pleaseSelectRemindTime'));
    return;
  }

  try {
    loading.value = true;
    await addReminder({
      ...form.value,
      remindTime: form.value.remindTime,
    });
    Message.success(t('common.saveSuccess'));
    showModal.value = false;
    emit('success');
    handleCancel();
  } finally {
    loading.value = false;
  }
}
</script>
