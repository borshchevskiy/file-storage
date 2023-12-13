const renameDirectoryModal = document.getElementById('renameDirectoryModal');
const renameDirectoryForm = document.getElementById('renameDirectoryForm');
const renameDirectoryModalCloseButton = document.getElementById('renameDirectoryModalClose');

renameDirectoryForm.addEventListener('submit', handleDirRename);
renameDirectoryModal.addEventListener('show.bs.modal', addDataToModal);

function addDataToModal(event) {
    const button = event.relatedTarget;
    // Extract info from data-bs-path and data-bs-name attributes
    const path = button.getAttribute('data-bs-path');
    const name = button.getAttribute('data-bs-name');
    // Update the modal's content.
    const modalInputPath = renameDirectoryModal.querySelector('.modal-body #path');
    const modalInputName = renameDirectoryModal.querySelector('.modal-body #oldName');
    modalInputPath.value = path;
    modalInputName.value = name;
}

async function handleDirRename(event) {

    let response = await formSubmitFetch(event);
    if (response.redirected) {
        let redirectResponse = await fetch(response.url);
        filesList.innerHTML = await redirectResponse.text();
    } else {
        document.body.innerHTML = await response.text();
    }

    renameDirectoryForm.reset();
    renameDirectoryModalCloseButton.click();
}