const renameFileModal = document.getElementById('renameFileModal')
const renameFileForm = document.getElementById('renameFileForm');
const renameFileModalCloseButton = document.getElementById('renameFileModalClose');

renameFileForm.addEventListener('submit', handleFileRename);
renameFileModal.addEventListener('show.bs.modal', addDataToModal);

function addDataToModal(event) {
    const button = event.relatedTarget;
    // Extract info from data-bs-path and data-bs-name attributes
    const path = button.getAttribute('data-bs-path');
    const name = button.getAttribute('data-bs-name');
    // Update the modal's content.
    const modalInputPath = renameFileModal.querySelector('.modal-body #path');
    const modalInputName = renameFileModal.querySelector('.modal-body #oldName');
    modalInputPath.value = path;
    modalInputName.value = name;

}

async function handleFileRename(event) {

    let response = await formSubmit(event);
    await handleFormSubmitResponse(response);
    renameFileForm.reset();
    renameFileModalCloseButton.click();
}