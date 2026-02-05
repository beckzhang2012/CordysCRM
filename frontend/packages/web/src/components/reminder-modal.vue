<template>
  <n-modal
    :show="show"
    preset="card"
    title="设置提醒"
    :mask-closable="false"
    :closable="false"
    style="width: 500px"
    @update:show="handleClose"
  >
    <n-form ref="formRef" :model="formData" :rules="rules">
      <n-form-item path="remindTime" label="提醒时间">
        <n-date-picker v-model:value="formData.remindTime" type="datetime" placeholder="选择提醒时间" clearable />
      </n-form-item>

      <n-form-item path="content" label="提醒内容">
        <n-input v-model:value="formData.content" type="textarea" :rows="4" placeholder="请输入提醒内容" />
      </n-form-item>
    </n-form>

    <template #footer>
      <n-space justify="end">
        <n-button type="primary" :loading="loading" @click="handleSave"> 保存 </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue';

  import { addReminder } from '@/api/modules/reminder';

  import type { FormInst } from 'naive-ui';
  import type { FormRules } from 'naive-ui/es/form/src/interface';

  const props = defineProps<{
    show: boolean;
    sourceId: string;
    sourceName: string;
  }>();

  const emit = defineEmits<{
    'update:show': [value: boolean];
    'saved': [];
  }>();

  const formRef = ref<FormInst | null>(null);
  const loading = ref(false);

  const formData = reactive({
    remindTime: null as Date | null,
    content: '',
  });

  const rules = computed<FormRules>(() => ({
    remindTime: {
      required: true,
      message: '请选择提醒时间',
      trigger: ['blur', 'input'],
    },
    content: {
      required: true,
      message: '请输入提醒内容',
      trigger: ['blur', 'input'],
    },
  }));

  const resetForm = () => {
    formData.remindTime = null;
    formData.content = '';
  };

  watch(
    () => props.show,
    (val) => {
      if (val) {
        resetForm();
      }
    }
  );

  const handleClose = () => {
    formData.remindTime = null;
    formData.content = '';
  };

  const handleClose = () => {
    emit('update:show', false);
  };

  const handleSave = async () => {
    if (!formRef.value) return;

    await formRef.value.validate(async (errors) => {
      if (!errors) {
        loading.value = true;
        try {
          await addReminder({
            sourceId: props.sourceId,
            sourceName: props.sourceName,
            remindTime: formData.remindTime ? formData.remindTime.toISOString() : '',
            remindContent: formData.content,
          });
          emit('saved');
          handleClose();
        } catch (error) {
          // console.error('保存提醒失败', error);
        } finally {
          loading.value = false;
        }
      }
    });
  };
</script>
