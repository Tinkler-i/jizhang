<template>
  <button
    :class="[
      'btn',
      `btn-${type}`,
      `btn-${size}`,
      { 'btn-block': block, 'is-loading': loading }
    ]"
    :disabled="disabled || loading"
  >
    <slot></slot>
  </button>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'secondary', 'danger', 'success'].includes(v)
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md', 'lg'].includes(v)
  },
  disabled: Boolean,
  loading: Boolean,
  block: Boolean
})
</script>

<style scoped>
@import '../assets/styles/button.scss';
</style>
