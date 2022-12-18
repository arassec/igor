<template>
    <div class="tooltip">
        <span v-if="hasValidationErrors()" class="tooltiptext">{{validationError}}</span>
        <input :type="type" autocomplete="off" :disabled="disabled" :value="text"
               @input="updateSelf($event.target.value)"
               :class="hasValidationErrors() ? 'alert' : ''"/>
    </div>
</template>

<script>
    export default {
        name: "input-validated",
        props: ['text', 'type', 'disabled', 'propertyId', 'parentId', 'validationErrors', 'isNumber'],
        model: {
            prop: "text",
            event: "input"
        },
        methods: {
            updateSelf(text) {
                if (this.isNumber) {
                    let reg = /^-?\d+$/;
                    if (reg.test(text) === true) {
                        this.$emit("input", text);
                    }
                } else {
                    this.$emit("input", text);
                }
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

</style>
