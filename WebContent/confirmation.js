
function postConfirmation(resultData) {
    let confirmTableBodyElement = jQuery("#confirmation_cart_body");
    let totalPriceElement = jQuery("#total_price");
    let total_price_value = 0;
    const cart = resultData['cart'];
    console.log(resultData, cart);
    const startingId = resultData['starting_id'];

    for (let i = 0; i < cart.length; i++) {
        const title = cart[i]["Title"];
        const start_quantity = cart[i]["Quantity"];
        const price = cart[i]['Price'];
        total_price_value += (price * start_quantity);
        let cartHTML = "<tr>";
        cartHTML +=
            "<th>" + (startingId + i) + "</th>"+
            "<th>" + title + "</th>" +
            "<th>" + price + "</th>" +
            "<th>" + start_quantity + "</th>" +
            "<th>" + start_quantity * price + "</th>";
        cartHTML += "</tr>";
        confirmTableBodyElement.append(cartHTML);
    }
    totalPriceElement.append(total_price_value.toString());

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