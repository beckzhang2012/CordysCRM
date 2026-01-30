<template>
  <CrmModal
    v-model:show="showModal"
    :title="t('customer.reminder.setReminder')"
    :ok-loading="loading"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <div>
      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="left"
        :label-width="100"
        require-mark-placement="left"
      >
        <n-form-item
          require-mark-placement="left"
          label-placement="left"
          path="reminderTime"
          :label="t('customer.reminder.reminderTime')"
        >
          <n-date-picker
            v-model:value="form.reminderTime"
            class="w-[340px]"
            type="datetime"
            clearable
            :is-disabled="isDisabledDate"
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
          :label="t('customer.reminder.reminderContent')"
        >
          <n-input
            v-model:value="form.content"
            :maxlength="500"
            type="textarea"
            :placeholder="t('common.pleaseInput')"
            :autosize="{ minRows: 3, maxRows: 6 }"
          />
        </n-form-item>
      </n-form>
    </div>
  </CrmModal>
</template>

<script lang="ts" setup>
  import { FormInst, FormItemRule, FormRules, NDatePicker, NForm, NFormItem, NInput, useMessage } from 'naive-ui';
  import { cloneDeep } from 'lodash-es';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { ReminderAddParams } from '@lib/shared/models/reminder';

  import CrmModal from '@/components/pure/crm-modal/index.vue';

  import { addReminder } from '@/api/modules';

  const Message = useMessage();

  const { t } = useI18n();

  const props = defineProps<{
    customerId: string;
    customerName: string;
  }>();

  const emit = defineEmits<{
    (e: 'saved'): void;
    (e: 'cancel'): void;
  }>();

  const showModal = defineModel<boolean>('show', {
    required: true,
  });

  function validateReminderTime(rule: FormItemRule, value: number) {
    if (!value) {
      return new Error(t('common.notNull', { value: t('customer.reminder.reminderTime') }));
    }
    const now = Date.now();
    if (value < now) {
      return new Error(t('common.dateCannotBeInPast'));
    }
    return true;
  }

  const rules: FormRules = {
    reminderTime: [{ trigger: ['blur', 'change'], required: true, validator: validateReminderTime }],
    content: [
      {
        trigger: ['input', 'blur'],
        required: true,
        message: t('common.notNull', { value: t('customer.reminder.reminderContent') }),
      },
    ],
  };

  const initForm = {
    reminderTime: null as number | null,
    content: '',
  };

  const form = ref(cloneDeep(initForm));

  const loading = ref<boolean>(false);
  const formRef = ref<FormInst | null>(null);

  function isDisabledDate(timestamp: number) {
    return timestamp < Date.now() - 86400000;
  }

  function handleCancel() {
    showModal.value = false;
    form.value = cloneDeep(initForm);
    emit('cancel');
  }

  async function handleConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          loading.value = true;
          const params: ReminderAddParams = {
            customerId: props.customerId,
            customerName: props.customerName,
            reminderTime: form.value.reminderTime!,
            content: form.value.content,
            receiver: '',
          };
          await addReminder(params);
          Message.success(t('common.addSuccess'));
          loading.value = false;
          emit('saved');
          handleCancel();
        } catch (error) {
          loading.value = false;
          console.log(error);
        }
      }
    });
  }

  watch(
    () => showModal.value,
    (val) => {
      if (val) {
        form.value = cloneDeep(initForm);
      }
    }
  );
</script>
