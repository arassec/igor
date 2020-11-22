<template>
    <div id="app-content" data-e2e="app-content">
        <nav>
            <div class="main-menu">
                <router-link to="/" class="top-logo-link">
                    <div class="top-logo-container">
                        <img alt="igor-logo" src="@/assets/igor-logo-top.png" class="top-logo"/>
                    </div>
                </router-link>
                <ul>
                    <li>
                        <router-link to="/" data-e2e="navigation-job-overview">
                            <font-awesome-icon icon="toolbox"/>
                            Jobs
                        </router-link>
                    </li>
                    <li>
                        <router-link to="/connector-overview" data-e2e="navigation-connector-overview">
                            <font-awesome-icon icon="link"/>
                            Connectors
                        </router-link>
                    </li>
                </ul>
            </div>
            <div class="title">
                <div>
                    <h1 class="heading" data-e2e="navigation-heading">{{ heading }}</h1>
                    <a class="documentation-link" target="_blank" rel="noopener noreferrer"
                       href="/igor/index.html">
                        v{{ version }} Reference Documentation
                    </a>
                </div>
            </div>
        </nav>

        <router-view/>

    </div>
</template>

<script>
import {version} from '../../package.json';

export default {
    name: 'app-navigation',
    components: {},
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
            } else if (this.$route.name === 'job-overview') {
                return 'Job Overview'
            } else if (this.$route.name === 'job-editor') {
                return 'Job-Editor'
            } else if (this.$route.name === 'connector-overview') {
                return 'Connector Overview'
            } else if (this.$route.name === 'connector-editor') {
                return 'Connector-Editor'
            } else {
                return 'Undefined'
            }
        },
        version: function () {
            return version;
        }
    }
}
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
}

nav {
    flex: 1;
    display: flex;
    flex-direction: row;
    background-color: var(--color-background);
    border-bottom: 2px solid var(--color-font);
    height: 2.8em;
}

nav ul {
    flex-grow: 1;
    margin-left: 2em;
}

nav ul li {
    float: left;
    margin-left: .5em;
}

nav ul li a {
    color: var(--color-font);
    text-decoration: none;
    font-size: 120%;
    outline: none;
    padding: 0.2em;
}

nav ul li:hover a {
    background-color: var(--color-foreground);
}

nav h1 {
    flex-grow: 2;
    font-size: 175%;
    margin: 0 1.5em 0 -5em;
}

.documentation-link {
    margin: -2.5em .25em 0 0;
    font-size: 85%;
    float: right;
}

.top-logo-link {
    margin-right: 6em;
}

.top-logo {
    height: 2.4em;
    position: absolute;
    top: 3px;
    left: 3px;
}

.top-logo-container div {
    text-decoration: none;
}

.top-logo-container:hover {
    cursor: pointer;
}

</style>
