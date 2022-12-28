<template>
    <div id="app-content" data-e2e="app-content">
        <nav>
            <div class="main-menu">
                <ul>
                    <li>
                        <app-navigation-item
                            target="/"
                            data-e-to-e="navigation-job-overview"
                            icon="toolbox"
                            label="Jobs"
                        />
                    </li>
                    <li>
                        <app-navigation-item
                            target="/connector-overview"
                            data-e-to-e="navigation-connector-overview"
                            icon="link"
                            label="Connectors"
                        />
                    </li>
                </ul>
            </div>
            <div class="title">
                <h1 class="heading" data-e2e="navigation-heading">
                    {{ heading }}
                </h1>
                <a class="documentation-link" target="_blank" rel="noopener noreferrer" href="/igor/index.html">
                    <div class="top-logo-container">
                        <img alt="igor-logo" src="@/assets/igor-logo-top.png" class="top-logo" />
                        <label class="version">v{{ version }}</label>
                    </div>
                </a>
            </div>
        </nav>

        <router-view />
    </div>
</template>

<script>
import AppNavigationItem from "@/components/app-navigation-item.vue";
import { version } from "@/../package.json";

export default {
    name: "app-navigation",
    components: { AppNavigationItem },
    data: function () {
        return {
            importFile: null,
            showImportDialog: false,
        };
    },
    computed: {
        heading: function () {
            if (this.$route.name === "app-status") {
                return "Dashboard";
            } else if (this.$route.name === "job-overview") {
                return "Job Overview";
            } else if (this.$route.name === "job-editor") {
                return "Job-Editor";
            } else if (this.$route.name === "connector-overview") {
                return "Connector Overview";
            } else if (this.$route.name === "connector-editor") {
                return "Connector-Editor";
            } else {
                return "Undefined";
            }
        },
        version: function () {
            return version;
        },
    },
};
</script>

<style scoped>
.main-menu {
    flex-basis: 50%;
    display: flex;
    flex-direction: row;
}

.main-menu a {
    text-decoration: none;
}

.title {
    flex-basis: 50%;
    display: flex;
}

.title ul {
    float: right;
}

nav {
    flex: 1;
    display: flex;
    flex-direction: row;
    background-color: var(--color-background);
    border-bottom: 2px solid var(--color-font);
}

nav ul {
    flex-grow: 1;
}

nav ul li {
    float: left;
}

nav h1 {
    flex-grow: 2;
    font-size: 350%;
    margin: 0 0 0 -2em;
}

.top-logo-container {
    display: flex;
    flex-direction: column;
}

.top-logo {
    height: 4em;
    float: right;
    padding: 0.25em;
}

.version {
    margin: -0.75em 0 0 1em;
    font-size: 75%;
}

.version:hover {
    cursor: pointer;
}

.documentation-link {
    text-decoration: none;
}

.documentation-link:hover {
    background-color: var(--color-foreground);
}
</style>
