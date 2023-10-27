
function postConfirmation(resultData) {
    if (resultData['paid']) {
        let confirmTableBodyElement = jQuery("#confirmation_cart_body");
        let totalPriceElement = jQuery("#total_price");
        let total_price_value = 0;
        const cart = resultData['cart'];
        const startingId = resultData['starting_id'];
        for (let i = 0; i < cart.length; i++) {
            const title = cart[i]["Title"];
            const start_quantity = cart[i]["Quantity"];
            const price = cart[i]['Price'];
            total_price_value += (price * start_quantity);
            let cartHTML = "<tr>";
            cartHTML +=
                "<td>" + (startingId + i) + "</td>"+
                "<td>" + title + "</td>" +
                "<td>" + price + "</td>" +
                "<td>" + start_quantity + "</td>" +
                "<td>" + start_quantity * price + "</td>";
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
    } else {
        location.href = 'shoppingcart.html';
    }

}

jQuery.ajax(
    {
        dataType: "json",
        method: "GET",
        url : "api/shopping-cart",
        success : (resultData) => postConfirmation(resultData)
    }
)