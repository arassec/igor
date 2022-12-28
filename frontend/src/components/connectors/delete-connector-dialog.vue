<template>
    <modal-dialog @close="$emit('cancel')">
        <template v-slot:header>
            <h1>Delete Connector?</h1>
        </template>
        <template v-slot:body>
            <div class="paragraph">
                Do you really want to delete connector:
                <div class="truncate highlight">{{ connectorName }}</div>
            </div>
            <div class="paragraph" v-if="affectedJobs.length > 0">
                The following jobs are using this connector and will not be working any more:
                <ul class="a">
                    <li v-for="affectedJob in affectedJobs" :key="affectedJob.key">
                        <div class="truncate highlight">
                            {{ affectedJob.value }}
                        </div>
                    </li>
                </ul>
            </div>
            <div class="paragraph" v-if="affectedJobs.length > 0">
                Delete affected jobs, too:
                <font-awesome-icon
                    :icon="deleteJobs ? 'check-square' : 'square'"
                    v-on:click="deleteJobs = !deleteJobs"
                />
            </div>
        </template>
        <template v-slot:footer>
            <layout-row>
                <template v-slot:left>
                    <input-button v-on:clicked="$emit('cancel')" icon="times" />
                </template>
                <template v-slot:right>
                    <input-button
                        v-on:clicked="deleteJobs ? $emit('delete-plus') : $emit('delete')"
                        icon="check"
                        data-e2e="delete-connector-confirm"
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
    name: "delete-connector-dialog",
    components: { InputButton, LayoutRow, ModalDialog },
    props: ["connectorId", "connectorName"],
    data: function () {
        return {
            affectedJobs: [],
            deleteJobs: false,
        };
    },
    mounted: function () {
        IgorBackend.getData("/api/connector/" + this.connectorId + "/job-references?pageNumber=0&pageSize=666").then(
            (data) => {
                this.affectedJobs = Array.from(data.items);
            }
        );
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
