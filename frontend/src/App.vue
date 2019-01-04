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
    --main-background-color: #FDFDFF;
    --nav-background-color: #62929E;
    --panel-background-color: #546A7B;
    --font-color-light: #FDFDFF;
    --font-color-dark: #546A7B;
    --element-background-color: #62929E;
    --element-background-color-focus: #FDFDFF;
    --info-background-color: #62929E;
    --alert-background-color: #DF6A7C;
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

  .button-margin-right {
    margin-right: 5px;
  }

  .button-margin-left {
    margin-left: 5px;
  }

</style>
