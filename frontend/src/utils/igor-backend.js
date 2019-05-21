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
  postData: async function (url, payload, wipMessage, successMessage, errorMessage) {
    store.setWip(wipMessage)
    try {
      let response = await window.axios.post(url, payload)
      store.setFeedback(successMessage, false)
      return response.data
    } catch (error) {
      if (error.response.data) {
        store.setFeedback(errorMessage + '(' + error.response.data + ')', true)
      } else {
        store.setFeedback(errorMessage + '(' + error + ')', true)
      }
      if (error.response.data === 'NAME_ALREADY_EXISTS_ERROR') {
        return error.response.data
      }
    } finally {
      store.clearWip()
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
    store.setWip(wipMessage)
    await window.axios.delete(url).then(() => {
      store.setFeedback(successMessage, false)
    }).catch((error) => {
      if (error.response.data) {
        store.setFeedback(errorMessage + '(' + error.response.data + ')', true)
      } else {
        store.setFeedback(errorMessage + '(' + error + ')', true)
      }
    }).finally(() => {
      store.clearWip()
    })
  }
}
