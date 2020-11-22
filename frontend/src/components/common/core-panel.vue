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
  }

  .core-panel-small {
    padding: .1em .5em .1em .5em;
  }

  .core-panel-normal {
    padding: .5em;
  }

  .panel >>> table tr {
    line-height: 1.5em;
  }

  .panel >>> table tr td {
    text-align: left;
    padding-right: 1em;
  }

  .panel >>> label {
    color: var(--color-font);
    margin-right: 1em;
  }

  .panel >>> input, select {
    border: none;
    background-color: var(--color-foreground);
    height: 2em;
    min-width: 20em;
    width: 100%;
    color: var(--color-font);
  }

  .panel >>> textarea {
    background-color: var(--color-foreground);
    color: var(--color-font);
    border: none;
  }

  .panel >>> input {
    text-indent: .2em;
  }

  .panel >>> input.alert {
    background-color: var(--color-alert);
  }

  .panel >>> textarea.alert {
    background-color: var(--color-alert);
  }

  .panel >>> select option {
    border: none;
    outline: none;
    background-color: var(--color-foreground);
    height: 2em;
  }

  .panel >>> select:focus,
  .panel >>> input:focus {
    background-color: var(--color-font);
    color: var(--color-background);
    outline: none;
  }

</style>
