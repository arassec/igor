<template>
  <div class="row" v-on:click="$emit('clicked')">
    <div class="left">
      <slot name="left"/>
    </div>
    <div class="right">
      <slot name="right"/>
    </div>
  </div>
</template>

<script>
export default {
  name: 'layout-row'
}
</script>

<style scoped>

  .row {
    display: flex;
    flex-direction: row;
  }

  .left {

  }

  .right {
    margin-left: auto;
  }

</style>
