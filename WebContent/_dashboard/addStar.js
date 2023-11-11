
function handleAddStar(resultData) {
    if (resultData["status"] === "success") {
        $('#add-star-form')[0].reset();
        let ids = `successfully added star id: ${resultData['star_id']}`;
        $("#result-message").text(ids);
    } else {
        $("#result-message").text(resultData['message']);
    }

}

let addMovieForm = $('#add-star-form')
function addStar(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        "../api/employee/dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: 'type=star&' + addMovieForm.serialize(),
            success: handleAddStar
        }
    );
}

addMovieForm.submit(addStar);