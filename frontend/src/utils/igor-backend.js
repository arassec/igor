import {store} from '../main.js'

export default {
    getData: async function (url) {
        try {
            let response = await window.axios.get(url)
            return response.data
        } catch (error) {
            if (error.response.data) {
                store.setFeedback('Backend request failed! (' + error.response.data + ')', true)
            } else {
                store.setFeedback('Backend request failed! (' + error + ')', true)
            }
        }
    },
    getResponse: async function (url) {
        try {
            return await window.axios.get(url)
        } catch (error) {
            if (error.response.data) {
                store.setFeedback('Backend request failed! (' + error.response.data + ')', true)
            } else {
                store.setFeedback('Backend request failed! (' + error + ')', true)
            }
        }
    },
    postData: async function (url, payload, wipMessage, successMessage, errorMessage, cancelCallback, cancelTokenSource) {
        if (wipMessage || cancelCallback) {
            store.setWip(wipMessage, cancelCallback)
        }
        let result = {
            status: 0,
            data: {}
        }
        try {
            let response;
            if (cancelTokenSource) {
                response = await window.axios.post(url, payload, {cancelToken: cancelTokenSource.token});
            } else {
                response = await window.axios.post(url, payload);
            }
            store.setFeedback(successMessage, false);
            result.status = response.status;
            result.data = response.data;
            return result;
        } catch (error) {
            if (!window.axios.isCancel(error)) {
                result.status = error.response.status;
                result.data = error.response.data;
                store.setFeedback(errorMessage, true)
            }
            return result;
        } finally {
            if (wipMessage) {
                store.clearWip()
            }
        }
    },
    putData: async function (url, payload, wipMessage, successMessage, errorMessage) {
        store.setWip(wipMessage)
        try {
            let response = await window.axios.put(url, payload)
            store.setFeedback(successMessage, false)
            return response.data
        } catch (error) {
            if (error.response.data) {
                store.setFeedback(errorMessage + '(' + error.response.data + ')', true)
            } else {
                store.setFeedback(errorMessage + '(' + error + ')', true)
            }
        } finally {
            store.clearWip()
        }
    },
    deleteData: async function (url, wipMessage, successMessage, errorMessage) {
        if (wipMessage) {
            store.setWip(wipMessage)
        }
        await window.axios.delete(url).then(() => {
            if (successMessage) {
                store.setFeedback(successMessage, false)
            }
        }).catch((error) => {
            if (error.response.data) {
                store.setFeedback(errorMessage + ' (' + error.response.data + ')', true)
            } else {
                store.setFeedback(errorMessage + ' (' + error + ')', true)
            }
        }).finally(() => {
            store.clearWip()
        })
    }
}
