<template>
    <button :class="disabled ? 'disabled' : ''" v-on:click.stop="fire()">
        <font-awesome-icon :icon="icon" class="fa-fw" /><label v-if="label"> {{ label }}</label>
    </button>
</template>

<script>
export default {
    name: "add-item-button",
    props: ["icon", "disabled", "label"],
    methods: {
        fire: function () {
            if (!this.disabled) {
                this.$emit("clicked");
            }
        },
    },
};
</script>

<style scoped>
button {
    border: 1px solid var(--color-background);
    color: var(--color-background);
    background-color: Transparent;
    cursor: pointer;
    padding: 0.25em;
    min-width: 9em;
}

button::-moz-focus-inner {
    border: 0;
}

button:hover:not(.disabled) {
    background-color: var(--color-foreground);
    border: 1px solid var(--color-foreground);
    color: var(--color-font);
    cursor: pointer;
}

button label:hover:not(.disabled) {
    cursor: pointer;
}
</style>
