import { defineStore } from "pinia";
import { reactive } from "vue";

export const useWipStore = defineStore("wip", () => {
    const wip = reactive({
        timeout: undefined,
        message: "",
        cancelCallback: undefined,
    });

    function setWip(message, cancelCallback) {
        let component = this;
        this.wip.cancelCallback = cancelCallback;
        this.wip.timeout = setTimeout(function () {
            component.wip.message = message;
        }, 250);
    }

    function getWip() {
        return this.wip;
    }

    function clearWip() {
        if (this.wip.timeout !== undefined) {
            clearTimeout(this.wip.timeout);
        }
        this.wip.cancelCallback = undefined;
        this.wip.message = "";
    }

    return { wip, setWip, getWip, clearWip };
});
