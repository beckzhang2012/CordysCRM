<template>
  <div class="customer-tags">
    <div class="tags-header">
      <h3>{{ $t('customer.tags.title') }}</h3>
      <button v-if="editable" @click="showAddTagModal = true" class="add-tag-btn">
        {{ $t('customer.tags.add') }}
      </button>
    </div>
    
    <div v-if="loading" class="loading">
      <van-loading type="spinner" color="#1989fa" />
    </div>
    
    <div v-else-if="tags.length === 0" class="no-tags">
      {{ $t('customer.tags.empty') }}
    </div>
    
    <div v-else class="tags-list">
      <div 
        v-for="tag in tags" 
        :key="tag.id"
        class="tag-item"
        :style="{ backgroundColor: tag.color + '20', borderColor: tag.color }"
      >
        <span class="tag-name">{{ tag.name }}</span>
        <button 
          v-if="editable" 
          @click="removeTag(tag.id)"
          class="remove-tag-btn"
          :style="{ color: tag.color }"
        >
          ×
        </button>
      </div>
    </div>
    
    <!-- 添加标签弹窗 -->
    <van-popup
      v-model:show="showAddTagModal"
      position="bottom"
      round
      :style="{ height: '70%' }"
    >
      <div class="add-tag-modal">
        <div class="modal-header">
          <h3>{{ $t('customer.tags.add') }}</h3>
          <button @click="showAddTagModal = false" class="close-btn">×</button>
        </div>
        
        <div class="modal-content">
          <div class="search-box">
            <van-search
              v-model="searchKeyword"
              placeholder="搜索标签"
              @input="handleSearch"
            />
          </div>
          
          <div class="available-tags">
            <h4>可选标签</h4>
            <div class="tags-grid">
              <div 
                v-for="tag in availableTags" 
                :key="tag.id"
                class="available-tag-item"
                :class="{ selected: selectedTags.includes(tag.id) }"
                :style="{ 
                  backgroundColor: selectedTags.includes(tag.id) ? tag.color : tag.color + '20',
                  borderColor: tag.color
                }"
                @click="toggleTagSelection(tag.id)"
              >
                <span>{{ tag.name }}</span>
                <van-icon v-if="selectedTags.includes(tag.id)" name="success" color="#fff" />
              </div>
            </div>
          </div>
          
          <div v-if="availableTags.length === 0 && !loadingTags" class="no-available-tags">
            暂无标签
          </div>
          
          <div v-if="loadingTags" class="loading-tags">
            <van-loading type="spinner" color="#1989fa" />
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="showAddTagModal = false" class="cancel-btn">
            {{ $t('common.cancel') }}
          </button>
          <button 
            @click="confirmAddTags" 
            class="confirm-btn"
            :disabled="selectedTags.length === 0"
          >
            {{ $t('common.confirm') }} ({{ selectedTags.length }})
          </button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useCustomerApi } from '@lib/shared/api/modules/customer';
import { showToast } from 'vant';

const props = defineProps<{
  customerId: string;
  editable?: boolean;
}>();

const emit = defineEmits<{
  (e: 'tagsUpdated', tags: any[]): void;
}>();

const customerApi = useCustomerApi();

const tags = ref<any[]>([]);
const availableTags = ref<any[]>([]);
const selectedTags = ref<string[]>([]);
const searchKeyword = ref('');
const loading = ref(false);
const loadingTags = ref(false);
const showAddTagModal = ref(false);

// 加载客户标签
const loadCustomerTags = async () => {
  if (!props.customerId) return;
  
  loading.value = true;
  try {
    const response = await customerApi.getCustomerTags(props.customerId);
    tags.value = response.data || [];
  } catch (error) {
    console.error('Failed to load customer tags:', error);
    showToast({ message: '加载标签失败', type: 'error' });
  } finally {
    loading.value = false;
  }
};

// 加载可用标签
const loadAvailableTags = async () => {
  loadingTags.value = true;
  try {
    const response = await customerApi.getCustomerTagList();
    const allTags = response.data || [];
    
    // 过滤掉客户已有的标签
    const existingTagIds = tags.value.map(tag => tag.id);
    availableTags.value = allTags.filter((tag: any) => !existingTagIds.includes(tag.id));
  } catch (error) {
    console.error('Failed to load available tags:', error);
    showToast({ message: '加载可用标签失败', type: 'error' });
  } finally {
    loadingTags.value = false;
  }
};

// 搜索标签
const handleSearch = async () => {
  if (!searchKeyword.value) {
    await loadAvailableTags();
    return;
  }
  
  // 这里可以实现搜索逻辑，暂时使用前端过滤
  const filteredTags = availableTags.value.filter((tag: any) => 
    tag.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  );
  availableTags.value = filteredTags;
};

// 切换标签选择
const toggleTagSelection = (tagId: string) => {
  const index = selectedTags.value.indexOf(tagId);
  if (index > -1) {
    selectedTags.value.splice(index, 1);
  } else {
    selectedTags.value.push(tagId);
  }
};

// 确认添加标签
const confirmAddTags = async () => {
  if (selectedTags.value.length === 0) return;
  
  loadingTags.value = true;
  try {
    await customerApi.addTagsToCustomer(props.customerId, selectedTags.value);
    showToast({ message: '添加标签成功', type: 'success' });
    showAddTagModal.value = false;
    selectedTags.value = [];
    await loadCustomerTags();
    emit('tagsUpdated', tags.value);
  } catch (error) {
    console.error('Failed to add tags:', error);
    showToast({ message: '添加标签失败', type: 'error' });
  } finally {
    loadingTags.value = false;
  }
};

// 移除标签
const removeTag = async (tagId: string) => {
  try {
    await customerApi.removeTagsFromCustomer(props.customerId, [tagId]);
    showToast({ message: '移除标签成功', type: 'success' });
    await loadCustomerTags();
    emit('tagsUpdated', tags.value);
  } catch (error) {
    console.error('Failed to remove tag:', error);
    showToast({ message: '移除标签失败', type: 'error' });
  }
};

// 监听弹窗显示，加载可用标签
watch(showAddTagModal, async (newVal) => {
  if (newVal) {
    await loadAvailableTags();
  }
});

// 监听customerId变化，重新加载标签
watch(() => props.customerId, async (newVal) => {
  if (newVal) {
    await loadCustomerTags();
  }
});

// 初始化加载标签
onMounted(async () => {
  await loadCustomerTags();
});
</script>

<style scoped>
.customer-tags {
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
}

.tags-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tags-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.add-tag-btn {
  padding: 4px 12px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.no-tags {
  color: #999;
  padding: 20px;
  text-align: center;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 16px;
  border: 1px solid;
  font-size: 12px;
  gap: 8px;
}

.tag-name {
  color: #333;
}

.remove-tag-btn {
  background: none;
  border: none;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 添加标签弹窗 */
.add-tag-modal {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.search-box {
  margin-bottom: 16px;
}

.available-tags h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
}

.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.available-tag-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 16px;
  border: 1px solid;
  font-size: 14px;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.available-tag-item.selected {
  color: #fff;
}

.no-available-tags {
  color: #999;
  text-align: center;
  padding: 40px 0;
}

.loading-tags {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.modal-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

.modal-footer button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
}

.confirm-btn {
  background-color: #1989fa;
  color: #fff;
}

.confirm-btn:disabled {
  background-color: #c8c9cc;
  cursor: not-allowed;
}
</style>