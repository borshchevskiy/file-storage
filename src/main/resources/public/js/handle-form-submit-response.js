async function handleFormSubmitResponse(response) {
    if (response.redirected) {
        let redirectResponse = await fetch(response.url);
        filesList.innerHTML = await redirectResponse.text();
    } else {
        document.body.innerHTML = await response.text();
    }
}