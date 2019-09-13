# Example use API to communicate embedded dashboards in iframes

This example shows how to use a dashboard in an iframe and send messages to create gadgets, modify them, create filters and save the dashboard.

## How to configure the example

To use the example application, you must upload as a web project to the platform.
Once deployed we access it.
Fill the field Dashboard url, with the url of the dashboards taking into account that we need the specific to edit using the dashboard inside the iframe, it must contain the word "editfulliframe", an example of url would be something like this:

"http: // localhost / controlpanel / dashboards / editfulliframe / fc449fc5-b35b-4ab7-8c92-56a46d9dfc5c"

We must also fill in the OAuth2 Token field: with the user's OAuth2 token, we can retrieve it for example from the control panel in the top menu by clicking on the word API, it can also be retrieved using the APIs.

After this we click on OPEN DASHBOARD
the dashboard will be loaded and we will be able to create gadgets and modify them.
To modify them we have to drag the square of DRAG on GADGET
select in the update gadget the type that we want to modify.