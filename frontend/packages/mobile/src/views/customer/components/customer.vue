<template>
  <div class="flex h-full flex-col overflow-hidden">
    <div class="flex items-center gap-[12px] bg-[var(--text-n10)] p-[8px_16px]">
      <van-button
        v-permission="['CUSTOMER_MANAGEMENT:ADD']"
        plain
        icon="plus"
        type="primary"
        size="small"
        @click="goCreate"
      >
      </van-button>
      <van-search
        v-model="keyword"
        shape="round"
        :placeholder="t('customer.searchPlaceholder')"
        class="crm-search"
        @search="search"
        @clear="search"
      />
    </div>
    <div class="filter-buttons">
      <van-button
        v-for="item of tabList"
        :key="item.name"
        round
        size="small"
        class="!border-none !px-[16px] !py-[4px] !text-[14px]"
        :class="
          activeFilter === item.name
            ? '!bg-[var(--primary-7)] !text-[var(--primary-8)]'
            : '!bg-[var(--text-n9)] !text-[var(--text-n1)]'
        "
        @click="activeFilter = item.name"
      >
        {{ item.tab }}
      </van-button>
      <van-button
        round
        size="small"
        class="!border-none !px-[16px] !py-[4px] !text-[14px]"
        :class="
          showTagFilter
            ? '!bg-[var(--primary-7)] !text-[var(--primary-8)]'
            : '!bg-[var(--text-n9)] !text-[var(--text-n1)]'
        "
        @click="showTagFilter = true"
      >
        Tag Filter
      </van-button>
    </div>

    <!-- 标签筛选弹窗 -->
    <van-popup v-model:show="showTagFilter" position="bottom" :style="{ height: '80%' }">
      <div class="p-[16px]">
        <div class="mb-[16px] flex items-center justify-between">
          <span class="text-[18px] font-medium">Tag Filter</span>
          <van-icon name="cross" size="24" @click="showTagFilter = false" />
        </div>

        <!-- 搜索标签 -->
        <van-search v-model="tagSearch" placeholder="Search" @search="handleSearchTag" />

        <!-- 已选标签 -->
        <div class="mt-[16px]">
          <span class="text-[14px]">Selected: {{ selectedTags.length }}</span>
          <div class="mt-[8px] flex flex-wrap gap-[8px]">
            <van-tag v-for="tag in selectedTags" :key="tag" type="primary" closeable @close="handleRemoveTag(tag)">
              {{ tag }}
            </van-tag>
          </div>
        </div>

        <!-- 可选标签 -->
        <div class="mt-[16px]">
          <span class="text-[14px]">Available Tags</span>
          <div class="mt-[8px] flex flex-wrap gap-[8px]">
            <van-tag
              v-for="tag in filteredTags"
              :key="tag"
              :type="selectedTags.includes(tag) ? 'primary' : 'default'"
              @click="toggleTag(tag)"
            >
              {{ tag }}
            </van-tag>
          </div>
        </div>

        <!-- 筛选按钮 -->
        <van-button type="primary" block class="mt-[24px]" @click="handleTagFilter"> Filter </van-button>
        <van-button plain block class="mt-[12px]" @click="handleClearTagFilter"> Clear Filter </van-button>
      </div>
    </van-popup>

    <CrmList
      ref="crmListRef"
      :keyword="keyword"
      :list-params="listParams"
      class="p-[16px]"
      :item-gap="16"
      :close-init-load="!activeFilter"
      :load-list-api="getCustomerList"
      :transform="transformFormData"
    >
      <template #item="{ item }">
        <CrmListCommonItem
          :item="item"
          :actions="getListItemActions(item.collaborationType)"
          name-key="ownerName"
          @click="goDetail"
        ></CrmListCommonItem>
      </template>
    </CrmList>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { closeToast, showConfirmDialog, showLoadingToast, showSuccessToast } from 'vant';

  import { CustomerSearchTypeEnum } from '@lib/shared/enums/customerEnum';
  import { FormDesignKeyEnum } from '@lib/shared/enums/formDesignEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { CollaborationType } from '@lib/shared/models/customer';

  import CrmList from '@/components/pure/crm-list/index.vue';
  import CrmListCommonItem from '@/components/pure/crm-list-common-item/index.vue';

  import { deleteCustomer, getCustomerList, getCustomerTagList } from '@/api/modules';
  import useFormCreateTransform from '@/hooks/useFormCreateTransform';
  import useHiddenTab from '@/hooks/useHiddenTab';

  import { CommonRouteEnum, CustomerRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const router = useRouter();

  const crmListRef = ref<InstanceType<typeof CrmList>>();
  const keyword = ref('');
  const selectedTags = ref<string[]>([]);
  const allTags = ref<string[]>([]);
  const showTagFilter = ref(false);
  const tagSearch = ref('');

  const filteredTags = computed(() => {
    if (!tagSearch.value) return allTags.value;
    return allTags.value.filter((tag) => tag.toLowerCase().includes(tagSearch.value.toLowerCase()));
  });
  const filterButtons = [
    {
      name: CustomerSearchTypeEnum.ALL,
      tab: t('customer.all'),
    },
    {
      name: CustomerSearchTypeEnum.SELF,
      tab: t('customer.mine'),
    },
    {
      name: CustomerSearchTypeEnum.DEPARTMENT,
      tab: t('customer.deptCustomer'),
    },
    {
      name: CustomerSearchTypeEnum.CUSTOMER_COLLABORATION,
      tab: t('customer.cooperationCustomer'),
    },
  ];
  const { tabList, activeFilter } = useHiddenTab(filterButtons, FormDesignKeyEnum.CUSTOMER);

  // 生命周期钩子
  onActivated(async () => {
    await loadAllTags();
    crmListRef.value?.loadList(true);
  });

  const listParams = computed(() => {
    return {
      viewId: activeFilter.value,
      keyword: keyword.value,
      tags: selectedTags.value.join(','),
    };
  });

  const { transformFormData } = await useFormCreateTransform(FormDesignKeyEnum.CUSTOMER);

  const actions = [
    {
      key: 'edit',
      label: t('common.edit'),
      icon: 'iconicon_handwritten_signature',
      permission: ['CUSTOMER_MANAGEMENT:UPDATE'],
      action: (item: any) => {
        router.push({
          name: CommonRouteEnum.FORM_CREATE,
          query: {
            id: item.id,
            formKey: FormDesignKeyEnum.CUSTOMER,
            needInitDetail: 'Y',
          },
        });
      },
    },
    {
      key: 'transfer',
      label: t('common.transfer'),
      icon: 'iconicon_jump',
      permission: ['CUSTOMER_MANAGEMENT:TRANSFER'],
      action: (item: any) => {
        router.push({
          name: CustomerRouteEnum.CUSTOMER_TRANSFER,
          query: {
            id: item.id,
            apiKey: FormDesignKeyEnum.CUSTOMER,
          },
        });
      },
    },
    {
      key: 'writeRecord',
      label: t('common.writeRecord'),
      icon: 'iconicon_edit1',
      permission: ['CUSTOMER_MANAGEMENT:UPDATE'],
      action: (item: any) => {
        router.push({
          name: CommonRouteEnum.FORM_CREATE,
          query: {
            id: item.id,
            formKey: FormDesignKeyEnum.FOLLOW_RECORD_CUSTOMER,
            initialSourceName: item.name,
          },
        });
      },
    },
    {
      key: 'delete',
      label: t('common.delete'),
      icon: 'iconicon_delete',
      permission: ['CUSTOMER_MANAGEMENT:DELETE'],
      action: (item: any) => {
        showConfirmDialog({
          title: t('customer.deleteTitle'),
          message: t('customer.deleteTip'),
          confirmButtonText: t('common.confirmDelete'),
          confirmButtonColor: 'var(--error-red)',
          beforeClose: async (action) => {
            if (action === 'confirm') {
              try {
                await deleteCustomer(item.id);
                showSuccessToast(t('common.deleteSuccess'));
                crmListRef.value?.loadList(true);
                return Promise.resolve(true);
              } catch (error) {
                // eslint-disable-next-line no-console
                console.log(error);
                return Promise.resolve(false);
              }
            } else {
              return Promise.resolve(true);
            }
          },
        });
      },
    },
  ];

  function getListItemActions(collaborationType: CollaborationType) {
    if (activeFilter.value === CustomerSearchTypeEnum.CUSTOMER_COLLABORATION) {
      return collaborationType === 'COLLABORATION' ? actions.filter((item) => item.key === 'writeRecord') : [];
    }
    return actions;
  }

  watch(
    () => activeFilter.value,
    () => {
      nextTick(() => {
        crmListRef.value?.loadList(true);
      });
    }
  );

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

  function handleTagFilter() {
    showTagFilter.value = false;
    crmListRef.value?.loadList(true);
  }

  function handleClearTagFilter() {
    selectedTags.value = [];
    tagSearch.value = '';
    showTagFilter.value = false;
    crmListRef.value?.loadList(true);
  }

  onActivated(async () => {
    await loadAllTags();
    crmListRef.value?.loadList(true);
  });

  async function search() {
    nextTick(async () => {
      showLoadingToast(t('common.searching'));
      await crmListRef.value?.loadList(true);
      closeToast();
    });
  }

  function goCreate() {
    router.push({
      name: CommonRouteEnum.FORM_CREATE,
      query: {
        formKey: FormDesignKeyEnum.CUSTOMER,
      },
    });
  }

  function goDetail(item: any) {
    router.push({
      name: CustomerRouteEnum.CUSTOMER_DETAIL,
      query: {
        id: item.id,
        name: item.name,
        needInitDetail: 'Y',
      },
    });
  }
</script>

<style lang="less" scoped>
  .filter-buttons {
    @apply flex;

    gap: 8px;
    padding: 8px 4px;
    background-color: var(--text-n10);
    .half-px-border-bottom();
  }
</style>
