<template>
  <div class="customer-tag-manage">
    <div class="tag-header">
      <span class="tag-title">{{ t('customer.tag.title') }}</span>
      <n-button
        v-if="!readonly"
        text
        type="primary"
        size="small"
        @click="showTagModal = true"
      >
        <template #icon>
          <CrmIcon type="iconicon_add" :size="14" />
        </template>
        {{ t('customer.tag.add') }}
      </n-button>
    </div>
    <div class="tag-content">
      <n-empty v-if="customerTags.length === 0" :description="t('customer.tag.empty')" size="small" />
      <n-space v-else :size="8" wrap>
        <n-tag
          v-for="tag in customerTags"
          :key="tag.id"
          :closable="!readonly"
          @close="handleRemoveTag(tag)"
        >
          {{ tag.name }}
        </n-tag>
      </n-space>
    </div>

    <!-- 标签管理弹窗 -->
    <n-modal
      v-model:show="showTagModal"
      :title="t('customer.tag.manageTitle')"
      preset="dialog"
      :show-icon="false"
      style="width: 500px"
      @after-leave="handleModalClose"
    >
      <div class="tag-modal-content">
        <!-- 搜索和新建 -->
        <div class="tag-search-area">
          <n-input
            v-model:value="searchKeyword"
            :placeholder="t('customer.tag.searchPlaceholder')"
            clearable
          >
            <template #prefix>
              <CrmIcon type="iconicon_search" :size="16" />
            </template>
          </n-input>
          <n-button type="primary" @click="handleCreateTag">
            <template #icon>
              <CrmIcon type="iconicon_add" :size="14" />
            </template>
            {{ t('customer.tag.createNew') }}
          </n-button>
        </div>

        <!-- 新建标签输入框 -->
        <div v-if="isCreating" class="tag-create-area">
          <n-input
            ref="createInputRef"
            v-model:value="newTagName"
            :placeholder="t('customer.tag.inputName')"
            maxlength="50"
            show-count
            @keyup.enter="confirmCreateTag"
          />
          <n-button type="primary" size="small" @click="confirmCreateTag">
            {{ t('common.confirm') }}
          </n-button>
          <n-button size="small" @click="cancelCreate">
            {{ t('common.cancel') }}
          </n-button>
        </div>

        <!-- 标签列表 -->
        <div class="tag-list-area">
          <div class="tag-list-title">{{ t('customer.tag.allTags') }}</div>
          <n-spin :show="loading">
            <div class="tag-list">
              <n-empty v-if="filteredTags.length === 0" :description="t('common.noData')" size="small" />
              <n-checkbox-group v-else v-model:value="selectedTagIds">
                <n-space vertical :size="8">
                  <div
                    v-for="tag in filteredTags"
                    :key="tag.id"
                    class="tag-item"
                  >
                    <n-checkbox :value="tag.id">
                      <n-tag>{{ tag.name }}</n-tag>
                    </n-checkbox>
                    <n-popconfirm
                      :show-icon="false"
                      :positive-text="t('common.confirmDelete')"
                      :negative-text="t('common.cancel')"
                      @positive-click="handleDeleteTag(tag)"
                    >
                      <template #trigger>
                        <n-button text type="error" size="tiny">
                          <CrmIcon type="iconicon_delete" :size="14" />
                        </n-button>
                      </template>
                      <template #default>
                        <div>{{ t('customer.tag.deleteConfirm') }}</div>
                        <div class="text-gray-400 text-xs mt-1">
                          {{ t('customer.tag.deleteContent', { name: tag.name }) }}
                        </div>
                      </template>
                    </n-popconfirm>
                  </div>
                </n-space>
              </n-checkbox-group>
            </div>
          </n-spin>
        </div>

        <!-- 已选标签 -->
        <div v-if="selectedTagIds.length > 0" class="tag-selected-area">
          <div class="tag-selected-title">
            {{ t('customer.tag.selected') }} ({{ selectedTagIds.length }})
          </div>
          <n-space :size="8" wrap>
            <n-tag
              v-for="tagId in selectedTagIds"
              :key="tagId"
              closable
              @close="handleUnselectTag(tagId)"
            >
              {{ getTagNameById(tagId) }}
            </n-tag>
          </n-space>
        </div>
      </div>

      <template #action>
        <n-button @click="showTagModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="saving" @click="handleSave">
          {{ t('common.save') }}
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
/* eslint-disable no-shadow */
import { ref, computed, watch, nextTick } from 'vue';
import { useMessage } from 'naive-ui';
import { useI18n } from '@lib/shared/hooks/useI18n';
import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
import {
  getCustomerTagList,
  getCustomerTagsByCustomerId,
  addCustomerTag,
  deleteCustomerTag,
  setCustomerTags,
  removeTagFromCustomer,
} from '@/api/modules';

  // eslint-disable-next-line no-shadow
  interface TagItem {
    id: string;
    name: string;
  }

  const props = defineProps<{
    customerId: string;
    readonly?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update'): void;
  }>();

  const { t } = useI18n();
  const Message = useMessage();

  // 状态
  const showTagModal = ref(false);
  const loading = ref(false);
  const saving = ref(false);
  const allTags = ref<TagItem[]>([]);
  const customerTags = ref<TagItem[]>([]);
  const selectedTagIds = ref<string[]>([]);
  const searchKeyword = ref('');
  const isCreating = ref(false);
  const newTagName = ref('');
  const createInputRef = ref<any>(null);

  // 过滤后的标签列表
  const filteredTags = computed(() => {
    if (!searchKeyword.value) return allTags.value;
    const keyword = searchKeyword.value.toLowerCase();
    return allTags.value.filter(tagItem => tagItem.name.toLowerCase().includes(keyword));
  });

  // 根据ID获取标签名称
  function getTagNameById(id: string): string {
    const tagItem = allTags.value.find(tagItem => tagItem.id === id);
    return tagItem?.name || '';
  }

  // 加载所有标签
  async function loadAllTags() {
    try {
      loading.value = true;
      const res = await getCustomerTagList();
      allTags.value = res || [];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  }

  // 加载客户标签
  async function loadCustomerTags() {
    if (!props.customerId) return;
    try {
      const res = await getCustomerTagsByCustomerId(props.customerId);
      customerTags.value = res || [];
      selectedTagIds.value = customerTags.value.map(tag => tag.id);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  // 打开弹窗时加载数据
  watch(showTagModal, (val) => {
    if (val) {
      loadAllTags();
      loadCustomerTags();
    }
  });

  // 创建标签
  function handleCreateTag() {
    isCreating.value = true;
    nextTick(() => {
      createInputRef.value?.focus();
    });
  }

  // 确认创建标签
  async function confirmCreateTag() {
    if (!newTagName.value.trim()) {
      Message.warning(t('customer.tag.nameRequired'));
      return;
    }
    try {
      await addCustomerTag({ name: newTagName.value.trim() });
      Message.success(t('customer.tag.createSuccess'));
      newTagName.value = '';
      isCreating.value = false;
      loadAllTags();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  // 取消创建
  function cancelCreate() {
    isCreating.value = false;
    newTagName.value = '';
  }

  // 删除标签
  async function handleDeleteTag(tag: TagItem) {
    try {
      await deleteCustomerTag(tag.id);
      Message.success(t('customer.tag.deleteSuccess'));
      loadAllTags();
      // 如果删除的是已选标签，从选中列表中移除
      const index = selectedTagIds.value.indexOf(tag.id);
      if (index > -1) {
        selectedTagIds.value.splice(index, 1);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  // 取消选择标签
  function handleUnselectTag(tagId: string) {
    const index = selectedTagIds.value.indexOf(tagId);
    if (index > -1) {
      selectedTagIds.value.splice(index, 1);
    }
  }

  // 保存客户标签
  async function handleSave() {
    if (!props.customerId) return;
    try {
      saving.value = true;
      await setCustomerTags({
        customerId: props.customerId,
        tagIds: selectedTagIds.value,
      });
      Message.success(t('customer.tag.saveSuccess'));
      showTagModal.value = false;
      await loadCustomerTags();
      emit('update');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      saving.value = false;
    }
  }

  // 从客户移除标签
  async function handleRemoveTag(tag: TagItem) {
    try {
      await removeTagFromCustomer({
        customerId: props.customerId,
        tagId: tag.id,
      });
      Message.success(t('customer.tag.removeSuccess'));
      await loadCustomerTags();
      emit('update');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  // 关闭弹窗时重置状态
  function handleModalClose() {
    searchKeyword.value = '';
    isCreating.value = false;
    newTagName.value = '';
  }

  // 监听客户ID变化
  watch(() => props.customerId, () => {
    loadCustomerTags();
  }, { immediate: true });
</script>

<style lang="less" scoped>
  .customer-tag-manage {
    padding: 16px 24px;
    border-top: 1px solid var(--border-color);

    .tag-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .tag-title {
        font-weight: 500;
        font-size: 14px;
        color: var(--text-color);
      }
    }

    .tag-content {
      min-height: 40px;
    }
  }

  .tag-modal-content {
    .tag-search-area {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
    }

    .tag-create-area {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
      padding: 12px;
      background-color: var(--fill-color);
      border-radius: 4px;
    }

    .tag-list-area {
      .tag-list-title {
        font-weight: 500;
        margin-bottom: 8px;
        color: var(--text-color);
      }

      .tag-list {
        max-height: 300px;
        overflow-y: auto;

        .tag-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 4px 0;
        }
      }
    }

    .tag-selected-area {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid var(--border-color);

      .tag-selected-title {
        font-weight: 500;
        margin-bottom: 8px;
        color: var(--text-color);
      }
    }
  }
</style>
