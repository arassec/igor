<template>
    <transition name="snackbar" v-if="feedback.message.length > 0">
        <div class="snackbar" :class="{'alert': feedback.alert, 'feedback': !feedback.alert}">
            <layout-row :scroll-left="true">
                <p slot="left">
                    {{ feedback.message }}
                </p>
                <input-button slot="right" icon="times" v-on:clicked="clearFeedback"/>
            </layout-row>

        </div>
    </transition>
</template>

<script>
import LayoutRow from './layout-row'
import InputButton from './input-button'

export default {
    name: 'feedback-snackbar',
    components: {InputButton, LayoutRow},
    methods: {
      clearFeedback: function () {
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
        overflow: hidden;
        max-width: 400px;
        width: 400px;
        color: var(--color-font);
        padding: 15px;
        display: block;
        position: fixed;
        z-index: 999;
        right: 25%;
        left: 50%;
        margin-left: -200px;
        bottom: 0px;
        border-left: .1em solid var(--color-font);
        border-top: .1em solid var(--color-font);
        border-right: .1em solid var(--color-font);
    }

    .feedback {
        background-color: var(--color-foreground);
    }

    .alert {
        background-color: var(--color-alert);
    }

    .snackbar-enter-active, .snackbar-leave-active {
        transition: opacity .75s;
    }

    .snackbar-enter, .snackbar-leave-to {
        opacity: 0;
    }

</style>
