<template>
  <div>
    <n-modal v-model:show="show" preset="card" title="u8BB0u4F4Fu63D0u9192" style="width: 600px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="120">
        <n-form-item path="remindTime" label="u63D0u9192u65F6u95F4" style="width: 100%">
          <n-date-picker v-model:value="form.remindTime" type="datetime" clearable @update:value="handleTimeChange" />
        </n-form-item>
        <n-form-item path="remindContent" label="u63D0u9192u5185u5BB9" style="width: 100%">
          <n-input v-model:value="form.remindContent" placeholder="u8BF7u8F93u5165u63D0u9192u5185u5BB9" :max-length="500" show-count :input-style="{ width: '100%' }" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button type="primary" @click="handleConfirm" :loading="confirmLoading">u786Eu8BA4</n-button>
          <n-button @click="handleCancel">u53D6u6D88</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed } from 'vue';
  import { useMessage } from 'naive-ui';
  import { FormRules } from 'naive-ui/lib/form/src/interface';
  import dayjs from 'dayjs';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import { addReminder } from '@/api/modules/reminder';

  const props = defineProps<{
    show: boolean;
    sourceId: string;
    sourceName: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:show', show: boolean): void;
    (e: 'saved'): void;
  }>();

  const formRef = ref();
  const confirmLoading = ref(false);
  const Message = useMessage();
  const { t } = useI18n();

  const form = reactive({
    remindTime: null,
    remindContent: '',
  });

  const rules: FormRules = {
    remindTime: {
      required: true,
      message: '请选择提醒时间',
      trigger: 'change',
    },
    remindContent: {
      required: true,
      message: '请输入提醒内容',
      trigger: 'blur',
    },
  };

  const show = computed({
    get: () => props.show,
    set: (value) => emit('update:show', value),
  });

  const handleTimeChange = (val: any) => {
    form.remindTime = val;
  };

  const handleConfirm = async () => {
    await formRef.value?.validate();
    confirmLoading.value = true;

    try {
      const params = {
        sourceId: props.sourceId,
        sourceName: props.sourceName,
        remindTime: dayjs(form.remindTime).format('YYYY-MM-DD HH:mm:ss'),
        remindContent: form.remindContent,
      };
      await addReminder(params);
      Message.success('提醒设置成功');
      emit('saved');
      show.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      confirmLoading.value = false;
    }
  };

  const handleCancel = () => {
    show.value = false;
  };
</script>

<style lang="less