<template>
  <div class="button" v-on:click="$emit('clicked')">
    <font-awesome-icon :icon="icon" :class="iconstyle"/> {{label}}
  </div>

</template>

<script>
export default {
  name: 'label-button',
  props: ['icon', 'label'],
  computed: {
    iconstyle: function () {
      if (this.icon === 'eye' || this.icon === 'clock' || this.icon === 'cogs' || this.icon === 'crosshairs') {
        return 'minus-four'
      }
      return ''
    }
  }
}
</script>

<style scoped>
  .button {
    border: 1px solid var(--main-background-color);
    color: var(--font-color-light);
    background-color: Transparent;
    cursor: pointer;
    height: 25px;
    display: inline-block;
    text-align: center;
    padding: 2px 5px 5px 5px;
  }

  .button::-moz-focus-inner {
    border: 0;
  }

  .button:hover {
    background-color: var(--element-background-color-focus);
    color: var(--font-color-dark);
    cursor: pointer;
  }

  .button:before {
    content: '';
    display: inline-block;
    height: 100%;
    vertical-align: middle;
  }

  svg {
    margin: auto;
    text-align: center;
    vertical-align: middle;
  }

  .minus-four {
    margin-top: -4px;
  }

</style>
