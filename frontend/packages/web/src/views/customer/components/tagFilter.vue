<template>
  <n-popover trigger="click" placement="bottom" :width="300">
    <template #trigger>
      <n-button :type="selectedTag ? 'primary' : 'default'" ghost class="tag-filter-btn">
        <template #icon>
          <n-icon><Pricetag /></n-icon>
        </template>
        {{ selectedTag ? selectedTag.name : t('customer.tagFilter') }}
        <n-icon v-if="selectedTag" class="ml-[4px] cursor-pointer" @click.stop="handleClear">
          <Close />
        </n-icon>
      </n-button>
    </template>
    <div class="tag-filter-popover">
      <n-input v-model:value="searchKeyword" :placeholder="t('customer.searchTag')" clearable class="mb-[12px]">
        <template #prefix>
          <n-icon><Search /></n-icon>
        </template>
      </n-input>
      <n-spin :show="loading">
        <div v-if="filteredTags.length === 0" class="no-tag">
          {{ t('customer.noTagAvailable') }}
        </div>
        <div v-else class="tag-list">
          <div
            v-for="tag in filteredTags"
            :key="tag.id"
            class="tag-item"
            :class="{ active: selectedTagId === tag.id }"
            @click="handleSelectTag(tag)"
          >
            <span class="tag-color" :style="{ backgroundColor: tag.color }"></span>
            <span class="tag-name">{{ tag.name }}</span>
            <n-icon v-if="selectedTagId === tag.id" class="check-icon">
              <Checkmark />
            </n-icon>
          </div>
        </div>
      </n-spin>
    </div>
  </n-popover>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue';
  import { NButton, NIcon, NInput, NPopover, NSpin } from 'naive-ui';
  import { Checkmark, Close, Pricetag, Search } from '@vicons/ionicons5';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import { getCustomerTagList } from '@/api/modules';

  interface TagItem {
    id: string;
    name: string;
    color: string;
    createTime: number;
  }

  const props = defineProps<{
    modelValue?: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string | undefined): void;
    (e: 'change', tag: TagItem | undefined): void;
  }>();

  const { t } = useI18n();

  const allTags = ref<TagItem[]>([]);
  const loading = ref(false);
  const searchKeyword = ref('');
  const selectedTagId = ref<string | undefined>(props.modelValue);

  const filteredTags = computed(() => {
    if (!searchKeyword.value) {
      return allTags.value;
    }
    return allTags.value.filter((tag) => tag.name.toLowerCase().includes(searchKeyword.value.toLowerCase()));
  });

  const selectedTag = computed(() => {
    if (!selectedTagId.value) return undefined;
    return allTags.value.find((tag) => tag.id === selectedTagId.value);
  });

  async function loadTags() {
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

  function handleSelectTag(tag: TagItem) {
    if (selectedTagId.value === tag.id) {
      selectedTagId.value = undefined;
      emit('update:modelValue', undefined);
      emit('change', undefined);
    } else {
      selectedTagId.value = tag.id;
      emit('update:modelValue', tag.id);
      emit('change', tag);
    }
  }

  function handleClear() {
    selectedTagId.value = undefined;
    emit('update:modelValue', undefined);
    emit('change', undefined);
  }

  watch(
    () => props.modelValue,
    (val) => {
      selectedTagId.value = val;
    }
  );

  onMounted(() => {
    loadTags();
  });
</script>

<style lang="less" scoped>
  .tag-filter-btn {
    margin-right: 8px;
  }
  .tag-filter-popover {
    max-height: 300px;
    .no-tag {
      padding: 20px 0;
      text-align: center;
      color: var(--n-text-color-disabled);
    }
    .tag-list {
      overflow-y: auto;
      max-height: 250px;
      .tag-item {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        cursor: pointer;
        border-radius: 4px;
        transition: background-color 0.2s;
        &:hover {
          background-color: var(--n-color-hover);
        }
        &.active {
          background-color: var(--n-color-hover);
        }
        .tag-color {
          margin-right: 8px;
          width: 12px;
          height: 12px;
          border-radius: 50%;
        }
        .tag-name {
          flex: 1;
        }
        .check-icon {
          color: var(--n-primary-color);
        }
      }
    }
  }
</style>
