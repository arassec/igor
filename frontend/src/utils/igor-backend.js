import axios from "axios";
import { useFeedbackStore } from "@/stores/feedback";
import { useWipStore } from "@/stores/wip";

export default {
    getData: async function (url) {
        try {
            let response = await axios.get(url);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                useFeedbackStore().setFeedback("Backend request failed! (" + error.response.data + ")", true);
            } else {
                useFeedbackStore().setFeedback("Backend request failed! (" + error + ")", true);
            }
        }
    },
    getResponse: async function (url) {
        try {
            return await axios.get(url);
        } catch (error) {
            if (error.response && error.response.data) {
                useFeedbackStore().setFeedback("Backend request failed! (" + error.response.data + ")", true);
            } else {
                useFeedbackStore().setFeedback("Backend request failed! (" + error + ")", true);
            }
        }
    },
    postData: async function (
        url,
        payload,
        wipMessage,
        successMessage,
        errorMessage,
        cancelCallback,
        cancelTokenSource
    ) {
        if (wipMessage || cancelCallback) {
            useWipStore().setWip(wipMessage, cancelCallback);
        }
        let result = {
            status: 0,
            data: {},
        };
        try {
            let response;
            if (cancelTokenSource) {
                response = await axios.post(url, payload, {
                    cancelToken: cancelTokenSource.token,
                });
            } else {
                response = await axios.post(url, payload);
            }
            useFeedbackStore().setFeedback(successMessage, false);
            result.status = response.status;
            result.data = response.data;
            return result;
        } catch (error) {
            if (!axios.isCancel(error)) {
                if (error.response) {
                    result.status = error.response.status;
                    result.data = error.response.data;
                }
                useFeedbackStore().setFeedback(errorMessage, true);
            }
            return result;
        } finally {
            if (wipMessage) {
                useWipStore().clearWip();
            }
        }
    },
    putData: async function (url, payload, wipMessage, successMessage, errorMessage) {
        useWipStore().setWip(wipMessage);
        try {
            let response = await axios.put(url, payload);
            useFeedbackStore().setFeedback(successMessage, false);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                useFeedbackStore().setFeedback(errorMessage + "(" + error.response.data + ")", true);
            } else {
                useFeedbackStore().setFeedback(errorMessage + "(" + error + ")", true);
            }
        } finally {
            useWipStore().clearWip();
        }
    },
    deleteData: async function (url, wipMessage, successMessage, errorMessage) {
        if (wipMessage) {
            useWipStore().setWip(wipMessage);
        }
        await axios
            .delete(url)
            .then(() => {
                if (successMessage) {
                    useFeedbackStore().setFeedback(successMessage, false);
                }
            })
            .catch((error) => {
                if (error.response && error.response.data) {
                    useFeedbackStore().setFeedback(errorMessage + " (" + error.response.data + ")", true);
                } else {
                    useFeedbackStore().setFeedback(errorMessage + " (" + error + ")", true);
                }
            })
            .finally(() => {
                useWipStore().clearWip();
            });
    },
};
