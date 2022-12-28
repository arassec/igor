import { defineStore } from "pinia";
import { reactive } from "vue";

export const useTupleStore = defineStore("tuple", () => {
    const valueStore = reactive({});

    function setValue(key, value) {
        valueStore[key] = value;
    }

    function getValue(key) {
        return valueStore[key];
    }

    return { setValue, getValue };
});
