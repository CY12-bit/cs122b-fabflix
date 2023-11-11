
function handleAddMovie(resultData) {
    if (resultData["status"] === "success") {
        $('#add-movie-form')[0].reset();
        let ids = `star id: ${resultData['star_id']}, movie id: ${resultData['movie_id']}, genre id: ${resultData['genre_id']}`;
        $("#result-message").text(ids);
    } else {
        $("#result-message").text(resultData['message']);
    }

}

let addMovieForm = $('#add-movie-form')
function addMovie(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        "../api/employee/dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: 'type=movie&' + addMovieForm.serialize(),
            success: handleAddMovie
        }
    );
}

addMovieForm.submit(addMovie);