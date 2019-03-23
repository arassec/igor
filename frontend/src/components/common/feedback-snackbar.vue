<template>
  <transition name="snackbar" v-if="feedback.message.length > 0">
    <div class="snackbar" :class="{'alert': feedback.alert, 'feedback': !feedback.alert}">
      <button-row>
        <p slot="left">
        {{ feedback.message }}
        </p>
        <input-button slot="right" icon="times" v-on:clicked="clearFeedback"/>
      </button-row>

    </div>
  </transition>
</template>

<script>
import ButtonRow from './button-row'
import InputButton from './input-button'
export default {
  name: 'feedback-snackbar',
  components: {InputButton, ButtonRow},
  methods: {
    clearFeedback: function() {
      this.$root.$data.store.clearFeedback()
    }
  },
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
