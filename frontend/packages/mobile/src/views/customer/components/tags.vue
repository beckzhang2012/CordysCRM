<template>
  <div class="customer-tags">
    <div v-if="loading" class="loading">
      <van-loading type="spinner" color="#1989fa" />
    </div>
    <div v-else>
      <div v-if="customerTags.length === 0" class="empty">
        <van-empty description="暂无标签" />
      </div>
      <div v-else class="tags-list">
        <div 
          v-for="tag in customerTags" 
          :key="tag.id" 
          class="tag-item"
          :style="{ backgroundColor: tag.color || '#1989fa' }"
        >
          <span class="tag-name">{{ tag.name }}</span>
          <van-icon 
            v-if="!readonly" 
            name="cross" 
            size="16" 
            color="#fff" 
            class="tag-remove" 
            @click="handleRemoveTag(tag)"
          />
        </div>
      </div>
      <div v-if="!readonly" class="add-tags-section">
        <div class="section-title">添加标签</div>
        <div class="available-tags">
          <div 
            v-for="tag in availableTags" 
            :key="tag.id" 
            class="available-tag-item"
            :class="{ 'selected': selectedTags.includes(tag.id) }"
            @click="handleSelectTag(tag.id)"
          >
            <span class="tag-name">{{ tag.name }}</span>
            <div 
              class="tag-color" 
              :style="{ backgroundColor: tag.color || '#1989fa' }"
            ></div>
          </div>
        </div>
        <van-button 
          type="primary" 
          block 
          :disabled="selectedTags.length === 0"
          @click="handleAddTags"
          style="margin-top: 20px;"
        >
          添加选中标签
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useCustomerApi } from '@lib/shared/api/modules/customer';
import { CDR } from '@/api/http';
import { showToast, showConfirmDialog } from 'vant';

const props = defineProps({
  customerId: {
    type: String,
    required: true
  },
  customerName: {
    type: String,
    default: ''
  },
  readonly: {
    type: Boolean,
    default: false
  }
});

const { getCustomerTags, getCustomerTagList, addCustomerTags, removeCustomerTags } = useCustomerApi(CDR);

const loading = ref(false);
const customerTags = ref<any[]>([]);
const availableTags = ref<any[]>([]);
const selectedTags = ref<string[]>([]);

onMounted(() => {
  loadCustomerTags();
  loadAvailableTags();
});

const loadCustomerTags = async () => {
  loading.value = true;
  try {
    const res = await getCustomerTags(props.customerId);
    customerTags.value = res.data || [];
  } catch (error) {
    showToast('获取客户标签失败');
  } finally {
    loading.value = false;
  }
};

const loadAvailableTags = async () => {
  try {
    const res = await getCustomerTagList();
    const allTags = res.data || [];
    // 过滤掉客户已有的标签
    availableTags.value = allTags.filter(tag => 
      !customerTags.value.some(customerTag => customerTag.id === tag.id)
    );
  } catch (error) {
    showToast('获取可用标签失败');
  }
};

const handleRemoveTag = async (tag: any) => {
  try {
    await removeCustomerTags({
      customerId: props.customerId,
      tagIds: [tag.id]
    });
    showToast('标签移除成功');
    await loadCustomerTags();
    await loadAvailableTags();
  } catch (error) {
    showToast('标签移除失败');
  }
};

const handleSelectTag = (tagId: string) => {
  const index = selectedTags.value.indexOf(tagId);
  if (index > -1) {
    selectedTags.value.splice(index, 1);
  } else {
    selectedTags.value.push(tagId);
  }
};

const handleAddTags = async () => {
  try {
    await addCustomerTags({
      customerId: props.customerId,
      tagIds: selectedTags.value
    });
    showToast('标签添加成功');
    selectedTags.value = [];
    await loadCustomerTags();
    await loadAvailableTags();
  } catch (error) {
    showToast('标签添加失败');
  }
};
</script>

<style lang="less" scoped>
.customer-tags {
  padding: 16px;
  min-height: 300px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.empty {
  padding: 50px 0;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 16px;
  color: #fff;
  font-size: 14px;
}

.tag-name {
  white-space: nowrap;
}

.tag-remove {
  cursor: pointer;
}

.add-tags-section {
  margin-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
}

.available-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.available-tag-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;

  &.selected {
    border-color: #1989fa;
    background-color: #f0f9ff;
  }
}

.available-tag-item .tag-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}
</style>
