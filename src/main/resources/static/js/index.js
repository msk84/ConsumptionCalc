let addProject = document.getElementById('AddProject')
addProject.addEventListener('shown.bs.modal', function (event) {
    $("#projectName").focus();
})

function showModalOnErrors(hasErrors) {
    if(hasErrors) {
        $('#AddProject').modal('show');
        console.debug("Showing modal due to errors.");
    }
}