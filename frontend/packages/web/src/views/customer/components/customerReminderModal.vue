<template>
  <n-modal
    v-model:show="visible"
    preset="card"
    :title="t('customer.reminder')"
    :mask-closable="false"
    style="width: 500px;"
    :closable="true"
    :close-on-esc="true"
    @update:show="handleClose"
  >
    <n-form ref="formRef" :model="formData" :rules="rules" size="large">
      <n-form-item path="reminderTime" :label="t('customer.reminderTime')">
        <n-date-picker
          v-model:value="formData.reminderTime"
          type="datetime"
          :is-datetime="true"
          :clearable="true"
          :show-time="true"
          :hour-step="1"
          :minute-step="5"
          :second-step="0"
          :actions="['now', 'confirm']"
          :placeholder="t('common.select')"
          style="width: 100%"
        />
      </n-form-item>
      <n-form-item path="content" :label="t('customer.reminderContent')">
        <n-input
          v-model:value="formData.content"
          type="textarea"
          :placeholder="t('customer.reminderContentPlaceholder')"
          :autosize="{ minRows: 4, maxRows: 8 }"
          show-count
          :maxlength="500"
        />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="handleClose">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleSave">
          {{ t('common.confirm') }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue';
  import { NForm, useMessage, type FormInst, type FormRules } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { SaveCustomerReminderParams } from '@lib/shared/models/customer';

  import { addCustomerReminder } from '@/api/modules';

  const props = defineProps<{
    customerId: string;
    customerName: string;
  }>();

  const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'success'): void;
  }>();

  const { t } = useI18n();
  const Message = useMessage();

  const visible = ref(false);
  const loading = ref(false);
  const formRef = ref<FormInst | null>(null);

  const formData = reactive<SaveCustomerReminderParams>({
    customerId: props.customerId,
    customerName: props.customerName,
    reminderTime: undefined,
    content: '',
  });

  const rules: FormRules = {
    reminderTime: [
      {
        required: true,
        message: t('customer.reminderTimeRequired'),
        trigger: 'blur',
      },
    ],
    content: [
      {
        required: true,
        message: t('customer.reminderContentRequired'),
        trigger: 'blur',
      },
    ],
  };

  function open() {
    visible.value = true;
    formData.customerId = props.customerId;
    formData.customerName = props.customerName;
    formData.reminderTime = undefined;
    formData.content = '';
    formRef.value?.clearValidation();
  }

  function handleClose() {
    visible.value = false;
    emit('close');
  }

  async function handleSave() {
    try {
      await formRef.value?.validate();
      loading.value = true;

      await addCustomerReminder({
        customerId: props.customerId,
        customerName: props.customerName,
        reminderTime: formData.reminderTime!,
        content: formData.content,
      });

      Message.success(t('customer.reminderAddSuccess'));
      emit('success');
      handleClose();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  }

  defineExpose({
    open,
  });
</script>

<style lang="less" scoped></style>
