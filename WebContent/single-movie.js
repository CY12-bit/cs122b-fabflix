/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
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

function addMovieToCart() {
    jQuery.ajax(
        "api/shopping-cart", {
            method: "POST",
            data : "movieId="+getParameterByName('id')+"&movieTitle="+document.title+"&value=inc"
        }
    )
    alert("Successfully Added to Cart!")
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");
    console.log("result data: ", resultData);

    document.title = resultData[0]["title"];

    let movieHeadingElement = jQuery('#movie_heading');
    movieHeadingElement.text(resultData[0]["title"]);

    let movieInfoElement = jQuery("#movie_info");

    // TODO: link to genre browse page


    let genreBadges = resultData[0]['genres'].map(genre => `<a href="movielist.html?genre=${genre['id']}" class="badge badge-secondary badge-secondary">${genre['name']}</a>`)
    movieInfoElement.append(
        "<p>" + resultData[0]["director"] + "  •  " + resultData[0]["year"] + " </p>" +
        "<p>" + genreBadges.join('  ') + "</p>" +
        "<p>" + resultData[0]["rating"] + "/10 (#votes: " + resultData[0]["numVotes"] + ") </p>"
    );
    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let starTableBodyElement = jQuery("#stars_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    const starsInfo = resultData[0]["stars"];

    console.log('starsInfo', starsInfo)
    for (let i = 0; i < starsInfo.length; i++) {
        let rowHTML = "";
        const star = starsInfo[i];
        // console.log('star', star)
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + star['id'] + '">'
            + star['name'] +     // display star_name for the link text
            '</a>' +
            "</th>";
        const birthYear = star["birthYear"] ? star["birthYear"] : 'N/A';
        rowHTML += "<th>" + birthYear + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// ASSIGN 'ADD CART' functionality to add_cart button


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});