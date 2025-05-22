$(document).ready(function () {
  // Handle AJAX form submission for order status updates
  $("#statusUpdateForm").submit(function (e) {
    e.preventDefault();

    var form = $(this);
    var url = form.attr("action");
    var statusBadge = $(".status-badge");
    var currentStatus = $("#orderStatus").val();

    // Get CSRF token from meta tag or hidden input field
    var csrfToken =
      $("meta[name='_csrf']").attr("content") || $("input[name='_csrf']").val();
    var csrfHeader =
      $("meta[name='_csrf_header']").attr("content") || "X-CSRF-TOKEN";

    // Setup CSRF headers
    var headers = {};
    if (csrfToken) {
      headers[csrfHeader] = csrfToken;
    }

    // Add CSRF token as request parameter as well
    var data = {
      status: currentStatus,
    };
    if (csrfToken) {
      data["_csrf"] = csrfToken;
    }

    $.ajax({
      type: "POST",
      url: url,
      headers: headers,
      data: data,
      success: function (response) {
        // Update the status badge on the page
        statusBadge.text(currentStatus);
        statusBadge.attr(
          "class",
          "meta-value status-badge status-" + currentStatus.toLowerCase()
        );

        // Show success message
        $("#statusFeedback")
          .html(
            '<div class="alert alert-success">Status updated successfully to ' +
              currentStatus +
              "</div>"
          )
          .show()
          .delay(3000)
          .fadeOut();
      },
      error: function (xhr, status, error) {
        console.error("Status update error:", error);
        console.error("Response:", xhr.responseText);

        // Show error message with details if available
        var errorMsg = "Failed to update status. Please try again.";
        if (xhr.responseText) {
          try {
            var response = JSON.parse(xhr.responseText);
            if (response.message) {
              errorMsg = response.message;
            }
          } catch (e) {
            // Not JSON or can't parse
          }
        }

        $("#statusFeedback")
          .html('<div class="alert alert-danger">' + errorMsg + "</div>")
          .show()
          .delay(5000)
          .fadeOut();
      },
    });
  });
});
