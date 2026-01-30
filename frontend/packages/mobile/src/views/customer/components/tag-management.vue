<template>
  <div class="customer-tag-management">
    <div class="tag-header">
      <div class="tag-title">客户标签</div>
      <van-button
        v-if="hasAnyPermission(['CUSTOMER_MANAGEMENT:UPDATE']) && collaborationType !== 'READ_ONLY'"
        size="small"
        type="primary"
        @click="handleAddTag"
      >
        添加标签
      </van-button>
    </div>

    <div class="tag-list">
      <van-tag
        v-for="tag in customerTags"
        :key="tag.id"
        :color="tag.color || '#1989fa'"
        closeable
        @close="handleDeleteTag(tag.id)"
      >
        {{ tag.name }}
      </van-tag>
      <div v-if="!customerTags.length" class="empty-tags">暂无标签</div>
    </div>

    <!-- 添加标签弹窗 -->
    <van-popup v-model:show="showAddTagPopup" position="bottom" round>
      <div class="add-tag-popup">
        <div class="popup-header">
          <div class="popup-title">添加标签</div>
          <van-icon name="cross" @click="showAddTagPopup = false" />
        </div>

        <div class="popup-content">
          <van-field v-model="newTag.name" label="标签名称" placeholder="请输入标签名称" required />
          <van-field v-model="newTag.color" label="标签颜色" placeholder="请选择标签颜色">
            <template #input>
              <div class="color-picker">
                <div
                  v-for="color in colorOptions"
                  :key="color"
                  class="color-option"
                  :class="{ active: newTag.color === color }"
                  :style="{ backgroundColor: color }"
                  @click="newTag.color = color"
                />
              </div>
            </template>
          </van-field>
          <van-field
            v-model="newTag.description"
            label="标签描述"
            placeholder="请输入标签描述"
            type="textarea"
            rows="3"
          />
        </div>

        <div class="popup-footer">
          <van-button block type="primary" @click="handleSaveTag">保存</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 选择已有标签弹窗 -->
    <van-popup v-model:show="showSelectTagPopup" position="bottom" round>
      <div class="select-tag-popup">
        <div class="popup-header">
          <div class="popup-title">选择标签</div>
          <van-icon name="cross" @click="showSelectTagPopup = false" />
        </div>

        <div class="popup-content">
          <van-search v-model="tagSearchKeyword" placeholder="搜索标签" @search="handleSearchTags" />

          <div class="tag-options">
            <van-checkbox-group v-model="selectedTagIds">
              <van-checkbox v-for="tag in availableTags" :key="tag.id" :name="tag.id" :disabled="isTagSelected(tag.id)">
                <div class="tag-option">
                  <van-tag :color="tag.color || '#1989fa'">{{ tag.name }}</van-tag>
                  <span class="tag-description">{{ tag.description }}</span>
                </div>
              </van-checkbox>
            </van-checkbox-group>
          </div>
        </div>

        <div class="popup-footer">
          <van-button block type="primary" @click="handleRelateTags">确定</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, computed } from 'vue';
  import { showSuccessToast, showFailToast } from 'vant';

  import type {
    CustomerTagResponse,
    CustomerTagAddRequest,
    CustomerTagRelationRequest,
  } from '@lib/shared/models/customer';
  import {
    addCustomerTag,
    updateCustomerTag,
    deleteCustomerTag,
    getCustomerTagList,
    getCustomerTags,
    relateCustomerTags,
  } from '@/api/modules';
  import { hasAnyPermission } from '@/utils/permission';

  interface Props {
    customerId: string;
    collaborationType?: string;
  }

  const props = defineProps<Props>();

  const customerTags = ref<CustomerTagResponse[]>([]);
  const availableTags = ref<CustomerTagResponse[]>([]);
  const showAddTagPopup = ref(false);
  const showSelectTagPopup = ref(false);
  const tagSearchKeyword = ref('');
  const selectedTagIds = ref<string[]>([]);

  const newTag = ref<CustomerTagAddRequest>({
    name: '',
    color: '#1989fa',
    description: '',
  });

  const colorOptions = ['#1989fa', '#07c160', '#ff976a', '#ed6a0c', '#f44336', '#7232dd', '#323233', '#969799'];

  // 初始化标签数据
  async function initTags() {
    try {
      // 获取客户已有标签
      const customerTagResponse = await getCustomerTags(props.customerId);
      customerTags.value = customerTagResponse.tags || [];

      // 获取所有可用标签
      const tagListResponse = await getCustomerTagList({
        pageNum: 1,
        pageSize: 100,
      });
      availableTags.value = tagListResponse.list || [];
    } catch (error) {
      console.error('获取标签失败:', error);
    }
  }

  // 检查标签是否已选中
  function isTagSelected(tagId: string): boolean {
    return customerTags.value.some((tag) => tag.id === tagId);
  }

  // 处理添加标签
  function handleAddTag() {
    showSelectTagPopup.value = true;
    selectedTagIds.value = [];
  }

  // 处理删除标签
  async function handleDeleteTag(tagId: string) {
    try {
      // 从客户标签中移除该标签
      const remainingTagIds = customerTags.value.filter((tag) => tag.id !== tagId).map((tag) => tag.id);

      await relateCustomerTags({
        customerId: props.customerId,
        tagIds: remainingTagIds,
      });

      showSuccessToast('删除标签成功');
      await initTags();
    } catch (error) {
      showFailToast('删除标签失败');
      console.error('删除标签失败:', error);
    }
  }

  // 处理搜索标签
  async function handleSearchTags() {
    try {
      const response = await getCustomerTagList({
        pageNum: 1,
        pageSize: 100,
        name: tagSearchKeyword.value,
      });
      availableTags.value = response.list || [];
    } catch (error) {
      console.error('搜索标签失败:', error);
    }
  }

  // 处理保存标签
  async function handleSaveTag() {
    if (!newTag.value.name.trim()) {
      showFailToast('请输入标签名称');
      return;
    }

    try {
      await addCustomerTag(newTag.value);
      showSuccessToast('添加标签成功');
      showAddTagPopup.value = false;
      newTag.value = {
        name: '',
        color: '#1989fa',
        description: '',
      };
      await handleSearchTags(); // 刷新标签列表
    } catch (error) {
      showFailToast('添加标签失败');
      console.error('添加标签失败:', error);
    }
  }

  // 处理关联标签
  async function handleRelateTags() {
    try {
      // 合并已有标签和新选中的标签
      const existingTagIds = customerTags.value.map((tag) => tag.id);
      const allTagIds = [...new Set([...existingTagIds, ...selectedTagIds.value])];

      await relateCustomerTags({
        customerId: props.customerId,
        tagIds: allTagIds,
      });

      showSuccessToast('关联标签成功');
      showSelectTagPopup.value = false;
      await initTags();
    } catch (error) {
      showFailToast('关联标签失败');
      console.error('关联标签失败:', error);
    }
  }

  onMounted(() => {
    initTags();
  });
</script>

<style lang="less" scoped>
  .customer-tag-management {
    margin-bottom: 16px;
    padding: 16px;
    border-radius: 8px;
    background-color: var(--van-background-2);
    .tag-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      .tag-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--van-text-color);
      }
    }
    .tag-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      .empty-tags {
        font-size: 14px;
        color: var(--van-text-color-2);
      }
    }
  }
  .add-tag-popup,
  .select-tag-popup {
    padding: 16px;
    .popup-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      .popup-title {
        font-size: 16px;
        font-weight: 600;
      }
    }
    .popup-content {
      margin-bottom: 16px;
      .color-picker {
        display: flex;
        gap: 8px;
        .color-option {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          cursor: pointer;
          &.active {
            border: 2px solid var(--van-text-color);
          }
        }
      }
      .tag-options {
        overflow-y: auto;
        max-height: 300px;
        .tag-option {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          .tag-description {
            font-size: 12px;
            color: var(--van-text-color-2);
          }
        }
      }
    }
    .popup-footer {
      display: flex;
      gap: 8px;
    }
  }
</style>
