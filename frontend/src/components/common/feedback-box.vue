<template>
    <layout-row :class="{info: !alert,  alert: alert}" class="margin-bottom"
                v-on:clicked="clickable ? $emit('feedback-clicked') : false"
                :clickable="clickable" :align-left="alignLeft">
    <slot slot="left" name="left"/>
    <slot slot="right" name="right"/>
  </layout-row>
</template>

<script>
    import LayoutRow from './layout-row'

    export default {
  name: 'feedback-box',
  components: {LayoutRow},
    props: ['alert', 'clickable', 'alignLeft']
}
</script>

<style scoped>

  div {
    color: var(--color-font);
    line-height: 25px;
    padding: 5px;
  }

  .margin-bottom {
    margin-bottom: 5px;
  }

  .info {
    background-color: var(--color-foreground);
  }

  .alert {
    background-color: var(--color-alert);
  }

</style>
