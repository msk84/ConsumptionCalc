$(function(){
    $("#AddCounterData").on('show.bs.modal', function(){
        $("addCounterDataTimestamp").val(Date.now());
        $("#counterValue").val("0.0");
        $("#comment").val("");
    });

    $("#AddCounterData").on('shown.bs.modal', function(){
        $("#counterValue").focus();
    });
});

function showModalOnErrors(hasErrors) {
    if(hasErrors) {
        $('#AddCounterData').modal('show');
        console.debug("Showing modal due to errors.");
    }
}