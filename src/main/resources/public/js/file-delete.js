function updateListenersForDeleteForms() {
    let filesListTable = document.getElementById('filesListTable');

    filesListTable.querySelectorAll('#filesListTable tr td form.deleteForm')
        .forEach(item => item.addEventListener('submit', handleDelete));
}

async function handleDelete(event) {

    let response = await formSubmit(event);
    await handleFormSubmitResponse(response);
}