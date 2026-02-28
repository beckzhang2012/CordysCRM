<template>
  <div class="detail-page">
    <div class="detail-header">
      <van-button icon="arrow-left" plain type="primary" size="small" @click="goBack"> Back </van-button>
      <span class="detail-title">{{ t('customer.detail') }}</span>
      <div class="header-actions">
        <van-button icon="edit" plain type="primary" size="small" @click="goEdit"> Edit </van-button>
      </div>
    </div>

    <div class="detail-content">
      <div class="detail-section">
        <div class="section-title">Basic Information</div>
        <div class="section-content">
          <div class="info-item">
            <span class="info-label">Name</span>
            <span class="info-value">{{ customerDetail?.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Phone</span>
            <span class="info-value">{{ customerDetail?.phone }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Email</span>
            <span class="info-value">{{ customerDetail?.email }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Tags</span>
            <div class="info-value tag-list">
              <van-tag v-for="tag in customerTags" :key="tag" type="primary" plain class="tag-item">
                {{ tag }}
              </van-tag>
              <van-button icon="plus" plain size="mini" type="primary" @click="showTagManager = true"> Add </van-button>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">Additional Information</div>
        <div class="section-content">
          <div class="info-item">
            <span class="info-label">Company</span>
            <span class="info-value">{{ customerDetail?.company }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Address</span>
            <span class="info-value">{{ customerDetail?.address }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 标签管理弹窗 -->
    <van-popup v-model:show="showTagManager" position="bottom" :style="{ height: '80%' }">
      <div class="tag-manager">
        <div class="tag-manager-header">
          <span class="header-title">Manage Tags</span>
          <van-icon name="cross" size="24" @click="showTagManager = false" />
        </div>

        <!-- 搜索标签 -->
        <div class="tag-search">
          <van-search v-model="tagSearch" placeholder="Search" @search="handleSearchTag" />
        </div>

        <!-- 已选标签 -->
        <div class="selected-tags">
          <span class="section-title">Selected: {{ selectedTags.length }}</span>
          <div class="tag-list">
            <van-tag
              v-for="tag in selectedTags"
              :key="tag"
              type="primary"
              closeable
              @close="handleRemoveTag(tag)"
              class="tag-item"
            >
              {{ tag }}
            </van-tag>
          </div>
        </div>

        <!-- 可选标签 -->
        <div class="available-tags">
          <span class="section-title">Available Tags</span>
          <div class="tag-list">
            <van-tag
              v-for="tag in filteredTags"
              :key="tag"
              :type="selectedTags.includes(tag) ? 'primary' : 'default'"
              @click="toggleTag(tag)"
              class="tag-item"
            >
              {{ tag }}
            </van-tag>
          </div>
        </div>

        <!-- 自定义标签 -->
        <div class="custom-tag">
          <van-field
            v-model="customTag"
            placeholder="Input custom tag"
            right-icon="plus"
            @click-right-icon="handleAddCustomTag"
          />
        </div>

        <!-- 保存按钮 -->
        <div class="tag-manager-footer">
          <van-button type="primary" block @click="handleSaveTags"> Save </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { showSuccessToast, showFailToast } from 'vant';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { getCustomer, getCustomerTag, saveCustomerTag, getCustomerTagList } from '@/api/modules';

  const { t } = useI18n();
  const router = useRouter();

  const customerDetail = ref<any>(null);
  const customerTags = ref<string[]>([]);
  const showTagManager = ref(false);
  const tagSearch = ref('');
  const selectedTags = ref<string[]>([]);
  const allTags = ref<string[]>([]);
  const customTag = ref('');

  const filteredTags = computed(() => {
    if (!tagSearch.value) return allTags.value;
    return allTags.value.filter((tag) => tag.toLowerCase().includes(tagSearch.value.toLowerCase()));
  });

  onMounted(async () => {
    await loadCustomerDetail();
    await loadCustomerTags();
    await loadAllTags();
  });

  async function loadCustomerDetail() {
    try {
      const id = router.currentRoute.value.query.id as string;
      const data = await getCustomer(id);
      customerDetail.value = data;
    } catch (error) {
      console.error('加载客户详情失败:', error);
    }
  }

  async function loadCustomerTags() {
    try {
      const id = router.currentRoute.value.query.id as string;
      const tags = await getCustomerTag(id);
      customerTags.value = tags;
      selectedTags.value = [...tags];
    } catch (error) {
      console.error('加载客户标签失败:', error);
    }
  }

  async function loadAllTags() {
    try {
      const tags = await getCustomerTagList();
      allTags.value = tags;
    } catch (error) {
      console.error('加载所有标签失败:', error);
    }
  }

  function toggleTag(tag: string) {
    const index = selectedTags.value.indexOf(tag);
    if (index > -1) {
      selectedTags.value.splice(index, 1);
    } else {
      selectedTags.value.push(tag);
    }
  }

  function handleRemoveTag(tag: string) {
    const index = selectedTags.value.indexOf(tag);
    if (index > -1) {
      selectedTags.value.splice(index, 1);
    }
  }

  function handleSearchTag() {
    // 搜索逻辑已经在computed中处理
  }

  async function handleAddCustomTag() {
    if (!customTag.value.trim()) {
      showFailToast('Please input tag');
      return;
    }
    if (!selectedTags.value.includes(customTag.value)) {
      selectedTags.value.push(customTag.value);
    }
    customTag.value = '';
  }

  async function handleSaveTags() {
    try {
      const id = router.currentRoute.value.query.id as string;
      await saveCustomerTag({ customerId: id, tagNames: selectedTags.value });
      showSuccessToast('Tags saved successfully');
      customerTags.value = [...selectedTags.value];
      showTagManager.value = false;
    } catch (error) {
      showFailToast('Failed to save tags');
    }
  }

  function goBack() {
    router.back();
  }

  function goEdit() {
    router.push({
      name: 'customer-edit',
      query: {
        id: router.currentRoute.value.query.id,
      },
    });
  }
</script>

<style lang="less" scoped>
  .detail-page {
    height: 100%;
    background-color: #f5f5f5;
  }
  .detail-header {
    display: flex;
    align-items: center;
    padding: 16px;
    background-color: #ffffff;
    box-shadow: 0 2px 8px rgb(0 0 0 / 10%);
    .header-actions {
      margin-left: auto;
    }
    .detail-title {
      font-size: 18px;
      font-weight: 600;
      text-align: center;
      flex: 1;
    }
  }
  .detail-content {
    padding: 16px;
  }
  .detail-section {
    overflow: hidden;
    margin-bottom: 16px;
    border-radius: 8px;
    background-color: #ffffff;
  }
  .section-title {
    padding: 12px 16px;
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid #eeeeee;
    color: #333333;
  }
  .section-content {
    padding: 16px;
  }
  .info-item {
    display: flex;
    margin-bottom: 12px;
    .info-label {
      width: 100px;
      font-size: 14px;
      color: #666666;
    }
    .info-value {
      flex: 1;
      font-size: 14px;
      color: #333333;
    }
  }
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  .tag-item {
    margin-bottom: 4px;
  }
  .tag-manager {
    display: flex;
    padding: 16px;
    height: 100%;
    background-color: #ffffff;
    flex-direction: column;
  }
  .tag-manager-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    .header-title {
      flex: 1;
      font-size: 18px;
      font-weight: 600;
    }
  }
  .tag-search {
    margin-bottom: 16px;
  }
  .selected-tags {
    margin-bottom: 16px;
    .section-title {
      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 600;
      color: #333333;
    }
  }
  .available-tags {
    margin-bottom: 16px;
    .section-title {
      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 600;
      color: #333333;
    }
  }
  .custom-tag {
    margin-bottom: 16px;
  }
  .tag-manager-footer {
    margin-top: auto;
  }
</style>
