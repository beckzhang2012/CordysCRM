<template>
  <div class="tag-list-page">
    <crm-page-header title="标签管理" :show-back="true" @back="handleBack">
      <template #right>
        <crm-text-button @click="handleAddTag" text="新建" />
      </template>
    </crm-page-header>
    <crm-page-wrapper>
      <div v-if="loading" class="loading">
        <van-loading type="spinner" color="#1989fa" />
      </div>
      <div v-else-if="tags.length === 0" class="empty">
        <van-empty description="暂无标签" />
      </div>
      <div v-else class="tag-list">
        <van-cell-group>
          <van-cell
            v-for="tag in tags"
            :key="tag.id"
            :title="tag.name"
            :value="tag.description || '无描述'"
            :class="['tag-item', { 'system-tag': tag.isSystem }]"
            @click="handleEditTag(tag)"
          >
            <template #extra>
              <div class="tag-extra">
                <div 
                  class="tag-color" 
                  :style="{ backgroundColor: tag.color || '#1989fa' }"
                ></div>
                <van-icon name="arrow" size="16" color="#999" />
              </div>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </crm-page-wrapper>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCustomerApi } from '@lib/shared/api/modules/customer';
import { CDR } from '@/api/http';
import { showToast } from 'vant';

const router = useRouter();
const { getCustomerTagList, deleteCustomerTag } = useCustomerApi(CDR);

const loading = ref(false);
const tags = ref<any[]>([]);

onMounted(() => {
  loadTags();
});

const loadTags = async () => {
  loading.value = true;
  try {
    const res = await getCustomerTagList();
    tags.value = res.data || [];
  } catch (error) {
    showToast('获取标签列表失败');
  } finally {
    loading.value = false;
  }
};

const handleBack = () => {
  router.back();
};

const handleAddTag = () => {
  router.push('/customer/tag/add');
};

const handleEditTag = (tag: any) => {
  router.push(`/customer/tag/edit?id=${tag.id}`);
};
</script>

<style lang="less" scoped>
.tag-list-page {
  height: 100vh;
  background-color: #f5f5f5;
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

.tag-list {
  padding: 10px;
}

.tag-item {
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;

  &.system-tag {
    background-color: #f0f9ff;
  }
}

.tag-extra {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tag-color {
  width: 20px;
  height: 20px;
  border-radius: 50%;
}
</style>
