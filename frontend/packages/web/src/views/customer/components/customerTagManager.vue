<template>
  <div class="customer-tag-manager p-[16px_24px]">
    <div class="mb-[12px] flex items-center justify-between">
      <span class="text-[14px] font-medium text-[var(--text-n1)]">{{ t('customer.tag') }}</span>
      <n-button
        v-if="!readonly"
        v-permission="['CUSTOMER_MANAGEMENT:UPDATE']"
        type="primary"
        size="small"
        @click="handleAddClick"
      >
        {{ t('common.add') }}
      </n-button>
    </div>
    <div class="flex flex-wrap gap-[8px]">
      <CrmTag
        v-for="tag in tags"
        :key="tag.id"
        :color="getTagColor(tag.color)"
        :closable="!readonly"
        @close="handleRemoveTag(tag)"
      >
        {{ tag.name }}
      </CrmTag>
      <n-tag
        v-if="tags.length === 0"
        class="border-dashed border-[var(--border-3)] bg-transparent text-[var(--text-n4)]"
      >
        {{ t('common.noData') }}
      </n-tag>
    </div>
  </div>
  <CrmModal
    v-model:show="addTagModalVisible"
    :title="t('customer.addTag')"
    :ok-loading="addTagLoading"
    @confirm="handleAddTags"
  >
    <n-form ref="formRef" :model="form" label-placement="left" label-width="auto">
      <n-form-item path="tags" :label="t('customer.tag')">
        <n-select
          v-model:value="form.tags"
          multiple
          filterable
          :loading="tagOptionsLoading"
          :options="tagOptions"
          :on-search="handleSearchTags"
          :placeholder="t('common.pleaseSelect', { value: t('customer.tag') })"
        />
      </n-form-item>
    </n-form>
  </CrmModal>
</template>

<script setup lang="ts">
  import { NButton, NForm, NFormItem, NSelect, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { CustomerTagItem } from '@lib/shared/models/customer';

  import CrmModal from '@/components/pure/crm-modal/index.vue';
  import CrmTag from '@/components/pure/crm-tag/index.vue';

  import { addTagsToCustomer, getCustomerTagOptions, getTagsByCustomerId, removeTagsFromCustomer } from '@/api/modules';

  const props = defineProps<{
    sourceId: string;
    readonly?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const { t } = useI18n();
  const Message = useMessage();

  const tags = ref<CustomerTagItem[]>([]);
  const tagsLoading = ref(false);

  const addTagModalVisible = ref(false);
  const addTagLoading = ref(false);
  const form = ref<{ tags: string[] }>({
    tags: [],
  });

  const tagOptions = ref<{ label: string; value: string }[]>([]);
  const tagOptionsLoading = ref(false);

  function getTagColor(color: string) {
    return {
      color,
      borderColor: color,
      textColor: color,
    };
  }

  async function loadTags() {
    try {
      tagsLoading.value = true;
      const result = await getTagsByCustomerId(props.sourceId);
      tags.value = result || [];
    } catch {
      // ignore error
    } finally {
      tagsLoading.value = false;
    }
  }

  async function loadTagOptions(keyword?: string) {
    try {
      tagOptionsLoading.value = true;
      const result = await getCustomerTagOptions(keyword);
      tagOptions.value = (result || []).map((item: { id: string; name: string }) => ({
        label: item.name,
        value: item.id,
      }));
    } catch {
      // ignore error
    } finally {
      tagOptionsLoading.value = false;
    }
  }

  function handleSearchTags(keyword: string) {
    loadTagOptions(keyword);
  }

  function handleAddClick() {
    form.value.tags = [];
    loadTagOptions();
    addTagModalVisible.value = true;
  }

  async function handleAddTags() {
    if (!form.value.tags || form.value.tags.length === 0) {
      Message.warning(t('common.pleaseSelect', { value: t('customer.tag') }));
      return;
    }
    try {
      addTagLoading.value = true;
      await addTagsToCustomer({
        customerId: props.sourceId,
        tagIds: form.value.tags,
      });
      Message.success(t('common.addSuccess'));
      addTagModalVisible.value = false;
      loadTags();
      emit('change');
    } catch {
      // ignore error
    } finally {
      addTagLoading.value = false;
    }
  }

  async function handleRemoveTag(tag: CustomerTagItem) {
    try {
      await removeTagsFromCustomer({
        customerId: props.sourceId,
        tagIds: [tag.id],
      });
      Message.success(t('common.removeSuccess'));
      loadTags();
      emit('change');
    } catch {
      // ignore error
    }
  }

  onMounted(() => {
    loadTags();
  });

  watch(
    () => props.sourceId,
    () => {
      loadTags();
    }
  );

  defineExpose({
    loadTags,
  });
</script>

<style lang="less" scoped>
  .customer-tag-manager {
    border-bottom: 1px solid var(--border-3);
  }
</style>
