/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleMovieAdd(movieId, movieTitle) {
    // ajax request
    // given buttons a general class name
    // also label buttons with id of movie ie id=movieId
    // use js to get the id of element clicked
    // you might need a param in the function, use google
    console.log("Clicked on Add Cart Button in Single Star Page");
    jQuery.ajax( "api/shopping-cart", {
        method: "POST",
        data : "movieId=" + movieId + "&movieTitle="+movieTitle+"&value=inc",
        success: indicateMovieAdd
    })


}

function buildDataQuery() {
    const params = ['star', 'title', 'genre', 'year', 'director', 'beginsWith'];
    let url_query = [];
    params.forEach(p => {
        if (getParameterByName(p)) {
            url_query.push(p + '=' + getParameterByName(p));
        }
    })

    return url_query.join('&');
}

function buildDisplayQuery() {
    const params = ['page', 'records', 'sortOrder'];
    let url_query = [];
    params.forEach(param => {
        let val = getParameterByName(param);
        if (val && (!isNaN(val) || param === 'sortOrder')) {
            url_query.push(param + '=' + val);
        }
    })

    return url_query.join('&');
}

function createPaginationButtons() {
    let paginationEl = $('#pagination');
    let pageNum = getParameterByName('page');
    let records = getParameterByName('records');

    let prevPageLink = 'movielist.html?' + buildDataQuery();
    let nextPageLink = prevPageLink;

    let currPage = (pageNum && !isNaN(pageNum)) ? parseInt(pageNum) : 0;
    prevPageLink += '&page=' + (currPage - 1);
    nextPageLink += '&page=' + (currPage + 1);

    if (records && !isNaN(records)) {
        prevPageLink += '&records=' + (parseInt(records));
        nextPageLink += '&records=' + (parseInt(records));
    }

    if (currPage > 0)
        paginationEl.append(`<a href="${prevPageLink}"> <button class="btn btn-info">previous</button></a>`)
    paginationEl.append(`<a href="${nextPageLink}"> <button class="btn btn-info">next</button></a>`)

}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 *
 * NEED TO FIGURE OUT HOW TO REMOVE DUPLICATE FUNCTIONS!!!
 */
function handleStarResult(resultData) {
    console.log("movielist: populating movielist table from resultData", resultData);

    // Populate the movielist table
    // Find the empty table body by id "movie_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        const genres = resultData[i]["movie_genres"];
        const stars = resultData[i]["movie_stars"];

        let genresHTML = '';
        for (let i = 0; i < genres.length; i++) {
            genresHTML += '<a href="movielist.html?genre=' + genres[i]['genre_id'] + '"' + ' class="badge badge-secondary"' + '>'
            genresHTML += genres[i]['genre_name']  + '</a>';   // display genre_name for the link text
            if (i < genres.length - 1) {
                genresHTML += " ";
            }
        }

        let starsHTML = '';
        for (let i= 0; i < stars.length; i++) {
            starsHTML += '<a href="single-star.html?id=' + stars[i]['star_id'] + '"' + ' class="badge badge-primary"' + '>'
                + stars[i]["star_name"] + '</a>';
            if (i < stars.length - 1) {
                starsHTML += " ";
            }
        }

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] + '</a>' + "</th>" +
            "<th>" + resultData[i]["movie_year"] + "</th>" +
            "<th>" + resultData[i]["movie_director"] + "</th>" +
            "<th>" + genresHTML + "</th>" +
            "<th>" + starsHTML + "</th>" +
            "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th>" + '<button class="btn btn-outline-dark" id = ' + '\'' + resultData[i]["movie_title"] + '\'' + ' onclick = handleMovieAdd(\'' + resultData[i]['movie_id'] + '\',\''+ encodeURIComponent(resultData[i]["movie_title"])+ '\')>' + 'Add' + '</button></th>';
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
    createPaginationButtons();
}

let display_form = $("#display_form");
function handleDisplayForm(formSubmitEvent) {
    console.log("submit display form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    let page = getParameterByName('page') ? getParameterByName('page') : 0;
    location.href = "movielist.html?" + buildDataQuery() + '&page=' + page + '&' + display_form.serialize();
}

function indicateMovieAdd(resultData) {
    console.log(resultData);
    alert("Successfully Added to Cart!")
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let query = '?' + buildDataQuery() + '&' + buildDisplayQuery();
let apiEndpoint = "api/movielist";
if (getParameterByName('genre')) {
    apiEndpoint = "api/browse-genre";
} else if (getParameterByName('beginsWith')) {
    apiEndpoint = "api/browse-title";
}

console.log(apiEndpoint+query);
console.log(location.href)

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: apiEndpoint + query, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "POST", // Setting request method
    data: {'curr-movielist': location.href},
    url: 'api/movielist-jump', // Setting request url, which is mapped by StarsServlet in Stars.java
});



display_form.submit(handleDisplayForm);
