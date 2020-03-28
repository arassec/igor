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
    color: var(--color-font);
    background-color: var(--color-background);
    margin-bottom: .5em;
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
    color: var(--color-font);
    margin-right: 15px;
  }

  .panel >>> input, select {
    border: none;
    background-color: var(--color-foreground);
    height: 25px;
    min-width: 280px;
    color: var(--color-font);
  }

  .panel >>> input {
    text-indent: 5px;
  }

  .panel >>> select option {
    border: none;
    outline: none;
    background-color: var(--color-foreground);
    height: 25px;
  }

  .panel >>> select:focus,
  .panel >>> input:focus {
    background-color: var(--color-font);
    color: var(--color-background);
    outline: none;
  }

</style>
