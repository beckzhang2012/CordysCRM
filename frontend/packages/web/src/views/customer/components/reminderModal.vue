<template>
  <CrmModal
    v-model:show="showModal"
    size="small"
    :title="t('common.setReminder')"
    show-icon
    :mask-closable="false"
    type="primary"
    :ok-loading="loading"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <n-form ref="formRef" :model="form" label-placement="left" require-mark-placement="left">
      <n-form-item path="reminderTime" :label="t('common.reminderTime')" required>
        <n-date-picker
          v-model:value="form.reminderTime"
          type="datetime"
          :placeholder="t('common.pleaseSelect')"
          clearable
          style="width: 100%"
        />
      </n-form-item>
      <n-form-item path="reminderContent" :label="t('common.reminderContent')" required>
        <n-input
          v-model:value="form.reminderContent"
          type="textarea"
          :placeholder="t('common.pleaseInput')"
          :rows="3"
          maxlength="500"
          show-count
        />
      </n-form-item>
    </n-form>
  </CrmModal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInst, NDatePicker, NForm, NFormItem, NInput } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmModal from '@/components/pure/crm-modal/index.vue';

  const { t } = useI18n();

  const props = defineProps<{
    customerId: string;
    customerName: string;
  }>();

  const showModal = defineModel<boolean>('show', {
    required: true,
    default: false,
  });

  const emit = defineEmits<{
    (e: 'success'): void;
  }>();

  const formRef = ref<FormInst | null>(null);
  const loading = ref(false);

  const form = ref({
    reminderTime: null as number | null,
    reminderContent: '',
  });

  function handleCancel() {
    showModal.value = false;
    form.value.reminderTime = null;
    form.value.reminderContent = '';
  }

  async function handleConfirm() {
    formRef.value?.validate(async (error) => {
      if (!error) {
        try {
          loading.value = true;
          // 这里需要调用后端API保存提醒
          // 由于没有专门的提醒API，暂时使用localStorage存储
          const reminders = JSON.parse(localStorage.getItem('customerReminders') || '[]');
          reminders.push({
            id: Date.now().toString(),
            customerId: props.customerId,
            customerName: props.customerName,
            reminderTime: form.value.reminderTime,
            reminderContent: form.value.reminderContent,
            isRead: false,
            createTime: Date.now(),
          });
          localStorage.setItem('customerReminders', JSON.stringify(reminders));

          // 触发自定义事件通知有新提醒
          window.dispatchEvent(new CustomEvent('newReminder'));

          emit('success');
          handleCancel();
        } catch (e) {
          console.error(e);
        } finally {
          loading.value = false;
        }
      }
    });
  }
</script>

<style scoped></style>
