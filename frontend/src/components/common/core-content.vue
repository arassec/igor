<template>
    <div class="content">
        <slot/>
    </div>
</template>

<script>
    export default {
        name: 'core-content'
    }
</script>

<style scoped>

    .content {
        margin-right: 25px;
        flex-shrink: 1;
        overflow: hidden;
    }

</style>
