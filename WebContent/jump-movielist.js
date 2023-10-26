function setMovielistUrl(resultData) {
    let homeLink = jQuery('#movielist_url')
    console.log('movielist url', resultData);
    if (resultData['prev-movielist'] !== '') {
        homeLink.attr('href', resultData['prev-movielist']);
    } else {
        homeLink.attr('href', 'movielist.html');
    }
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movielist-jump", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => setMovielistUrl(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

