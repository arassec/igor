<template>
  <div :class="{info: !alert,  alert: alert}">
    <font-awesome-icon v-if="icon != null && iconClass != null" :icon="icon" :class="iconClass"/>
    <font-awesome-icon v-if="icon != null && iconClass == null" :icon="icon"/>
    <label>{{text}}</label>
  </div>
</template>

<script>
export default {
  name: 'feedback-box',
  props: ['text', 'alert', 'icon', 'iconClass']
}
</script>

<style scoped>

  div {
    color: var(--font-color-light);
    line-height: 25px;
    max-width: calc(var(--content-width) - 30px);
    padding: 5px 15px 5px 15px;
  }

  .info {
    background-color: var(--info-background-color);
  }

  .alert {
    background-color: var(--alert-background-color);
  }

</style>
