<template>
  <div class="customer-tags-container">
    <div class="tags-header">
      <span class="title">{{ t('customer.tags') }}</span>
      <van-button
        v-if="!readonly"
        size="small"
        type="primary"
        plain
        @click="showAddDialog = true"
      >
        {{ t('customer.addTag') }}
      </van-button>
    </div>

    <div class="tags-list">
      <van-tag
        v-for="tag in tags"
        :key="tag"
        type="primary"
        @click="handleTagClick(tag)"
      >
        {{ tag }}
        <van-icon
          v-if="!readonly"
          name="cross"
          class="tag-close"
          @click.stop="handleRemoveTag(tag)"
        />
      </van-tag>
      <div v-if="tags.length === 0" class="empty-tags">
        {{ t('customer.noTags') }}
      </div>
    </div>

    <van-dialog
      v-model:show="showAddDialog"
      :title="t('customer.addTag')"
      show-cancel-button
      @confirm="handleAddTag"
    >
      <van-field
        v-model="newTag"
        :placeholder="t('customer.inputTag')"
        :rules="[{ required: true, message: t('customer.inputTag') }]"
        @keyup.enter="handleAddTag"
      />
      <div class="suggestions" v-if="suggestions.length > 0">
        <span class="suggestions-title">{{ t('customer.suggestedTags') }}</span>
        <van-tag
          v-for="tag in suggestions"
          :key="tag"
          type="primary"
          plain
          class="suggestion-tag"
          @click="selectSuggestion(tag)"
        >
          {{ tag }}
        </van-tag>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { showSuccessToast, showConfirmDialog } from 'vant';

import { useI18n } from '@lib/shared/hooks/useI18n';

import { getCustomerTags, saveCustomerTags, deleteCustomerTags, searchCustomerTags } from '@/api/modules/customer';

const props = defineProps<{
  customerId: string;
  readonly?: boolean;
}>();

const { t } = useI18n();

const tags = ref<string[]>([]);
const showAddDialog = ref(false);
const newTag = ref('');
const suggestions = ref<string[]>([]);

onMounted(() => {
  loadTags();
  loadSuggestions();
});

async function loadTags() {
  try {
    const res = await getCustomerTags(props.customerId);
    tags.value = res.tags || [];
  } catch (error) {
    console.error('Failed to load tags:', error);
  }
}

async function loadSuggestions() {
  try {
    const res = await searchCustomerTags({ keyword: '' });
    suggestions.value = res.tags || [];
  } catch (error) {
    console.error('Failed to load suggestions:', error);
  }
}

function handleTagClick(tag: string) {
}

function handleRemoveTag(tag: string) {
  showConfirmDialog({
    title: t('customer.removeTagTitle'),
    message: t('customer.removeTagConfirm', { tag }),
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    async beforeClose(action) {
      if (action === 'confirm') {
        try {
          await deleteCustomerTags({
            customerId: props.customerId,
            tags: [tag],
          });
          tags.value = tags.value.filter((t) => t !== tag);
          showSuccessToast(t('customer.removeTagSuccess'));
        } catch (error) {
          console.error('Failed to remove tag:', error);
        }
      }
      return true;
    },
  });
}

function selectSuggestion(tag: string) {
  newTag.value = tag;
}

async function handleAddTag() {
  if (!newTag.value.trim()) {
    return;
  }

  const tag = newTag.value.trim();
  if (tags.value.includes(tag)) {
    showSuccessToast(t('customer.tagAlreadyExists'));
    showAddDialog.value = false;
    newTag.value = '';
    return;
  }

  try {
    await saveCustomerTags({
      customerId: props.customerId,
      tags: [...tags.value, tag],
    });
    tags.value.push(tag);
    showSuccessToast(t('customer.addTagSuccess'));
    showAddDialog.value = false;
    newTag.value = '';
  } catch (error) {
    console.error('Failed to add tag:', error);
  }
}
</script>

<style scoped>
.customer-tags-container {
  padding: 16px;
}

.tags-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-close {
  margin-left: 4px;
}

.empty-tags {
  color: var(--gray-6);
  font-size: 14px;
}

.suggestions {
  margin-top: 16px;
}

.suggestions-title {
  display: block;
  font-size: 14px;
  color: var(--gray-6);
  margin-bottom: 8px;
}

.suggestion-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}
</style>
