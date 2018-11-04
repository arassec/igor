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
    background-color: var(--alert-background-color);
    color: var(--font-color-light);
  }

</style>
