const createDirectoryForm = document.getElementById('createDirectoryForm');

createDirectoryForm.addEventListener('submit', handleDirCreate);

async function handleDirCreate(event) {

    let response = await formSubmitFetch(event);
    if (response.redirected) {
        let redirectResponse = await fetch(response.url);
        filesList.innerHTML = await redirectResponse.text();
    } else {
        document.body.innerHTML = await response.text();
    }
    createDirectoryForm.reset();
}