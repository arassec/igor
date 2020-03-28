<template>
    <a class="overview-tile">
        <div class="content">
            <div class="title" :class="active ? '' : 'disabled'" v-on:click="$emit('clicked')">
                <slot name="title"/>
            </div>
            <div class="menu" v-on:click="$emit('clicked')">
                <slot name="menu"/>
            </div>
            <div slot="action" class="action" v-on:click="$emit('action-clicked')">
                <slot name="action"/>
            </div>
        </div>
    </a>
</template>

<script>
    export default {
        name: "overview-tile",
        props: ['active'],
        components: {}
    }
</script>

<style scoped>

    .overview-tile {
        background-color: var(--color-background);
        color: var(--color-font);
        width: 20em;
        height: 11em;
        margin: 1em;
        transition: .25s;
        display: flex;
    }

    .overview-tile:hover {
        background-color: var(--color-foreground);
        transition: background-color .25s linear;
        cursor: pointer;
    }

    .overview-tile:hover .menu {
        background-color: var(--color-foreground);
        transition: background-color .25s linear;
    }

    .menu {
        padding: .5em .5em .5em .5em;
        background-color: var(--color-background);
        transition: .25s;
    }

    .content {
        overflow: hidden;
        display: flex;
        flex-direction: column;
        width: 100%;
    }

    .title {
        text-align: center;
        vertical-align: middle;
        margin: .5em .2em .2em .2em;
        line-height: 1.25em;
        font-size: 200%;
        overflow: hidden;
        flex-grow: 1;
        z-index: 1;
    }

    .title div {
        height: 100%;
    }

    .disabled {
        opacity: 50%;
    }

</style>