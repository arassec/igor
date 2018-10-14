<template>
  <div>
    <nav>
      <router-link to="/" v-bind:class="isActiveTab('home') ? activeClass : ''"
                   @click.native="setActiveTab('home')">Home
      </router-link>
      <router-link to="/jobs" v-bind:class="isActiveTab('jobs') ? activeClass : ''"
                   @click.native="setActiveTab('jobs')">Jobs
      </router-link>
      <router-link to="/services" v-bind:class="isActiveTab('services') ? activeClass : ''"
                   @click.native="setActiveTab('services')">Services
      </router-link>
    </nav>

    <router-view/>

  </div>
</template>

<script>
export default {
  name: 'app-navigation',
  data: function () {
    return {
      choice: this.$route.name,
      activeClass: 'active'
    }
  },
  methods: {
    setActiveTab: function (val) {
      this.choice = val
    },
    isActiveTab: function (val) {
      return this.choice === val
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

  nav a {
    text-decoration: none;
    cursor: pointer;
    font-size: 150%;
    color: var(--font-color-light);
    padding: 5px 10px 5px 10px;
    outline: none;
  }

  .active {
    background-color: var(--panel-background-color);
  }

</style>
