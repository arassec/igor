<template>
    <div class="flex-container">
        <input-validated  :disabled="disabled"
                          :parent-id="parentId"
                          :property-id="propertyId"
                          :validation-errors="validationErrors"
                          :type="type"
                          :text="text"
                          @input="updateSelf"
        />
        <input-button :icon="icon"
                      v-on:clicked="$emit('icon-clicked')"
                      class="small-margin-left"/>
    </div>
</template>

<script>
import InputValidated from "./input-validated";
import InputButton from "./input-button";

export default {
        name: "input-validated-with-button",
        props: ['text', 'type', 'disabled', 'propertyId', 'parentId', 'validationErrors', 'icon'],
        components: {InputButton, InputValidated},
        model: {
            prop: "text",
            event: "input"
        },
        methods: {
            updateSelf(text) {
                this.$emit("input", text);
            },
        }
        }
</script>

<style scoped>

    .flex-container {
        display: flex;
    }

    .small-margin-left {
      margin-left: .3em;
    }

</style>