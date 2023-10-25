/**
 * Function updates the cart row price based off the quantity change.
 * Function also changes the total price of the cart
 */
function updateCartRow(movieId, change) {
    let cart_quantity = parseInt(document.getElementById(movieId).innerText);
    let movie_price = parseFloat(document.getElementById(movieId+"_price").innerText);
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
        document.getElementById(movieId+"_subtotal").innerHTML =
            (parseFloat(document.getElementById(movieId+"_subtotal").innerText) +
            movie_price).toString();
    }
    // Change the total cart price
    document.getElementById("total_price").innerHTML =
        (parseFloat(document.getElementById("total_price").innerText) + movie_price).toString();
}

function populateCartTable(resultData) {
    console.log("populateCartTable: populating cart info from resultData");
    // Find the empty cart table body
    let cartTableBodyElement = jQuery("#cart_table_body");
    let totalPriceElement = jQuery("#total_price");
    let cartHTML = "";
    let total_price = 0;
    // Iterate through the movie Information.
    for (let i = 0; i < resultData.length; i++) {
        cartHTML += "<tr id=" + '\'' + resultData[i]['Id'] + '_row\'' + '>';
        const title = resultData[i]["Title"];
        const start_quantity = resultData[i]["Quantity"];
        const price = resultData[i]['Price'];
        total_price += (price * start_quantity);
        cartHTML +=
            "<th>" + title + "</th>" +
            "<th id=" + '\'' + resultData[i]['Id'] + '_price\'' + '>' + price + "</th>" +
            "<th>" + "<div class='quantity_wrapper'>" +
            '<button' + ' onclick = changeQuantity(\'' + resultData[i]['Id'] + '\','+ '\'dec' + '\')>' + '-' + '</button>'
            + "<h3 id=" +  '\'' + resultData[i]['Id'] + '\'>' + start_quantity
            + '</h3>'
            + '<button' + ' onclick = changeQuantity(\'' + resultData[i]['Id'] + '\',' + '\'inc' + '\')>' + '+' + '</button>'
            + "</div>" + "</th>" + // HAVE TO ADD BUTTONS TO CHANGE QUANTITY
            "<th id=" + '\'' + resultData[i]['Id'] + '_subtotal\'' + '>' + start_quantity * price + "</th>" +
            "<th>" + '<button' + ' onclick = removeFromCart(\'' + resultData[i]['Id'] + '\')>' + 'Remove' + '</button></th>';
        cartHTML += "</tr>";
    }
    cartTableBodyElement.append(cartHTML);
    totalPriceElement.append(total_price.toString());
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
    let movie_subTotal = parseFloat(document.getElementById(movieId + "_subtotal").innerText);
    let current_total = parseFloat(document.getElementById("total_price").innerText);

    jQuery.ajax( "api/shopping-cart", {
        method : "POST",
        data : "movieId=" + movieId + "&value=remove",
    })

    // wait what happens if it fails?
    document.getElementById("total_price").innerHTML = (current_total-movie_subTotal).toString() || "0";
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