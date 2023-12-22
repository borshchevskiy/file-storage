let deleteForm = document.getElementById('deleteForm');

deleteForm.addEventListener('submit', handleDelete);

async function handleDelete(event) {

    let response = await formSubmit(event);
    await handleFormSubmitResponse(response);

    deleteForm = document.getElementById('deleteForm');
    deleteForm.addEventListener('submit', handleDelete);
}