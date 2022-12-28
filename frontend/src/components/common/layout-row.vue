<template>
    <div class="row" v-on:click="clickable ? $emit('clicked') : false" :class="clickable ? 'clickable' : ''">
        <div :class="scrollLeft ? 'scroll' : 'left'">
            <slot name="left" />
        </div>
        <div :class="alignLeft ? '' : 'right'">
            <slot name="right" />
        </div>
    </div>
</template>

<script>
export default {
    name: "layout-row",
    props: ["scrollLeft", "alignLeft", "clickable"],
};
</script>

<style scoped>
.row {
    display: flex;
    flex-direction: row;
    padding: 0.25em;
}

.scroll {
    overflow-x: auto;
    margin-right: 1em;
    flex: 1 1 auto;
}

.left {
    flex: 1 1 auto;
    min-width: 0;
}

.right {
    flex: 0 0 auto;
}

.clickable:hover {
    cursor: pointer;
    background-color: var(--color-font);
    color: var(--color-background);
}

.clickable:hover * {
    cursor: pointer;
    color: var(--color-background);
    border-color: var(--color-background);
}

.clickable:hover * .button {
    cursor: pointer;
    color: var(--color-background);
    border-color: var(--color-background);
}

.clickable:hover * .button:hover {
    cursor: pointer;
    color: var(--color-font);
    background-color: var(--color-background);
}
</style>
