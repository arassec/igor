<template>
    <modal-dialog @close="$emit('cancel')">
        <h1 slot="header">Delete Job?</h1>
        <div slot="body">
            <div class="paragraph">
                Do you really want to delete job '{{jobName}}'?
            </div>
            <div class="paragraph" v-if="exclusiveServices.length > 0">
                The following services are only used by this job:
                <ul class="a">
                    <li v-for="exclusiveService in exclusiveServices" :key="exclusiveService.key">
                        {{exclusiveService.value}}
                    </li>
                </ul>
            </div>
            <div class="paragraph" v-if="exclusiveServices.length > 0">
                Delete unused services, too:
                <font-awesome-icon :icon="deleteServices ? 'check-square' : 'square'" v-on:click="deleteServices = !deleteServices"/>
            </div>
        </div>

        <div slot="footer">
            <button-row>
                <input-button slot="left" v-on:clicked="$emit('cancel')" icon="times"/>
                <input-button slot="right" v-on:clicked="deleteServices ? $emit('delete-plus') : $emit('delete')" icon="check"/>
            </button-row>
        </div>
    </modal-dialog>
</template>

<script>
    import ModalDialog from "../common/modal-dialog";
    import ButtonRow from "../common/button-row";
    import InputButton from "../common/input-button";
    export default {
        name: "delete-job-dialog",
        components: {InputButton, ButtonRow, ModalDialog},
        props: ['jobId', 'jobName'],
        data: function () {
            return {
                exclusiveServices: [],
                deleteServices: false
            }
        },
        mounted: function () {
            let component = this
            this.$http.get('/api/job/' + component.jobId + "/exclusive-service-references").then(function (response) {
                component.exclusiveServices = Array.from(response.data)
            }).catch(function (error) {
                component.$root.$data.store.setFeedback('Could not load unused services (' + error + ')', true)
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
