let deleteForm = document.getElementById('deleteForm');

deleteForm.addEventListener('submit', handleDelete);

async function handleDelete(event) {

    let response = await formSubmitFetch(event);
    if (response.redirected) {
        let redirectResponse = await fetch(response.url);
        filesList.innerHTML = await redirectResponse.text();
    } else {
        document.body.innerHTML = await response.text();
    }
    deleteForm = document.getElementById('deleteForm');
    deleteForm.addEventListener('submit', handleDelete);
}