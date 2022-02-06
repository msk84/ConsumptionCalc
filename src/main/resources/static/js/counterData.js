$(function(){

    $("#AddCounterData").on('show.bs.modal', function(){
        $("addCounterDataTimestamp").val(Date.now());
        //console.log("set timestamp");
    });

});