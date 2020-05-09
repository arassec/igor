<template>
    <Navigation/>
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
        --color-font: #FDFDFF;
        --color-foreground: #62929E;
        --color-background: #546A7B;
        --color-alert: #DF6A7C;
    }

    @font-face {
        font-family: 'Roboto';
        src: url('/font/Roboto-Light-webfont.woff') format('woff');
        font-weight: normal;
        font-style: normal;
    }

    body {
        background-color: var(--color-font);
        font-family: 'Roboto', sans-serif;
    }

    a {
        color: var(--color-font);
    }

    h1 {
        font-size: 150%;
        color: var(--color-font);
        margin-bottom: 1em;
    }

    h2 {
        font-size: 125%;
        color: var(--color-font);
        margin-bottom: .5em;
    }

    h3 {
        font-size: 110%;
        color: var(--color-font);
        margin-bottom: .25em;
    }

    select option {
        background-color: var(--color-background);
        color: var(--color-font);
    }

    .margin-right {
        margin-right: .5em;
    }

    .margin-left {
        margin-left: .5em;
    }

    .inactive {
        opacity: 0.5;
    }

    .sticky {
        z-index: 5;
        position: sticky;
        top: 1em;
    }

    .truncate {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .max-width {
        max-width: 38em;
    }

    .highlight {
        max-width: 50em;
        margin: .5em;
    }

    .paragraph {
        margin-bottom: .75em;
    }

    .paragraph ul {
        margin-top: .5em;
        list-style-type: square;
    }

    .paragraph ul li {
        margin-left: 2em;
    }

    .info {
        padding: .25em;
        background-color: var(--color-foreground);
    }

    .alert {
        padding: .25em;
        background-color: var(--color-alert);
    }

    .shadow {
        box-shadow: .1em .1em .25em 0 rgba(163, 163, 163, 0.75);
    }

    .disabled {
        opacity: .5;
    }

    div.table {
        display: table;
        border-collapse: collapse;
    }

    div.tr {
        display: table-row;
    }

    div.td {
        display: table-cell;
        padding: 0 0 .25em .25em;
    }

</style>
