<template>
    <div class="container">
        <slot/>
    </div>
</template>

<script>
export default {
  name: 'core-container'
}
</script>

<style scoped>

    .container {
        display: flex;
    }

</style>
