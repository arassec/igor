<template>
  <div class="test-result-marker">
    <div class="label">
      <font-awesome-icon icon="plug" class="test-button" v-on:click="$emit('clicked')"/>
    </div>
  </div>
</template>

<script>

export default {
  name: 'test-result-marker',
}
</script>

<style scoped>

  .test-result-marker {
    position: relative;
    right: calc(-1 * var(--content-width));
    height: 0;
  }

  .label {
    position: absolute;
    left: -15px;
    top: -15px;
    background-color: var(--panel-background-color);
    width: 35px;
    height: 35px;

    border-radius: 0px 25px 25px 0px;

    -webkit-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    -moz-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
  }

  .arrow {
    position: absolute;
    top: -15px;
    left: 10px;
    width: 0;
    height: 0;
    border-top: 18px solid transparent;
    border-bottom: 17px solid transparent;
    border-left: 15px solid var(--panel-background-color);
  }

  .test-button {
    margin: 10px 0px 0px 10px;
  }

  .test-button:hover {
    color: var(--font-color-dark);
    cursor: pointer;
  }

</style>
