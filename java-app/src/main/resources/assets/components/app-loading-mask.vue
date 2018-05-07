
<style lang="scss">
 .app-loading-mask-vue {
     position: fixed;
     top: 0px;
     left: 0px;
     width: 100vw;
     height: 100vh;
     z-index: 5000;
     background-color: #E9E9E9;
     padding-top: 20%;
     text-align: center;
     img {
         max-height: 40px;
         max-width: 40px;
     }
 }
</style>

<template>
    <div v-if="$store.state.appLoading" class="app-loading-mask-vue">
        <h4>Application Loading</h4>
        <img src="/st-resource/clubby/spinner.gif">
    </div>
</template>

<script>
 module.exports = {
     props: {

     },
     data: function() {
         return {
         };
     },
     methods: {
     }
 };
</script>
