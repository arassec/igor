<template>
    <div class="flex-container">
        <input-validated
            :disabled="disabled"
            :parent-id="parentId"
            :property-id="propertyId"
            :validation-errors="validationErrors"
            :type="type"
            :model-value="modelValue"
            @input="updateSelf"
        />
        <input-button
            :icon="icon"
            v-on:clicked="$emit('icon-clicked')"
            class="small-margin-left"
            :data-e2e="buttonDataE2e"
        />
    </div>
</template>

<script>
import InputValidated from "./input-validated.vue";
import InputButton from "./input-button.vue";

export default {
    name: "input-validated-with-button",
    props: ["modelValue", "type", "disabled", "propertyId", "parentId", "validationErrors", "icon", "buttonDataE2e"],
    components: { InputButton, InputValidated },
    methods: {
        updateSelf(text) {
            this.$emit("update:modelValue", text);
        },
    },
};
</script>

<style scoped>
.flex-container {
    display: flex;
}

.small-margin-left {
    margin-left: 0.3em;
}
</style>
