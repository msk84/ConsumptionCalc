let addCounter = document.getElementById('AddCounter')
addCounter.addEventListener('shown.bs.modal', function (event) {
    $("#counterName").focus();
})

function showModalOnErrors(hasErrors) {
    if(hasErrors) {
        $('#AddCounter').modal('show');
        console.debug("Showing modal due to errors.");
    }
}