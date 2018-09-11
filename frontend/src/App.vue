<template>
  <div id="app">
    <Navigation/>
  </div>
</template>

<script>
import Navigation from './components/app-navigation'

export default {
  name: 'app',
  components: {
    Navigation
  }
}
</script>

<style>
  /* reset css */
  html {
    box-sizing: border-box;
    font-size: 16px;
  }

  *, *:before, *:after {
    box-sizing: inherit;
  }

  body, h1, h2, h3, h4, h5, h6, p, ol, ul {
    margin: 0;
    padding: 0;
    font-weight: normal;
  }

  ol, ul {
    list-style: none;
  }

  img {
    max-width: 100%;
    height: auto;
  }

  /* igor css */
  :root {
    --main-background-color: #eeeeee;
    --nav-background-color: #5BB6A7;
    --panel-background-color: #3C99A0;
    --font-color-light: #eeeeee;
    --font-color-dark: #444444;
    --element-background-color: #999999;
    --element-background-color-focus: #ffffff;
    --info-background-color: #5BB6A7;
    --alert-background-color: #DF6A7C;
    --content-width: 800px;
  }

  body {
    background-color: var(--main-background-color);
    font-family: "Courier New", Courier, monospace;
  }

  h1 {
    font-size: 150%;
    color: var(--font-color-light);
    margin-bottom: 20px;
  }

  h2 {
    font-size: 125%;
    color: var(--font-color-light);
    margin-bottom: 15px;
  }

  h3 {
    font-size: 110%;
    color: var(--font-color-light);
    margin-bottom: 10px;
  }

  select option {
    background-color: var(--panel-background-color);
    color: var(--font-color-light);
  }

</style>
