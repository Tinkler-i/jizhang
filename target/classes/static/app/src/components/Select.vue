<template>
  <div class="form-group" :class="{ 'has-error': error }">
    <label v-if="label" :for="id">{{ label }}</label>
    <select
      :id="id"
      :value="modelValue"
      :disabled="disabled"
      :required="required"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <!-- 支持通过 slot 传入 option 和 optgroup -->
      <slot>
        <!-- 如果没有 slot，则使用 props 中的 options -->
        <option v-if="placeholder" value="">{{ placeholder }}</option>
        <option v-for="item in options" :key="item.value" :value="item.value">
          {{ item.label }}
        </option>
      </slot>
    </select>
    <span v-if="error" class="error-message">{{ error }}</span>
    <span v-if="help" class="help-text">{{ help }}</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: [String, Number],
  label: String,
  placeholder: String,
  options: {
    type: Array,
    default: () => []
  },
  disabled: Boolean,
  required: Boolean,
  error: String,
  help: String,
  id: {
    type: String,
    default: () => 'select-' + Math.random().toString(36).substr(2, 9)
  }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
@import '../assets/styles/form.scss';
</style>
