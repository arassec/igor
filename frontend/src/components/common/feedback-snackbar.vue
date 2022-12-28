<template>
    <transition name="snackbar" v-if="feedback.message.length > 0" appear>
        <div class="snackbar" :class="{ alert: feedback.alert, feedback: !feedback.alert }" data-e2e="snackbar">
            <layout-row :scroll-left="true">
                <template v-slot:left>
                    {{ feedback.message }}
                </template>
                <template v-slot:right>
                    <input-button icon="times" v-on:clicked="clearFeedback" data-e2e="dismiss-snackbar-button" />
                </template>
            </layout-row>
        </div>
    </transition>
</template>

<script>
import LayoutRow from "./layout-row.vue";
import InputButton from "./input-button.vue";
import { useFeedbackStore } from "@/stores/feedback";

export default {
    name: "feedback-snackbar",
    components: { InputButton, LayoutRow },
    methods: {
        clearFeedback: function () {
            useFeedbackStore().clearFeedback();
        },
    },
    computed: {
        feedback: function () {
            return useFeedbackStore().getFeedback();
        },
    },
};
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
    border-left: 0.1em solid var(--color-font);
    border-top: 0.1em solid var(--color-font);
    border-right: 0.1em solid var(--color-font);
}

.feedback {
    background-color: var(--color-foreground);
}

.alert {
    background-color: var(--color-alert);
}

.snackbar-enter-active,
.snackbar-leave-active {
    transition: opacity 0.75s;
}

.snackbar-enter-from,
.snackbar-leave-to {
    opacity: 0;
}
</style>
