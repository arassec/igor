<template>
  <div class="error">
    <slot/>
  </div>
</template>

<script>
export default {
  name: 'validation-error'
}
</script>

<style scoped>

  .error {
    padding: 0px 5px 0px 5px;
    line-height: 24px;
    color: var(--color-alert);
    font-weight: bold;
  }

</style>
