<template>
  <layout-row :class="{info: !alert,  alert: alert}" class="margin-bottom" v-on:clicked="$emit('feedback-clicked')">
    <slot slot="left" name="left"/>
    <slot slot="right" name="right"/>
  </layout-row>
</template>

<script>
import LayoutRow from './layout-row'
export default {
  name: 'feedback-box',
  components: {LayoutRow},
  props: ['alert']
}
</script>

<style scoped>

  div {
    color: var(--font-color-light);
    line-height: 25px;
    padding: 5px;
  }

  .margin-bottom {
    margin-bottom: 5px;
  }

  .info {
    background-color: var(--info-background-color);
  }

  .alert {
    background-color: var(--alert-background-color);
  }

</style>
