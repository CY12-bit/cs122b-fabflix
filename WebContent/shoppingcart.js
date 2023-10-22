
/**
 * Function populates the HTML cart table with
 * movies in the cart associated with the user session.
 *
 * Assume that resultData contains all the movie information for movies in the cart
 * currently.
 * - Title
 * - Starting Quantity
 */

function updateCartRow(movieId, change) {
    let cart_quantity = parseInt(document.getElementById(movieId).innerText);
    let movie_price = parseInt(document.getElementById(movieId+"_price").innerText);
    if (change === "dec") {
        cart_quantity -= 1;
        movie_price = -1 * movie_price;
    }
    else { cart_quantity += 1; }

    if (cart_quantity === 0) {
        document.getElementById(movieId+"_row").remove();
    }
    else {
        document.getElementById(movieId).innerHTML = cart_quantity.toString();
        document.getElementById(movieId+"_total_price").innerHTML =
            (parseInt(document.getElementById(movieId+"_total_price").innerText) +
            movie_price).toString();
    }
}

function populateCartTable(resultData) {
    console.log("populateCartTable: populating cart info from resultData");
    // Find the empty cart table body
    let cartTableBodyElement = jQuery("#cart_table_body");
    let cartHTML = "";

    // Iterate through the movie Information.
    for (let i = 0; i < resultData.length; i++) {
        cartHTML += "<tr id=" + '\'' + resultData[i]['Id'] + '_row\'' + '>';
        const title = resultData[i]["Title"];
        const start_quantity = resultData[i]["Quantity"];
        const price = 10; // DEFAULT PRICE FOR NOW
        cartHTML +=
            "<th>" + title + "</th>" +
            "<th id=" + '\'' + resultData[i]['Id'] + '_price\'' + '>' + price + "</th>" +
            "<th>" + "<div class='quantity_wrapper'>" +
            '<button' + ' onclick = changeQuantity(\'' + resultData[i]['Id'] + '\','+ '\'dec' + '\')>' + '-' + '</button>'
            + "<h3 id=" +  '\'' + resultData[i]['Id'] + '\'>' + start_quantity
            + '</h3>'
            + '<button' + ' onclick = changeQuantity(\'' + resultData[i]['Id'] + '\',' + '\'inc' + '\')>' + '+' + '</button>'
            + "</div>" + "</th>" + // HAVE TO ADD BUTTONS TO CHANGE QUANTITY
            "<th id=" + '\'' + resultData[i]['Id'] + '_total_price\'' + '>' + start_quantity * price + "</th>" +
            "<th>" + '<button' + ' onclick = removeFromCart(\'' + resultData[i]['Id'] + '\')>' + 'Remove' + '</button></th>';
        cartHTML += "</tr>";
    }
    cartTableBodyElement.append(cartHTML);
    console.log("populateCartTable: finished populating cart table");
}

/**
 * Function either increases or decreases the quantity of a certain movie
 * by 1 or -1. If the movie quantity is 1 and the user tries to decrease,
 * it will not work.
 */
function changeQuantity(movieId, change) {
    console.log("changing movie quantity in cart");
    jQuery.ajax( "api/shopping-cart",{
        method: "POST",
        data : "movieId=" + movieId + "&value=" + change,
        success : updateCartRow(movieId, change)
    })
}

/**
 * Function removes specified movie from the cart
 */
function removeFromCart(movieId) {
    console.log("removeFromCart: removing item from cart");
    jQuery.ajax( "api/shopping-cart", {
        method : "POST",
        data : "movieId=" + movieId + "&value=remove",
    })
    document.getElementById(movieId+"_row").remove();
}


// Make HTTP Get Request when calling shopping cart page
jQuery.ajax(
    {
        dataType: "json",
        method: "GET",
        url : "api/shopping-cart",
        success : (resultData) => populateCartTable(resultData)
    }
)