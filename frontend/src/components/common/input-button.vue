<template>
    <button v-on:click="$emit('clicked')">
        <slot>Button</slot>
    </button>
</template>

<script>
export default {
  name: 'input-button'
}
</script>

<style scoped>
    button {
        border: 1px solid var(--main-background-color);
        color: var(--font-color-light);
        background-color: Transparent;
        cursor: pointer;
        height: 25px;
    }

    button::-moz-focus-inner {
        border: 0;
    }

    button:hover {
        background-color: var(--element-background-color-focus);
        color: var(--font-color-dark);
        cursor: pointer;
    }

</style>
