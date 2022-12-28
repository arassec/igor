<template>
    <div>
        <div>
            <img src="../../assets/igor-logo-background.png" alt="igor-logo-512" width="512" />
            <font-awesome-icon :icon="icon" class="bi-right fa-2x" />
        </div>
    </div>
</template>

<script>
export default {
    name: "background-icon",
    props: ["icon"],
};
</script>

<style scoped>
div {
    font-size: 48px;
}

img {
    opacity: 0.1;
    position: fixed;
    bottom: -50px;
    right: -10px;
    z-index: -1;
}

.bi-right {
    color: var(--color-background);
    opacity: 0.1;
    position: fixed;
    bottom: 140px;
    right: 370px;
    z-index: -1;
}
</style>
