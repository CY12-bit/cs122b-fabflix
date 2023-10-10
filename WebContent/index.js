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
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        const genres = resultData[i]["movie_genres"];
        const stars = resultData[i]["movie_stars"];

        let genresHTML = '';
        for (let i = 0; i < genres.length; i++) {
            genresHTML += '<span class="badge badge-pill badge-secondary' + '">';
            genresHTML += genres[i];   // display star_name for the link text
            genresHTML += '</span>'
            if (i < genres.length - 1) {
                genresHTML += ", ";
            }
        }

        let starsHTML = '';
        for (let i= 0; i < stars.length; i++) {
            starsHTML += '<a href="single-star.html?id=' + stars[i]['star_id'] + '"' + ' class="badge badge-primary"' + '>'
                + stars[i]["star_name"] + '</a>';
            if (i < stars.length - 1) {
                starsHTML += ", ";
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
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movielist", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});