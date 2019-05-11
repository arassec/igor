<template>
    <div>
        <nav>
            <router-link to="/">
                <font-awesome-icon icon="cog" class="logo"/>
            </router-link>
            <h1>{{heading}}</h1>
        </nav>

        <router-view/>

    </div>
</template>

<script>
  export default {
    name: 'app-navigation',
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
      }
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

</style>
