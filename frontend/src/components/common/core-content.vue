<template>
  <div class="content">
    <slot/>
  </div>
</template>

<script>
export default {
  name: 'core-content'
}
</script>

<style scoped>

  .content {
    min-width: var(--content-width);
    margin-right: 25px;
    flex-shrink: 0;
  }

</style>
