<template>
  <div class="customer-tag-manager">
    <div class="tag-header">
      <span class="tag-title">{{ t('customer.tag.title') }}</span>
      <n-button v-if="!readonly" type="primary" size="small" @click="handleAddTag">
        <template #icon>
          <icon-plus />
        </template>
        {{ t('customer.tag.add') }}
      </n-button>
    </div>

    <div class="tag-list">
      <n-tag
        v-for="tag in customerTags"
        :key="tag.id"
        :color="{ color: tag.color, textColor: '#fff' }"
        closable
        :disabled="readonly"
        @close="handleRemoveTag(tag)"
      >
        {{ tag.name }}
      </n-tag>
      <n-empty v-if="customerTags.length === 0" :description="t('customer.tag.empty')" size="small" />
    </div>

    <!-- 添加/编辑标签弹窗 -->
    <n-modal
      v-model:show="showTagModal"
      :title="isEdit ? t('customer.tag.edit') : t('customer.tag.add')"
      preset="dialog"
      :positive-text="t('common.save')"
      :negative-text="t('common.cancel')"
      @positive-click="handleSaveTag"
      @negative-click="handleCancel"
    >
      <n-form ref="formRef" :model="formData" :rules="rules">
        <n-form-item :label="t('customer.tag.name')" path="name">
          <n-input
            v-model:value="formData.name"
            :placeholder="t('customer.tag.namePlaceholder')"
            maxlength="20"
            show-count
          />
        </n-form-item>
        <n-form-item :label="t('customer.tag.color')" path="color">
          <div class="color-picker">
            <div
              v-for="color in presetColors"
              :key="color"
              class="color-item"
              :style="{ backgroundColor: color }"
              :class="{ active: formData.color === color }"
              @click="formData.color = color"
            />
          </div>
        </n-form-item>
      </n-form>
    </n-modal>

    <!-- 选择标签弹窗 -->
    <n-modal
      v-model:show="showSelectModal"
      :title="t('customer.tag.select')"
      preset="dialog"
      :positive-text="t('common.confirm')"
      :negative-text="t('common.cancel')"
      @positive-click="handleConfirmSelect"
      @negative-click="handleCancelSelect"
    >
      <n-input
        v-model:value="searchKeyword"
        :placeholder="t('customer.tag.searchPlaceholder')"
        clearable
        class="search-input"
        @input="handleSearch"
      >
        <template #prefix>
          <icon-search />
        </template>
      </n-input>

      <div class="available-tags">
        <n-checkbox-group v-model:value="selectedTagIds">
          <n-space>
            <n-checkbox v-for="tag in filteredTags" :key="tag.id" :value="tag.id" class="tag-checkbox">
              <n-tag :color="{ color: tag.color, textColor: '#fff' }">
                {{ tag.name }}
              </n-tag>
            </n-checkbox>
          </n-space>
        </n-checkbox-group>
        <n-empty v-if="filteredTags.length === 0" :description="t('customer.tag.noResult')" size="small" />
      </div>

      <n-button type="primary" ghost class="create-new-btn" @click="handleCreateNew">
        <template #icon>
          <icon-plus />
        </template>
        {{ t('customer.tag.createNew') }}
      </n-button>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { NButton, NColorPicker, NEmpty, NForm, NFormItem, NInput, NModal, NSelect, NTag, useMessage } from 'naive-ui';

  import type { CustomerTagItem } from '@lib/shared/models/customer';

  import {
    addCustomerTag,
    bindCustomerTags,
    deleteCustomerTag,
    getCustomerTagList,
    getCustomerTags,
    updateCustomerTag,
  } from '@/api/modules';

  const props = defineProps<{
    customerId: string;
    readonly?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update'): void;
  }>();

  const { t } = useI18n();
  const message = useMessage();

  const customerTags = ref<CustomerTagItem[]>([]);
  const allTags = ref<CustomerTagItem[]>([]);
  const showTagModal = ref(false);
  const showSelectModal = ref(false);
  const isEdit = ref(false);
  const formRef = ref<any>(null);
  const searchKeyword = ref('');
  const selectedTagIds = ref<string[]>([]);

  const presetColors = [
    '#1677FF',
    '#52C41A',
    '#FAAD14',
    '#F5222D',
    '#722ED1',
    '#EB2F96',
    '#13C2C2',
    '#FA8C16',
    '#1890FF',
    '#2F54EB',
    '#FADB14',
    '#A0D911',
  ];

  const formData = ref({
    id: '',
    name: '',
    color: presetColors[0],
  });

  const rules = {
    name: [
      { required: true, message: t('customer.tag.nameRequired'), trigger: 'blur' },
      { max: 20, message: t('customer.tag.nameMaxLength'), trigger: 'blur' },
    ],
  };

  const filteredTags = computed(() => {
    if (!searchKeyword.value) return allTags.value;
    return allTags.value.filter((tag) => tag.name.toLowerCase().includes(searchKeyword.value.toLowerCase()));
  });

  // 加载客户标签
  async function loadCustomerTags() {
    try {
      const res = await getCustomerTags(props.customerId);
      customerTags.value = res.data || [];
    } catch (error) {
      console.error('Failed to load customer tags:', error);
    }
  }

  // 加载所有标签
  async function loadAllTags() {
    try {
      const res = await getCustomerTagList();
      allTags.value = res.data || [];
    } catch (error) {
      console.error('Failed to load all tags:', error);
    }
  }

  // 添加标签
  function handleAddTag() {
    isEdit.value = false;
    formData.value = {
      id: '',
      name: '',
      color: presetColors[0],
    };
    selectedTagIds.value = customerTags.value.map((tag) => tag.id);
    showSelectModal.value = true;
    loadAllTags();
  }

  // 移除标签
  async function handleRemoveTag(tag: CustomerTagItem) {
    try {
      // 从已选标签中移除
      selectedTagIds.value = selectedTagIds.value.filter((id) => id !== tag.id);
      await bindCustomerTags({
        customerId: props.customerId,
        tagIds: selectedTagIds.value,
      });
      message.success(t('customer.tag.removeSuccess'));
      await loadCustomerTags();
      emit('update');
    } catch (error) {
      message.error(t('customer.tag.removeFailed'));
    }
  }

  // 保存标签
  async function handleSaveTag() {
    try {
      await formRef.value?.validate();

      if (isEdit.value) {
        await updateCustomerTag({
          id: formData.value.id,
          name: formData.value.name,
          color: formData.value.color,
        });
        message.success(t('customer.tag.updateSuccess'));
      } else {
        await addCustomerTag({
          name: formData.value.name,
          color: formData.value.color,
        });
        message.success(t('customer.tag.addSuccess'));
      }

      showTagModal.value = false;
      await loadAllTags();
      return true;
    } catch (error) {
      return false;
    }
  }

  // 取消
  function handleCancel() {
    showTagModal.value = false;
  }

  // 搜索
  function handleSearch() {
    // 搜索逻辑在 computed 中处理
  }

  // 确认选择
  async function handleConfirmSelect() {
    try {
      await bindCustomerTags({
        customerId: props.customerId,
        tagIds: selectedTagIds.value,
      });
      message.success(t('customer.tag.bindSuccess'));
      showSelectModal.value = false;
      await loadCustomerTags();
      emit('update');
    } catch (error) {
      message.error(t('customer.tag.bindFailed'));
    }
  }

  // 取消选择
  function handleCancelSelect() {
    showSelectModal.value = false;
  }

  // 创建新标签
  function handleCreateNew() {
    showSelectModal.value = false;
    isEdit.value = false;
    formData.value = {
      id: '',
      name: searchKeyword.value,
      color: presetColors[0],
    };
    showTagModal.value = true;
  }

  watch(
    () => props.customerId,
    () => {
      if (props.customerId) {
        loadCustomerTags();
      }
    },
    { immediate: true }
  );

  onMounted(() => {
    if (props.customerId) {
      loadCustomerTags();
    }
  });
</script>

<style lang="less" scoped>
  .customer-tag-manager {
    padding: 16px;
  }
  .tag-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }
  .tag-title {
    font-size: 14px;
    font-weight: 500;
  }
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    min-height: 60px;
  }
  .color-picker {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  .color-item {
    width: 24px;
    height: 24px;
    border: 2px solid transparent;
    border-radius: 4px;
    cursor: pointer;
    &.active {
      border-color: #000000;
    }
  }
  .search-input {
    margin-bottom: 16px;
  }
  .available-tags {
    overflow-y: auto;
    margin-bottom: 16px;
    max-height: 200px;
  }
  .tag-checkbox {
    :deep(.n-checkbox__label) {
      padding: 0;
    }
  }
  .create-new-btn {
    width: 100%;
  }
</style>
