<template>
  <div class="form-group" :class="{ 'has-error': error }">
    <label v-if="label" :for="id">{{ label }}</label>
    <input
      :id="id"
      :value="modelValue"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <span v-if="error" class="error-message">{{ error }}</span>
    <span v-if="help" class="help-text">{{ help }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: String,
  type: {
    type: String,
    default: 'text'
  },
  label: String,
  placeholder: String,
  disabled: Boolean,
  required: Boolean,
  error: String,
  help: String,
  id: {
    type: String,
    default: () => 'input-' + Math.random().toString(36).substr(2, 9)
  }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
@import '../assets/styles/form.scss';
</style>
