<template>
    <div>
        <layout-row>
            <div slot="left">
                <nav>
                    <router-link to="/">
                        <font-awesome-icon icon="cog" class="logo"/>
                    </router-link>
                    <h1>{{heading}}</h1>
                </nav>
            </div>
            <div slot="right" v-if="isOnAppStatus">
                <font-awesome-icon class="nav-button" icon="file-upload" v-on:click.stop="openShowImportDialog"/>
            </div>
        </layout-row>

        <router-view/>

        <modal-dialog v-if="showImportDialog" @close="showImportDialog = false" v-on:cancel="showImportDialog = false">
            <h1 slot="header">Import data?</h1>
            <div slot="body">
                <div class="paragraph">
                    Select a previously exported JSON file to import.
                </div>
                <div class="paragraph alert">
                    WARNING: existing services and jobs will be overwritten by the import!
                </div>
                <div class="paragraph">
                    <label>Select file: </label>
                    <label>
                        <input-button class="button-margin-right" icon="folder-open"/>
                        <input type="file" @change="importFileChanged"/>
                    </label>
                    <label v-if="importFile != null">{{importFile.name}}</label>
                </div>
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showImportDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="executeImport" icon="check"/>
            </layout-row>
        </modal-dialog>

    </div>
</template>

<script>
    import ModalDialog from "./common/modal-dialog";
    import LayoutRow from "./common/layout-row";
    import InputButton from "./common/input-button";
    import IgorBackend from "../utils/igor-backend";

    export default {
        name: 'app-navigation',
        components: {InputButton, LayoutRow, ModalDialog},
        data: function () {
            return {
                importFile: null,
                showImportDialog: false
            }
        },
        computed: {
            heading: function () {
                if (this.$route.name === 'app-status') {
                    return 'Dashboard'
                } else if (this.$route.name === 'job-editor') {
                    return 'Job-Editor'
                } else if (this.$route.name === 'service-editor') {
                    return 'Service-Editor'
                } else {
                    return 'Undefined'
                }
            },
            isOnAppStatus: function() {
                return (this.$route.name === 'app-status');
            }
        },
        methods: {
            openShowImportDialog: function () {
                this.importFile = null;
                this.showImportDialog = true;
            },
            importFileChanged: function (event) {
                this.importFile = event.target.files[0];
            },
            executeImport: function () {
                if (!this.importFile) {
                    this.showImportDialog = false;
                } else {
                    let reader = new FileReader();
                    reader.onload = (e) => {
                        this.showImportDialog = false;
                        IgorBackend.postData('/api/transfer', JSON.parse(e.target.result), 'Importing job', 'Import finished', 'Import failed').then(() => {
                            this.importFile = null
                        });
                    };
                    reader.readAsText(this.importFile);
                }
            },
        }
    }
</script>

<style scoped>

    nav {
        display: flex;
        background-color: var(--nav-background-color);
        margin-bottom: 20px;
        -webkit-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        -moz-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    }

    nav h1 {
        font-size: 225%;
        margin: 0px 0px 0px 10px;
    }

    input[type="file"] {
        display: none;
    }

    .logo {
        background-color: var(--font-color-dark);
        color: var(--font-color-light);
        font-size: 255%;
        padding: 5px;
    }

    .logo:hover {
        background-color: var(--element-background-color-focus);
        color: var(--font-color-dark);
        cursor: pointer;

    }

    .nav-button {
        background-color: var(--element-background-color);
        color: var(--font-color-light);
        font-size: 255%;
        padding: 5px;
    }

    .nav-button:hover {
        background-color: var(--element-background-color-focus);
        color: var(--font-color-dark);
        cursor: pointer;
    }

</style>
