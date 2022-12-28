<template>
    <div class="content">
        <slot />
    </div>
</template>

<script>
export default {
    name: "core-content",
};
</script>

<style scoped>
.content {
    margin: 1em 1em 0 0;
    flex-shrink: 1;
}
</style>
