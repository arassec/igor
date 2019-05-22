<template>
  <div class="row" v-on:click="$emit('clicked')">
    <div :class="scrollLeft ? 'scroll' : ''">
      <slot name="left"/>
    </div>
    <div class="right">
      <slot name="right"/>
    </div>
  </div>
</template>

<script>
export default {
  name: 'layout-row',
  props: ['scrollLeft']
}
</script>

<style scoped>

  .row {
    display: flex;
    flex-direction: row;
  }

  .scroll {
    overflow-x: auto;
    margin-right: 15px;
  }

  .right {
    margin-left: auto;
  }

</style>
