<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<template>
    <div class="image-upload-target-vue">
        <form :action="completeFormAction"
              class="image-dropzone dropzone"
              id="my-image-dropzone">
        </form>        
    </div>
</template>

<script>
 module.exports = {
     props: {
         message: {
             type: String,
             default: "Drag one or more files here. Or click to open a file picker.",
         },
         isPublic: {
             default: false
         },
         callback: Function,
         formAction: {
             type: String,
             default: "/st-user-uploads/upload-file"
         },         
     },
     computed: {
         completeFormAction: function() {
             var action = this.formAction;
             if (action.indexOf('?') === -1) {
                 action += '?';
             } else {
                 action += '&';
             }
             if (this.isPublic) {
                 action += 'stUploadIsPublic=true';
             }
             return action;
         }
     },
     mounted: function() {
         var self = this;
         console.log('form-components/image-uploader.mounted');
         self.dropzone = new Dropzone($(self.$el).find('.image-dropzone').get(0), {
             dictDefaultMessage: self.message,
             uploadMultiple: false,
             headers: {
                 'X-Requested-By': 'XMLHttpRequest'
             },
             //             parallelUploads: true,
             maxFiles: 1,
             acceptedFiles: 'image/*,.jpg,.png,.svg,.gif',
             init: function() {
                 this.on("uploadprogress", function(file, percent, c, d) {
                     if (percent === 100) {
                         $(file.previewTemplate).find(".dz-progress").html("Processing...");
                     }
                 });
                 this.on("success", function(file, response) { 
                     //var o = JSON.parse(response);
                     this.removeFile(file);                     
                     if (self.callback) {
                         self.callback(response);
                     }
                     self.$emit('uploaded', {domFile: file, fileInfo: response});

                 });
             },
         });

     }
 };
</script>
