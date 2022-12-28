import { defineStore } from "pinia";
import { reactive, ref } from "vue";

export const useFeedbackStore = defineStore("feedback", () => {
    const routeChanged = ref(false);

    const feedback = reactive({
        timeout: 0,
        message: "",
        alert: false,
    });

    function setRouteChanged(value) {
        routeChanged.value = value;
    }

    function isRouteChanged() {
        return routeChanged.value;
    }

    function setFeedback(message, alert) {
        feedback.message = message;
        feedback.alert = alert;
        routeChanged.value = false;
        let component = this;
        if (feedback.timeout > 0) {
            clearTimeout(feedback.timeout);
        }
        if (feedback.alert) {
            feedback.timeout = setTimeout(function () {
                component.clearFeedback();
            }, 15000);
        } else {
            feedback.timeout = setTimeout(function () {
                component.clearFeedback();
            }, 5000);
        }
    }

    function getFeedback() {
        return feedback;
    }

    function clearFeedback() {
        feedback.message = "";
        feedback.alert = false;
        routeChanged.value = false;
    }

    return {
        setRouteChanged,
        isRouteChanged,
        setFeedback,
        getFeedback,
        clearFeedback,
    };
});
