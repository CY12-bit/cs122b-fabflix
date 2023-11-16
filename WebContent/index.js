
/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // TODO: if you want to check past query results first, you can do it here
    console.log('checking session data', query, sessionStorage.getItem(query))
    if (sessionStorage.getItem(query) !== null) {
        const dataString = sessionStorage.getItem(query);
        // const data = dataString.split(";").map(movie => JSON.parse(movie));
        const data = JSON.parse(dataString);
        doneCallback( { suggestions: data } );
        return;
    }

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    console.log("sending AJAX request to backend Java Servlet")
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/movie-suggestion?title=" + encodeURI(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    console.log(data)

    // TODO: if you want to cache the result into a global variable you can do it here
    // const dataString = data.map(movie => JSON.stringify(movie)).join(';');
    const dataString = JSON.stringify(data);

    console.log('caching', query, dataString)
    sessionStorage.setItem(query, dataString);

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    location.href = "single-movie.html?id="+suggestion["data"];
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["heroID"])
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3,
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode === 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})


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