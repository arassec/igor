<template>
    <div class="tooltip">
        <span  v-if="hasValidationErrors()" class="tooltiptext">{{validationError}}</span>
        <textarea rows="8" cols="35" :type="type" autocomplete="off" :disabled="disabled" :value="text" @input="updateSelf($event.target.value)"
               :class="hasValidationErrors() ? 'alert' : ''" class="textarea"/>
    </div>

</template>

<script>
    export default {
        name: "textarea-validated",
        props: ['text', 'type', 'disabled', 'propertyId', 'parentId', 'validationErrors'],
        model: {
            prop: "text",
            event: "input"
        },
        methods: {
            updateSelf(text) {
                this.$emit("input", text);
            },
            hasValidationErrors() {
                if (this.validationErrors) {
                    return (this.parentId in this.validationErrors) && (this.propertyId in this.validationErrors[this.parentId]);
                }
                return false;
            }
        },
        computed: {
            validationError: function () {
                if (this.validationErrors) {
                    return this.validationErrors[this.parentId][this.propertyId];
                }
                return null;
            }
        }
    }
</script>

<style scoped>

    .tooltip .tooltiptext {
        background-color: var(--color-alert);
        border: 1px solid var(--color-font);
    }

    .tooltip .tooltiptext::after {
        border-color: var(--color-font) transparent transparent transparent;
    }

    .textarea {
        width: 100%;
        height: 100%;
        resize: vertical;
    }

</style>