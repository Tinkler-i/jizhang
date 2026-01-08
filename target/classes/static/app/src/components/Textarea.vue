<template>
  <div class="form-group" :class="{ 'has-error': error }">
    <label v-if="label" :for="id">{{ label }}</label>
    <textarea
      :id="id"
      v-model="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      :rows="rows"
      @input="$emit('update:modelValue', $event.target.value)"
    ></textarea>
    <span v-if="error" class="error-message">{{ error }}</span>
    <span v-if="help" class="help-text">{{ help }}</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: String,
  label: String,
  placeholder: String,
  disabled: Boolean,
  required: Boolean,
  error: String,
  help: String,
  rows: {
    type: Number,
    default: 4
  },
  id: {
    type: String,
    default: () => 'textarea-' + Math.random().toString(36).substr(2, 9)
  }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
@import '../assets/styles/form.scss';
</style>
