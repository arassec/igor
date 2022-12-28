<template>
    <button :class="[disabled ? 'disabled' : '', selected ? 'selected' : '']" v-on:click.stop="fire()" :style="cssVars">
        <font-awesome-icon
            :icon="selected ? 'check-circle' : icon"
            class="fa-fw"
            :class="spinning && !selected ? 'fa-spin' : ''"
        />
        <label v-if="label"> {{ label }}</label>
    </button>
</template>

<script>
export default {
    name: "toggle-button",
    props: ["icon", "disabled", "label", "spinning", "fontcolor", "bgcolor", "selected"],
    methods: {
        fire: function () {
            if (!this.disabled) {
                this.$emit("selected", this.selected);
            }
        },
    },
    computed: {
        cssVars() {
            return {
                "--set-font-color": this.fontcolor,
                "--set-background-color": this.bgcolor,
            };
        },
    },
};
</script>

<style scoped>
button {
    border: 1px solid var(--color-font);
    color: var(--color-font);
    background-color: Transparent;
    cursor: pointer;
    padding: 0.25em;
}

button::-moz-focus-inner {
    border: 0;
}

button:hover:not(.disabled) {
    background-color: var(--color-font);
    color: var(--color-background);
    cursor: pointer;
}

button label:hover:not(.disabled) {
    cursor: pointer;
}

.selected:not(.disabled) {
    color: var(--set-font-color);
    background-color: var(--set-background-color);
}
</style>
