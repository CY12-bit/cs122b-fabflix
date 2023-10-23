// let search_form = $("#search_form");
//
// /**
//  * Handle the data returned by ...
//  * @param resultDataString jsonObject
//  */
// function handleSearchResult(resultDataString) {
//     console.log(resultDataString);
//     let resultDataJson = JSON.parse(resultDataString);
//
//     console.log("handle search response");
//     console.log(resultDataJson);
//     console.log(resultDataJson["status"]);
//
//     // If login succeeds, it will redirect the user to index.html
//     if (resultDataJson["status"] === "success") {
//         window.location.replace("movielist.html");
//     } else {
//         // If login fails, the web page will display
//         // error messages on <div> with id "login_error_message"
//         console.log("show error message");
//         console.log(resultDataJson["message"]);
//         $("#search_error_message").text(resultDataJson["message"]);
//     }
// }
//
//
// /**
//  * Submit the form content with POST method
//  * @param formSubmitEvent
//  */
// function submitSearchForm(formSubmitEvent) {
//     console.log("submit search form");
//     /**
//      * When users click the submit button, the browser will not direct
//      * users to the url defined in HTML form. Instead, it will call this
//      * event handler when the event is triggered.
//      */
//     formSubmitEvent.preventDefault();
//     console.log(search_form.serialize());
//     $.ajax(
//         "api/movielist", {
//             method: "GET",
//             // Serialize the login form to the data sent by POST request
//             url: "api/movielist?" + search_form.serialize(), // Setting request url, which is mapped by StarsServlet in Stars.java
//             success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
//         }
//     );
// }

function handleGenreData(genreData) {
    console.log('genres', genreData)
    let genreList = $('#genre-list')
    genreData.forEach(g => {
        genreList.append(`<a href="movielist.html?genre=${g['genre_id']}" class="badge badge-secondary genre-badge">${g['genre_name']}</a>`)
    });
}

function populateTitleBrowse () {
    let alpha = ["A","B","C","D","E","F","G","H","I","J","K","L","M",
                            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"];
    const title_alpha = $('#title-alpha');
    alpha.forEach(o => title_alpha.append(`<a href="movielist.html?beginsWith=${o}">${o}</a>&nbsp;&nbsp;`));

    let num = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*"];
    const title_num = $('#title-num');
    num.forEach(o => title_num.append(`<a href="movielist.html?beginsWith=${o}">${o}</a>&nbsp;&nbsp;`));

}

populateTitleBrowse();

$.ajax(
    "api/allGenres", {
        method: "GET",
        url: "api/allGenres",
        success: (resultData) => handleGenreData(resultData)
    }
);
// Bind the submit action of the form to a handler function
// search_form.submit(submitSearchForm);