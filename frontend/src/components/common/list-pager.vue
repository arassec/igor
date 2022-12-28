<template>
    <div class="list-pager" v-if="page.totalPages > 0" :class="dark ? 'dark' : ''">
        <input-button
            icon="angle-double-left"
            v-on:clicked="$emit('first')"
            class="pager-button margin-right"
            :disabled="page.number === 0"
        />
        <input-button
            icon="angle-left"
            v-on:clicked="$emit('previous')"
            class="pager-button margin-right"
            :disabled="page.number === 0"
        />
        <label :class="page.totalPages === 1 ? 'disabled' : ''">{{ page.number + 1 }}/{{ page.totalPages }}</label>
        <input-button
            icon="angle-right"
            v-on:clicked="$emit('next')"
            class="pager-button margin margin-left margin-right"
            :disabled="page.number === page.totalPages - 1"
        />
        <input-button
            icon="angle-double-right"
            v-on:clicked="$emit('last')"
            class="pager-button"
            :disabled="page.number === page.totalPages - 1"
        />
    </div>
</template>

<script>
import InputButton from "./input-button.vue";

export default {
    name: "list-pager",
    components: { InputButton },
    props: ["page", "dark"],
};
</script>

<style scoped>
.list-pager {
    margin: 1em;
    text-align: center;
}

.dark .pager-button {
    border: 1px solid var(--color-background);
    background-color: var(--color-background);
    color: var(--color-font);
}

.dark .pager-button:hover {
    border: 1px solid var(--color-foreground);
    background-color: var(--color-foreground);
    color: var(--color-font);
}

.dark .disabled:hover {
    border: 1px solid var(--color-background);
    background-color: var(--color-background);
}
</style>
