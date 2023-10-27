function populateCartTable(resultData) {
    console.log("populateCartTable: populating cart info from resultData", resultData);
    // Find the empty cart table body
    let cartTableBodyElement = jQuery("#cart_table_body");
    $("#cart_table_body tr").remove();

    let totalPriceElement = jQuery("#total_price");
    let cartHTML = "";
    let total_price = 0;
    let cart = resultData['cart'];

    if (cart.length === 0) {
        let paymentButton = jQuery('#payment-button');
        let paymentLink = jQuery('#payment-link');
        paymentButton.attr('disabled', true);
        paymentLink.removeAttr('href');
    }

    // Iterate through the movie Information.
    for (let i = 0; i < cart.length; i++) {
        cartHTML += "<tr id=" + '\'' + cart[i]['Id'] + '_row\'' + '>';
        const title = cart[i]["Title"];
        const start_quantity = cart[i]["Quantity"];
        const price = cart[i]['Price'];
        total_price += (price * start_quantity);
        cartHTML +=
            "<th>" + title + "</th>" +
            "<th id=" + '\'' + cart[i]['Id'] + '_price\'' + '>' + price + "</th>" +
            "<th>" + "<div class='quantity_wrapper'>" +
            "<button class='btn btn-outline-dark'" + " onclick = changeQuantity('" + cart[i]['Id'] + '\','+ '\'dec' + '\')>' + '-' + '</button>'
            + "<h3 id=" +  '\'' + cart[i]['Id'] + '\'>' + start_quantity
            + '</h3>'
            + '<button class="btn btn-outline-dark"' + ' onclick = changeQuantity(\'' + cart[i]['Id'] + '\',' + '\'inc' + '\')>' + '+' + '</button>'
            + "</div>" + "</th>" + // HAVE TO ADD BUTTONS TO CHANGE QUANTITY
            "<th>" + start_quantity * price + "</th>" +
            "<th>" + '<button class="btn btn-outline-dark"' + ' onclick = removeFromCart(\'' + cart[i]['Id'] + '\')>' + 'Remove' + '</button></th>';
        cartHTML += "</tr>";
    }
    cartTableBodyElement.append(cartHTML);
    totalPriceElement.text(total_price.toString());
    console.log("populateCartTable: finished populating cart table");
}

/**
 * Function either increases or decreases the quantity of a certain movie
 * by 1 or -1. If the movie quantity is 1 and the user tries to decrease,
 * it will not work.
 *
 * Originally, Colin's function has a disconnect between front end and back end so if backend failed
 * front end would incorrectly change
 */
function changeQuantity(movieId, change) {
    console.log("changing movie quantity in cart");
    jQuery.ajax( "api/shopping-cart",{
        method: "POST",
        data : "movieId=" + movieId + "&value=" + change,
        success: (resultData) => populateCartTable(resultData)
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
        success: (resultData) => populateCartTable(resultData)
    });
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