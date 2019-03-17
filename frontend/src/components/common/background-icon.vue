<template>
  <div>
    <div v-if="right">
      <font-awesome-icon v-if="iconTwo != null" :icon="iconTwo" class="bi-right-left fa-6x"/>
      <font-awesome-icon :icon="iconOne" class="bi-right fa-10x"/>
    </div>
    <div v-if="left">
      <font-awesome-icon :icon="iconOne" class="bi-left fa-10x"/>
    </div>
  </div>
</template>

<script>
export default {
  name: 'background-icon',
  props: ['iconOne', 'iconTwo', 'left', 'right']
}
</script>

<style scoped>

  div {
    font-size: 48px;
  }

  .bi-right {
    color: var(--nav-background-color);
    opacity: 0.3;
    position: fixed;
    bottom: -20px;
    right: 20px;
    z-index: -1;
  }

  .bi-right-left {
    color: var(--nav-background-color);
    opacity: 0.3;
    position: fixed;
    bottom: -20px;
    right: 420px;
    z-index: -1;
  }

  .bi-left {
    color: var(--nav-background-color);
    opacity: 0.3;
    position: fixed;
    bottom: 10px;
    left: 20px;
    z-index: -1;
  }

</style>
