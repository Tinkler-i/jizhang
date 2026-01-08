<template>
  <div class="card" :class="{ [`card-${variant}`]: variant }">
    <div v-if="$slots.header" class="card-header">
      <slot name="header"></slot>
    </div>
    <div class="card-body">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
@import '../assets/styles/card.scss';
</style>
