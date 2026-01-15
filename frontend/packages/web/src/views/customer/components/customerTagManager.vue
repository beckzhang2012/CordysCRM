<template>
  <div class="customer-tag-manager">
    <div class="tag-list">
      <n-space>
        <n-tag
          v-for="tag in customerTags"
          :key="tag.id"
          :color="{ color: tag.color, textColor: '#fff' }"
          closable
          @close="handleRemoveTag(tag.id)"
        >
          {{ tag.name }}
        </n-tag>
      </n-space>
      <n-button
        v-if="!showAddTag"
        text
        type="primary"
        @click="showAddTag = true"
      >
        + 添加标签
      </n-button>
    </div>
    <div v-if="showAddTag" class="add-tag-form">
      <n-space>
        <n-input
          v-model:value="newTagName"
          placeholder="输入标签名称"
          style="width: 200px"
          @keyup.enter="handleAddTag"
        />
        <n-color-picker
          v-model:value="newTagColor"
          :modes="['hex']"
          style="width: 80px"
        />
        <n-button type="primary" @click="handleAddTag">
          确定
        </n-button>
        <n-button @click="showAddTag = false">
          取消
        </n-button>
      </n-space>
    </div>
    <div v-if="showTagSelector" class="tag-selector">
      <n-select
        v-model:value="selectedTagIds"
        :options="tagOptions"
        multiple
        filterable
        placeholder="选择标签"
        style="width: 300px"
        @update:value="handleTagSelect"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch, onMounted } from 'vue';
  import { NButton, NInput, NColorPicker, NSelect, NSpace, NTag, useMessage } from 'naive-ui';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import type { CustomerTag } from '@lib/shared/models/customer';

  import {
    getCustomerTagList,
    getCustomerTagByCustomerId,
    saveCustomerTag,
    deleteCustomerTag,
    updateCustomerTags,
  } from '@/api/modules';

  const props = defineProps<{
    customerId: string;
    readonly?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update'): void;
  }>();

  const { t } = useI18n();
  const Message = useMessage();

  const allTags = ref<CustomerTag[]>([]);
  const customerTags = ref<CustomerTag[]>([]);
  const selectedTagIds = ref<string[]>([]);
  const showAddTag = ref(false);
  const showTagSelector = ref(false);
  const newTagName = ref('');
  const newTagColor = ref('#18a058');

  const tagOptions = computed(() => {
    return allTags.value.map(tag => ({
      label: tag.name,
      value: tag.id,
      style: `background-color: ${tag.color}; color: #fff;`,
    }));
  });

  async function loadAllTags() {
    try {
      const res = await getCustomerTagList();
      allTags.value = res.data || [];
    } catch (error) {
      console.error('加载标签列表失败:', error);
    }
  }

  async function loadCustomerTags() {
    try {
      const res = await getCustomerTagByCustomerId(props.customerId);
      customerTags.value = res.data || [];
      selectedTagIds.value = customerTags.value.map(tag => tag.id);
    } catch (error) {
      console.error('加载客户标签失败:', error);
    }
  }

  async function handleAddTag() {
    if (!newTagName.value.trim()) {
      Message.warning('请输入标签名称');
      return;
    }

    try {
      const res = await saveCustomerTag({
        name: newTagName.value.trim(),
        color: newTagColor.value,
      });

      if (res.data) {
        allTags.value.push(res.data);
        selectedTagIds.value.push(res.data.id);
        await applyTagUpdates();
        Message.success('标签添加成功');
        newTagName.value = '';
        showAddTag.value = false;
      }
    } catch (error) {
      Message.error('标签添加失败');
      console.error(error);
    }
  }

  async function handleRemoveTag(tagId: string) {
    try {
      selectedTagIds.value = selectedTagIds.value.filter(id => id !== tagId);
      await applyTagUpdates();
      Message.success('标签移除成功');
    } catch (error) {
      Message.error('标签移除失败');
      console.error(error);
    }
  }

  async function handleTagSelect() {
    await applyTagUpdates();
  }

  async function applyTagUpdates() {
    try {
      await updateCustomerTags({
        customerId: props.customerId,
        tagIds: selectedTagIds.value,
      });
      await loadCustomerTags();
      emit('update');
    } catch (error) {
      Message.error('更新标签失败');
      console.error(error);
    }
  }

  onMounted(() => {
    loadAllTags();
    loadCustomerTags();
  });
</script>

<style lang="less" scoped>
  .customer-tag-manager {
    .tag-list {
      margin-bottom: 12px;
    }

    .add-tag-form {
      margin-top: 12px;
      padding: 12px;
      background: var(--n-color);
      border-radius: 4px;
    }

    .tag-selector {
      margin-top: 12px;
    }
  }
</style>
