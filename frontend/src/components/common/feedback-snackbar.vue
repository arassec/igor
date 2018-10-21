<template>
  <transition name="snackbar" v-if="feedback.message.length > 0">
    <div class="snackbar" :class="{'alert': feedback.alert, 'feedback': !feedback.alert}">
      {{ feedback.message }}
    </div>
  </transition>
</template>

<script>
export default {
  name: 'feedback-snackbar',
  computed: {
    feedback: function () {
      return this.$root.$data.store.getFeedback()
    }
  }
}
</script>

<style scoped>

  .snackbar {
    width: 250px;

    color: var(--font-color-light);
    padding: 15px;
    position: fixed;
    z-index: 1;
    left: calc(100vw - 250px);
    top: 57px;
    -webkit-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    -moz-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
  }

  .feedback {
    background-color: var(--nav-background-color);
  }

  .alert {
    background-color: var(--alert-background-color);
  }

  .snackbar-enter-active, .snackbar-leave-active {
    transition: opacity .75s;
  }

  .snackbar-enter, .snackbar-leave-to {
    opacity: 0;
  }

</style>
