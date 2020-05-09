<template>
    <core-content class="sticky ">
        <div class="documentation-container">

            <input-button slot="right" icon="times" v-on:clicked="$emit('close')" style="float: right;"/>

            <div id="documentation" v-html="format(documentation)"/>
        </div>
    </core-content>
</template>

<script>
    import CoreContent from "./core-content";
    import InputButton from "./input-button";

    export default {
        name: "documentation-container",
        components: {InputButton, CoreContent},
        props: ['documentation'],
        methods: {
            format: function () {
                if (!this.documentation) {
                    return;
                }
                let MarkdownIt = require('markdown-it');
                let md = new MarkdownIt();
                return md.render(this.documentation);
            }
        }
    }
</script>

<style scoped>

    .content {
        z-index: 3;
        flex: 2;
        margin-right: 0;
        overflow: hidden;
    }

    .documentation-container {
        color: var(--color-font);
        background-color: var(--color-background);
        padding: 1em;
    }

    #documentation >>> h1:not(:first-of-type) {
        margin: 2em 0 1em 0;
    }

    #documentation >>> table {
        border-collapse: collapse;
        margin: 1em 0 1em 0;
    }

    #documentation >>> p {
        margin: 0 0 1em 0;
    }

    #documentation >>> pre {
        border: 1px solid var(--color-font);
        padding: .5em;
    }

    #documentation >>> table, th, td {
        border: 1px solid var(--color-font);
    }

    #documentation >>> th {
        border: 1px solid var(--color-font);
        padding: .25em;
    }

    #documentation >>> td {
        border: 1px solid var(--color-font);
        padding: .25em;
    }

</style>