<template>
    <button :class="disabled ? 'disabled' : ''" v-on:click.stop="fire()">
        <font-awesome-icon :icon="icon" class="fa-fw"/>
        <label v-if="label"> {{label}}</label>
    </button>
</template>

<script>
    export default {
        name: 'input-button',
        props: ['icon', 'disabled', 'label'],
        methods: {
            fire: function () {
                if (!this.disabled) {
                    this.$emit('clicked')
                }
            }
        }
    }
</script>

<style scoped>
    button {
        border: 1px solid var(--color-font);
        color: var(--color-font);
        background-color: Transparent;
        cursor: pointer;
        padding: .25em;
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

</style>
