
function postConfirmation(resultData) {
    let confirmTableBodyElement = jQuery("#confirmation_cart_body");
    let totalPriceElement = jQuery("#total_price");
    let total_price_value = 0;
    let cartHTML = "";
    for (let i = 0; i < resultData.length; i++) {
        const title = resultData[i]["Title"];
        const start_quantity = resultData[i]["Quantity"];
        const price = resultData[i]['Price'];
        total_price_value += (price * start_quantity);
        cartHTML +=
            "<th>" + title + "</th>" +
            "<th id=" + '\'' + resultData[i]['Id'] + '_price\'' + '>' + price + "</th>" +
            "<th>" +
            "<h3 id=" +  '\'' + resultData[i]['Id'] + '\'>' + start_quantity
            + '</h3>'
            + "</th>" +
            "<th id=" + '\'' + resultData[i]['Id'] + '_subtotal\'' + '>' + start_quantity * price + "</th>";
        cartHTML += "</tr>";
    }
    totalPriceElement.append(total_price_value.toString());
    confirmTableBodyElement.append(cartHTML);
    console.log("Finished populating confirmation page");

    jQuery.ajax(
        "api/shopping-cart",
        {
            method: "POST",
            data : "value=clearCart"
        }
    )
    console.log("Cleared Cart");
    // HOWEVER, WE CANNOT SEE THE CART EVER AGAIN
}

jQuery.ajax(
    {
        dataType: "json",
        method: "GET",
        url : "api/shopping-cart",
        success : (resultData) => postConfirmation(resultData)
    }
)