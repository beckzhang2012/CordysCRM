<template>
  <div class="customer-tag-container">
    <div class="tag-header">
      <span class="tag-title">{{ t('customer.tag') }}</span>
      <n-button
        v-if="!readonly && hasAnyPermission(['CUSTOMER_MANAGEMENT:UPDATE'])"
        text
        type="primary"
        @click="showTagSelector = true"
      >
        <template #icon>
          <n-icon><Add /></n-icon>
        </template>
        {{ t('common.add') }}
      </n-button>
    </div>
    <div class="tag-content">
      <div v-if="customerTags.length === 0" class="no-tag">
        {{ t('customer.noTag') }}
      </div>
      <div v-else class="tag-list">
        <n-tag
          v-for="tag in customerTags"
          :key="tag.id"
          :color="{ color: tag.color, textColor: '#fff', borderColor: tag.color }"
          closable
          :disabled="readonly"
          @close="handleRemoveTag(tag)"
        >
          {{ tag.name }}
        </n-tag>
      </div>
    </div>

    <n-modal
      v-model:show="showTagSelector"
      preset="card"
      :title="t('customer.selectTag')"
      style="width: 500px"
      :bordered="false"
    >
      <div class="tag-selector">
        <n-input v-model:value="searchKeyword" :placeholder="t('customer.searchTag')" clearable class="mb-[12px]">
          <template #prefix>
            <n-icon><Search /></n-icon>
          </template>
        </n-input>

        <div v-if="filteredTags.length === 0" class="no-tag-available">
          <span>{{ t('customer.noTagAvailable') }}</span>
          <n-button text type="primary" @click="showAddTagModal = true">
            {{ t('customer.createTag') }}
          </n-button>
        </div>
        <div v-else class="tag-options">
          <n-checkbox-group v-model:value="selectedTagIds">
            <div v-for="tag in filteredTags" :key="tag.id" class="tag-option-item">
              <n-checkbox :value="tag.id">
                <div class="tag-option-content">
                  <span class="tag-color-dot" :style="{ backgroundColor: tag.color }"></span>
                  <span>{{ tag.name }}</span>
                </div>
              </n-checkbox>
            </div>
          </n-checkbox-group>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-between">
          <n-button @click="showAddTagModal = true">
            <template #icon>
              <n-icon><Add /></n-icon>
            </template>
            {{ t('customer.createTag') }}
          </n-button>
          <div>
            <n-button @click="showTagSelector = false">{{ t('common.cancel') }}</n-button>
            <n-button type="primary" class="ml-[12px]" :loading="bindLoading" @click="handleBindTags">
              {{ t('common.confirm') }}
            </n-button>
          </div>
        </div>
      </template>
    </n-modal>

    <n-modal
      v-model:show="showAddTagModal"
      preset="card"
      :title="t('customer.createTag')"
      style="width: 400px"
      :bordered="false"
    >
      <n-form ref="formRef" :model="newTagForm" :rules="formRules">
        <n-form-item :label="t('customer.tagName')" path="name">
          <n-input v-model:value="newTagForm.name" :placeholder="t('customer.tagNamePlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('customer.tagColor')" path="color">
          <div class="color-picker">
            <div
              v-for="color in presetColors"
              :key="color"
              class="color-item"
              :class="{ active: newTagForm.color === color }"
              :style="{ backgroundColor: color }"
              @click="newTagForm.color = color"
            ></div>
          </div>
        </n-form-item>
      </n-form>

      <template #footer>
        <div class="flex justify-end">
          <n-button @click="showAddTagModal = false">{{ t('common.cancel') }}</n-button>
          <n-button type="primary" class="ml-[12px]" :loading="addTagLoading" @click="handleAddTag">
            {{ t('common.confirm') }}
          </n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import {
    NButton,
    NCheckbox,
    NCheckboxGroup,
    NForm,
    NFormItem,
    NIcon,
    NInput,
    NModal,
    NTag,
    useMessage,
  } from 'naive-ui';
  import { Add, Search } from '@vicons/ionicons5';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import {
    addCustomerTag,
    bindCustomerTag,
    getCustomerTagList,
    getCustomerTagsByCustomerId,
    unbindCustomerTag,
  } from '@/api/modules';
  import { hasAnyPermission } from '@/utils/permission';

  interface TagItem {
    id: string;
    name: string;
    color: string;
    createTime: number;
  }

  const props = defineProps<{
    sourceId: string;
    readonly?: boolean;
  }>();

  const { t } = useI18n();
  const Message = useMessage();

  const customerTags = ref<TagItem[]>([]);
  const allTags = ref<TagItem[]>([]);
  const showTagSelector = ref(false);
  const showAddTagModal = ref(false);
  const searchKeyword = ref('');
  const selectedTagIds = ref<string[]>([]);
  const bindLoading = ref(false);
  const addTagLoading = ref(false);

  const newTagForm = ref({
    name: '',
    color: '#1890ff',
  });

  const formRef = ref();
  const formRules = {
    name: [{ required: true, message: t('customer.tagNameRequired'), trigger: 'blur' }],
  };

  const presetColors = [
    '#1890ff',
    '#52c41a',
    '#faad14',
    '#f5222d',
    '#722ed1',
    '#13c2c2',
    '#eb2f96',
    '#fa8c16',
    '#2f54eb',
    '#a0d911',
  ];

  const filteredTags = computed(() => {
    if (!searchKeyword.value) {
      return allTags.value;
    }
    return allTags.value.filter((tag) => tag.name.toLowerCase().includes(searchKeyword.value.toLowerCase()));
  });

  async function loadCustomerTags() {
    try {
      const res = await getCustomerTagsByCustomerId(props.sourceId);
      customerTags.value = res || [];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  async function loadAllTags() {
    try {
      const res = await getCustomerTagList();
      allTags.value = res || [];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  async function handleBindTags() {
    if (selectedTagIds.value.length === 0) {
      showTagSelector.value = false;
      return;
    }

    try {
      bindLoading.value = true;
      const existingTagIds = customerTags.value.map((tag) => tag.id);
      const allTagIds = [...new Set([...existingTagIds, ...selectedTagIds.value])];
      await bindCustomerTag({
        customerId: props.sourceId,
        tagIds: allTagIds,
      });
      await loadCustomerTags();
      showTagSelector.value = false;
      selectedTagIds.value = [];
      Message.success(t('common.success'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      bindLoading.value = false;
    }
  }

  async function handleRemoveTag(tag: TagItem) {
    try {
      await unbindCustomerTag(props.sourceId, tag.id);
      customerTags.value = customerTags.value.filter((item) => item.id !== tag.id);
      Message.success(t('common.success'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  async function handleAddTag() {
    try {
      await formRef.value?.validate();
      addTagLoading.value = true;
      const res = await addCustomerTag({
        name: newTagForm.value.name,
        color: newTagForm.value.color,
      });
      await loadAllTags();
      selectedTagIds.value.push(res.id);
      newTagForm.value = { name: '', color: '#1890ff' };
      showAddTagModal.value = false;
      Message.success(t('common.success'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      addTagLoading.value = false;
    }
  }

  onMounted(() => {
    loadCustomerTags();
    loadAllTags();
  });

  defineExpose({
    loadCustomerTags,
  });
</script>

<style lang="less" scoped>
  .customer-tag-container {
    padding: 16px 24px;
    border-bottom: 1px solid var(--n-border-color);
    .tag-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      .tag-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--n-text-color);
      }
    }
    .tag-content {
      .no-tag {
        font-size: 13px;
        color: var(--n-text-color-disabled);
      }
      .tag-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
    }
  }
  .tag-selector {
    .no-tag-available {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 20px 0;
      color: var(--n-text-color-disabled);
    }
    .tag-options {
      overflow-y: auto;
      max-height: 300px;
      .tag-option-item {
        padding: 8px 0;
        border-bottom: 1px solid var(--n-border-color);
        &:last-child {
          border-bottom: none;
        }
        .tag-option-content {
          display: flex;
          align-items: center;
          gap: 8px;
          .tag-color-dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;
          }
        }
      }
    }
  }
  .color-picker {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    .color-item {
      width: 24px;
      height: 24px;
      border: 2px solid transparent;
      border-radius: 4px;
      transition: all 0.2s;
      cursor: pointer;
      &:hover {
        transform: scale(1.1);
      }
      &.active {
        border-color: var(--n-text-color);
      }
    }
  }
</style>
