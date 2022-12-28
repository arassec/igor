<template>
    <div class="panel" :class="small ? 'core-panel-small' : 'core-panel-normal'">
        <slot />
    </div>
</template>

<script>
export default {
    name: "core-panel",
    props: ["small"],
};
</script>

<style scoped>
.panel {
    color: var(--color-font);
    background-color: var(--color-background);
}

.core-panel-small {
    padding: 0.1em 0.5em 0.1em 0.5em;
}

.core-panel-normal {
    padding: 0.5em;
}

.panel :deep(table tr) {
    line-height: 1.5em;
}

.panel :deep(table tr td) {
    text-align: left;
    padding-right: 1em;
}

.panel :deep(label) {
    color: var(--color-font);
    margin-right: 1em;
}

.panel :deep(input, select) {
    border: none;
    background-color: var(--color-foreground);
    height: 2em;
    min-width: 20em;
    width: 100%;
    color: var(--color-font);
}

.panel :deep(select) {
    border: none;
    background-color: var(--color-foreground);
    height: 2em;
    min-width: 20em;
    width: 100%;
    color: var(--color-font);
}

.panel :deep(textarea) {
    background-color: var(--color-foreground);
    color: var(--color-font);
    border: none;
}

.panel :deep(input) {
    text-indent: 0.2em;
}

.panel :deep(input.alert) {
    background-color: var(--color-alert);
}

.panel :deep(textarea.alert) {
    background-color: var(--color-alert);
}

.panel :deep(select:focus),
.panel :deep(input:focus) {
    background-color: var(--color-font);
    color: var(--color-background);
    outline: none;
}
</style>
