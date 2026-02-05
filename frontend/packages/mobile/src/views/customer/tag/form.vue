<template>
  <div class="tag-form-page">
    <crm-page-header 
      :title="isEdit ? '编辑标签' : '新建标签'" 
      :show-back="true" 
      @back="handleBack"
    ></crm-page-header>
    <crm-page-wrapper>
      <van-form @submit="handleSubmit">
        <van-field
          v-model="form.name"
          name="name"
          label="标签名称"
          placeholder="请输入标签名称"
          :rules="[{ required: true, message: '请输入标签名称' }]"
        />
        <van-field
          v-model="form.description"
          name="description"
          label="标签描述"
          placeholder="请输入标签描述"
          type="textarea"
          :rows="3"
        />
        <van-field
          v-model="form.color"
          name="color"
          label="标签颜色"
          placeholder="请输入颜色代码"
        >
          <template #extra>
            <div 
              class="color-preview" 
              :style="{ backgroundColor: form.color || '#1989fa' }"
            ></div>
          </template>
        </van-field>
        <van-field
          v-model="form.type"
          name="type"
          label="标签类型"
          placeholder="请输入标签类型"
        />
        <div class="form-actions">
          <van-button 
            type="default" 
            block 
            @click="handleBack"
            style="margin-right: 10px;"
          >
            取消
          </van-button>
          <van-button type="primary" block native-type="submit">
            {{ isEdit ? '保存' : '创建' }}
          </van-button>
        </div>
      </van-form>
    </crm-page-wrapper>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCustomerApi } from '@lib/shared/api/modules/customer';
import { CDR } from '@/api/http';
import { showToast } from 'vant';

const router = useRouter();
const route = useRoute();
const { addCustomerTag, updateCustomerTag, getCustomerTag } = useCustomerApi(CDR);

const tagId = computed(() => route.query.id as string);
const isEdit = computed(() => !!tagId.value);

const form = ref({
  name: '',
  description: '',
  color: '#1989fa',
  type: ''
});

onMounted(() => {
  if (isEdit.value) {
    loadTagDetail();
  }
});

const loadTagDetail = async () => {
  try {
    const res = await getCustomerTag(tagId.value);
    if (res.data) {
      form.value = {
        name: res.data.name || '',
        description: res.data.description || '',
        color: res.data.color || '#1989fa',
        type: res.data.type || ''
      };
    }
  } catch (error) {
    showToast('获取标签详情失败');
  }
};

const handleBack = () => {
  router.back();
};

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateCustomerTag({
        id: tagId.value,
        ...form.value
      });
      showToast('标签更新成功');
    } else {
      await addCustomerTag(form.value);
      showToast('标签创建成功');
    }
    router.push('/customer/tag');
  } catch (error: any) {
    showToast(error.message || '操作失败');
  }
};
</script>

<style lang="less" scoped>
.tag-form-page {
  height: 100vh;
  background-color: #f5f5f5;
}

.form-actions {
  display: flex;
  padding: 20px 10px;
  gap: 10px;
}

.color-preview {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #ddd;
}
</style>
