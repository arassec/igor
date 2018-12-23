<template>
  <div class="container">
    <slot/>

    <feedback-snackbar/>

  </div>
</template>

<script>
import FeedbackSnackbar from './feedback-snackbar'

export default {
  name: 'core-container',
  components: {FeedbackSnackbar}
}
</script>

<style scoped>

  .container {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    flex-wrap: nowrap ;
  }

</style>
