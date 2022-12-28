import { defineStore } from "pinia";
import { reactive } from "vue";

export const useConnectorDataStore = defineStore("connectordata", () => {
    const connectorData = reactive({
        connectorConfiguration: null,
    });

    function setConnectorData(connectorConfiguration) {
        connectorData.connectorConfiguration = connectorConfiguration;
    }

    function getConnectorData() {
        return connectorData;
    }

    function clearConnectorData() {
        connectorData.connectorConfiguration = null;
    }

    return { setConnectorData, getConnectorData, clearConnectorData };
});
