import { defineStore } from "pinia";
import { reactive } from "vue";

export const useJobDataStore = defineStore("jobdata", () => {
    const jobData = reactive({
        jobConfiguration: null,
        selectionKey: null,
        parameterIndex: null,
        connectorCategoryCandidates: null,
        connectorParameter: null,
    });

    function setJobData(jobConfiguration, selectionKey, parameterIndex, connectorCategoryCandidates) {
        jobData.jobConfiguration = jobConfiguration;
        jobData.selectionKey = selectionKey;
        jobData.parameterIndex = parameterIndex;
        jobData.connectorCategoryCandidates = connectorCategoryCandidates;
    }

    function getJobData() {
        return jobData;
    }

    function clearJobData() {
        jobData.jobConfiguration = null;
        jobData.selectionKey = null;
        jobData.parameterIndex = null;
        jobData.connectorCategoryCandidates = null;
        jobData.connectorParameter = null;
    }

    return { setJobData, getJobData, clearJobData };
});
