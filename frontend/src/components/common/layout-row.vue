<template>
    <div class="row" v-on:click="clickable ? $emit('clicked') : false" :class="clickable ? 'clickable' : ''">
    <div :class="scrollLeft ? 'scroll' : ''">
      <slot name="left"/>
    </div>
        <div :class="alignLeft ? '' : 'right'">
      <slot name="right"/>
    </div>
  </div>
</template>

<script>
export default {
  name: 'layout-row',
  props: ['scrollLeft', 'alignLeft', 'clickable']
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

  .clickable:hover {
      cursor: pointer;
      background-color: var(--main-background-color);
      color: var(--panel-background-color);
  }

  .clickable:hover * {
      cursor: pointer;
      color: var(--panel-background-color);
      border-color: var(--panel-background-color);
  }

  .clickable:hover * .button {
      cursor: pointer;
      color: var(--panel-background-color);
      border-color: var(--panel-background-color);
  }

  .clickable:hover * .button:hover {
      cursor: pointer;
      color: var(--font-color-light);
      background-color: var(--panel-background-color);
  }


</style>
