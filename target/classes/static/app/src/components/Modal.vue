<template>
  <Teleport v-if="modelValue" to="body">
    <div class="modal-overlay" @click="handleBackdropClick">
      <div class="modal" :class="{ [`modal-${size}`]: size }">
        <div class="modal-header">
          <h2>{{ title }}</h2>
          <button class="modal-close" @click="$emit('update:modelValue', false)">
            ✕
          </button>
        </div>
        <div class="modal-body">
          <slot></slot>
        </div>
        <div v-if="$slots.footer" class="modal-footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  modelValue: Boolean,
  title: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md', 'lg'].includes(v)
  },
  closeOnBackdrop: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

const handleBackdropClick = (event) => {
  // 只在点击背景（不是 modal 内容）时关闭
  if (props.closeOnBackdrop && event.target.classList.contains('modal-overlay')) {
    emit('update:modelValue', false)
  }
}
</script>

<style scoped>
@import '../assets/styles/modal.scss';
</style>
