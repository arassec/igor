<template>
    <div class="container">
        <slot />
        <feedback-snackbar />
        <wip-window />
    </div>
</template>

<script>
import FeedbackSnackbar from "./feedback-snackbar.vue";
import WipWindow from "./wip-window.vue";

export default {
    name: "core-container",
    components: { WipWindow, FeedbackSnackbar },
};
</script>

<style scoped>
.container {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
}
</style>
