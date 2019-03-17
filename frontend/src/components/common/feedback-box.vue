<template>
  <button-row :class="{info: !alert,  alert: alert}" class="margin-bottom">
    <slot slot="left" name="feedback"/>
    <slot slot="right" name="button"/>
  </button-row>
</template>

<script>
import ButtonRow from './button-row'
export default {
  name: 'feedback-box',
  components: {ButtonRow},
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
