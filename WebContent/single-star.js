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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");
    console.log("result data: ", resultData);

    document.title = resultData[0]["name"];

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieHeadingElement = jQuery('#star_heading');
    movieHeadingElement.text(resultData[0]["name"]);

    let starInfoElement = jQuery("#star_info");
    const birthYear = resultData[0]["birthYear"] ? resultData[0]["birthYear"] : 'N/A';


    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Birth Year: " + birthYear + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    const movieInfo = resultData[0]["movies"];
    console.log("movieInfo", movieInfo, movieInfo.length)
    for (let i = 0; i < movieInfo.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        // rowHTML += "<th>" + movieInfo[i]["title"] + "</th>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + movieInfo[i]['id'] + '">'
            + movieInfo[i]["title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + movieInfo[i]["year"] + "</th>";
        rowHTML += "<th>" + movieInfo[i]["director"] + "</th>";
        rowHTML += "<th>" + '<button id = ' + '\'' + movieInfo[i]['id'] + '\'' + ' onclick = handleMovieAdd(\'' + movieInfo[i]['id'] + '\')>' + 'Add' + '</button></th>';
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


function indicateMovieAdd(resultData) {
    console.log(resultData);
    alert("Successfully Added to Cart!")
}


// 1. Fill out the function so that it sends a POST request to ShoppingCartServlet.java with movieId=...&value=inc
// 2. Indicate whether adding it to the cart is a success or failure
function handleMovieAdd(movieId) {
   // ajax request
    // given buttons a general class name
    // also label buttons with id of movie ie id=movieId
    // use js to get the id of element clicked
    // you might need a param in the function, use google
    console.log("Clicked on Add Cart Button in Single Star Page");
    jQuery.ajax( "api/shopping-cart", {
        method: "POST",
        data : "movieId=" + movieId + "&value=inc",
        success: indicateMovieAdd
    })


}

// jQuery('.button-classname').click(handleMovieAdd)

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});