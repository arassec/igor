<template>
    <div>
        <nav>
            <ul>
                <li>
                    <router-link to="/">
                        <font-awesome-icon icon="toolbox"/> Jobs
                    </router-link>
                </li>
                <li>
                    <router-link to="/service-overview">
                        <font-awesome-icon icon="cogs"/> Services
                    </router-link>
                </li>
            </ul>
            <h1 class="heading">{{heading}}</h1>
        </nav>

        <router-view/>

    </div>
</template>

<script>
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
                } else if (this.$route.name === 'service-overview') {
                    return 'Service Overview'
                } else if (this.$route.name === 'service-editor') {
                    return 'Service-Editor'
                } else {
                    return 'Undefined'
                }
            }
        }
    }
</script>

<style scoped>

    nav {
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
        margin: .5em 1em .5em 1em;
    }

    nav ul li a {
        color: var(--color-font);
        text-decoration: none;
        font-size: 150%;
        padding: .35em;
        outline: none;
    }

    nav ul li a:hover {
        background-color: var(--color-foreground);
    }

    nav h1 {
        flex-grow: 1;
        font-size: 220%;
        margin: 0 1.5em 0 -5em;
    }

</style>
