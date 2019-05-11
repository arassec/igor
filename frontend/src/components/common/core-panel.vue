<template>
  <div class="panel" :class="small ? 'core-panel-small' : 'core-panel-normal'">
    <slot/>
  </div>
</template>

<script>
export default {
  name: 'core-panel',
  props: ['small']
}
</script>

<style scoped>

  .panel {
    color: var(--font-color-light);
    background-color: var(--panel-background-color);
    margin-bottom: 10px;
    -webkit-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    -moz-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
  }

  .core-panel-small {
    padding: 5px 15px 5px 15px;
  }

  .core-panel-normal {
    padding: 15px;
  }

  .panel >>> table tr {
    line-height: 25px;
  }

  .panel >>> table tr td {
    text-align: left;
    padding-right: 15px;
  }

  .panel >>> label {
    color: var(--font-color-light);
    margin-right: 15px;
  }

  .panel >>> input, select {
    border: none;
    background-color: var(--element-background-color);
    height: 25px;
    min-width: 300px;
    color: var(--font-color-light);
  }

  .panel >>> input {
    text-indent: 5px;
  }

  .panel >>> select option {
    border: none;
    outline: none;
    background-color: var(--element-background-color);
    height: 25px;
  }

  .panel >>> select:focus,
  .panel >>> input:focus {
    background-color: var(--element-background-color-focus);
    color: var(--panel-background-color);
    outline: none;
  }

</style>
