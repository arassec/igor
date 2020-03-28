<template>
    <button v-on:click.stop="fire()" :disabled="disabled">
        <font-awesome-icon :icon="icon" class="fa-fw"/>
    </button>
</template>

<script>
    export default {
        name: "icon-button",
        props: ['icon', 'disabled'],
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
        background-color: transparent;
        color: var(--color-font);
        border: none;
        padding: 0;
    }

    button:hover:enabled {
        background-color: var(--color-font);
        color: var(--color-background);
        cursor: pointer;
    }

    button:disabled {
        opacity: 0.5;
    }

</style>