<template>
  <div class="list-name truncate">
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
    margin-top: 3px;
  }
</style>
