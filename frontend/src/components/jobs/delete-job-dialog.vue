<template>
    <modal-dialog @close="$emit('cancel')">
        <template v-slot:header>
            <h1>Delete Job?</h1>
        </template>
        <template v-slot:body>
            <div class="paragraph">
                Do you really want to delete job:
                <div class="truncate highlight">{{ jobName }}</div>
            </div>
            <div class="paragraph" v-if="exclusiveConnectors.length > 0">
                The following connectors are only used by this job:
                <ul class="a">
                    <li v-for="exclusiveConnector in exclusiveConnectors" :key="exclusiveConnector.key">
                        <div class="truncate highlight">
                            {{ exclusiveConnector.value }}
                        </div>
                    </li>
                </ul>
            </div>
            <div class="paragraph" v-if="exclusiveConnectors.length > 0">
                Delete unused connectors, too:
                <font-awesome-icon :icon="deleteJob ? 'check-square' : 'square'" v-on:click="deleteJob = !deleteJob" />
            </div>
        </template>
        <template v-slot:footer>
            <layout-row>
                <template v-slot:left>
                    <input-button v-on:clicked="$emit('cancel')" icon="times" />
                </template>
                <template v-slot:right>
                    <input-button
                        v-on:clicked="deleteJob ? $emit('delete-plus') : $emit('delete')"
                        icon="check"
                        data-e2e="delete-job-confirm-button"
                    />
                </template>
            </layout-row>
        </template>
    </modal-dialog>
</template>

<script>
import ModalDialog from "../common/modal-dialog.vue";
import LayoutRow from "../common/layout-row.vue";
import InputButton from "../common/input-button.vue";
import IgorBackend from "../../utils/igor-backend.js";

export default {
    name: "delete-job-dialog",
    components: { InputButton, LayoutRow, ModalDialog },
    props: ["jobId", "jobName"],
    data: function () {
        return {
            exclusiveConnectors: [],
            deleteJob: false,
        };
    },
    mounted: function () {
        IgorBackend.getData("/api/job/" + this.jobId + "/exclusive-connector-references")
            .then((data) => {
                this.exclusiveConnectors = Array.from(data);
            })
            .catch((error) => {
                console.error("Error during backend request: " + error);
            });
    },
};
</script>

<style scoped>
.paragraph {
    margin-bottom: 20px;
}

.paragraph ul {
    margin-top: 10px;
    list-style-type: square;
}

.paragraph ul li {
    margin-left: 45px;
}
</style>
