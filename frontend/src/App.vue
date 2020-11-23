<template>
    <app-navigation/>
</template>

<script>
import AppNavigation from "./components/app-navigation";

export default {
        name: 'app',
        components: {
            AppNavigation
        }
    }
</script>

<style>
    /* reset css */
    html {
        box-sizing: border-box;
    }

    body {
        overflow-x: hidden;
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
        --animate-css-duration: 0.3s;
    }

    @font-face {
        font-family: 'Roboto';
        src: url('/fonts/Roboto-Light-webfont.woff') format('woff');
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

    *:focus {
        outline: none;
    }

    .margin-right {
        margin-right: .5em;
    }

    .margin-left {
        margin-left: .5em;
    }

    .margin-bottom {
        margin-bottom: 2em;
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

    .disabled {
        opacity: .5;
    }

    div.table {
        display: table;
        border-collapse: collapse;
        width: 100%;
    }

    div.tr {
        display: table-row;
    }

    div.td {
        display: table-cell;
        padding: 0 0 .25em .25em;
    }

    div.td:first-child {
        min-width: 10em;
    }

    div.td:last-child:not(.align-left) {
        text-align: right;
        width: 100%;
    }

    .tooltip {
        position: relative;
        display: inline-block;
        width: 100%;
    }

    .tooltip .tooltiptext {
        visibility: hidden;
        background-color: var(--color-foreground);
        color: var(--color-font);
        text-align: center;
        padding: .1em .5em .1em .5em;
        position: absolute;
        z-index: 1;
        bottom: 125%;
        left: 50%;
        transform: translateX(-50%);
    }

    .tooltip:hover .tooltiptext {
        visibility: visible;
    }

    .tooltip .tooltiptext::after {
        content: " ";
        position: absolute;
        top: 100%; /* At the bottom of the tooltip */
        left: 50%;
        margin-left: -5px;
        border-width: 5px;
        border-style: solid;
        border-color: var(--color-foreground) transparent transparent transparent;
    }

    .full-width {
        width: 100%;
    }

    .align-left {
        text-align: left;
    }

    .spacer-top {
        border-top: .1em solid var(--color-font);
    }

</style>
