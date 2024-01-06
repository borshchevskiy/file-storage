const createDirectoryForm = document.getElementById('createDirectoryForm');

createDirectoryForm.addEventListener('submit', handleDirCreate);

async function handleDirCreate(event) {

    let response = await formSubmit(event);
    await handleFormSubmitResponse(response);
    createDirectoryForm.reset();
}