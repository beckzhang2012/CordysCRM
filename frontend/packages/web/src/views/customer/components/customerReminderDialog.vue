<template>
  <CrmModal
    v-model:show="showModal"
    :title="reminder.id ? t('customer.editReminder') : t('customer.addReminder')"
    :ok-loading="loading"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <n-form
      ref="formRef"
      :model="reminder"
      :rules="rules"
      label-placement="left"
      :label-width="100"
      require-mark-placement="left"
    >
      <n-form-item
        require-mark-placement="left"
        label-placement="left"
        path="remindTime"
        :label="t('customer.reminderTime')"
      >
        <n-date-picker
          v-model:value="reminder.remindTime"
          type="datetime"
          class="w-full"
          clearable
          :placeholder="t('customer.pleaseSelectReminderTime')"
          :is-date-disabled="disabledDate"
        >
          <template #date-icon>
            <CrmIcon class="text-[var(--text-n4)]" type="iconicon_time" :size="16" />
          </template>
        </n-date-picker>
      </n-form-item>
      <n-form-item
        require-mark-placement="left"
        label-placement="left"
        path="content"
        :label="t('customer.reminderContent')"
      >
        <n-input
          v-model:value="reminder.content"
          :maxlength="1000"
          type="textarea"
          :placeholder="t('customer.reminderContentPlaceholder')"
          :rows="4"
        />
      </n-form-item>
    </n-form>
  </CrmModal>
</template>

<script setup lang="ts">
  import { FormInst, FormRules, NDatePicker, NForm, NFormItem, NInput, useMessage } from 'naive-ui';
  import { cloneDeep } from 'lodash-es';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { CustomerReminderAddParams, CustomerReminderListItem, CustomerReminderUpdateParams } from '@lib/shared/models/customer';

  import CrmModal from '@/components/pure/crm-modal/index.vue';
  import CrmIcon from '@/components/pure/crm-icon/index.vue';

  import { addCustomerReminder, updateCustomerReminder } from '@lib/shared/api/modules/customer';

  const { t } = useI18n();
  const Message = useMessage();

  const props = defineProps<{
    customerId?: string;
    reminder?: CustomerReminderListItem | null;
  }>();

  const emit = defineEmits<{
    (e: 'saved'): void;
    (e: 'cancel'): void;
  }>();

  const showModal = defineModel<boolean>('show', {
    required: true,
  });

  const initForm: CustomerReminderAddParams & { id?: string } = {
    id: undefined,
    customerId: '',
    content: '',
    remindTime: undefined,
  };

  const reminder = ref<CustomerReminderAddParams & { id?: string }>(cloneDeep(initForm));
  const loading = ref(false);
  const formRef = ref<FormInst | null>(null);

  const rules: FormRules = {
    remindTime: [
      {
        trigger: ['change', 'blur'],
        required: true,
        message: t('customer.pleaseSelectReminderTime'),
      },
    ],
    content: [
      {
        trigger: ['input', 'blur'],
        required: true,
        message: t('customer.pleaseEnterReminderContent'),
      },
    ],
  };

  function disabledDate(timestamp: number): boolean {
    return timestamp < Date.now() - 24 * 60 * 60 * 1000;
  }

  function handleCancel() {
    showModal.value = false;
    reminder.value = cloneDeep(initForm);
    emit('cancel');
  }

  async function handleConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          loading.value = true;
          
          if (reminder.value.id) {
            const updateParams: CustomerReminderUpdateParams = {
              id: reminder.value.id,
              content: reminder.value.content,
              remindTime: reminder.value.remindTime,
            };
            await updateCustomerReminder(updateParams);
            Message.success(t('customer.reminderUpdateSuccess'));
          } else {
            const addParams: CustomerReminderAddParams = {
              customerId: props.customerId || '',
              content: reminder.value.content || '',
              remindTime: reminder.value.remindTime || 0,
            };
            await addCustomerReminder(addParams);
            Message.success(t('customer.reminderSetSuccess'));
          }
          
          emit('saved');
          handleCancel();
        } catch (error) {
          console.error(error);
        } finally {
          loading.value = false;
        }
      }
    });
  }

  watch(
    () => showModal.value,
    (val) => {
      if (val) {
        if (props.reminder) {
          reminder.value = {
            id: props.reminder.id,
            customerId: props.reminder.customerId,
            content: props.reminder.content,
            remindTime: props.reminder.remindTime,
          };
        } else {
          reminder.value = cloneDeep(initForm);
          reminder.value.customerId = props.customerId || '';
          reminder.value.remindTime = Date.now() + 30 * 60 * 1000;
        }
      }
    }
  );
</script>

<style lang="less" scoped></style>
