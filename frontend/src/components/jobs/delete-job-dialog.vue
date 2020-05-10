<template>
    <modal-dialog @close="$emit('cancel')">
        <h1 slot="header">Delete Job?</h1>
        <div slot="body">
            <div class="paragraph">
                Do you really want to delete job:
                <div class="truncate highlight">{{jobName}}</div>
            </div>
            <div class="paragraph" v-if="exclusiveConnectors.length > 0">
                The following connectors are only used by this job:
                <ul class="a">
                    <li v-for="exclusiveConnector in exclusiveConnectors" :key="exclusiveConnector.key">
                        <div class="truncate highlight">{{exclusiveConnector.value}}</div>
                    </li>
                </ul>
            </div>
            <div class="paragraph" v-if="exclusiveConnectors.length > 0">
                Delete unused connectors, too:
                <font-awesome-icon :icon="deleteJob ? 'check-square' : 'square'"
                                   v-on:click="deleteJob = !deleteJob"/>
            </div>
        </div>

        <div slot="footer">
            <layout-row>
                <input-button slot="left" v-on:clicked="$emit('cancel')" icon="times"/>
                <input-button slot="right" v-on:clicked="deleteJob ? $emit('delete-plus') : $emit('delete')"
                              icon="check"/>
            </layout-row>
        </div>
    </modal-dialog>
</template>

<script>
    import ModalDialog from "../common/modal-dialog";
    import LayoutRow from "../common/layout-row";
    import InputButton from "../common/input-button";
    import IgorBackend from "../../utils/igor-backend.js";

    export default {
        name: "delete-job-dialog",
        components: {InputButton, LayoutRow, ModalDialog},
        props: ['jobId', 'jobName'],
        data: function () {
            return {
                exclusiveConnectors: [],
                deleteJob: false
            }
        },
        mounted: function () {
            IgorBackend.getData('/api/job/' + this.jobId + "/exclusive-connector-references").then((data) => {
                this.exclusiveConnectors = Array.from(data)
            })
        }
    }
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
