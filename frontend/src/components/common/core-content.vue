<template>
    <div class="content" :class="{'overflow-hidden': overflowHidden}">
        <slot/>
    </div>
</template>

<script>
    export default {
        name: 'core-content',
        props: ['overflowHidden']
    }
</script>

<style scoped>

    .content {
        margin: 1em 1em 0 0;
        flex-shrink: 1;
    }

    .overflow-hidden {
        overflow: hidden;
    }

</style>
