<template>
  <CrmModal
    v-model:show="showModal"
    :title="t('customer.setReminder')"
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
          :label="t('customer.reminderTime')"
        >
          <n-date-picker
            v-model:value="form.reminderTime"
            class="w-full"
            type="datetime"
            clearable
            :placeholder="t('customer.selectReminderTime')"
          >
            <template #date-icon>
              <CrmIcon class="text-[var(--text-n4)]" type="iconicon_time" :size="16" />
            </template>
          </n-date-picker>
        </n-form-item>
        <n-form-item
          require-mark-placement="left"
          label-placement="left"
          path="reminderContent"
          :label="t('customer.reminderContent')"
        >
          <n-input
            v-model:value="form.reminderContent"
            type="textarea"
            :maxlength="255"
            :placeholder="t('customer.pleaseInputReminderContent')"
            :autosize="{ minRows: 3, maxRows: 5 }"
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

  import CrmModal from '@/components/pure/crm-modal/index.vue';
  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import { reminderService } from '@/utils/reminderService';

  const Message = useMessage();
  const { t } = useI18n();

  const props = defineProps<{
    customerId?: string;
    customerName?: string;
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
      return new Error(t('customer.pleaseSelectReminderTime'));
    }
    const now = Date.now();
    if (value < now) {
      return new Error(t('customer.reminderTimeCannotBeInPast'));
    }
    return true;
  }

  const rules: FormRules = {
    reminderTime: [{ trigger: ['input', 'blur'], required: true, validator: validateReminderTime }],
    reminderContent: [
      {
        trigger: ['input', 'blur'],
        required: true,
        message: t('customer.pleaseInputReminderContent'),
      },
    ],
  };

  const initForm = {
    reminderTime: 0,
    reminderContent: '',
  };

  const form = ref(cloneDeep(initForm));
  const loading = ref<boolean>(false);
  const formRef = ref<FormInst | null>(null);

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
          
          // 使用提醒服务保存提醒数据
          reminderService.addReminder({
            customerId: props.customerId || '',
            customerName: props.customerName || '',
            reminderTime: form.value.reminderTime as number,
            reminderContent: form.value.reminderContent,
            isActive: true,
          });
          
          Message.success(t('customer.reminderSetSuccess'));
          loading.value = false;
          emit('saved');
          handleCancel();
        } catch (error) {
          console.error('设置提醒失败:', error);
          Message.error(t('customer.reminderSetFailed'));
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
        // 重置表单
        form.value = cloneDeep(initForm);
      }
    }
  );
</script>

<style lang="less" scoped></style>