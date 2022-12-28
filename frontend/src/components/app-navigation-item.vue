<template>
    <router-link :to="target" :data-e2e="dataEToE">
        <div class="nav-item-container">
            <div class="icon">
                <font-awesome-icon :icon="icon" />
            </div>
            <div class="label">
                {{ label }}
            </div>
        </div>
    </router-link>
</template>

<script setup>
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";

defineProps(["target", "dataEToE", "icon", "label"]);
</script>

<style scoped>
.nav-item-container {
    display: flex;
    flex-direction: column;
    min-width: 6em;
    border-right: 1px solid var(--color-font);
    padding: 0.5em;
}

.nav-item-container:hover {
    cursor: pointer;
    background-color: var(--color-foreground);
}

.icon {
    text-align: center;
    font-size: 200%;
}

.label {
    text-align: center;
    font-size: 75%;
}
</style>
