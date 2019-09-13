(function ( $ ) {
 
    $.fn.jPreview = function() {
        var jPreview = this;

        jPreview.preview = function(selector){
            var container = $(selector).data('jpreview-container');

            $(selector).change(function(){
                $(container).empty();
                $.each(selector.files, function(index, file){
                    var imageData = jPreview.readImageData(file, function(data){
                        jPreview.addPreviewImage(container, data);
                    });
                });
            });
        }

        jPreview.readImageData = function(file, successCallback){
            var reader = new FileReader();
            reader.onload = function(event){
                successCallback(event.target.result);
            }
            reader.readAsDataURL(file);
        }
        
        jPreview.addPreviewImage = function(container, imageDataUrl){
            $(container).append('<div class="jpreview-image img-responsive thumbnail" style="background-image: url('+ imageDataUrl +')"></div>');
        }

        var selectors = $(this);
        return $.each(selectors, function(index, selector){
            jPreview.preview(selector);
        });
 
    };
 
}( jQuery ));