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
        margin-right: 25px;
        flex-shrink: 1;
    }

    .overflow-hidden {
        overflow: hidden;
    }

</style>
