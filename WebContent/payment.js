let payment_form = jQuery("#payment_form");


// Function calculates the total cost of the cart
// Will probably be worked on since by default it's $10 for each movie regardless
function calculateTotalCost(resultData) {
    console.log("Calculating Total Cart Cost");
    let total_cost_display = jQuery("#final_payment");
    let total_cost = 0;
    for (let i = 0; i < resultData.length;i++) {
        total_cost += resultData[i]["Quantity"] * 10; // NEED TO UN-HARDCODE PRICE LATER
    }
    total_cost_display.append(`Final Cart Cost: $${total_cost}`);
    console.log("Finished Calculating Total Cart Cost");
}

// Function outputs correct response to user depending on
// whether they entered valid credit card info or not
function handlePaymentResult(resultData) {
    console.log("handle payment response");
    console.log(resultData);
    console.log(resultData["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultData["status"] === "success") {
        window.location.replace("confirmation.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultData["message"]);
        $("#payment_error_message").text(resultData["message"]);
    }
}

// Function sends HTTP Post request to SQL database with order information if it's correct
function submitPaymentForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    jQuery.ajax(
        "api/payment", {
            method: "POST",
            data: payment_form.serialize(),
            success : handlePaymentResult
        }
    );
}

// Bind submit action of form to a handler function
payment_form.submit(submitPaymentForm);

// Get the shopping cart contents, so we can calculate the total cart cost
jQuery.ajax(
    {
        dataType: "json",
        method: "GET",
        url : "api/shopping-cart",
        success : (resultData) => calculateTotalCost(resultData)
    }
)