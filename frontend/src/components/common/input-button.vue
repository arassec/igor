<template>
  <div :class="disabled ? 'disabled' : 'button'" v-on:click="fire()">
    <font-awesome-icon :icon="icon" :class="iconstyle"/>
  </div>
</template>

<script>
export default {
  name: 'input-button',
  props: ['icon', 'disabled'],
  methods: {
    fire: function () {
      if (!this.disabled) {
        this.$emit('clicked')
      }
    }
  },
  computed: {
    iconstyle: function () {
      if (this.icon === 'eye' || this.icon === 'clock' || this.icon === 'cogs' || this.icon === 'info') {
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
    width: 25px;
    display: inline-block;
    text-align: center;
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

  .disabled {
    border: 1px solid var(--main-background-color);
    color: var(--font-color-light);
    background-color: Transparent;
    height: 25px;
    width: 25px;
    display: inline-block;
    text-align: center;
    opacity: 0.5;
  }

  .disabled::-moz-focus-inner {
    border: 0;
  }

  .disabled:before {
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
