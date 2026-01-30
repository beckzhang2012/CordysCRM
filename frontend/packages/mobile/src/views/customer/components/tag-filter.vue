<template>
  <div class="tag-filter">
    <van-button
      size="small"
      round
      :class="['filter-button', selectedTagIds.length > 0 ? 'active' : '']"
      @click="showTagFilterPopup = true"
    >
      <van-icon name="label-o" />
      <span v-if="selectedTagIds.length === 0">标签筛选</span>
      <span v-else>已选 {{ selectedTagIds.length }} 个标签</span>
    </van-button>

    <van-button v-if="selectedTagIds.length > 0" size="small" round class="clear-button" @click="clearTagFilter">
      清除筛选
    </van-button>

    <!-- 标签筛选弹窗 -->
    <van-popup v-model:show="showTagFilterPopup" position="bottom" round>
      <div class="tag-filter-popup">
        <div class="popup-header">
          <div class="popup-title">按标签筛选</div>
          <van-icon name="cross" @click="showTagFilterPopup = false" />
        </div>

        <div class="popup-content">
          <van-search v-model="tagSearchKeyword" placeholder="搜索标签" @search="handleSearchTags" />

          <div class="tag-options">
            <van-checkbox-group v-model="tempSelectedTagIds">
              <van-checkbox v-for="tag in availableTags" :key="tag.id" :name="tag.id">
                <div class="tag-option">
                  <van-tag :color="tag.color || '#1989fa'">{{ tag.name }}</van-tag>
                  <span class="tag-description">{{ tag.description }}</span>
                </div>
              </van-checkbox>
            </van-checkbox-group>
          </div>
        </div>

        <div class="popup-footer">
          <van-button block type="primary" @click="applyTagFilter">确定</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, watch } from 'vue';

  import type { CustomerTagResponse } from '@lib/shared/models/customer';
  import { getCustomerTagList } from '@/api/modules';

  interface Props {
    modelValue: string[]; // 选中的标签ID数组
  }

  interface Emits {
    (e: 'update:modelValue', value: string[]): void;
  }

  const props = defineProps<Props>();
  const emit = defineEmits<Emits>();

  const showTagFilterPopup = ref(false);
  const tagSearchKeyword = ref('');
  const availableTags = ref<CustomerTagResponse[]>([]);
  const tempSelectedTagIds = ref<string[]>([]);

  const selectedTagIds = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value),
  });

  // 初始化标签数据
  async function initTags() {
    try {
      const response = await getCustomerTagList({
        pageNum: 1,
        pageSize: 100,
      });
      availableTags.value = response.list || [];
    } catch (error) {
      console.error('获取标签失败:', error);
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

  // 应用标签筛选
  function applyTagFilter() {
    selectedTagIds.value = [...tempSelectedTagIds.value];
    showTagFilterPopup.value = false;
  }

  // 清除标签筛选
  function clearTagFilter() {
    selectedTagIds.value = [];
    tempSelectedTagIds.value = [];
  }

  // 监听弹窗显示状态，初始化临时选中状态
  watch(showTagFilterPopup, (newVal) => {
    if (newVal) {
      tempSelectedTagIds.value = [...selectedTagIds.value];
    }
  });

  onMounted(() => {
    initTags();
  });
</script>

<style lang="less" scoped>
  .tag-filter {
    display: flex;
    gap: 8px;
    padding: 8px 16px;
    background-color: var(--text-n10);
    .filter-button {
      display: flex;
      align-items: center;
      gap: 4px;
      &.active {
        color: var(--primary-8);
        background-color: var(--primary-7);
      }
    }
    .clear-button {
      color: var(--text-n1);
      background-color: var(--text-n9);
    }
  }
  .tag-filter-popup {
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
      .tag-options {
        overflow-y: auto;
        margin-top: 16px;
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
