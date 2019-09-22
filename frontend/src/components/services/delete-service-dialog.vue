<template>
    <modal-dialog @close="$emit('cancel')">
        <h1 slot="header">Delete Service?</h1>
        <div slot="body">
            <div class="paragraph">
                Do you really want to delete service:
                <div class="truncate highlight">{{serviceName}}</div>
            </div>
            <div class="paragraph" v-if="affectedJobs.length > 0">
                The following jobs are using this service and will not be working any more:
                <ul class="a">
                    <li v-for="affectedJob in affectedJobs" :key="affectedJob.key">
                        <div class="truncate highlight">{{affectedJob.value}}</div>
                    </li>
                </ul>
            </div>
            <div class="paragraph" v-if="affectedJobs.length > 0">
                Delete affected jobs, too:
                <font-awesome-icon :icon="deleteJobs ? 'check-square' : 'square'" v-on:click="deleteJobs = !deleteJobs"/>
            </div>
        </div>
        <div slot="footer">
            <layout-row>
                <input-button slot="left" v-on:clicked="$emit('cancel')" icon="times"/>
                <input-button slot="right" v-on:clicked="deleteJobs ? $emit('delete-plus') : $emit('delete')" icon="check"/>
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
    name: "delete-service-dialog",
    components: {InputButton, LayoutRow, ModalDialog},
    props: ['serviceId', 'serviceName'],
    data: function () {
      return {
        affectedJobs: [],
        deleteJobs: false
      }
    },
    mounted: function () {
      IgorBackend.getData('/api/service/' + this.serviceId + '/job-references?pageNumber=0&pageSize=666').then((data) => {
        this.affectedJobs = Array.from(data.items)
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
