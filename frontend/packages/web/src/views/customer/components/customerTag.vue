<template>
  <CrmCard :loading="loading" hide-footer>
    <div class="flex flex-col gap-[16px]">
      <div class="flex items-center justify-between">
        <div class="flex flex-wrap gap-[8px]">
          <n-tag
            v-for="tag in customerTags"
            :key="tag.id"
            :color="{ color: tag.color }"
            closable
            @close="handleRemoveTag(tag.id)"
          >
            {{ tag.name }}
          </n-tag>
          <n-tag v-if="customerTags.length === 0" type="default">{{ t('common.noData') }}</n-tag>
        </div>
        <n-popover trigger="click" :show-arrow="false">
          <template #trigger>
            <n-button v-if="!props.readonly" v-permission="['CUSTOMER_MANAGEMENT:UPDATE']" type="primary" size="small">
              <template #icon>
                <CrmIcon type="iconicon_add" />
              </template>
              {{ t('common.add') }}
            </n-button>
          </template>
          <div class="w-[300px]">
            <n-input v-model:value="searchKeyword" :placeholder="t('common.search')" class="mb-[8px]" />
            <div class="max-h-[300px] overflow-y-auto">
              <div
                v-for="tag in filteredAvailableTags"
                :key="tag.id"
                class="flex cursor-pointer items-center justify-between p-[8px] hover:bg-[var(--item-background-color-hover)]"
                @click="handleAddTag(tag.id)"
              >
                <n-tag :color="{ color: tag.color }">{{ tag.name }}</n-tag>
                <n-icon v-if="isTagSelected(tag.id)" class="text-[var(--icon-color-success)]">
                  <CrmIcon type="icontick" />
                </n-icon>
              </div>
              <n-empty v-if="filteredAvailableTags.length === 0" :description="t('common.noData')" size="small" />
            </div>
            <n-divider class="my-[8px]" />
            <div class="flex gap-[8px]">
              <n-input
                v-model:value="newTagName"
                :placeholder="t('customer.tagNamePlaceholder')"
                class="flex-1"
                @keyup.enter="handleCreateTag"
              />
              <n-popover trigger="click">
                <template #trigger>
                  <div
                    class="h-[32px] w-[32px] cursor-pointer rounded-[var(--border-radius-small)] border border-[var(--border-color-1)]"
                    :style="{ backgroundColor: newTagColor }"
                  ></div>
                </template>
                <n-color-picker v-model:value="newTagColor" modes="['preset']" />
              </n-popover>
              <n-button :disabled="!newTagName.trim()" size="small" @click="handleCreateTag">
                {{ t('common.create') }}
              </n-button>
            </div>
          </div>
        </n-popover>
      </div>
    </div>
  </CrmCard>
</template>

<script setup lang="ts">
  import { NButton, NDivider, NEmpty, NIcon, NInput, NPopover, NTag, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { CustomerTagItem } from '@lib/shared/models/customer';

  import CrmCard from '@/components/pure/crm-card/index.vue';
  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';

  import { addCustomerTag, getCustomerTagOptions, getCustomerTags, updateCustomerTagRelation } from '@/api/modules';

  const props = defineProps<{
    sourceId: string;
    readonly?: boolean;
  }>();

  const { t } = useI18n();
  const Message = useMessage();

  const loading = ref(false);
  const customerTags = ref<CustomerTagItem[]>([]);
  const availableTags = ref<CustomerTagItem[]>([]);
  const searchKeyword = ref('');
  const newTagName = ref('');
  const newTagColor = ref('#18a058');

  const filteredAvailableTags = computed(() => {
    if (!searchKeyword.value) {
      return availableTags.value;
    }
    return availableTags.value.filter((tag) => tag.name.toLowerCase().includes(searchKeyword.value.toLowerCase()));
  });

  function isTagSelected(tagId: string) {
    return customerTags.value.some((tag) => tag.id === tagId);
  }

  async function loadCustomerTags() {
    try {
      const res = await getCustomerTags(props.sourceId);
      customerTags.value = res;
    } catch (error) {
      // ignore error
    }
  }

  async function loadAvailableTags() {
    try {
      const res = await getCustomerTagOptions({ pageSize: 0, pageNum: 0 });
      availableTags.value = res.filter((tag) => tag.enable);
    } catch (error) {
      // ignore error
    }
  }

  async function handleAddTag(tagId: string) {
    if (isTagSelected(tagId)) {
      return;
    }
    try {
      const tagIds = [...customerTags.value.map((tag) => tag.id), tagId];
      await updateCustomerTagRelation({ customerId: props.sourceId, tagIds });
      Message.success(t('common.saveSuccess'));
      await loadCustomerTags();
    } catch (error) {
      // ignore error
    }
  }

  async function handleRemoveTag(tagId: string) {
    try {
      const tagIds = customerTags.value.filter((tag) => tag.id !== tagId).map((tag) => tag.id);
      await updateCustomerTagRelation({ customerId: props.sourceId, tagIds });
      Message.success(t('common.saveSuccess'));
      await loadCustomerTags();
    } catch (error) {
      // ignore error
    }
  }

  async function handleCreateTag() {
    if (!newTagName.value.trim()) {
      return;
    }
    try {
      await addCustomerTag({ name: newTagName.value.trim(), color: newTagColor.value });
      Message.success(t('common.createSuccess'));
      newTagName.value = '';
      newTagColor.value = '#18a058';
      await loadAvailableTags();
    } catch (error) {
      // ignore error
    }
  }

  onBeforeMount(async () => {
    loading.value = true;
    try {
      await Promise.all([loadCustomerTags(), loadAvailableTags()]);
    } finally {
      loading.value = false;
    }
  });
</script>

<style lang="less" scoped></style>
