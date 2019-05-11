<template>
  <div class="list-name">
    <slot/>
  </div>
</template>

<script>
export default {
  name: 'list-name'
}
</script>

<style scoped>
  .list-name {
    margin-top: 2px;
  }
</style>
